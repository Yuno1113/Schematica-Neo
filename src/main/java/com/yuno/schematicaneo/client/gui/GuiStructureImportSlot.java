package com.yuno.schematicaneo.client.gui;

import com.yuno.schematicaneo.client.gui.load.GuiSchematicEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;

class GuiStructureImportSlot extends GuiSlot {

    private final GuiStructureImporter gui;
    private final Minecraft minecraft = Minecraft.getMinecraft();
    protected int selectedIndex = -1;

    GuiStructureImportSlot(GuiStructureImporter gui) {
        super(Minecraft.getMinecraft(), gui.width, gui.height, 16, gui.height - 40, 24);
        this.gui = gui;
    }

    @Override
    protected int getSize() {
        return this.gui.structureFiles.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick, int mouseX, int mouseY) {
        GuiSchematicEntry entry = this.gui.structureFiles.get(index);
        if (entry.isDirectory()) {
            this.gui.changeDirectory(entry.getName());
        } else {
            this.gui.select(index);
        }
    }

    @Override
    protected boolean isSelected(int index) {
        return index == this.selectedIndex;
    }

    @Override
    protected void drawBackground() {}

    @Override
    protected void drawContainerBackground(Tessellator tessellator) {}

    @Override
    protected void drawSlot(int index, int x, int y, int height, Tessellator tessellator, int mouseX, int mouseY) {
        GuiSchematicEntry entry = this.gui.structureFiles.get(index);
        GuiHelper.drawItemStack(this.minecraft.renderEngine, this.minecraft.fontRenderer, x, y, entry.getItemStack());
        String name = entry.getName() + (entry.isDirectory() ? "/" : "");
        this.gui.drawString(this.minecraft.fontRenderer, name, x + 24, y + 6, 0xFFFFFF);
    }
}
