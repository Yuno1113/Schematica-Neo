package com.yuno.schematicaneo.client.handler;

import org.lwjgl.input.Keyboard;

import com.yuno.schematicaneo.api.ISchematic;
import com.yuno.schematicaneo.client.renderer.RendererSchematicGlobal;
import com.yuno.schematicaneo.client.world.SchematicWorld;
import com.yuno.schematicaneo.proxy.ClientProxy;
import com.yuno.schematicaneo.reference.Names;
import com.yuno.schematicaneo.world.storage.Schematic;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;

/** Client-only state and input handling for schematic editing. */
public class SchematicEditorHandler {

    public static final SchematicEditorHandler INSTANCE = new SchematicEditorHandler();

    public enum Mode {
        SELECT("select"), COPY("copy"), PASTE("paste"), CUT("cut"), REPLACE("replace"), ROTATE_META("rotateMeta"),
        MOVE_SCHEMATIC("moveSchematic");

        public final String key;

        Mode(String key) {
            this.key = key;
        }
    }

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private boolean enabled;
    private Mode mode = Mode.SELECT;
    private boolean selectSchematic = false;
    private int minX, minY, minZ, maxX, maxY, maxZ;
    private boolean hasSelection;
    private Clipboard clipboard;
    private int pasteX, pasteY, pasteZ;
    private boolean pastePreview;
    private int replaceMode;
    private Block replaceBlock;
    private int replaceMetadata;
    private boolean cornerSelectionEnabled;
    private int cornerAX, cornerAY, cornerAZ, cornerBX, cornerBY, cornerBZ;
    private SchematicWorld clipboardWorld;

    private static class Clipboard {
        final Block[][][] blocks;
        final byte[][][] metadata;

        Clipboard(int width, int height, int length) {
            this.blocks = new Block[width][height][length];
            this.metadata = new byte[width][height][length];
        }
    }

    private static class ReplaceTarget {
        final Block block;
        final int metadata;

