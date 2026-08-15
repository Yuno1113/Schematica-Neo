package com.yuno.schematicaneo.client.gui;

import com.yuno.schematicaneo.client.renderer.RendererSchematicGlobal;
import com.yuno.schematicaneo.client.world.SchematicWorld;
import com.yuno.schematicaneo.proxy.ClientProxy;
import com.yuno.schematicaneo.reference.Names;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class GuiSchematicVisibleBlock extends GuiScreen {

    private static final int SLOT = 18;
    private static final RenderItem ITEM_RENDERER = new RenderItem();
    private final GuiScreen parent;
    private int left;
    private int top;

    public GuiSchematicVisibleBlock(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.left = (this.width - 176) / 2;
        this.top = (this.height - 132) / 2;
        this.buttonList.add(new GuiButton(1, this.left + 118, this.top + 6, 50, 20, I18n.format(Names.Gui.DONE)));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) this.mc.displayGuiScreen(this.parent);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton != 0 || ClientProxy.schematic == null) return;
        int slot = getSlot(mouseX, mouseY);
        if (slot < 0) return;
        ItemStack stack = this.mc.thePlayer.inventory.getStackInSlot(slot);
        if (stack != null && stack.getItem() instanceof ItemBlock) {
            Block block = Block.getBlockFromItem(stack.getItem());
            ClientProxy.schematic.visibilityBlock = block;
            ClientProxy.schematic.visibilityMetadata = stack.getItemDamage();
            ClientProxy.schematic.visibilityMode = SchematicWorld.VISIBILITY_BLOCK;
            ClientProxy.schematic.isRenderingLayer = false;
            RendererSchematicGlobal.INSTANCE.refresh();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(this.left, this.top, this.left + 176, this.top + 132, 0xFFC6C6C6);
        this.fontRendererObj.drawString(I18n.format(Names.Gui.Control.VISIBILITY_BLOCK_TITLE), this.left + 8, this.top + 8, 0x404040);
        this.fontRendererObj.drawString(I18n.format(Names.Gui.Control.VISIBILITY_BLOCK_HELP), this.left + 8, this.top + 31, 0x555555);
        for (int row = 0; row < 4; row++) for (int column = 0; column < 9; column++) {
            int x = this.left + 7 + column * SLOT;
            int y = this.top + 50 + row * SLOT + (row == 3 ? 4 : 0);
            drawRect(x, y, x + SLOT, y + SLOT, 0xFF777777);
        }
        RenderHelper.enableGUIStandardItemLighting();
        for (int slot = 0; slot < this.mc.thePlayer.inventory.mainInventory.length; slot++) {
            ItemStack stack = this.mc.thePlayer.inventory.getStackInSlot(slot);
            if (stack == null) continue;
            int row = slot < 9 ? 3 : (slot - 9) / 9;
            int column = slot < 9 ? slot : (slot - 9) % 9;
            int x = this.left + 8 + column * SLOT;
            int y = this.top + 51 + row * SLOT + (row == 3 ? 4 : 0);
            ITEM_RENDERER.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y);
        }
        RenderHelper.disableStandardItemLighting();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int getSlot(int mouseX, int mouseY) {
        int x = mouseX - (this.left + 7);
        int y = mouseY - (this.top + 50);
        if (x < 0 || x >= SLOT * 9 || y < 0) return -1;
        int column = x / SLOT;
        if (y >= SLOT * 3 + 4 && y < SLOT * 4 + 4) return column;
        int row = y / SLOT;
        return row >= 0 && row < 3 ? 9 + row * 9 + column : -1;
    }
}
