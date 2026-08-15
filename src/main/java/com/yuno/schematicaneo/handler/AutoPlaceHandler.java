package com.yuno.schematicaneo.handler;

import com.yuno.schematicaneo.api.ISchematic;
import com.yuno.schematicaneo.client.world.SchematicWorld;
import com.yuno.schematicaneo.proxy.ClientProxy;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.WorldServer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoPlaceHandler {

    private static final int BUTTON_ID = 1000;
    private static final int STOP_BUTTON_ID = 1001;
    private static final int CHUNKS_PER_TICK = 1;
    private static final int LARGE_THRESHOLD = 160;
    private static final int LARGE_RADIUS = 128;

    private static final AutoPlaceHandler instance = new AutoPlaceHandler();

    private static volatile boolean placing = false;
    private static final Map<Long, List<BlockEntry>> pendingChunks = new HashMap<>();
    private static final Map<Long, List<BlockEntry>> remainingChunks = new HashMap<>();
    private static volatile int totalBlocks = 0;
    private static volatile int placedBlocks = 0;
    private static volatile boolean isLarge = false;
    private static long lastStatusMessageTime = 0;

    private static String playerName = "";

    private static class BlockEntry {
        final int x, y, z, blockId, meta;

        BlockEntry(int x, int y, int z, int blockId, int meta) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockId = blockId;
            this.meta = meta;
        }
    }

    public static AutoPlaceHandler getInstance() {
        return instance;
    }

    public static void addPlaceButton(GuiScreen gui) {
        try {
            Field btnListField = null;
            for (Field f : GuiScreen.class.getDeclaredFields()) {
                if (java.util.List.class.isAssignableFrom(f.getType())) {
                    btnListField = f;
                    btnListField.setAccessible(true);
                    break;
                }
            }
            if (btnListField != null) {
                @SuppressWarnings("unchecked")
                java.util.List<GuiButton> list = (java.util.List<GuiButton>) btnListField.get(gui);
                boolean hasPlace = false;
                boolean hasStop = false;
                for (GuiButton b : list) {
                    if (b.id == BUTTON_ID) hasPlace = true;
                    if (b.id == STOP_BUTTON_ID) hasStop = true;
                }
                if (!hasPlace) {
                    GuiButton placeBtn = new GuiButton(BUTTON_ID, gui.width - 90, gui.height - 246, 80, 20, "§6一键放置");
                    list.add(placeBtn);
                }
                if (!hasStop) {
                    GuiButton stopBtn = new GuiButton(STOP_BUTTON_ID, gui.width - 90, gui.height - 222, 80, 20, "§c停止放置");
                    list.add(stopBtn);
                }
            }
        } catch (Exception e) {
            FMLLog.warning("[SchematicaNeo] AutoPlace: addPlaceButton failed: " + e.getMessage());
        }
    }

    public static boolean handleAction(GuiButton button, GuiScreen gui) {
        if (button.id == BUTTON_ID) {
            startPlacement();
            return true;
        }
        if (button.id == STOP_BUTTON_ID) {
            stopPlacement();
            return true;
        }
        return false;
    }

    private static void stopPlacement() {
        if (!placing) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                    new ChatComponentText("§e[一键放置] 当前没有正在进行的放置"));
            return;
        }
        placing = false;
        clearPlacementQueues();
        Minecraft.getMinecraft().thePlayer.addChatMessage(
                new ChatComponentText("§c[一键放置] 已手动停止，已放置 " + placedBlocks + " 个方块"));
    }

    private static void startPlacement() {
        if (placing) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                    new ChatComponentText("§e[一键放置] 正在放置中，请等待完成"));
            return;
        }

        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                    new ChatComponentText("§c[一键放置] 仅支持可访问的服务器"));
            return;
        }

        EntityPlayerMP player = null;
        for (Object obj : server.getConfigurationManager().playerEntityList) {
            EntityPlayerMP p = (EntityPlayerMP) obj;
            if (p.getCommandSenderName().equals(Minecraft.getMinecraft().thePlayer.getCommandSenderName())) {
                player = p;
                break;
            }
        }
        if (player == null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                    new ChatComponentText("§c[一键放置] 无法找到玩家实例"));
            return;
        }

        if (!player.canCommandSenderUseCommand(2, "SchematicaNeo.place")) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                    new ChatComponentText("§c[一键放置] 需要管理员权限"));
            return;
        }

        try {
            SchematicWorld schematicWorld = ClientProxy.schematic;
            if (schematicWorld == null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(
                        new ChatComponentText("§c[一键放置] 未加载投影"));
                return;
            }

            ISchematic schematic = schematicWorld.getSchematic();
            if (schematic == null) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(
                        new ChatComponentText("§c[一键放置] 投影数据为空"));
                return;
            }

            int w = schematic.getWidth();
            int h = schematic.getHeight();
            int l = schematic.getLength();

            int ox = schematicWorld.position.x;
            int oy = schematicWorld.position.y;
            int oz = schematicWorld.position.z;

            playerName = player.getCommandSenderName();

            boolean large = w > LARGE_THRESHOLD || l > LARGE_THRESHOLD;
            isLarge = large;

            totalBlocks = 0;
            placedBlocks = 0;
            lastStatusMessageTime = 0;
            pendingChunks.clear();
            remainingChunks.clear();

            int px = (int) Math.floor(player.posX);
            int pz = (int) Math.floor(player.posZ);

            Map<Long, List<BlockEntry>> allChunks = new HashMap<>();
            int blockCount = 0;
            for (int y = 0; y < h; y++) {
                for (int z = 0; z < l; z++) {
                    for (int x = 0; x < w; x++) {
                        Block block = schematic.getBlock(x, y, z);
                        if (block == null || block.isAir(null, -1, -1, -1)) continue;
                        int meta = schematic.getBlockMetadata(x, y, z);
                        int wx = ox + x;
                        int wy = oy + y;
                        int wz = oz + z;
                        long chunkKey = packChunk(wx >> 4, wz >> 4);
                        allChunks.computeIfAbsent(chunkKey, k -> new ArrayList<>())
                                .add(new BlockEntry(wx, wy, wz, Block.getIdFromBlock(block), meta));
                        blockCount++;
                    }
                }
            }

            if (allChunks.isEmpty()) {
                Minecraft.getMinecraft().thePlayer.addChatMessage(
                        new ChatComponentText("§e[一键放置] 投影中没有方块"));
                return;
            }

            totalBlocks = blockCount;

            List<Map.Entry<Long, List<BlockEntry>>> ordered = new ArrayList<>(allChunks.entrySet());
            ordered.sort(Comparator.comparingDouble(e -> {
                int cx = (int) (e.getKey() >> 32) * 16 + 8;
                int cz = (int) (e.getKey().longValue() & 0xFFFFFFFFL) * 16 + 8;
                double dx = cx + 0.5 - px;
                double dz = cz + 0.5 - pz;
                return dx * dx + dz * dz;
            }));

            if (large) {
                int nearbyCount = 0;
                for (Map.Entry<Long, List<BlockEntry>> e : ordered) {
                    int cx = (int) (e.getKey() >> 32) * 16 + 8;
                    int cz = (int) (e.getKey().longValue() & 0xFFFFFFFFL) * 16 + 8;
                    double dx = cx + 0.5 - px;
                    double dz = cz + 0.5 - pz;
                    if (dx * dx + dz * dz <= LARGE_RADIUS * LARGE_RADIUS) {
                        pendingChunks.put(e.getKey(), e.getValue());
                        nearbyCount++;
                    } else {
                        remainingChunks.put(e.getKey(), e.getValue());
                    }
                }
                player.addChatMessage(new ChatComponentText(
                        "§6[一键放置] 大投影模式 (总" + totalBlocks + "方块, " + allChunks.size() + "区块)"));
                player.addChatMessage(new ChatComponentText(
                        "§7初始放置 " + nearbyCount + " 个附近区块，靠近未放置区域自动继续"));
            } else {
                for (Map.Entry<Long, List<BlockEntry>> e : ordered) {
                    pendingChunks.put(e.getKey(), e.getValue());
                }
                player.addChatMessage(new ChatComponentText(
                        "§6[一键放置] 开始放置 " + totalBlocks + " 个方块 (" + allChunks.size() + "区块)"));
            }

            placing = true;

        } catch (Exception e) {
            FMLLog.warning("[SchematicaNeo] AutoPlace startPlacement failed: " + e.getMessage());
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                    new ChatComponentText("§c[一键放置] 错误: " + e.getMessage()));
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!placing) return;

        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            placing = false;
            clearPlacementQueues();
            return;
        }

        EntityPlayerMP player = null;
        for (Object obj : server.getConfigurationManager().playerEntityList) {
            EntityPlayerMP p = (EntityPlayerMP) obj;
            if (p.getCommandSenderName().equals(playerName)) {
                player = p;
                break;
            }
        }
        if (player == null) {
            placing = false;
            clearPlacementQueues();
            return;
        }

        if (!player.canCommandSenderUseCommand(2, "SchematicaNeo.place")) {
            player.addChatMessage(new ChatComponentText("§c[一键放置] 权限不足，已停止"));
            placing = false;
            clearPlacementQueues();
            return;
        }

        try {
            if (isLarge && pendingChunks.size() < 5) {
                int px = (int) Math.floor(player.posX);
                int pz = (int) Math.floor(player.posZ);
                List<Long> nearbyChunks = new ArrayList<>();
                for (Map.Entry<Long, List<BlockEntry>> chunkEntry : remainingChunks.entrySet()) {
                    int chunkX = (int) (chunkEntry.getKey() >> 32);
                    int chunkZ = (int) (chunkEntry.getKey().longValue() & 0xFFFFFFFFL);
                    double dx = chunkX * 16 + 8.5 - px;
                    double dz = chunkZ * 16 + 8.5 - pz;
                    if (dx * dx + dz * dz <= LARGE_RADIUS * LARGE_RADIUS) {
                        pendingChunks.put(chunkEntry.getKey(), chunkEntry.getValue());
                        nearbyChunks.add(chunkEntry.getKey());
                    }
                }
                for (Long chunkKey : nearbyChunks) {
                    remainingChunks.remove(chunkKey);
                }
            }

            WorldServer world = server.worldServerForDimension(player.dimension);

            int chunksPlaced = 0;
            List<Long> toRemove = new ArrayList<>();
            for (Map.Entry<Long, List<BlockEntry>> chunkEntry : pendingChunks.entrySet()) {
                if (chunksPlaced >= CHUNKS_PER_TICK) break;

                for (BlockEntry entry : chunkEntry.getValue()) {
                    Block block = Block.getBlockById(entry.blockId);
                    if (block != null && block != Blocks.air) {
                        world.setBlock(entry.x, entry.y, entry.z, block, entry.meta, 3);
                    }
                    placedBlocks++;
                }
                toRemove.add(chunkEntry.getKey());
                chunksPlaced++;
            }
            for (Long key : toRemove) {
                pendingChunks.remove(key);
            }

            if (pendingChunks.isEmpty() && (!isLarge || remainingChunks.isEmpty())) {
                placing = false;
                player.addChatMessage(new ChatComponentText(
                        "§a[一键放置] 完成！共放置 " + placedBlocks + " 个方块"));
            } else if (isLarge && pendingChunks.isEmpty()) {
                int unplaced = totalBlocks - placedBlocks;
                if (unplaced <= 0) {
                    placing = false;
                    player.addChatMessage(new ChatComponentText(
                            "§a[一键放置] 完成！共放置 " + placedBlocks + " 个方块"));
                } else {
                    long now = System.currentTimeMillis();
                    if (now - lastStatusMessageTime >= 3000) {
                        lastStatusMessageTime = now;
                        player.addChatMessage(new ChatComponentText(
                                "§7[一键放置] 已完成附近区块 (" + placedBlocks + "/" + totalBlocks + ")，靠近未放置区域自动继续"));
                    }
                }
            } else if (chunksPlaced > 0) {
                if (!isLarge && placedBlocks % 1000 == 0) {
                    player.addChatMessage(new ChatComponentText(
                            "§7[一键放置] 进度: " + placedBlocks + "/" + totalBlocks + " (" + (totalBlocks - placedBlocks) + " 剩余)"));
                }
            }

        } catch (Exception e) {
            FMLLog.warning("[SchematicaNeo] AutoPlace onServerTick failed: " + e.getMessage());
            placing = false;
            clearPlacementQueues();
            player.addChatMessage(new ChatComponentText(
                    "§c[一键放置] 发生错误: " + e.getMessage()));
        }
    }

    private static void clearPlacementQueues() {
        pendingChunks.clear();
        remainingChunks.clear();
    }

    private static long packChunk(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | ((long) chunkZ) & 0xFFFFFFFFL;
    }

    public static boolean isPlacing() {
        return placing;
    }
}