        ReplaceTarget(Block block, int metadata) {
            this.block = block;
            this.metadata = metadata;
        }
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            ensureSelection();
        }
    }

    public Mode getMode() {
        return this.mode;
    }

    public void nextMode() {
        changeMode(1);
    }

    public void setMode(Mode mode) {
        if (mode == null) return;
        this.mode = mode;
        if (mode == Mode.PASTE) {
            resetPastePosition();
            this.pastePreview = false;
        }
        message(Names.Gui.Editor.CHAT_MODE, getModeLabel());
    }

    private void changeMode(int direction) {
        final Mode[] modes = Mode.values();
        this.mode = modes[(this.mode.ordinal() + direction + modes.length) % modes.length];
        if (this.mode == Mode.PASTE) {
            resetPastePosition();
            this.pastePreview = false;
        }
        message(Names.Gui.Editor.CHAT_MODE, getModeLabel());
    }

    public boolean isSelectSchematic() {
        return this.selectSchematic;
    }

    public void toggleSelectionSource() {
        this.selectSchematic = !this.selectSchematic;
        message(Names.Gui.Editor.CHAT_SOURCE, getSelectionSourceLabel());
    }

    public int getReplaceMode() {
        return this.replaceMode;
    }

    public String getReplaceModeLabel() {
        switch (this.replaceMode) {
            case 1: return I18n.format(Names.Gui.Editor.RANGE_SELECTION);
            case 2: return I18n.format(Names.Gui.Editor.RANGE_ALL);
            default: return I18n.format(Names.Gui.Editor.RANGE_SINGLE);
        }
    }

    public String getModeLabel() {
        return getModeLabel(this.mode);
    }

    public String getModeLabel(Mode mode) {
        return I18n.format(Names.Gui.Editor.PREFIX + "mode." + mode.key);
    }

    public String getModeTooltip() {
        return getModeTooltip(this.mode);
    }

    public String getModeTooltip(Mode mode) {
        return I18n.format(Names.Gui.Editor.PREFIX + "mode." + mode.key + ".tooltip");
    }

    public String getSelectionSourceLabel() {
        return I18n.format(this.selectSchematic ? Names.Gui.Editor.SOURCE_SCHEMATIC : Names.Gui.Editor.SOURCE_WORLD);
    }

    public boolean isPastePreview() {
        return this.pastePreview;
    }

    public void nextReplaceMode() {
        this.replaceMode = (this.replaceMode + 1) % 3;
        message(Names.Gui.Editor.CHAT_RANGE, getReplaceModeLabel());
    }

    public Block getReplaceBlock() {
        return this.replaceBlock;
    }

    public int getReplaceMetadata() {
        return this.replaceMetadata;
    }

    public void setReplaceTarget(Block block, int metadata) {
        this.replaceBlock = block;
        this.replaceMetadata = Math.max(0, metadata);
    }

    public boolean hasSelection() {
        return this.hasSelection;
    }

    public boolean isCornerSelectionEnabled() {
        return this.cornerSelectionEnabled;
    }

    public void toggleCornerSelection() {
        this.cornerSelectionEnabled = !this.cornerSelectionEnabled;
    }

    public int getMinX() { return this.minX; }
    public int getMinY() { return this.minY; }
    public int getMinZ() { return this.minZ; }
    public int getMaxX() { return this.maxX; }
    public int getMaxY() { return this.maxY; }
    public int getMaxZ() { return this.maxZ; }

    public boolean hasClipboard() {
        return this.clipboard != null;
    }

    public int getClipboardWidth() {
        return this.clipboard == null ? 0 : this.clipboard.blocks.length;
    }

    public int getClipboardHeight() {
        return this.clipboard == null ? 0 : this.clipboard.blocks[0].length;
    }

    public int getClipboardLength() {
        return this.clipboard == null ? 0 : this.clipboard.blocks[0][0].length;
    }

    public int getPasteX() { return this.pasteX; }
    public int getPasteY() { return this.pasteY; }
    public int getPasteZ() { return this.pasteZ; }

    public SchematicWorld getClipboardWorld() {
        return this.clipboardWorld;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!this.enabled || this.minecraft.currentScreen != null || !Keyboard.getEventKeyState()) return;
        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
            if (this.mode == Mode.COPY) copy(false);
            else if (this.mode == Mode.CUT) copy(true);
            else if (this.mode == Mode.PASTE) beginPaste();
        }
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (!this.enabled || this.minecraft.currentScreen != null) return;

        if (event.dwheel != 0 && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))) {
            event.setCanceled(true);
            changeMode(event.dwheel > 0 ? -1 : 1);
            return;
        }

        if (event.dwheel != 0 && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL))) {
            if (this.mode == Mode.SELECT) {
                event.setCanceled(true);
                adjustSelection(event.dwheel > 0 ? -1 : 1);
                return;
            }
        }

        if (event.dwheel != 0 && (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU))) {
            event.setCanceled(true);
            if (this.mode == Mode.PASTE) movePaste(event.dwheel > 0 ? 1 : -1);
            else if (this.mode == Mode.REPLACE || this.mode == Mode.ROTATE_META) nextReplaceMode();
            else if (this.mode == Mode.SELECT) moveSelection(event.dwheel > 0 ? 1 : -1);
            else if (this.mode == Mode.MOVE_SCHEMATIC) moveSchematic(event.dwheel > 0 ? 1 : -1);
            return;
        }

        if (event.button == 2 && event.buttonstate
            && (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU))) {
            if (this.mode == Mode.SELECT) {
                event.setCanceled(true);
                resetSelectionCenter();
                return;
            } else if (this.mode == Mode.MOVE_SCHEMATIC && ClientProxy.schematic != null) {
                event.setCanceled(true);
                ClientProxy.moveSchematicToPlayer(ClientProxy.schematic);
                RendererSchematicGlobal.INSTANCE.refresh();
                return;
            }
        }

        if (!event.buttonstate) return;
        if (this.mode == Mode.SELECT && this.cornerSelectionEnabled && this.minecraft.thePlayer.getHeldItem() == null
            && (event.button == 0 || event.button == 1)) {
            event.setCanceled(true);
            setSelectionCorner(event.button == 0);
            return;
        }
        if (event.button == 1 && this.minecraft.thePlayer.getHeldItem() == null) {
            if (this.mode == Mode.PASTE && this.pastePreview) {
                event.setCanceled(true);
                paste();
                this.pastePreview = false;
            } else if (this.mode == Mode.REPLACE) {
                event.setCanceled(true);
                replaceClicked();
            } else if (this.mode == Mode.ROTATE_META) {
                event.setCanceled(true);
                rotateClicked(1);
            }
        } else if (event.button == 0 && this.minecraft.thePlayer.getHeldItem() == null) {
            if (this.mode == Mode.PASTE && this.pastePreview) {
                event.setCanceled(true);
                this.pastePreview = false;
                message(Names.Gui.Editor.CHAT_CANCEL);
            } else if (this.mode == Mode.ROTATE_META) {
                event.setCanceled(true);
                rotateClicked(-1);
            }
        }
    }

    private void ensureSelection() {
        if (this.hasSelection) return;
        if (this.minecraft.thePlayer == null) return;
        int x = (int) Math.floor(this.minecraft.thePlayer.posX);
        int y = (int) Math.floor(this.minecraft.thePlayer.posY) - 1;
        int z = (int) Math.floor(this.minecraft.thePlayer.posZ);
        this.minX = this.maxX = x;
        this.minY = this.maxY = y;
        this.minZ = this.maxZ = z;
        this.cornerAX = this.cornerBX = x;
        this.cornerAY = this.cornerBY = y;
        this.cornerAZ = this.cornerBZ = z;
        this.hasSelection = true;
    }

    private void setSelectionCorner(boolean first) {
        MovingObjectPosition hit = this.selectSchematic ? ClientProxy.movingObjectPosition : this.minecraft.objectMouseOver;
        int x;
        int y;
        int z;
        if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            x = hit.blockX;
            y = hit.blockY;
            z = hit.blockZ;
            if (this.selectSchematic && ClientProxy.schematic != null) {
                x += ClientProxy.schematic.position.x;
                y += ClientProxy.schematic.position.y;
                z += ClientProxy.schematic.position.z;
            }
        } else {
            final double distance = 5.0;
            x = (int) Math.floor(this.minecraft.thePlayer.posX
                + this.minecraft.thePlayer.getLookVec().xCoord * distance);
            y = (int) Math.floor(this.minecraft.thePlayer.posY + this.minecraft.thePlayer.getEyeHeight()
                + this.minecraft.thePlayer.getLookVec().yCoord * distance);
            z = (int) Math.floor(this.minecraft.thePlayer.posZ
                + this.minecraft.thePlayer.getLookVec().zCoord * distance);
        }
        if (first) {
            this.cornerAX = x; this.cornerAY = y; this.cornerAZ = z;
        } else {
            this.cornerBX = x; this.cornerBY = y; this.cornerBZ = z;
        }
        this.minX = Math.min(this.cornerAX, this.cornerBX);
        this.minY = Math.min(this.cornerAY, this.cornerBY);
        this.minZ = Math.min(this.cornerAZ, this.cornerBZ);
        this.maxX = Math.max(this.cornerAX, this.cornerBX);
        this.maxY = Math.max(this.cornerAY, this.cornerBY);
        this.maxZ = Math.max(this.cornerAZ, this.cornerBZ);
        RendererSchematicGlobal.INSTANCE.refresh();
    }

    private void resetSelectionCenter() {
        int width = this.maxX - this.minX;
        int height = this.maxY - this.minY;
        int length = this.maxZ - this.minZ;
        this.hasSelection = false;
        ensureSelection();
        this.minX -= width / 2;
        this.minY -= height / 2;
        this.minZ -= length / 2;
        this.maxX = this.minX + width;
        this.maxY = this.minY + height;
        this.maxZ = this.minZ + length;
        RendererSchematicGlobal.INSTANCE.refresh();
    }

    private void adjustSelection(int amount) {
        ensureSelection();
        MovingObjectPosition hit = getSelectionHit();
        if (hit == null || !isHitWithinSelection(hit)) return;
        switch (hit.sideHit) {
            case 0: this.minY = Math.min(this.minY - amount, this.maxY); break;
            case 1: this.maxY = Math.max(this.maxY + amount, this.minY); break;
            case 2: this.minZ = Math.min(this.minZ - amount, this.maxZ); break;
            case 3: this.maxZ = Math.max(this.maxZ + amount, this.minZ); break;
            case 4: this.minX = Math.min(this.minX - amount, this.maxX); break;
            case 5: this.maxX = Math.max(this.maxX + amount, this.minX); break;
            default: break;
        }
        RendererSchematicGlobal.INSTANCE.refresh();
    }

    private void moveSelection(int amount) {
        ensureSelection();
        if (this.minecraft.thePlayer == null) return;
        final double lookX = this.minecraft.thePlayer.getLookVec().xCoord;
        final double lookY = this.minecraft.thePlayer.getLookVec().yCoord;
        final double lookZ = this.minecraft.thePlayer.getLookVec().zCoord;
        int dx = 0;
        int dy = 0;
        int dz = 0;
        if (Math.abs(lookX) >= Math.abs(lookY) && Math.abs(lookX) >= Math.abs(lookZ)) dx = lookX >= 0 ? amount : -amount;
        else if (Math.abs(lookY) >= Math.abs(lookZ)) dy = lookY >= 0 ? amount : -amount;
        else dz = lookZ >= 0 ? amount : -amount;
        this.minX += dx;
        this.maxX += dx;
        this.minY += dy;
        this.maxY += dy;
        this.minZ += dz;
        this.maxZ += dz;
        RendererSchematicGlobal.INSTANCE.refresh();
    }

    private void moveSchematic(int amount) {
        if (ClientProxy.schematic == null || this.minecraft.thePlayer == null) {
            message(Names.Gui.Editor.CHAT_NO_SCHEMATIC);
            return;
        }
        final double lookX = this.minecraft.thePlayer.getLookVec().xCoord;
        final double lookY = this.minecraft.thePlayer.getLookVec().yCoord;
        final double lookZ = this.minecraft.thePlayer.getLookVec().zCoord;
        if (Math.abs(lookX) >= Math.abs(lookY) && Math.abs(lookX) >= Math.abs(lookZ)) {
            ClientProxy.schematic.position.x += lookX >= 0 ? amount : -amount;
        } else if (Math.abs(lookY) >= Math.abs(lookZ)) {
            ClientProxy.schematic.position.y += lookY >= 0 ? amount : -amount;
        } else {
            ClientProxy.schematic.position.z += lookZ >= 0 ? amount : -amount;
        }
    }

    private MovingObjectPosition getSelectionHit() {
        if (this.minecraft.thePlayer == null) return null;
        final Vec3 start = Vec3.createVectorHelper(
            this.minecraft.thePlayer.posX,
            this.minecraft.thePlayer.posY + this.minecraft.thePlayer.getEyeHeight(),
            this.minecraft.thePlayer.posZ);
        final Vec3 look = this.minecraft.thePlayer.getLookVec();
        final Vec3 end = start.addVector(look.xCoord * 128.0, look.yCoord * 128.0, look.zCoord * 128.0);
        return AxisAlignedBB.getBoundingBox(
            this.minX,
            this.minY,
            this.minZ,
            this.maxX + 1,
            this.maxY + 1,
            this.maxZ + 1)
            .calculateIntercept(start, end);
    }

    private boolean isHitWithinSelection(MovingObjectPosition hit) {
        return hit.hitVec != null;
    }

    private void copy(boolean cut) {
        ensureSelection();
        int width = this.maxX - this.minX + 1;
        int height = this.maxY - this.minY + 1;
        int length = this.maxZ - this.minZ + 1;
        Clipboard result = new Clipboard(width, height, length);
        SchematicWorld schematicWorld = ClientProxy.schematic;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int worldX = this.minX + x;
                    int worldY = this.minY + y;
                    int worldZ = this.minZ + z;
                    Block block;
                    int metadata;
                    if (this.selectSchematic && schematicWorld != null) {
                        int sx = worldX - schematicWorld.position.x;
                        int sy = worldY - schematicWorld.position.y;
                        int sz = worldZ - schematicWorld.position.z;
                        block = schematicWorld.getSchematic().getBlock(sx, sy, sz);
                        metadata = schematicWorld.getSchematic().getBlockMetadata(sx, sy, sz);
                        if (cut) schematicWorld.getSchematic().setBlock(sx, sy, sz, Blocks.air, 0);
                    } else {
                        block = this.minecraft.theWorld.getBlock(worldX, worldY, worldZ);
                        metadata = this.minecraft.theWorld.getBlockMetadata(worldX, worldY, worldZ);
                    }
                    result.blocks[x][y][z] = block;
                    result.metadata[x][y][z] = (byte) metadata;
                }
            }
        }
        this.clipboard = result;
        this.clipboardWorld = createClipboardWorld(result);
        if (cut && this.selectSchematic) refresh();
        message(cut ? Names.Gui.Editor.CHAT_CUT : Names.Gui.Editor.CHAT_COPY, width, height, length);
    }

    private SchematicWorld createClipboardWorld(Clipboard source) {
        Schematic schematic = new Schematic(null, source.blocks.length, source.blocks[0].length, source.blocks[0][0].length);
        for (int x = 0; x < source.blocks.length; x++) {
            for (int y = 0; y < source.blocks[0].length; y++) {
                for (int z = 0; z < source.blocks[0][0].length; z++) {
                    Block block = source.blocks[x][y][z];
                    schematic.setBlock(x, y, z, block == null ? Blocks.air : block, source.metadata[x][y][z]);
                }
            }
        }
        return new SchematicWorld(schematic);
    }

    private void resetPastePosition() {
        if (this.minecraft.thePlayer == null) return;
        int distance = 5;
        this.pasteX = (int) Math.floor(this.minecraft.thePlayer.posX + this.minecraft.thePlayer.getLookVec().xCoord * distance);
        this.pasteY = (int) Math.floor(this.minecraft.thePlayer.posY);
        this.pasteZ = (int) Math.floor(this.minecraft.thePlayer.posZ + this.minecraft.thePlayer.getLookVec().zCoord * distance);
    }

    private void movePaste(int amount) {
        if (this.minecraft.thePlayer == null || !this.pastePreview) return;
        this.pasteX += (int) Math.round(this.minecraft.thePlayer.getLookVec().xCoord * amount);
        this.pasteY += (int) Math.round(this.minecraft.thePlayer.getLookVec().yCoord * amount);
        this.pasteZ += (int) Math.round(this.minecraft.thePlayer.getLookVec().zCoord * amount);
    }

    private void beginPaste() {
        if (this.clipboard == null) {
            message(Names.Gui.Editor.CHAT_EMPTY);
            return;
        }
        resetPastePosition();
        this.pastePreview = true;
        message(Names.Gui.Editor.CHAT_PREVIEW);
    }

    private void paste() {
        if (this.clipboard == null) {
            message(Names.Gui.Editor.CHAT_EMPTY);
            return;
        }
        SchematicWorld oldWorld = ClientProxy.schematic;
        if (oldWorld == null) {
            message(Names.Gui.Editor.CHAT_NO_SCHEMATIC);
            return;
        }
        ISchematic old = oldWorld.getSchematic();
        int minWorldX = Math.min(oldWorld.position.x, this.pasteX);
        int minWorldY = Math.min(oldWorld.position.y, this.pasteY);
        int minWorldZ = Math.min(oldWorld.position.z, this.pasteZ);
        int maxWorldX = Math.max(oldWorld.position.x + old.getWidth() - 1, this.pasteX + this.clipboard.blocks.length - 1);
        int maxWorldY = Math.max(oldWorld.position.y + old.getHeight() - 1, this.pasteY + this.clipboard.blocks[0].length - 1);
        int maxWorldZ = Math.max(oldWorld.position.z + old.getLength() - 1, this.pasteZ + this.clipboard.blocks[0][0].length - 1);
        Schematic merged = new Schematic(old.getIcon(), maxWorldX - minWorldX + 1, maxWorldY - minWorldY + 1, maxWorldZ - minWorldZ + 1);
        for (int x = 0; x < old.getWidth(); x++) for (int y = 0; y < old.getHeight(); y++) for (int z = 0; z < old.getLength(); z++) {
            merged.setBlock(x + oldWorld.position.x - minWorldX, y + oldWorld.position.y - minWorldY, z + oldWorld.position.z - minWorldZ,
                old.getBlock(x, y, z), old.getBlockMetadata(x, y, z));
        }
        for (TileEntity tileEntity : old.getTileEntities()) {
            NBTTagCompound tag = new NBTTagCompound();
            tileEntity.writeToNBT(tag);
            tag.setInteger("x", tileEntity.xCoord + oldWorld.position.x - minWorldX);
            tag.setInteger("y", tileEntity.yCoord + oldWorld.position.y - minWorldY);
            tag.setInteger("z", tileEntity.zCoord + oldWorld.position.z - minWorldZ);
            TileEntity copy = TileEntity.createAndLoadEntity(tag);
            if (copy != null) merged.setTileEntity(copy.xCoord, copy.yCoord, copy.zCoord, copy);
        }
        for (int x = 0; x < this.clipboard.blocks.length; x++) for (int y = 0; y < this.clipboard.blocks[0].length; y++) for (int z = 0; z < this.clipboard.blocks[0][0].length; z++) {
            int tx = x + this.pasteX - minWorldX;
            int ty = y + this.pasteY - minWorldY;
            int tz = z + this.pasteZ - minWorldZ;
            if (merged.getBlock(tx, ty, tz) == Blocks.air) merged.setBlock(tx, ty, tz, this.clipboard.blocks[x][y][z], this.clipboard.metadata[x][y][z]);
        }
        ClientProxy.replaceSchematic(merged, oldWorld.name, minWorldX, minWorldY, minWorldZ, oldWorld.isRendering);
        message(Names.Gui.Editor.CHAT_PASTE);
    }

    private void replaceClicked() {
        SchematicWorld world = ClientProxy.schematic;
        MovingObjectPosition hit = ClientProxy.movingObjectPosition;
        if (world == null || hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        ReplaceTarget target = getReplaceTarget();
        if (target == null) {
            message(Names.Gui.Editor.CHAT_REPLACE_TARGET);
            return;
        }

        ISchematic schematic = world.getSchematic();
        Block source = schematic.getBlock(hit.blockX, hit.blockY, hit.blockZ);
        int sourceMeta = schematic.getBlockMetadata(hit.blockX, hit.blockY, hit.blockZ);
        int count = 0;
        if (source == null || source == Blocks.air) return;
        if (this.replaceMode == 0) {
            count = setBlock(schematic, hit.blockX, hit.blockY, hit.blockZ, target.block, target.metadata) ? 1 : 0;
        } else if (this.replaceMode == 1) {
            count = replaceBounds(schematic, this.minX - world.position.x, this.minY - world.position.y,
                this.minZ - world.position.z, this.maxX - world.position.x, this.maxY - world.position.y,
                this.maxZ - world.position.z,
                source, sourceMeta, target.block, target.metadata);
        } else {
            count = replaceBounds(schematic, 0, 0, 0, schematic.getWidth() - 1, schematic.getHeight() - 1,
                schematic.getLength() - 1, source, sourceMeta, target.block, target.metadata);
        }
        if (count > 0) {
            refresh();
            message(Names.Gui.Editor.CHAT_REPLACED, count);
        }
    }

    private void rotateClicked(int delta) {
        SchematicWorld world = ClientProxy.schematic;
        MovingObjectPosition hit = ClientProxy.movingObjectPosition;
        if (world == null || hit == null) return;
        ISchematic schematic = world.getSchematic();
        Block source = schematic.getBlock(hit.blockX, hit.blockY, hit.blockZ);
        int sourceMeta = schematic.getBlockMetadata(hit.blockX, hit.blockY, hit.blockZ);
        int count;
        if (source == null || source == Blocks.air) return;
        if (this.replaceMode == 0) {
            count = setMetadata(schematic, hit.blockX, hit.blockY, hit.blockZ, sourceMeta + delta) ? 1 : 0;
        } else if (this.replaceMode == 1) {
            count = rotateBounds(schematic, this.minX - world.position.x, this.minY - world.position.y,
                this.minZ - world.position.z, this.maxX - world.position.x, this.maxY - world.position.y,
                this.maxZ - world.position.z,
                source, sourceMeta, delta);
        } else {
            count = rotateBounds(schematic, 0, 0, 0, schematic.getWidth() - 1, schematic.getHeight() - 1,
                schematic.getLength() - 1, source, sourceMeta, delta);
        }
        if (count > 0) {
            refresh();
            message(Names.Gui.Editor.CHAT_ROTATED, count);
        }
    }

    private ReplaceTarget getReplaceTarget() {
        if (this.replaceBlock != null && this.replaceBlock != Blocks.air) {
            return new ReplaceTarget(this.replaceBlock, this.replaceMetadata);
        }
        return null;
    }

    private int replaceBounds(ISchematic schematic, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
        Block source, int sourceMeta, Block target, int targetMeta) {
        int count = 0;
        for (int x = minX; x <= maxX; x++) for (int y = minY; y <= maxY; y++) for (int z = minZ; z <= maxZ; z++) {
            if (schematic.getBlock(x, y, z) == source && schematic.getBlockMetadata(x, y, z) == sourceMeta
                && setBlock(schematic, x, y, z, target, targetMeta)) count++;
        }
        return count;
    }

    private int rotateBounds(ISchematic schematic, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
        Block source, int sourceMeta, int delta) {
        int count = 0;
        for (int x = minX; x <= maxX; x++) for (int y = minY; y <= maxY; y++) for (int z = minZ; z <= maxZ; z++) {
            if (schematic.getBlock(x, y, z) == source && schematic.getBlockMetadata(x, y, z) == sourceMeta
                && setMetadata(schematic, x, y, z, sourceMeta + delta)) count++;
        }
        return count;
    }

    private boolean setBlock(ISchematic schematic, int x, int y, int z, Block block, int metadata) {
        return block != null && schematic.setBlock(x, y, z, block, metadata);
    }

    private boolean setMetadata(ISchematic schematic, int x, int y, int z, int metadata) {
        return schematic.setBlockMetadata(x, y, z, metadata);
    }

    private void refresh() {
        RendererSchematicGlobal.INSTANCE.destroyRendererSchematicChunks();
        if (ClientProxy.schematic != null) RendererSchematicGlobal.INSTANCE.createRendererSchematicChunks(ClientProxy.schematic);
        RendererSchematicGlobal.INSTANCE.refresh();
    }

    private void message(String key, Object... arguments) {
        if (this.minecraft.thePlayer != null) {
            this.minecraft.thePlayer.addChatMessage(new ChatComponentText(
                I18n.format(Names.Gui.Editor.CHAT_PREFIX) + I18n.format(key, arguments)));
        }
    }
}
