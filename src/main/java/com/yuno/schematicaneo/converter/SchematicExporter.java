package com.yuno.schematicaneo.converter;

import com.yuno.schematicaneo.SchematicaNeo;
import com.yuno.schematicaneo.world.storage.Schematic;
import cpw.mods.fml.common.FMLLog;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.*;

public class SchematicExporter {

    public static class ConversionResult {
        public final Schematic schematic;
        public final List<String> unknownBlockStates;
        public final Map<String, Integer> unknownMetaMap;

        public ConversionResult(Schematic schematic, List<String> unknownBlockStates, Map<String, Integer> unknownMetaMap) {
            this.schematic = schematic;
            this.unknownBlockStates = unknownBlockStates;
            this.unknownMetaMap = unknownMetaMap;
        }
    }

    public static ConversionResult convert(StructureData data) {
        try {
            Schematic schematic = new Schematic(new ItemStack(Blocks.stone), data.width, data.height, data.length);

            List<String> unknownList = new ArrayList<>();
            Set<String> unknownSet = new LinkedHashSet<>();
            Map<String, Integer> unknownIndexMap = new LinkedHashMap<>();

            for (int y = 0; y < data.height; y++) {
                for (int z = 0; z < data.length; z++) {
                    for (int x = 0; x < data.width; x++) {
                        String state = data.getBlockState(x, y, z);

                        if (state == null) {
                            schematic.setBlock(x, y, z, Blocks.air, 0);
                            continue;
                        }

                        if (state.equals("minecraft:air") || state.equals("minecraft:air[]") || state.startsWith("minecraft:id_0:") || state.startsWith("minecraft:cave_air") || state.startsWith("minecraft:void_air") || state.equals("minecraft:structure_void[]") || state.equals("minecraft:structure_void") || state.startsWith("minecraft:air[") || state.startsWith("minecraft:structure_void[")) {
                            schematic.setBlock(x, y, z, Blocks.air, 0);
                            continue;
                        }

                        // Parse block state properties and compute adjusted metadata
                        Map<String, String> props = new HashMap<>();
                        String baseState = state;
                        int bracket = state.indexOf('[');
                        if (bracket >= 0) {
                            baseState = state.substring(0, bracket);
                            String propStr = state.substring(bracket + 1, state.length() - 1);
                            for (String p : propStr.split(",")) {
                                String[] kv = p.split("=", 2);
                                if (kv.length == 2) props.put(kv[0], kv[1]);
                            }
                        }

                        MappingEntry mapping = MappingLoader.findMapping(baseState);
                        if (mapping != null) {
                            Block block = mapping.resolveBlock();
                            if (block != null) {
                                int meta = mapping.metadata;
                                if (!props.isEmpty()) {
                                    BlockMeta bm = applyBlockStateProps(baseState, props, new BlockMeta(block, meta));
                                    block = bm.block;
                                    meta = bm.meta;
                                }
                                schematic.setBlock(x, y, z, block, meta);
                            } else {
                                if (!unknownSet.contains(baseState)) {
                                    unknownSet.add(baseState);
                                    unknownList.add(baseState);
                                    unknownIndexMap.put(baseState, unknownList.size() - 1);
                                    FMLLog.warning("[SchematicaNeo] Unresolved block: %s (mapping found but resolveBlock failed)", state);
                                }
                                placeFallback(schematic, x, y, z, baseState, unknownIndexMap);
                            }
                        } else {
                            if (!unknownSet.contains(baseState)) {
                                unknownSet.add(baseState);
                                unknownList.add(baseState);
                                unknownIndexMap.put(baseState, unknownList.size() - 1);
                                FMLLog.warning("[SchematicaNeo] Unknown block state (no mapping): %s", state);
                            }
                            placeFallback(schematic, x, y, z, baseState, unknownIndexMap);
                        }
                    }
                }
            }

            return new ConversionResult(schematic, unknownList, unknownIndexMap);

        } catch (Exception e) {
            FMLLog.warning("[SchematicaNeo] SchematicExporter.convert failed: " + e.getMessage());
            return null;
        }
    }

    private static void placeFallback(Schematic schematic, int x, int y, int z, String state, Map<String, Integer> indexMap) {
        int idx = indexMap.get(state);
        Block fb;
        int fbMeta;
        if (idx < 16) {
            fb = SchematicaNeo.markerPrimary;
            fbMeta = idx;
        } else {
            fb = SchematicaNeo.markerSecondary;
            fbMeta = idx - 16;
        }
        if (fb == null) {
            schematic.setBlock(x, y, z, Blocks.dirt, 0);
            return;
        }
        schematic.setBlock(x, y, z, fb, fbMeta);
    }

    // 鈹€鈹€ Block state property 鈫?1.7.10 metadata conversion 鈹€鈹€

    private static class BlockMeta {
        public Block block;
        public int meta;
        BlockMeta(Block block, int meta) {
            this.block = block;
            this.meta = meta;
        }
    }

