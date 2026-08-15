package com.yuno.schematicaneo.client.gui.save;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

import com.github.lunatrius.core.client.gui.GuiScreenBase;
import com.github.lunatrius.core.util.vector.Vector3i;
import com.yuno.schematicaneo.SchematicaNeo;
import com.yuno.schematicaneo.client.handler.SchematicEditorHandler;
import com.yuno.schematicaneo.client.gui.GuiStructureImporter;
import com.yuno.schematicaneo.handler.ConfigurationHandler;
import com.yuno.schematicaneo.proxy.ClientProxy;
import com.yuno.schematicaneo.reference.Names;
import com.yuno.schematicaneo.reference.Reference;
import com.yuno.schematicaneo.world.schematic.SchematicFormat;

import java.io.File;

public class GuiSchematicSave extends GuiScreenBase {

    private GuiButton btnSave = null;
    private GuiButton btnSaveEdited = null;
    private GuiButton btnEditorEnable = null;
    private GuiButton btnEditorMode = null;
    private GuiButton btnSelectionSource = null;
    private GuiButton btnImport = null;
    private GuiButton btnReplaceConfig = null;
    private GuiButton btnCornerSelection = null;
    private GuiTextField tfFilename = null;

    private String filename = "";

    private final String strOn = I18n.format(Names.Gui.ON);
    private final String strOff = I18n.format(Names.Gui.OFF);

    public GuiSchematicSave(GuiScreen guiScreen) {
        super(guiScreen);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.textFields.clear();

        int id = 0;

        this.tfFilename = new GuiTextField(this.fontRendererObj, this.width - 235, this.height - 29, 100, 18);
        this.textFields.add(this.tfFilename);

        this.btnSave = new GuiButton(id++, this.width - 130, this.height - 30, 60, 20,
            I18n.format(Names.Gui.Editor.SAVE_SELECTION));
        this.btnSave.enabled = SchematicEditorHandler.INSTANCE.isEnabled()
            && SchematicEditorHandler.INSTANCE.hasSelection();
        this.buttonList.add(this.btnSave);
        this.btnSaveEdited = new GuiButton(id++, this.width - 65, this.height - 30, 55, 20,
            I18n.format(Names.Gui.Editor.SAVE_EDITED));
        this.btnSaveEdited.enabled = ClientProxy.schematic != null;
        this.buttonList.add(this.btnSaveEdited);

        SchematicEditorHandler editor = SchematicEditorHandler.INSTANCE;
        boolean selectionMode = editor.getMode() == SchematicEditorHandler.Mode.SELECT;
        String modeLabel = I18n.format(Names.Gui.Editor.MODE, editor.getModeLabel());
        if (editor.getMode() == SchematicEditorHandler.Mode.REPLACE
            || editor.getMode() == SchematicEditorHandler.Mode.ROTATE_META) {
            modeLabel += " (" + editor.getReplaceModeLabel() + ")";
        }
        this.btnEditorEnable = new GuiButton(id++, 10, this.height - 95, 85, 20,
            I18n.format(Names.Gui.Editor.ENABLE, editor.isEnabled() ? this.strOn : this.strOff));
        this.buttonList.add(this.btnEditorEnable);
        this.btnEditorMode = new GuiButton(id++, 100, this.height - 95, 105, 20, modeLabel);
        this.btnEditorMode.enabled = editor.isEnabled();
        this.buttonList.add(this.btnEditorMode);
        this.btnSelectionSource = new GuiButton(id++, 100, this.height - 120, 105, 20,
            I18n.format(Names.Gui.Editor.SELECTION_SOURCE, editor.getSelectionSourceLabel()));
        this.btnSelectionSource.enabled = editor.isEnabled() && editor.getMode() == SchematicEditorHandler.Mode.SELECT;
        this.btnSelectionSource.visible = selectionMode;
        this.buttonList.add(this.btnSelectionSource);
        this.btnImport = new GuiButton(id++, 10, this.height - 70, 95, 20, I18n.format(Names.Gui.Editor.IMPORT));
        this.buttonList.add(this.btnImport);
        this.btnReplaceConfig = new GuiButton(id++, 110, this.height - 70, 95, 20,
            I18n.format(Names.Gui.Editor.REPLACE_CONFIG));
        this.btnReplaceConfig.enabled = editor.isEnabled();
        this.btnReplaceConfig.visible = editor.getMode() == SchematicEditorHandler.Mode.REPLACE;
        this.buttonList.add(this.btnReplaceConfig);
        this.btnCornerSelection = new GuiButton(id++, 10, this.height - 120, 85, 20,
            I18n.format(Names.Gui.Editor.CORNER_SELECTION,
                editor.isCornerSelectionEnabled() ? this.strOn : this.strOff));
        this.btnCornerSelection.enabled = editor.isEnabled() && editor.getMode() == SchematicEditorHandler.Mode.SELECT;
        this.btnCornerSelection.visible = selectionMode;
        this.buttonList.add(this.btnCornerSelection);

        this.tfFilename.setMaxStringLength(1024);
        this.tfFilename.setText(this.filename);

    }

