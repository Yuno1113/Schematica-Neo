package com.yuno.schematicaneo.converter;

import java.util.List;

public class StructureData {

    public final int width;
    public final int height;
    public final int length;
    public final List<String> palette;
    public final int[] blockIndices;

    public StructureData(int width, int height, int length, List<String> palette, int[] blockIndices) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.palette = palette;
        this.blockIndices = blockIndices;
    }

    public int getIndex(int x, int y, int z) {
        return (y * length + z) * width + x;
    }

    public String getBlockState(int x, int y, int z) {
        int idx = blockIndices[getIndex(x, y, z)];
        if (idx >= 0 && idx < palette.size()) {
            return palette.get(idx);
        }
        return null;
    }
}
