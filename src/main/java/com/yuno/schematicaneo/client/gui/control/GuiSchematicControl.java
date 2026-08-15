package com.yuno.schematicaneo.client.gui.control;

import static com.yuno.schematicaneo.client.util.WorldServerName.worldServerName;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;

import com.github.lunatrius.core.client.gui.GuiNumericField;
import com.github.lunatrius.core.client.gui.GuiScreenBase;
import com.github.lunatrius.core.util.vector.Vector3i;
import com.yuno.schematicaneo.handler.AutoPlaceHandler;
import com.yuno.schematicaneo.SchematicaNeo;
import com.yuno.schematicaneo.client.gui.util.GuiOrCheckBoxHandler;
import com.yuno.schematicaneo.client.gui.GuiSchematicVisibleBlock;
import com.yuno.schematicaneo.client.printer.SchematicPrinter;
import com.yuno.schematicaneo.client.renderer.RendererSchematicGlobal;
import com.yuno.schematicaneo.client.world.SchematicWorld;
import com.yuno.schematicaneo.handler.ConfigurationHandler;
import com.yuno.schematicaneo.proxy.ClientProxy;
import com.yuno.schematicaneo.reference.Constants;
import com.yuno.schematicaneo.reference.Names;

import cpw.mods.fml.client.config.GuiCheckBox;

public class GuiSchematicControl extends GuiScreenBase {

    private final SchematicWorld schematic;
    private final SchematicPrinter printer;

    private int centerX = 0;
    private int centerY = 0;

    private GuiNumericField numericX = null;
    private GuiNumericField numericY = null;
    private GuiNumericField numericZ = null;

    private GuiButton btnUnload = null;
    private GuiButton btnLayerMode = null;
    private GuiButton btnBlockVisibility = null;
    private GuiNumericField nfLayer = null;

    private GuiButton btnHide = null;
    private GuiButton btnMove = null;
    private GuiButton btnFlip = null;
    private GuiCheckBox btnFlipX = null;
    private GuiCheckBox btnFlipY = null;
    private GuiCheckBox btnFlipZ = null;
    private static int lastCheckedFlip = 0;
    private GuiOrCheckBoxHandler flipBoxes;
    private GuiButton btnRotate = null;
    private GuiCheckBox btnRotateX = null;
    private GuiCheckBox btnRotateY = null;
    private GuiCheckBox btnRotateZ = null;
    private static int lastCheckedRotation = 0;
    private GuiOrCheckBoxHandler rotationBoxes;
    private GuiButton btnMaterials = null;
    private GuiButton btnPrint = null;
    private GuiButton btnPrintMode = null;
    private GuiButton btnSpeed = null;

    private static final int[] SPEED_DELAYS = { 1, 0, 0, 0, 0, 9, 5, 3 };
    private static final int[] SPEED_PLACEMENTS = { 1, 1, 2, 4, 8, 1, 1, 1 };
    private static final String[] SPEED_KEYS = { "fast", "faster", "high", "veryHigh", "maximum", "minimum", "slow", "medium" };
    private static final int[] MULTIPLAYER_SPEED_ORDER = { 0, 1, 2, 5, 6, 7 };
    private int speedIndex = 0;

    private GuiButton btnSaveCoordinates = null;

