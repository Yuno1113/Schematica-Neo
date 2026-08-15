package com.yuno.schematicaneo.client.gui.control;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import org.apache.commons.io.IOUtils;

import com.github.lunatrius.core.client.gui.GuiScreenBase;
import com.yuno.schematicaneo.SchematicaNeo;
import com.yuno.schematicaneo.client.util.BlockList;
import com.yuno.schematicaneo.client.world.SchematicWorld;
import com.yuno.schematicaneo.handler.ConfigurationHandler;
import com.yuno.schematicaneo.proxy.ClientProxy;
import com.yuno.schematicaneo.reference.Names;
import com.yuno.schematicaneo.reference.Reference;
import com.yuno.schematicaneo.util.ItemStackSortType;

import cpw.mods.fml.client.config.GuiUnicodeGlyphButton;

public class GuiSchematicMaterials extends GuiScreenBase {

    private GuiSchematicMaterialsSlot guiSchematicMaterialsSlot;

    private ItemStackSortType sortType = ItemStackSortType.fromString(ConfigurationHandler.sortType);

    private GuiUnicodeGlyphButton btnSort = null;
    private GuiButton btnDump = null;
    private GuiButton btnStacks = null;
    private GuiButton btnDone = null;

    private boolean showStacksInfo = false;

    public boolean isShowStacksInfo() {
        return showStacksInfo;
    }

    private final String strMaterialName = I18n.format(Names.Gui.Control.MATERIAL_NAME);
    private final String strMaterialAmount = I18n.format(Names.Gui.Control.MATERIAL_AMOUNT);

    protected final List<BlockList.WrappedItemStack> blockList;

    public GuiSchematicMaterials(GuiScreen guiScreen) {
        super(guiScreen);
        final Minecraft minecraft = Minecraft.getMinecraft();
        final SchematicWorld schematic = ClientProxy.schematic;
        this.blockList = new BlockList().getList(minecraft.thePlayer, schematic, minecraft.theWorld);
        this.sortType.sort(this.blockList);
    }

    @Override
    public void initGui() {
        int id = 0;

        this.btnSort = new GuiUnicodeGlyphButton(
            ++id,
            this.width / 2 - 164,
            this.height - 30,
            80,
            20,
            " " + I18n.format(Names.Gui.Control.SORT_PREFIX + this.sortType.label),
            this.sortType.glyph,
            2.0f);
        this.buttonList.add(this.btnSort);

        this.btnDump = new GuiButton(
            ++id,
            this.width / 2 - 82,
            this.height - 30,
            80,
            20,
            I18n.format(Names.Gui.Control.DUMP));
        this.buttonList.add(this.btnDump);

        this.btnStacks = new GuiButton(
            ++id,
            this.width / 2,
            this.height - 30,
            80,
            20,
            (this.showStacksInfo ? "§a✓ " : "§7") + "组:个");
        this.buttonList.add(this.btnStacks);

        this.btnDone = new GuiButton(++id, this.width / 2 + 82, this.height - 30, 80, 20, I18n.format(Names.Gui.DONE));
        this.buttonList.add(this.btnDone);

        this.guiSchematicMaterialsSlot = new GuiSchematicMaterialsSlot(this);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) {
        if (guiButton.enabled) {
            if (guiButton.id == this.btnSort.id) {
                this.sortType = this.sortType.next();
                this.sortType.sort(this.blockList);
                this.btnSort.displayString = " " + I18n.format(Names.Gui.Control.SORT_PREFIX + this.sortType.label);
                this.btnSort.glyph = this.sortType.glyph;

                ConfigurationHandler.propSortType.set(String.valueOf(this.sortType));
                ConfigurationHandler.loadConfiguration();
            } else if (guiButton.id == this.btnDump.id) {
                dumpMaterialList(this.blockList);
            } else if (guiButton.id == this.btnStacks.id) {
                this.showStacksInfo = !this.showStacksInfo;
                this.btnStacks.displayString = (this.showStacksInfo ? "§a✓ " : "§7") + "组:个";
            } else if (guiButton.id == this.btnDone.id) {
                this.mc.displayGuiScreen(this.parentScreen);
            } else {
                this.guiSchematicMaterialsSlot.actionPerformed(guiButton);
            }
        }
    }

    @Override
    public void renderToolTip(ItemStack stack, int x, int y) {
        super.renderToolTip(stack, x, y);
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        this.guiSchematicMaterialsSlot.drawScreen(x, y, partialTicks);

        drawString(this.fontRendererObj, this.strMaterialName, this.width / 2 - 108, 4, 0x00FFFFFF);
        drawString(
            this.fontRendererObj,
            this.strMaterialAmount,
            this.width / 2 + 108 - this.fontRendererObj.getStringWidth(this.strMaterialAmount),
            4,
            0x00FFFFFF);
        super.drawScreen(x, y, partialTicks);
    }

    private void dumpMaterialList(final List<BlockList.WrappedItemStack> blockList) {
        if (blockList.isEmpty()) {
            return;
        }

        final SchematicWorld schematic = ClientProxy.schematic;
        String schematicName = schematic != null && schematic.name != null && !schematic.name.isEmpty()
                ? schematic.name
                : "unknown";
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String fileName = schematicName + "-materials-" + dateStr + ".txt";

        StringBuilder sb = new StringBuilder();
        String header = String.format("%-40s %8s  %12s  %12s",
                "方块名称", "总数量", "组", "符文钢柜");
        sb.append(header).append(System.lineSeparator());
        sb.append(repeatChar('─', header.length())).append(System.lineSeparator());

        for (final BlockList.WrappedItemStack wrappedItemStack : blockList) {
            String name = wrappedItemStack.getItemStackDisplayName();
            int total = wrappedItemStack.total;
            int stacks = total / 64;
            int remainder = total % 64;
            String stacksStr = stacks + "组" + (remainder > 0 ? remainder + "个" : "0个");
            double cabinets = (double) stacks / 520.0;
            String cabinetsStr = String.format("%.2f", ceilTo2Dec(cabinets));

            sb.append(String.format("%-40s %8d  %12s  %12s",
                    name, total, stacksStr, cabinetsStr))
              .append(System.lineSeparator());
        }

        final File dumps = SchematicaNeo.proxy.getDirectory("dumps");
        final File outputFile = new File(dumps, fileName);
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            IOUtils.write(sb.toString(), outputStream);
            if (this.mc.thePlayer != null) {
                this.mc.thePlayer.addChatMessage(
                    new net.minecraft.util.ChatComponentText(
                        "§a材料列表已保存到: §f" + outputFile.getAbsolutePath()));
            }
        } catch (final Exception e) {
            Reference.logger.error("Could not dump the material list!", e);
            if (this.mc.thePlayer != null) {
                this.mc.thePlayer.addChatMessage(
                    new net.minecraft.util.ChatComponentText(
                        "§c材料列表保存失败: " + e.getMessage()));
            }
        }
    }

    private static double ceilTo2Dec(double value) {
        return Math.ceil(value * 100.0) / 100.0;
    }

    private static String repeatChar(char ch, int count) {
        char[] arr = new char[count];
        for (int i = 0; i < count; i++) arr[i] = ch;
        return new String(arr);
    }
}
