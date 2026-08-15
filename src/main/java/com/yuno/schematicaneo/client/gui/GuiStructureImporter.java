package com.yuno.schematicaneo.client.gui;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;


import com.github.lunatrius.core.client.gui.GuiScreenBase;
import com.yuno.schematicaneo.SchematicaNeo;
import com.yuno.schematicaneo.client.gui.load.GuiSchematicEntry;
import com.yuno.schematicaneo.converter.LitematicReader;
import com.yuno.schematicaneo.converter.MappingLoader;
import com.yuno.schematicaneo.converter.SchematicExporter;
import com.yuno.schematicaneo.converter.StructureData;
import com.yuno.schematicaneo.converter.VanillaStructureReader;
import com.yuno.schematicaneo.handler.ConfigurationHandler;
import com.yuno.schematicaneo.reference.Names;
import com.yuno.schematicaneo.reference.Reference;
import com.yuno.schematicaneo.world.schematic.SchematicFormat;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;

public class GuiStructureImporter extends GuiScreenBase {

    protected final List<GuiSchematicEntry> structureFiles = new ArrayList<>();
    protected File currentDirectory = ConfigurationHandler.schematicDirectory;
    private GuiStructureImportSlot structureSlot;
    private GuiButton btnOpenDir;
    private GuiButton btnConvert;
    private GuiButton btnLocalReference;
    private GuiButton btnBook;
    private GuiButton btnDone;
    private SchematicExporter.ConversionResult result;
    private File convertedFile;
    private String status;
    private boolean suppressResultActions;

    public GuiStructureImporter(GuiScreen parent) {
        super(parent);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int id = 0;
        this.btnOpenDir = new GuiButton(id++, this.width / 2 - 154, this.height - 36, 100, 20,
            I18n.format(Names.Gui.Load.OPEN_FOLDER));
        this.btnConvert = new GuiButton(id++, this.width / 2 - 50, this.height - 36, 100, 20,
            I18n.format(Names.Gui.Editor.IMPORT));
        this.btnLocalReference = new GuiButton(id++, this.width / 2 - 50, this.height - 36, 100, 20,
            I18n.format(Names.Gui.Editor.IMPORT_LOCAL_FILE));
        this.btnBook = new GuiButton(id++, this.width / 2 + 54, this.height - 36, 100, 20,
            I18n.format(Names.Gui.Editor.IMPORT_BOOK));
        this.btnDone = new GuiButton(id++, this.width / 2 + 54, this.height - 36, 100, 20,
            I18n.format(Names.Gui.DONE));
        this.buttonList.add(this.btnOpenDir);
        this.buttonList.add(this.btnConvert);
        this.buttonList.add(this.btnLocalReference);
        this.buttonList.add(this.btnBook);
        this.buttonList.add(this.btnDone);
        this.structureSlot = new GuiStructureImportSlot(this);
        reloadFiles();
        updateButtons();
    }