    private final String strSaveCoordinatesSuccess = I18n.format(Names.Chat.SAVE_COORDINATES_SUCCESS);
    private final String strSaveCoordinatesFail = I18n.format(Names.Chat.SAVE_COORDINATES_FAIL);
    private final String strSaveCoordinates = I18n.format(Names.Gui.Control.SAVE_COORDINATES);
    private final String strMoveSchematic = I18n.format(Names.Gui.Control.MOVE_SCHEMATIC);
    private final String strOperations = I18n.format(Names.Gui.Control.OPERATIONS);
    private final String strName = I18n.format(Names.Gui.Control.NAME);
    private final String strUnload = I18n.format(Names.Gui.Control.UNLOAD);
    private final String strAll = I18n.format(Names.Gui.Control.MODE_ALL);
    private final String strLayers = I18n.format(Names.Gui.Control.MODE_LAYERS);
    private final String strMaterials = I18n.format(Names.Gui.Control.MATERIALS);
    private final String strPrinter = I18n.format(Names.Gui.Control.PRINTER);
    private final String strHide = I18n.format(Names.Gui.Control.HIDE);
    private final String strShow = I18n.format(Names.Gui.Control.SHOW);
    private final String strX = I18n.format(Names.Gui.X);
    private final String strY = I18n.format(Names.Gui.Y);
    private final String strZ = I18n.format(Names.Gui.Z);
    private final String strOn = I18n.format(Names.Gui.ON);
    private final String strOff = I18n.format(Names.Gui.OFF);

    public GuiSchematicControl(GuiScreen guiScreen) {
        super(guiScreen);
        this.schematic = ClientProxy.schematic;
        this.printer = SchematicPrinter.INSTANCE;
    }