    private static BlockMeta applyBlockStateProps(String baseState, Map<String, String> props, BlockMeta bm) {
        Block block = bm.block;
        int meta = bm.meta;

        String name = baseState;
        if (name.startsWith("minecraft:")) name = name.substring(10);

        String facing = props.get("facing");
        String half = props.get("half");
        String type = props.get("type");
        String open = props.get("open");
        String hinge = props.get("hinge");

        boolean topHalf = "top".equals(half) || "upper".equals(half);
        boolean bottomHalf = "bottom".equals(half) || "lower".equals(half);

        // 鈹€鈹€ Slabs 鈹€鈹€
        if (name.endsWith("_slab")) {
            boolean addTop = "top".equals(type) || topHalf;
            boolean addDouble = "double".equals(type);

            if (block == Blocks.stone_slab || block == Blocks.wooden_slab) {
                // Vanilla 1.7.10 slab: material sub-metadata kept in meta, bit 3 (8) = upper half.
                // A double (full) slab is represented by a different block id.
                meta &= ~8;
                if (addTop) meta |= 8;
                if (addDouble) {
                    block = block == Blocks.stone_slab ? Blocks.double_stone_slab : Blocks.double_wooden_slab;
                    meta &= ~8;
                }
            } else if (block == Blocks.double_stone_slab || block == Blocks.double_wooden_slab) {
                // Already a full (double) slab: keep only the material sub-metadata.
                meta &= ~8;
            } else {
                // MMM (manametalmod) slab scheme: bottom=0, top=1, double=2 (same block id).
                meta = addDouble ? 2 : (addTop ? 1 : 0);
            }
        }

        // 鈹€鈹€ Stairs (1.7.10: east=0, west=1, south=2, north=3; bit 2 (4) = upside down) 鈹€鈹€
        if (name.endsWith("_stairs")) {
            int facingMeta = meta & 3;
            if ("east".equals(facing)) facingMeta = 0;
            else if ("west".equals(facing)) facingMeta = 1;
            else if ("south".equals(facing)) facingMeta = 2;
            else if ("north".equals(facing)) facingMeta = 3;
            meta = (meta & ~7) | facingMeta;
            if (topHalf) meta |= 4;
            else if (bottomHalf) meta &= ~4;
        }

        // 鈹€鈹€ Trapdoors 鈹€鈹€
        if (name.endsWith("_trapdoor")) {
            int facingMeta = 0;
            if ("west".equals(facing)) facingMeta = 0;
            else if ("east".equals(facing)) facingMeta = 1;
            else if ("north".equals(facing)) facingMeta = 2;
            else if ("south".equals(facing)) facingMeta = 3;
            meta = (meta & ~3) | facingMeta;
            if ("true".equals(open)) meta |= 4;
            else meta &= ~4;
            if (topHalf) meta |= 8;
            else if (bottomHalf) meta &= ~8;
        }

        // 鈹€鈹€ Doors (lower half facing + open) 鈹€鈹€
        if (name.endsWith("_door")) {
            if (topHalf) {
                // Upper half: hinge side
                if ("right".equals(hinge)) meta |= 1;
            } else {
                // Lower half: facing + open
                int facingMeta = 0;
                if ("west".equals(facing)) facingMeta = 0;
                else if ("north".equals(facing)) facingMeta = 1;
                else if ("east".equals(facing)) facingMeta = 2;
                else if ("south".equals(facing)) facingMeta = 3;
                meta = (meta & ~3) | facingMeta;
                if ("true".equals(open)) meta |= 4;
            }
        }

        // 鈹€鈹€ Furnace / Chest / Dispenser / Dropper 鈹€鈹€
        if (name.equals("furnace") || name.equals("blast_furnace") || name.equals("smoker")
                || name.equals("dispenser") || name.equals("dropper")
                || name.equals("chest") || name.equals("trapped_chest")
                || name.equals("barrel") || name.equals("shulker_box")) {
            int facingMeta = 2; // north
            if ("north".equals(facing)) facingMeta = 2;
            else if ("south".equals(facing)) facingMeta = 3;
            else if ("east".equals(facing)) facingMeta = 4;
            else if ("west".equals(facing)) facingMeta = 5;
            meta = (meta & ~7) | facingMeta;
        }

        // 鈹€鈹€ Ladder / Wall Torch 鈹€鈹€
        if (name.equals("ladder") || name.equals("wall_torch") || name.equals("soul_wall_torch")) {
            int facingMeta = 2; // north
            if ("north".equals(facing)) facingMeta = 2;
            else if ("south".equals(facing)) facingMeta = 3;
            else if ("west".equals(facing)) facingMeta = 4;
            else if ("east".equals(facing)) facingMeta = 5;
            meta = (meta & ~7) | facingMeta;
        }

        // 鈹€鈹€ Lever / Button 鈹€鈹€
        if (name.equals("lever") || name.endsWith("_button")) {
            String face = props.get("face");
            if ("true".equals(props.get("powered"))) meta |= 8;
            if ("ceiling".equals(face)) {
                meta = (meta & ~7) | 0; // ceiling: meta 0-3 for facing
                if ("south".equals(facing)) meta |= 0;
                else if ("west".equals(facing)) meta |= 1;
                else if ("north".equals(facing)) meta |= 2;
                else if ("east".equals(facing)) meta |= 3;
            } else if ("wall".equals(face)) {
                if ("north".equals(facing)) meta = (meta & ~7) | 4;
                else if ("south".equals(facing)) meta = (meta & ~7) | 5;
                else if ("west".equals(facing)) meta = (meta & ~7) | 6;
                else if ("east".equals(facing)) meta = (meta & ~7) | 7;
            } else {
                // floor
                if ("south".equals(facing)) meta = (meta & ~7) | 0;
                else if ("west".equals(facing)) meta = (meta & ~7) | 1;
                else if ("north".equals(facing)) meta = (meta & ~7) | 2;
                else if ("east".equals(facing)) meta = (meta & ~7) | 3;
            }
        }

        return new BlockMeta(block, meta);
    }
}