    private void updateButtons() {
        boolean converted = this.result != null;
        this.btnOpenDir.visible = !converted;
        this.btnConvert.visible = !converted;
        this.btnConvert.enabled = !converted && this.structureSlot != null && this.structureSlot.selectedIndex >= 0;
        this.btnLocalReference.visible = converted;
        this.btnLocalReference.enabled = converted && !this.result.unknownBlockStates.isEmpty();
        this.btnBook.visible = converted && this.mc.isSingleplayer();
        this.btnBook.enabled = this.btnBook.visible && !this.result.unknownBlockStates.isEmpty();
        this.btnDone.visible = true;
        if (converted) {
            this.btnLocalReference.xPosition = this.width / 2 - (this.btnBook.visible ? 154 : 50);
            this.btnBook.xPosition = this.width / 2 - 50;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;
        if (button == this.btnOpenDir) {
            openDirectory();
        } else if (button == this.btnConvert) {
            convertSelected();
        } else if (button == this.btnLocalReference) {
            if (this.suppressResultActions) return;
            saveReferenceFile();
        } else if (button == this.btnBook) {
            if (this.suppressResultActions) return;
            giveReferenceBook();
        } else if (button == this.btnDone) {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.structureSlot.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(this.fontRendererObj, I18n.format(Names.Gui.Editor.IMPORT_TITLE), this.width / 2, 4, 0xFFFFFF);
        int left = 10;
        int maxWidth = Math.max(24, this.width / 2 - 140);
        List<String> description = this.fontRendererObj.listFormattedStringToWidth(
            I18n.format(Names.Gui.Editor.IMPORT_DESCRIPTION), maxWidth);
        int y = 28;
        int maxLines = Math.max(1, (this.height - 105 - y) / 11);
        for (int i = 0; i < description.size() && i < maxLines; i++) {
            String line = description.get(i);
            if (i == maxLines - 1 && description.size() > maxLines) line += "...";
            drawString(this.fontRendererObj, line, left, y, 0xFFAA00);
            y += 11;
        }
        drawString(this.fontRendererObj, I18n.format(Names.Gui.Editor.IMPORT_FORMATS), left, y + 8, 0xFFAA00);
        if (this.result != null) {
            drawString(this.fontRendererObj, I18n.format(Names.Gui.Editor.IMPORT_COMPLETE), left, y + 30, 0x55FF55);
            drawString(this.fontRendererObj,
                I18n.format(Names.Gui.Editor.IMPORT_UNKNOWN_COUNT, uniqueUnknown().size()), left, y + 43, 0xFF7777);
            if (!this.mc.isSingleplayer()) {
                drawString(this.fontRendererObj, I18n.format(Names.Gui.Editor.IMPORT_BOOK_SINGLEPLAYER), left, y + 56, 0xAAAAAA);
            }
        }
        if (this.status != null) drawCenteredString(this.fontRendererObj, this.status, this.width / 2, this.height - 50, 0xFFFF55);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (this.btnLocalReference.visible && this.btnLocalReference.func_146115_a()) {
            drawHoveringText(java.util.Collections.singletonList(
                I18n.format(Names.Gui.Editor.IMPORT_REFERENCE_TOOLTIP)), mouseX, mouseY, this.fontRendererObj);
        } else if (this.btnBook.visible && this.btnBook.func_146115_a()) {
            drawHoveringText(java.util.Collections.singletonList(
                I18n.format(Names.Gui.Editor.IMPORT_REFERENCE_TOOLTIP)), mouseX, mouseY, this.fontRendererObj);
        }
        updateButtons();
    }

    protected void select(int index) {
        this.structureSlot.selectedIndex = index;
        updateButtons();
    }

    protected void changeDirectory(String name) {
        this.currentDirectory = new File(this.currentDirectory, name);
        reloadFiles();
        this.structureSlot.selectedIndex = -1;
    }

    private void reloadFiles() {
        this.structureFiles.clear();
        try {
            if (!this.currentDirectory.getCanonicalPath().equals(ConfigurationHandler.schematicDirectory.getCanonicalPath())) {
                this.structureFiles.add(new GuiSchematicEntry("..", Items.lava_bucket, 0, true));
            }
        } catch (Exception e) {
            Reference.logger.warn("Could not resolve structure import directory", e);
        }
        File[] files = this.currentDirectory.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                this.structureFiles.add(new GuiSchematicEntry(file.getName(), Items.water_bucket, 0, true));
            } else {
                String name = file.getName().toLowerCase();
                if (name.endsWith(".nbt") || name.endsWith(".litematic")) {
                    this.structureFiles.add(new GuiSchematicEntry(file.getName(), Items.paper, 0, false));
                }
            }
        }
    }

    private void convertSelected() {
        int index = this.structureSlot.selectedIndex;
        if (index < 0 || index >= this.structureFiles.size()) return;
        GuiSchematicEntry entry = this.structureFiles.get(index);
        if (entry.isDirectory()) return;
        File input = new File(this.currentDirectory, entry.getName());
        try {
            StructureData data = input.getName().toLowerCase().endsWith(".litematic")
                ? LitematicReader.read(input) : VanillaStructureReader.read(input);
            this.result = SchematicExporter.convert(data);
            if (this.result == null) throw new IllegalStateException("Conversion returned no result");
            this.suppressResultActions = true;
            String base = input.getName().substring(0, input.getName().lastIndexOf('.'));
            this.convertedFile = new File(ConfigurationHandler.schematicDirectory, base + ".schematic");
            SchematicFormat.writeToFile(this.convertedFile, this.result.schematic, this.mc.theWorld);
            SchematicaNeo.proxy.loadSchematic(this.mc.thePlayer, this.convertedFile.getParentFile(), this.convertedFile.getName());
            this.status = I18n.format(Names.Gui.Editor.IMPORT_COMPLETE_PATH, this.convertedFile.getAbsolutePath());
            updateButtons();
        } catch (Exception e) {
            Reference.logger.error("Structure conversion failed", e);
            this.status = I18n.format(Names.Gui.Editor.IMPORT_FAILED);
        }
    }