    @Override
    protected void actionPerformed(GuiButton guiButton) {
        if (guiButton.enabled) {
            if (guiButton.id == this.btnSave.id) {
                String path;
                if (ConfigurationHandler.useSchematicplusFormat) {
                    path = this.tfFilename.getText() + ".schemplus";
                } else {
                    path = this.tfFilename.getText() + ".schematic";
                }
                final SchematicEditorHandler editor = SchematicEditorHandler.INSTANCE;
                Vector3i from = new Vector3i(editor.getMinX(), editor.getMinY(), editor.getMinZ());
                Vector3i to = new Vector3i(editor.getMaxX(), editor.getMaxY(), editor.getMaxZ());
                net.minecraft.world.World sourceWorld = this.mc.theWorld;
                if (editor.isSelectSchematic() && ClientProxy.schematic != null) {
                    sourceWorld = ClientProxy.schematic;
                    from.sub(ClientProxy.schematic.position);
                    to.sub(ClientProxy.schematic.position);
                }
                if (SchematicaNeo.proxy.saveSchematic(
                    this.mc.thePlayer,
                    ConfigurationHandler.schematicDirectory,
                    path,
                    sourceWorld,
                    from,
                    to)) {
                    this.filename = "";
                    this.tfFilename.setText(this.filename);
                }
            } else if (guiButton.id == this.btnSaveEdited.id) {
                saveEditedSchematic();
            } else if (guiButton.id == this.btnEditorEnable.id) {
                SchematicEditorHandler.INSTANCE.setEnabled(!SchematicEditorHandler.INSTANCE.isEnabled());
                initGui();
            } else if (guiButton.id == this.btnEditorMode.id) {
                this.mc.displayGuiScreen(new GuiSchematicEditorMode(this));
            } else if (guiButton.id == this.btnSelectionSource.id) {
                SchematicEditorHandler.INSTANCE.toggleSelectionSource();
                initGui();
            } else if (guiButton.id == this.btnImport.id) {
                openImporter();
            } else if (guiButton.id == this.btnReplaceConfig.id) {
                this.mc.displayGuiScreen(new GuiSchematicReplaceConfig(this));
            } else if (guiButton.id == this.btnCornerSelection.id) {
                SchematicEditorHandler.INSTANCE.toggleCornerSelection();
                initGui();
            }
        }
    }

    private void saveEditedSchematic() {
        if (ClientProxy.schematic == null || ClientProxy.schematic.getSchematic() == null) return;
        String name = this.tfFilename.getText().trim();
        if (name.isEmpty()) name = ClientProxy.schematic.name + "_edited";
        final String extension = ConfigurationHandler.useSchematicplusFormat ? ".schemplus" : ".schematic";
        if (!name.toLowerCase().endsWith(extension)) name += extension;
        final File output = new File(ConfigurationHandler.schematicDirectory, name);
        try {
            if (SchematicFormat.writeToFile(output, ClientProxy.schematic.getSchematic(), this.mc.theWorld)) {
                this.filename = "";
                this.tfFilename.setText("");
            }
        } catch (Exception e) {
            Reference.logger.error("Could not save the edited schematic!", e);
        }
    }

    private void openImporter() {
        final File schematicsDirectory = new File(this.mc.mcDataDir, "schematics");
        if (!schematicsDirectory.exists()) schematicsDirectory.mkdirs();
        this.mc.displayGuiScreen(new GuiStructureImporter(this));
    }

    @Override
    protected void keyTyped(char character, int code) {
        super.keyTyped(character, code);
        this.filename = this.tfFilename.getText();
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        final SchematicEditorHandler editor = SchematicEditorHandler.INSTANCE;
        drawString(this.fontRendererObj, I18n.format(Names.Gui.Editor.TITLE), 10, this.height - 120, 0xFFFFFF);
        if (editor.isEnabled()) {
            drawString(this.fontRendererObj, I18n.format(Names.Gui.Editor.HELP_CONTROLS), 10, this.height - 45, 0x55FF55);
            drawString(this.fontRendererObj, I18n.format(Names.Gui.Editor.HELP_ACTION), 10, this.height - 33, 0x55FF55);
            if (editor.getMode() == SchematicEditorHandler.Mode.REPLACE
                || editor.getMode() == SchematicEditorHandler.Mode.ROTATE_META) {
                drawString(this.fontRendererObj, I18n.format(Names.Gui.Editor.CURRENT_RANGE, editor.getReplaceModeLabel()),
                    110, this.height - 58, 0x55FF55);
            }
        }

        super.drawScreen(par1, par2, par3);
        if (this.btnEditorMode.func_146115_a()) {
            drawHoveringText(java.util.Collections.singletonList(editor.getModeTooltip()), par1, par2, this.fontRendererObj);
        } else if (this.btnEditorEnable.func_146115_a()) {
            drawHoveringText(java.util.Collections.singletonList(I18n.format(Names.Gui.Editor.ENABLE_TOOLTIP)),
                par1, par2, this.fontRendererObj);
        } else if (this.btnSave.func_146115_a()) {
            drawHoveringText(java.util.Collections.singletonList(I18n.format(Names.Gui.Editor.SAVE_SELECTION_TOOLTIP)),
                par1, par2, this.fontRendererObj);
        } else if (this.btnSaveEdited.func_146115_a()) {
            drawHoveringText(java.util.Collections.singletonList(I18n.format(Names.Gui.Editor.SAVE_EDITED_TOOLTIP)),
                par1, par2, this.fontRendererObj);
        } else if (this.btnCornerSelection.visible && this.btnCornerSelection.func_146115_a()) {
            drawHoveringText(java.util.Collections.singletonList(I18n.format(Names.Gui.Editor.CORNER_SELECTION_TOOLTIP)),
                par1, par2, this.fontRendererObj);
        } else if (this.btnSelectionSource.visible && this.btnSelectionSource.func_146115_a()) {
            String tooltip = I18n.format(Names.Gui.Editor.SELECTION_SOURCE_TOOLTIP)
                .replace("\\\\n", "\n")
                .replace("\\n", "\n");
            drawHoveringText(java.util.Arrays.asList(tooltip.split("\n")), par1, par2, this.fontRendererObj);
        } else if (this.btnImport.func_146115_a()) {
            drawHoveringText(java.util.Collections.singletonList(I18n.format(Names.Gui.Editor.IMPORT_TOOLTIP)),
                par1, par2, this.fontRendererObj);
        }
    }
}