    @Override
    public void initGui() {
        this.centerX = this.width / 2;
        this.centerY = this.height / 2;

        this.buttonList.clear();

        int id = 0;

        this.numericX = new GuiNumericField(this.fontRendererObj, id++, this.centerX - 50, this.centerY - 30, 100, 20);
        this.buttonList.add(this.numericX);

        this.numericY = new GuiNumericField(this.fontRendererObj, id++, this.centerX - 50, this.centerY - 5, 100, 20);
        this.buttonList.add(this.numericY);

        this.numericZ = new GuiNumericField(this.fontRendererObj, id++, this.centerX - 50, this.centerY + 20, 100, 20);
        this.buttonList.add(this.numericZ);

        this.btnUnload = new GuiButton(id++, this.width - 90, this.height - 200, 80, 20, this.strUnload);
        this.buttonList.add(this.btnUnload);

        this.btnLayerMode = new GuiButton(
            id++,
            this.width - 90,
            this.height - 150 - 25,
            80,
            20,
            getVisibilityModeLabel());
        this.buttonList.add(this.btnLayerMode);

        this.btnBlockVisibility = new GuiButton(id++, this.width - 175, this.height - 150, 80, 20,
            I18n.format(Names.Gui.Control.VISIBILITY_BLOCK_CONFIG));
        this.btnBlockVisibility.visible = this.schematic != null
            && this.schematic.visibilityMode == SchematicWorld.VISIBILITY_BLOCK;
        this.buttonList.add(this.btnBlockVisibility);

        this.nfLayer = new GuiNumericField(this.fontRendererObj, id++, this.width - 90, this.height - 150, 80, 20);
        this.buttonList.add(this.nfLayer);

        this.btnHide = new GuiButton(
            id++,
            this.width - 90,
            this.height - 105,
            80,
            20,
            this.schematic != null && this.schematic.isRendering ? this.strHide : this.strShow);
        this.buttonList.add(this.btnHide);

        this.btnMove = new GuiButton(
            id++,
            this.width - 90,
            this.height - 80,
            80,
            20,
            I18n.format(Names.Gui.Control.MOVE_HERE));
        this.buttonList.add(this.btnMove);

        this.btnFlip = new GuiButton(
            id++,
            this.width - 90,
            this.height - 55,
            80,
            20,
            I18n.format(Names.Gui.Control.FLIP));
        this.buttonList.add(this.btnFlip);

        this.btnFlipX = new GuiCheckBox(id++, this.width - 90 - 60, this.height - 50, "X", false);
        this.buttonList.add(this.btnFlipX);
        this.btnFlipY = new GuiCheckBox(id++, this.width - 90 - 40, this.height - 50, "Y", false);
        this.buttonList.add(this.btnFlipY);
        this.btnFlipZ = new GuiCheckBox(id++, this.width - 90 - 20, this.height - 50, "Z", false);
        this.buttonList.add(this.btnFlipZ);
        flipBoxes = new GuiOrCheckBoxHandler(btnFlipX, btnFlipY, btnFlipZ);

        this.btnRotate = new GuiButton(
            id++,
            this.width - 90,
            this.height - 30,
            80,
            20,
            I18n.format(Names.Gui.Control.ROTATE));
        this.buttonList.add(this.btnRotate);

        this.btnRotateX = new GuiCheckBox(id++, this.width - 90 - 60, this.height - 25, "X", false);
        this.buttonList.add(this.btnRotateX);
        this.btnRotateY = new GuiCheckBox(id++, this.width - 90 - 40, this.height - 25, "Y", false);
        this.buttonList.add(this.btnRotateY);
        this.btnRotateZ = new GuiCheckBox(id++, this.width - 90 - 20, this.height - 25, "Z", false);
        this.buttonList.add(this.btnRotateZ);
        rotationBoxes = new GuiOrCheckBoxHandler(btnRotateX, btnRotateY, btnRotateZ);

        this.btnMaterials = new GuiButton(id++, 10, this.height - 70, 80, 20, this.strMaterials);
        this.buttonList.add(this.btnMaterials);

        this.btnPrintMode = new GuiButton(
            id++,
            10,
            this.height - 50,
            80,
            20,
            this.printer.isSortByDistance() ? "距离优先" : "按层放置");
        this.buttonList.add(this.btnPrintMode);

        this.btnPrint = new GuiButton(
            id++,
            10,
            this.height - 30,
            80,
            20,
            this.strPrinter + ": " + (this.printer.isPrinting() ? this.strOn : this.strOff));
        this.buttonList.add(this.btnPrint);

        this.speedIndex = 0;
        for (int i = 0; i < SPEED_DELAYS.length; i++) {
            if (SPEED_DELAYS[i] == ConfigurationHandler.placeDelay
                && SPEED_PLACEMENTS[i] == ConfigurationHandler.placementsPerCycle
                && isSpeedAllowed(i)) {
                this.speedIndex = i;
                break;
            }
        }
        ConfigurationHandler.placeDelay = SPEED_DELAYS[this.speedIndex];
        ConfigurationHandler.placementsPerCycle = SPEED_PLACEMENTS[this.speedIndex];
        this.btnSpeed = new GuiButton(
            id++,
            90,
            this.height - 30,
            60,
            20,
            getSpeedName(this.speedIndex));
        this.buttonList.add(this.btnSpeed);

        this.btnSaveCoordinates = new GuiButton(
            id++,
            this.centerX - 50,
            this.centerY + 45,
            100,
            20,
            this.strSaveCoordinates);
        this.buttonList.add(this.btnSaveCoordinates);

        this.numericX.setEnabled(this.schematic != null);
        this.numericY.setEnabled(this.schematic != null);
        this.numericZ.setEnabled(this.schematic != null);

        this.btnUnload.enabled = this.schematic != null;
        this.btnLayerMode.enabled = this.schematic != null;
        this.btnBlockVisibility.enabled = this.schematic != null;
        this.nfLayer.setEnabled(this.schematic != null && this.schematic.isRenderingLayer);

        this.btnHide.enabled = this.schematic != null;
        this.btnMove.enabled = this.schematic != null;

        this.btnFlip.enabled = this.schematic != null;
        this.btnFlipX.enabled = this.schematic != null;
        this.btnFlipY.enabled = this.schematic != null;
        this.btnFlipZ.enabled = this.schematic != null;
        this.btnRotate.enabled = this.schematic != null;
        this.btnRotateX.enabled = this.schematic != null;
        this.btnRotateY.enabled = this.schematic != null;
        this.btnRotateZ.enabled = this.schematic != null;
        this.btnMaterials.enabled = this.schematic != null;
        this.btnPrint.enabled = this.schematic != null && this.printer.isEnabled();
        this.btnPrintMode.enabled = this.schematic != null && this.printer.isEnabled();
        this.btnSpeed.enabled = this.schematic != null && this.printer.isEnabled();

        this.btnSaveCoordinates.enabled = this.schematic != null;

        setMinMax(this.numericX);
        setMinMax(this.numericY);
        setMinMax(this.numericZ);

        if (this.schematic != null) {
            setPoint(this.numericX, this.numericY, this.numericZ, this.schematic.position);
            rotationBoxes.checkBox(this.btnRotateX.id + lastCheckedRotation);
            flipBoxes.checkBox(this.btnFlipX.id + lastCheckedFlip);
        }

        this.nfLayer.setMinimum(0);
        this.nfLayer.setMaximum(this.schematic != null ? this.schematic.getHeight() - 1 : 0);
        if (this.schematic != null) {
            this.nfLayer.setValue(this.schematic.renderingLayer);
        }

        AutoPlaceHandler.addPlaceButton(this);
    }