    private List<Map.Entry<String, Integer>> referenceEntries() {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        if (this.result != null) entries.addAll(this.result.unknownMetaMap.entrySet());
        entries.sort((left, right) -> Integer.compare(left.getValue(), right.getValue()));
        return entries;
    }

    private LinkedHashSet<String> uniqueUnknown() {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (Map.Entry<String, Integer> entry : referenceEntries()) values.add(entry.getKey());
        return values;
    }

    private String formatReference(Map.Entry<String, Integer> entry) {
        String state = simplify(entry.getKey());
        String translated = MappingLoader.getChineseName(entry.getKey());
        return (entry.getValue() + 1) + "." + (translated == null ? state : translated + "(" + state + ")");
    }

    private void saveReferenceFile() {
        if (this.convertedFile == null) return;
        File output = new File(this.convertedFile.getParentFile(), this.convertedFile.getName().replace(".schematic", "-reference.txt"));
        try (FileWriter writer = new FileWriter(output)) {
            for (Map.Entry<String, Integer> entry : referenceEntries()) {
                writer.write(formatReference(entry) + System.lineSeparator());
            }
            this.status = I18n.format(Names.Gui.Editor.IMPORT_FILE_SAVED_PATH, output.getAbsolutePath());
        } catch (Exception e) {
            Reference.logger.error("Could not save structure reference file", e);
            this.status = I18n.format(Names.Gui.Editor.IMPORT_FAILED);
        }
    }

    private void giveReferenceBook() {
        if (!this.mc.isSingleplayer() || this.result == null) return;
        ItemStack book = new ItemStack(Items.written_book);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("title", I18n.format(Names.Gui.Editor.IMPORT_BOOK_TITLE));
        tag.setString("author", "SchematicaNeo");
        NBTTagList pages = new NBTTagList();
        StringBuilder page = new StringBuilder();
        int lines = 0;
        for (Map.Entry<String, Integer> entry : referenceEntries()) {
            page.append(formatReference(entry));
            if (++lines == 9) {
                pages.appendTag(new NBTTagString(page.toString()));
                page.setLength(0);
                lines = 0;
            } else page.append('\n');
        }
        if (page.length() > 0) pages.appendTag(new NBTTagString(page.toString()));
        tag.setTag("pages", pages);
        book.setTagCompound(tag);
        MinecraftServer server = MinecraftServer.getServer();
        if (server != null) {
            for (Object value : server.getConfigurationManager().playerEntityList) {
                EntityPlayerMP player = (EntityPlayerMP) value;
                if (player.getCommandSenderName().equals(this.mc.thePlayer.getCommandSenderName())) {
                    player.inventory.addItemStackToInventory(book);
                    this.status = I18n.format(Names.Gui.Editor.IMPORT_BOOK_CREATED);
                    break;
                }
            }
        }
    }

    private void openDirectory() {
        try {
            Class<?> desktop = Class.forName("java.awt.Desktop");
            Object instance = desktop.getMethod("getDesktop").invoke(null);
            desktop.getMethod("open", File.class).invoke(instance, ConfigurationHandler.schematicDirectory);
        } catch (Throwable e) {
            try {
                Runtime.getRuntime().exec(new String[] { "explorer.exe", ConfigurationHandler.schematicDirectory.getAbsolutePath() });
            } catch (Exception exception) {
                Reference.logger.error("Could not open schematic directory", exception);
            }
        }
    }

    private String simplify(String state) {
        int bracket = state.indexOf('[');
        if (bracket >= 0) state = state.substring(0, bracket);
        return state.startsWith("minecraft:") ? state.substring(10) : state;
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        super.mouseMovedOrUp(mouseX, mouseY, state);
        if (state == 0) this.suppressResultActions = false;
    }
}
