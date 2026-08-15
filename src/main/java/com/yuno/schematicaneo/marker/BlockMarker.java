package com.yuno.schematicaneo.marker;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class BlockMarker extends Block {

    private final boolean isPrimary;
    private IIcon[] icons = new IIcon[16];

    public BlockMarker(boolean isPrimary) {
        super(Material.rock);
        this.isPrimary = isPrimary;
        setBlockName(isPrimary ? "marker_primary" : "marker_secondary");
        setHardness(1.0F);
        setResistance(10.0F);
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    @Override
    public String getLocalizedName() {
        return "标记方块";
    }

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        String prefix = isPrimary ? "marker_primary_" : "marker_secondary_";
        for (int i = 0; i < 16; i++) {
            icons[i] = reg.registerIcon("schematicaneo:" + prefix + i);
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (meta >= 0 && meta < icons.length) return icons[meta];
        return icons[0];
    }
}