    private void setMinMax(GuiNumericField numericField) {
        numericField.setMinimum(Constants.World.MINIMUM_COORD);
        numericField.setMaximum(Constants.World.MAXIMUM_COORD);
    }

    private void setPoint(GuiNumericField numX, GuiNumericField numY, GuiNumericField numZ, Vector3i point) {
        numX.setValue(point.x);
        numY.setValue(point.y);
        numZ.setValue(point.z);
    }

    private String getVisibilityModeLabel() {
        if (this.schematic == null) return this.strAll;
        switch (this.schematic.visibilityMode) {
            case SchematicWorld.VISIBILITY_LAYER: return this.strLayers;
            case SchematicWorld.VISIBILITY_SELECTION:
                return I18n.format(Names.Gui.Control.VISIBILITY_SELECTION);
            case SchematicWorld.VISIBILITY_BLOCK:
                return I18n.format(Names.Gui.Control.VISIBILITY_BLOCK);
            default: return this.strAll;
        }
    }

    @Override
    protected void actionPerformed(GuiButton guiButton) {
        if (AutoPlaceHandler.handleAction(guiButton, this)) {
            return;
        }
        if (guiButton.enabled) {
            if (this.schematic == null) {
                return;
            }

            if (guiButton.id == this.numericX.id) {
                this.schematic.position.x = this.numericX.getValue();
                RendererSchematicGlobal.INSTANCE.refresh();
            } else if (guiButton.id == this.numericY.id) {
                this.schematic.position.y = this.numericY.getValue();
                RendererSchematicGlobal.INSTANCE.refresh();
            } else if (guiButton.id == this.numericZ.id) {
                this.schematic.position.z = this.numericZ.getValue();
                RendererSchematicGlobal.INSTANCE.refresh();
            } else if (guiButton.id == this.btnUnload.id) {
                SchematicaNeo.proxy.unloadSchematic();
                this.mc.displayGuiScreen(this.parentScreen);
            } else if (guiButton.id == this.btnLayerMode.id) {
                this.schematic.visibilityMode = (this.schematic.visibilityMode + 1) % 4;
                this.schematic.isRenderingLayer = this.schematic.visibilityMode == SchematicWorld.VISIBILITY_LAYER;
                this.btnLayerMode.displayString = getVisibilityModeLabel();
                this.nfLayer.setEnabled(this.schematic.isRenderingLayer);
                this.btnBlockVisibility.visible = this.schematic.visibilityMode == SchematicWorld.VISIBILITY_BLOCK;
                RendererSchematicGlobal.INSTANCE.refresh();
            } else if (guiButton.id == this.btnBlockVisibility.id) {
                this.mc.displayGuiScreen(new GuiSchematicVisibleBlock(this));
            } else if (guiButton.id == this.nfLayer.id) {
                this.schematic.renderingLayer = this.nfLayer.getValue();
                RendererSchematicGlobal.INSTANCE.refresh();
            } else if (guiButton.id == this.btnHide.id) {
                boolean rendering = this.schematic.toggleRendering();
                this.btnHide.displayString = rendering ? this.strHide : this.strShow;
                if (rendering) {
                    RendererSchematicGlobal.INSTANCE.createRendererSchematicChunks(this.schematic);
                } else {
                    RendererSchematicGlobal.INSTANCE.destroyRendererSchematicChunks();
                }
            } else if (guiButton.id == this.btnMove.id) {
                ClientProxy.moveSchematicToPlayer(this.schematic);
                RendererSchematicGlobal.INSTANCE.refresh();
                setPoint(this.numericX, this.numericY, this.numericZ, this.schematic.position);
            } else if (guiButton.id == this.btnFlip.id) {
                int checkedBoxId = flipBoxes.getCheckedBoxId();
                if (checkedBoxId == btnFlipX.id) {
                    this.schematic.flip(ForgeDirection.EAST);
                } else if (checkedBoxId == btnFlipY.id) {
                    this.schematic.flip(ForgeDirection.UP);
                } else if (checkedBoxId == btnFlipZ.id) {
                    this.schematic.flip(ForgeDirection.SOUTH);
                } else {
                    throw new RuntimeException("Somehow no check box selected!");
                }
                RendererSchematicGlobal.INSTANCE.createRendererSchematicChunks(this.schematic);
                SchematicPrinter.INSTANCE.refresh();
            } else if (guiButton.id == this.btnFlipX.id) {
                flipBoxes.checkBox(btnFlipX);
                lastCheckedFlip = 0;
            } else if (guiButton.id == this.btnFlipY.id) {
                flipBoxes.checkBox(btnFlipY);
                lastCheckedFlip = 1;
            } else if (guiButton.id == this.btnFlipZ.id) {
                flipBoxes.checkBox(btnFlipZ);
                lastCheckedFlip = 2;
            } else if (guiButton.id == this.btnRotate.id) {
                int checkedBoxId = rotationBoxes.getCheckedBoxId();
                if (checkedBoxId == btnRotateX.id) {
                    this.schematic.rotate(ForgeDirection.EAST);
                } else if (checkedBoxId == btnRotateY.id) {
                    this.schematic.rotate(ForgeDirection.UP);
                } else if (checkedBoxId == btnRotateZ.id) {
                    this.schematic.rotate(ForgeDirection.SOUTH);
                } else {
                    throw new RuntimeException("Somehow no check box selected!");
                }
                RendererSchematicGlobal.INSTANCE.createRendererSchematicChunks(this.schematic);
                SchematicPrinter.INSTANCE.refresh();
            } else if (guiButton.id == this.btnRotateX.id) {
                rotationBoxes.checkBox(btnRotateX);
                lastCheckedRotation = 0;
            } else if (guiButton.id == this.btnRotateY.id) {
                rotationBoxes.checkBox(btnRotateY);
                lastCheckedRotation = 1;
            } else if (guiButton.id == this.btnRotateZ.id) {
                rotationBoxes.checkBox(btnRotateZ);
                lastCheckedRotation = 2;
            } else if (guiButton.id == this.btnMaterials.id) {
                this.mc.displayGuiScreen(new GuiSchematicMaterials(this));
            } else if (guiButton.id == this.btnPrint.id && this.printer.isEnabled()) {
                boolean isPrinting = this.printer.togglePrinting();
                this.btnPrint.displayString = this.strPrinter + ": " + (isPrinting ? this.strOn : this.strOff);
            } else if (guiButton.id == this.btnPrintMode.id && this.printer.isEnabled()) {
                this.printer.toggleSortByDistance();
                this.btnPrintMode.displayString = this.printer.isSortByDistance() ? "距离优先" : "按层放置";
            } else if (guiButton.id == this.btnSpeed.id) {
                this.speedIndex = getNextSpeedIndex();
                ConfigurationHandler.placeDelay = SPEED_DELAYS[this.speedIndex];
                ConfigurationHandler.placementsPerCycle = SPEED_PLACEMENTS[this.speedIndex];
                this.btnSpeed.displayString = getSpeedName(this.speedIndex);
            } else if (guiButton.id == this.btnSaveCoordinates.id) {
                String worldServerName = worldServerName(this.mc);
                EntityPlayerSP player = mc.thePlayer;
                if (player != null) {
                    if (ClientProxy.addCoordinatesAndRotation(
                        worldServerName,
                        this.schematic.name,
                        this.numericX.getValue(),
                        this.numericY.getValue(),
                        this.numericZ.getValue(),
                        this.schematic.rotationStateX,
                        this.schematic.rotationStateY,
                        this.schematic.rotationStateZ,
                        this.schematic.flipStateX,
                        this.schematic.flipStateY,
                        this.schematic.flipStateZ)) {
                        mc.thePlayer.addChatMessage(new ChatComponentText(strSaveCoordinatesSuccess));
                    } else {
                        mc.thePlayer.addChatMessage(new ChatComponentText(strSaveCoordinatesFail));
                    }
                }
            }
        }
    }

