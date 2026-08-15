package com.yuno.schematicaneo.client.gui.save;

import com.yuno.schematicaneo.client.handler.SchematicEditorHandler;
import com.yuno.schematicaneo.reference.Names;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/** Mode picker opened from the editor mode button. */
public class GuiSchematicEditorMode extends GuiScreen {

    private static final int FIRST_MODE_BUTTON = 10;
    private final GuiScreen parent;
    private final SchematicEditorHandler editor = SchematicEditorHandler.INSTANCE;
    public GuiSchematicEditorMode(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        final SchematicEditorHandler.Mode[] modes = SchematicEditorHandler.Mode.values();
        final int width = 150;
        final int startX = (this.width - width) / 2;
        final int startY = (this.height - modes.length * 24) / 2;
        for (int i = 0; i < modes.length; i++) {
            this.buttonList.add(new GuiButton(
                FIRST_MODE_BUTTON + i,
                startX,
                startY + i * 24,
                width,
                20,
                editor.getModeLabel(modes[i])));
        }
        this.buttonList.add(new GuiButton(1, startX, startY + modes.length * 24 + 4, width, 20,
            I18n.format(Names.Gui.DONE)));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id >= FIRST_MODE_BUTTON
            && button.id < FIRST_MODE_BUTTON + SchematicEditorHandler.Mode.values().length) {
            editor.setMode(SchematicEditorHandler.Mode.values()[button.id - FIRST_MODE_BUTTON]);
            this.mc.displayGuiScreen(this.parent);
        } else if (button.id == 1) {
            this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        for (Object entry : this.buttonList) {
            GuiButton button = (GuiButton) entry;
            if (button.id >= FIRST_MODE_BUTTON && button.id < FIRST_MODE_BUTTON + SchematicEditorHandler.Mode.values().length
                && button.mousePressed(this.mc, mouseX, mouseY)) {
                drawHoveringText(
                    java.util.Collections.singletonList(editor.getModeTooltip(SchematicEditorHandler.Mode.values()[button.id - FIRST_MODE_BUTTON])),
                    mouseX,
                    mouseY,
                    this.fontRendererObj);
                break;
            }
        }
    }
}
