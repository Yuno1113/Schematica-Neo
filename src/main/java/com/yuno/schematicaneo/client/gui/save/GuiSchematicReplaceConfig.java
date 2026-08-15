package com.yuno.schematicaneo.client.gui.save;

import org.lwjgl.input.Keyboard;

import com.yuno.schematicaneo.client.handler.SchematicEditorHandler;
import com.yuno.schematicaneo.reference.Names;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class GuiSchematicReplaceConfig extends GuiScreen {

    private static final int SLOT = 18;
    private static final RenderItem ITEM_RENDERER = new RenderItem();
    private final GuiScreen parent;
    private final SchematicEditorHandler editor = SchematicEditorHandler.INSTANCE;
    private GuiTextField metadataField;
    private int left;
    private int top;

    public GuiSchematicReplaceConfig(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.left = (this.width - 176) / 2;
        this.top = (this.height - 150) / 2;
        this.metadataField = new GuiTextField(this.fontRendererObj, this.left + 86, this.top + 30, 35, 18);
        this.metadataField.setMaxStringLength(10);
        this.metadataField.setText(Integer.toString(this.editor.getReplaceMetadata()));
        this.buttonList.add(new GuiButton(1, this.left + 126, this.top + 29, 42, 20, I18n.format(Names.Gui.DONE)));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            applyMetadata();
            this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        boolean wasMetadataFocused = this.metadataField.isFocused();
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.metadataField.mouseClicked(mouseX, mouseY, mouseButton);
        if (wasMetadataFocused && !this.metadataField.isFocused()) applyMetadata();
        if (mouseButton != 0) return;
        int slot = getInventorySlot(mouseX, mouseY);
        if (slot < 0) return;
        ItemStack stack = this.mc.thePlayer.inventory.getStackInSlot(slot);
        if (stack != null && stack.getItem() instanceof ItemBlock) {
            Block block = Block.getBlockFromItem(stack.getItem());
            if (block != null) {
                this.editor.setReplaceTarget(block, stack.getItemDamage());
                this.metadataField.setText(Integer.toString(Math.max(0, stack.getItemDamage())));
            }
        }
    }

    @Override
    protected void keyTyped(char character, int keyCode) {
        if (this.metadataField.isFocused()) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                applyMetadata();
                this.metadataField.setFocused(false);
                return;
            }
            if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
                applyMetadata();
                this.metadataField.setFocused(false);
            } else {
                this.metadataField.textboxKeyTyped(character, keyCode);
            }
            return;
        }
        super.keyTyped(character, keyCode);
    }

    private void applyMetadata() {
        int metadata = 0;
        try {
            metadata = Integer.parseInt(this.metadataField.getText());
        } catch (NumberFormatException ignored) {}
        metadata = validateMetadata(this.editor.getReplaceBlock(), Math.max(0, metadata));
        this.metadataField.setText(Integer.toString(metadata));
        this.editor.setReplaceTarget(this.editor.getReplaceBlock(), metadata);
    }

    private int validateMetadata(Block block, int metadata) {
        return Math.max(0, metadata);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        drawRect(this.left, this.top, this.left + 176, this.top + 150, 0xFFC6C6C6);
        this.fontRendererObj.drawString(I18n.format(Names.Gui.Editor.REPLACE_TITLE), this.left + 8, this.top + 8, 0x404040);
        this.fontRendererObj.drawString(I18n.format(Names.Gui.Editor.REPLACE_HELP), this.left + 8, this.top + 52, 0x404040);
        this.fontRendererObj.drawString(I18n.format(Names.Gui.Editor.REPLACE_METADATA), this.left + 8, this.top + 35, 0x404040);
        this.fontRendererObj.drawString(I18n.format(Names.Gui.Editor.REPLACE_TARGET), this.left + 8, this.top + 21, 0x404040);
        this.metadataField.drawTextBox();
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 9; column++) {
                int x = this.left + 7 + column * SLOT;
                int y = this.top + 65 + row * SLOT + (row == 3 ? 4 : 0);
                drawRect(x, y, x + SLOT, y + SLOT, 0xFF777777);
            }
        }
        RenderHelper.enableGUIStandardItemLighting();
        Block target = this.editor.getReplaceBlock();
        if (target != null) {
            try {
                ItemStack targetStack = new ItemStack(target, 1, this.editor.getReplaceMetadata());
                ITEM_RENDERER.renderItemAndEffectIntoGUI(
                    this.fontRendererObj,
                    this.mc.getTextureManager(),
                    targetStack,
                    this.left + 61,
                    this.top + 16);
                this.fontRendererObj.drawString(targetStack.getDisplayName(), this.left + 82, this.top + 21, 0x404040);
            } catch (Exception ignored) {
                this.editor.setReplaceTarget(target, 0);
                this.metadataField.setText("0");
            }
        } else {
            this.fontRendererObj.drawString(I18n.format(Names.Gui.Editor.REPLACE_NONE), this.left + 61, this.top + 21, 0x777777);
        }
        for (int slot = 0; slot < this.mc.thePlayer.inventory.mainInventory.length; slot++) {
            ItemStack stack = this.mc.thePlayer.inventory.getStackInSlot(slot);
            if (stack == null) continue;
            int displayRow = slot < 9 ? 3 : (slot - 9) / 9;
            int displayColumn = slot < 9 ? slot : (slot - 9) % 9;
            int x = this.left + 8 + displayColumn * SLOT;
            int y = this.top + 66 + displayRow * SLOT + (displayRow == 3 ? 4 : 0);
            ITEM_RENDERER.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y);
        }
        RenderHelper.disableStandardItemLighting();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private int getInventorySlot(int mouseX, int mouseY) {
        int relativeX = mouseX - (this.left + 7);
        int relativeY = mouseY - (this.top + 65);
        if (relativeX < 0 || relativeX >= 9 * SLOT || relativeY < 0) return -1;
        int column = relativeX / SLOT;
        if (relativeY >= 3 * SLOT + 4 && relativeY < 4 * SLOT + 4) return column;
        int row = relativeY / SLOT;
        return row >= 0 && row < 3 ? 9 + row * 9 + column : -1;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        applyMetadata();
        super.onGuiClosed();
    }
}