    @Override
    public void drawScreen(int par1, int par2, float par3) {
        drawCenteredString(this.fontRendererObj, this.strMoveSchematic, this.centerX, this.centerY - 45, 0xFFFFFF);
        drawCenteredString(this.fontRendererObj, this.strMaterials, 50, this.height - 85, 0xFFFFFF);
        drawCenteredString(this.fontRendererObj, this.strLayers, this.width - 50, this.height - 165, 0xFFFFFF);
        drawCenteredString(this.fontRendererObj, this.strOperations, this.width - 50, this.height - 120, 0xFFFFFF);

        if (this.schematic != null) {
            drawString(
                this.fontRendererObj,
                this.strName + ": " + this.schematic.name,
                10,
                this.height - 195,
                0xFFFFFF);
        }
        drawString(this.fontRendererObj, this.strX, this.centerX - 65, this.centerY - 24, 0xFFFFFF);
        drawString(this.fontRendererObj, this.strY, this.centerX - 65, this.centerY + 1, 0xFFFFFF);
        drawString(this.fontRendererObj, this.strZ, this.centerX - 65, this.centerY + 26, 0xFFFFFF);

        super.drawScreen(par1, par2, par3);
        if (this.btnSpeed.func_146115_a()) {
            String tooltip = I18n.format(Names.Gui.Control.PRINT_SPEED_TOOLTIP)
                .replace("\\\\n", "\n")
                .replace("\\n", "\n");
            drawHoveringText(java.util.Arrays.asList(tooltip.split("\n")), par1, par2, this.fontRendererObj);
        }
    }

    private String getSpeedName(int index) {
        return I18n.format(Names.Gui.Control.PRINT_SPEED_PREFIX + SPEED_KEYS[index]);
    }

    private boolean isSpeedAllowed(int index) {
        if (this.mc.isSingleplayer()) return true;
        for (int allowed : MULTIPLAYER_SPEED_ORDER) if (allowed == index) return true;
        return false;
    }

    private int getNextSpeedIndex() {
        if (this.mc.isSingleplayer()) return (this.speedIndex + 1) % SPEED_DELAYS.length;
        for (int i = 0; i < MULTIPLAYER_SPEED_ORDER.length; i++) {
            if (MULTIPLAYER_SPEED_ORDER[i] == this.speedIndex) {
                return MULTIPLAYER_SPEED_ORDER[(i + 1) % MULTIPLAYER_SPEED_ORDER.length];
            }
        }
        return MULTIPLAYER_SPEED_ORDER[0];
    }
}
