package com.yuno.schematicaneo.converter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LitematicReader {

    public static StructureData read(File file) throws IOException {
        CustomNBTReader.Node root = CustomNBTReader.readCompressed(file);
        if (root == null) throw new IOException("Cannot read .litematic file");

        CustomNBTReader.Node regions = root.getChild("Regions");
        if (regions == null || regions.children.isEmpty()) {
            throw new IOException("No regions found in .litematic file");
        }

        String firstRegionName = regions.children.keySet().iterator().next();
        CustomNBTReader.Node region = regions.getChild(firstRegionName);

        int w, h, l;
        CustomNBTReader.Node sizeNode = region.getChild("Size");
        if (sizeNode != null && sizeNode.children.containsKey("x")) {
            w = Math.abs(sizeNode.getInteger("x"));
            h = Math.abs(sizeNode.getInteger("y"));
            l = Math.abs(sizeNode.getInteger("z"));
        } else {
            int[] size = region.getIntArray("Size");
            if (size != null && size.length >= 3) {
                w = Math.abs(size[0]);
                h = Math.abs(size[1]);
                l = Math.abs(size[2]);
            } else {
                w = h = l = 1;
            }
        }

        List<CustomNBTReader.Node> paletteList = region.getList("BlockStatePalette");
        List<String> palette = new ArrayList<>();
        for (CustomNBTReader.Node state : paletteList) {
            palette.add(CustomNBTReader.formatBlockState(state));
        }

        if (palette.isEmpty()) {
            palette.add("minecraft:air");
        }

        int volume = w * h * l;
        int[] blockIndices = new int[volume];
        Arrays.fill(blockIndices, -1);

        long[] blockStates = region.getLongArray("BlockStates");
        if (blockStates != null && blockStates.length > 0) {
            int bitsPerEntry = Math.max(32 - Integer.numberOfLeadingZeros(palette.size() - 1), 2);
            unpackBlockStates(blockStates, blockIndices, bitsPerEntry, volume);
        }

        StructureData data = new StructureData(w, h, l, palette, blockIndices);

        return data;
    }

    private static void unpackBlockStates(long[] packed, int[] result, int bits, int total) {
        long mask = bits >= 64 ? -1L : (1L << bits) - 1;
        for (int i = 0; i < total; i++) {
            long bitIndex = (long) i * bits;
            int longIndex = (int) (bitIndex / 64);
            int bitOffset = (int) (bitIndex % 64);

            if (longIndex >= packed.length) break;

            long value;
            if (bitOffset + bits <= 64) {
                value = packed[longIndex] >>> bitOffset;
            } else {
                int remaining = 64 - bitOffset;
                long low = packed[longIndex] >>> bitOffset;
                if (longIndex + 1 < packed.length) {
                    long high = packed[longIndex + 1] << remaining;
                    value = low | high;
                } else {
                    value = low;
                }
            }

            result[i] = (int) (value & mask);
        }
    }
}
