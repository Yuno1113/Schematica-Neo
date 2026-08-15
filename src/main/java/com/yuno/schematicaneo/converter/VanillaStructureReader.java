package com.yuno.schematicaneo.converter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VanillaStructureReader {

    public static StructureData read(File file) throws IOException {
        CustomNBTReader.Node root;
        try {
            root = CustomNBTReader.readCompressed(file);
        } catch (Exception e) {
            root = CustomNBTReader.readFile(file);
        }
        if (root == null) throw new IOException("Cannot read file: " + file.getName());
        return readFromNode(root);
    }

    public static StructureData readFromNode(CustomNBTReader.Node root) {
        int[] size = root.getIntArray("size");
        if (size == null || size.length < 3) {
            size = root.getIntArray("Size");
            if (size == null || size.length < 3) {
                size = new int[]{1, 1, 1};
            }
        }
        int w = size[0];
        int h = size[1];
        int l = size[2];
        int volume = w * h * l;

        List<CustomNBTReader.Node> paletteList = root.getList("palette");
        if (paletteList.isEmpty()) paletteList = root.getList("Palette");

        List<String> palette = new ArrayList<>();
        int[] blockIndices = new int[volume];

        // Check for classic Schematic format (Blocks byte array)
        byte[] blocksArray = null;
        if (root.getChild("Blocks") != null && root.getChild("Blocks").value instanceof byte[]) {
            blocksArray = (byte[]) root.getChild("Blocks").value;
        }

        if (blocksArray != null && blocksArray.length >= volume) {
            // Classic Schematic format with byte array block IDs
            byte[] dataArray = root.getChild("Data") != null && root.getChild("Data").value instanceof byte[]
                    ? (byte[]) root.getChild("Data").value : null;
            byte[] addArray = root.getChild("AddBlocks") != null && root.getChild("AddBlocks").value instanceof byte[]
                    ? (byte[]) root.getChild("AddBlocks").value : null;

            java.util.Map<String, Integer> stateToIdx = new java.util.LinkedHashMap<>();
            for (int i = 0; i < volume; i++) {
                int blockId = blocksArray[i] & 0xFF;
                if (addArray != null) {
                    int addIdx = i / 2;
                    if (addIdx < addArray.length) {
                        blockId |= ((i & 1) == 0 ? (addArray[addIdx] & 0xF) : (addArray[addIdx] >> 4 & 0xF)) << 8;
                    }
                }
                int meta = dataArray != null ? (dataArray[i] & 0xF) : 0;
                String state = "minecraft:id_" + blockId + ":" + meta;
                Integer idx = stateToIdx.get(state);
                if (idx == null) {
                    idx = palette.size();
                    palette.add(state);
                    stateToIdx.put(state, idx);
                }
                blockIndices[i] = idx;
            }
            if (palette.isEmpty()) palette.add("minecraft:air");

            StructureData data = new StructureData(w, h, l, palette, blockIndices);

            return data;
        }

        // 1.13+ structure format (palette + palette-indexed blocks)
        if (!paletteList.isEmpty()) {
            for (CustomNBTReader.Node state : paletteList) {
                palette.add(CustomNBTReader.formatBlockState(state));
            }
        }
        if (palette.isEmpty()) palette.add("minecraft:air");

        // Default all positions to -1 (empty/air). .nbt format omits air positions from blocks list.
        Arrays.fill(blockIndices, -1);

        List<CustomNBTReader.Node> blocksList = root.getList("blocks");
        if (blocksList.isEmpty()) blocksList = root.getList("Blocks");

        for (CustomNBTReader.Node entry : blocksList) {
            int[] pos = entry.getIntArray("pos");
            if (pos == null) pos = entry.getIntArray("Pos");
            if (pos == null || pos.length < 3) continue;
            int stateIdx = entry.getInteger("state");
            if (stateIdx < 0 || stateIdx >= palette.size()) continue;
            int x = pos[0];
            int y = pos[1];
            int z = pos[2];
            if (x < 0 || x >= w || y < 0 || y >= h || z < 0 || z >= l) continue;
            blockIndices[(y * l + z) * w + x] = stateIdx;
        }

        StructureData data = new StructureData(w, h, l, palette, blockIndices);

        return data;
    }
}
