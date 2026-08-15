package com.yuno.schematicaneo.client.renderer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import org.lwjgl.opengl.GL11;

import com.github.lunatrius.core.util.vector.Vector3d;
import com.yuno.schematicaneo.client.world.SchematicWorld;
import com.yuno.schematicaneo.client.handler.SchematicEditorHandler;
import com.yuno.schematicaneo.proxy.ClientProxy;
import com.yuno.schematicaneo.reference.Constants;
import com.yuno.schematicaneo.reference.Reference;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class RendererSchematicGlobal {

    public static final RendererSchematicGlobal INSTANCE = new RendererSchematicGlobal();

    private final Minecraft minecraft = Minecraft.getMinecraft();
    private final Profiler profiler = this.minecraft.mcProfiler;

    private final Frustrum frustrum = new Frustrum();
    public RenderBlocks renderBlocks = null;
    public final List<RendererSchematicChunk> sortedRendererSchematicChunk = new ArrayList<>();
    private final RendererSchematicChunkComparator rendererSchematicChunkComparator = new RendererSchematicChunkComparator();

    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL_MS = 80;
    private static final double FAST_MOVE_THRESHOLD = 0.25;

    private float lastPlayerX, lastPlayerZ;
    private boolean hasLastPosition = false;

    private final List<RendererSchematicChunk> visibleRendererSchematicChunk = new ArrayList<>();

    private RendererSchematicGlobal() {}

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        EntityPlayerSP player = this.minecraft.thePlayer;
        if (player != null) {
            ClientProxy.setPlayerData(player, event.partialTicks);

            this.profiler.startSection("schematica");
            SchematicWorld schematic = ClientProxy.schematic;
            if ((schematic != null && schematic.isRendering)
                || ClientProxy.isRenderingGuide
                || SchematicEditorHandler.INSTANCE.isEnabled()) {
                render(schematic);
            }

            this.profiler.endSection();
        }
    }

    public void render(SchematicWorld schematic) {
        GL11.glPushMatrix();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);

        Vector3d playerPosition = ClientProxy.playerPosition.clone();
        Vector3d extra = new Vector3d();
        if (schematic != null) {
            extra.add(schematic.position.toVector3d());
            playerPosition.sub(extra);
        }

        GL11.glTranslated(-playerPosition.x, -playerPosition.y, -playerPosition.z);

        this.profiler.startSection("schematic");
        if (schematic != null && schematic.isRendering) {
            this.profiler.startSection("updateFrustrum");
            updateFrustrum(schematic);

            this.profiler.endStartSection("sortAndUpdate");
            if (RendererSchematicChunk.getCanUpdate()) {
                sortAndUpdate(schematic);
            }

            this.profiler.endStartSection("render");
            this.visibleRendererSchematicChunk.clear();
            for (RendererSchematicChunk renderer : this.sortedRendererSchematicChunk) {
                if (renderer.isInFrustrum) {
                    this.visibleRendererSchematicChunk.add(renderer);
                }
            }
            int pass;
            for (pass = 0; pass < 3; pass++) {
                for (RendererSchematicChunk renderer : this.visibleRendererSchematicChunk) {
                    renderer.render(pass);
                }
            }
            this.profiler.endSection();
        }

        renderClipboardPreview(schematic, extra);

        this.profiler.endStartSection("guide");

        RenderHelper.initBuffers();

        this.profiler.startSection("dataPrep");
        if (schematic != null && schematic.isRendering) {
            RenderHelper.drawCuboidOutline(
                RenderHelper.VEC_ZERO,
                schematic.dimensions(),
                RenderHelper.LINE_ALL,
                0.75f,
                0.0f,
                0.75f,
                0.25f);
        }

        if (ClientProxy.isRenderingGuide) {
            Vector3d start = new Vector3d();
            Vector3d end = new Vector3d();

            ClientProxy.pointMin.toVector3d(start)
                .sub(extra);
            ClientProxy.pointMax.toVector3d(end)
                .sub(extra)
                .add(1, 1, 1);
            RenderHelper.drawCuboidOutline(
                start.toVector3f(),
                end.toVector3f(),
                RenderHelper.LINE_ALL,
                0.0f,
                0.75f,
                0.0f,
                0.25f);

            ClientProxy.pointA.toVector3d(start)
                .sub(extra);
            end.set(start)
                .add(1, 1, 1);
            RenderHelper.drawCuboidOutline(
                start.toVector3f(),
                end.toVector3f(),
                RenderHelper.LINE_ALL,
                0.75f,
                0.0f,
                0.0f,
                0.25f);
            RenderHelper.drawCuboidSurface(
                start.toVector3f(),
                end.toVector3f(),
                RenderHelper.QUAD_ALL,
                0.75f,
                0.0f,
                0.0f,
                0.25f);

            ClientProxy.pointB.toVector3d(start)
                .sub(extra);
            end.set(start)
                .add(1, 1, 1);
            RenderHelper.drawCuboidOutline(
                start.toVector3f(),
                end.toVector3f(),
                RenderHelper.LINE_ALL,
                0.0f,
                0.0f,
                0.75f,
                0.25f);
            RenderHelper.drawCuboidSurface(
                start.toVector3f(),
                end.toVector3f(),
                RenderHelper.QUAD_ALL,
                0.0f,
                0.0f,
                0.75f,
                0.25f);
        }

        final SchematicEditorHandler editor = SchematicEditorHandler.INSTANCE;

        if (editor.isEnabled() && editor.getMode() == SchematicEditorHandler.Mode.PASTE
            && editor.hasClipboard() && editor.isPastePreview()) {
            Vector3d start = new Vector3d(editor.getPasteX(), editor.getPasteY(), editor.getPasteZ());
            Vector3d end = start.clone().add(
                editor.getClipboardWidth(),
                editor.getClipboardHeight(),
                editor.getClipboardLength());
            if (schematic != null) {
                start.sub(extra);
                end.sub(extra);
            }
            RenderHelper.drawCuboidOutline(
                start.toVector3f(),
                end.toVector3f(),
                RenderHelper.LINE_ALL,
                0.75f,
                0.75f,
                0.0f,
                0.45f);
        }

        int quadCount = RenderHelper.getQuadCount();
        int lineCount = RenderHelper.getLineCount();

        if (quadCount > 0 || lineCount > 0) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);

            GL11.glLineWidth(1.5f);

            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

            this.profiler.endStartSection("quad");
            if (quadCount > 0) {
                GL11.glVertexPointer(3, 0, RenderHelper.getQuadVertexBuffer());
                GL11.glColorPointer(4, 0, RenderHelper.getQuadColorBuffer());
                GL11.glDrawArrays(GL11.GL_QUADS, 0, quadCount);
            }

            this.profiler.endStartSection("line");
            if (lineCount > 0) {
                GL11.glVertexPointer(3, 0, RenderHelper.getLineVertexBuffer());
                GL11.glColorPointer(4, 0, RenderHelper.getLineColorBuffer());
                GL11.glDrawArrays(GL11.GL_LINES, 0, lineCount);
            }

            this.profiler.endSection();

            GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        if (editor.isEnabled() && editor.hasSelection()) {
            drawEditorSelection(editor, schematic);
        }

        this.profiler.endSection();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPopMatrix();
    }

    private void drawEditorSelection(SchematicEditorHandler editor, SchematicWorld schematic) {
        final double offsetX = schematic == null ? 0 : schematic.position.x;
        final double offsetY = schematic == null ? 0 : schematic.position.y;
        final double offsetZ = schematic == null ? 0 : schematic.position.z;
        final double minX = editor.getMinX() - offsetX;
        final double minY = editor.getMinY() - offsetY;
        final double minZ = editor.getMinZ() - offsetZ;
        final double maxX = editor.getMaxX() + 1 - offsetX;
        final double maxY = editor.getMaxY() + 1 - offsetY;
        final double maxZ = editor.getMaxZ() + 1 - offsetZ;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(3.0f);
        GL11.glColor4f(0.0f, 0.9f, 0.9f, 0.8f);
        GL11.glBegin(GL11.GL_LINES);
        drawSelectionEdge(minX, minY, minZ, maxX, minY, minZ);
        drawSelectionEdge(minX, minY, maxZ, maxX, minY, maxZ);
        drawSelectionEdge(minX, maxY, minZ, maxX, maxY, minZ);
        drawSelectionEdge(minX, maxY, maxZ, maxX, maxY, maxZ);
        drawSelectionEdge(minX, minY, minZ, minX, maxY, minZ);
        drawSelectionEdge(maxX, minY, minZ, maxX, maxY, minZ);
        drawSelectionEdge(minX, minY, maxZ, minX, maxY, maxZ);
        drawSelectionEdge(maxX, minY, maxZ, maxX, maxY, maxZ);
        drawSelectionEdge(minX, minY, minZ, minX, minY, maxZ);
        drawSelectionEdge(maxX, minY, minZ, maxX, minY, maxZ);
        drawSelectionEdge(minX, maxY, minZ, minX, maxY, maxZ);
        drawSelectionEdge(maxX, maxY, minZ, maxX, maxY, maxZ);
        GL11.glEnd();
        GL11.glLineWidth(1.5f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void drawSelectionEdge(double x1, double y1, double z1, double x2, double y2, double z2) {
        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x2, y2, z2);
    }

    private void renderClipboardPreview(SchematicWorld schematic, Vector3d schematicOrigin) {
        final SchematicEditorHandler editor = SchematicEditorHandler.INSTANCE;
        final SchematicWorld preview = editor.getClipboardWorld();
        if (!editor.isEnabled() || editor.getMode() != SchematicEditorHandler.Mode.PASTE
            || !editor.isPastePreview() || preview == null) return;

        final double offsetX = editor.getPasteX() - (schematic == null ? 0 : schematic.position.x);
        final double offsetY = editor.getPasteY() - (schematic == null ? 0 : schematic.position.y);
        final double offsetZ = editor.getPasteZ() - (schematic == null ? 0 : schematic.position.z);
        final RenderBlocks previewRenderer = new RenderBlocks(preview);
        final int ambientOcclusion = this.minecraft.gameSettings.ambientOcclusion;
        this.minecraft.gameSettings.ambientOcclusion = 0;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        GL11.glTranslated(offsetX, offsetY, offsetZ);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        for (int pass = 0; pass < 2; pass++) {
            Tessellator.instance.startDrawingQuads();
            for (int y = 0; y < preview.getHeight(); y++) {
                for (int z = 0; z < preview.getLength(); z++) {
                    for (int x = 0; x < preview.getWidth(); x++) {
                        Block block = preview.getBlock(x, y, z);
                        if (block != null && block != Blocks.air && block.canRenderInPass(pass)) {
                            try {
                                previewRenderer.renderBlockByRenderType(block, x, y, z);
                            } catch (Exception e) {
                                Reference.logger.debug("Skipping incompatible clipboard preview block: {}", block, e);
                            }
                        }
                    }
                }
            }
            Tessellator.instance.draw();
        }
        GL11.glPopMatrix();
        GL11.glPopAttrib();
        this.minecraft.gameSettings.ambientOcclusion = ambientOcclusion;
    }

    private void updateFrustrum(SchematicWorld schematic) {
        this.frustrum.setPosition(
            ClientProxy.playerPosition.x - schematic.position.x,
            ClientProxy.playerPosition.y - schematic.position.y,
            ClientProxy.playerPosition.z - schematic.position.z);
        for (RendererSchematicChunk rendererSchematicChunk : this.sortedRendererSchematicChunk) {
            rendererSchematicChunk.isInFrustrum = this.frustrum
                .isBoundingBoxInFrustum(rendererSchematicChunk.getBoundingBox());
        }
    }

    private void sortAndUpdate(SchematicWorld schematic) {
        boolean hasDirty = false;
        for (RendererSchematicChunk rendererSchematicChunk : this.sortedRendererSchematicChunk) {
            if (rendererSchematicChunk.getDirty()) {
                hasDirty = true;
                break;
            }
        }

        if (!hasDirty) {
            return;
        }

        float px = (float) ClientProxy.playerPosition.x;
        float pz = (float) ClientProxy.playerPosition.z;
        float speed = 0.0f;
        if (this.hasLastPosition) {
            float dx = px - this.lastPlayerX;
            float dz = pz - this.lastPlayerZ;
            speed = dx * dx + dz * dz;
        }
        this.lastPlayerX = px;
        this.lastPlayerZ = pz;
        this.hasLastPosition = true;

        long now = System.currentTimeMillis();
        long interval = speed > FAST_MOVE_THRESHOLD ? UPDATE_INTERVAL_MS * 4 : UPDATE_INTERVAL_MS;
        if (now - this.lastUpdateTime < interval) {
            return;
        }

        this.lastUpdateTime = now;

        this.rendererSchematicChunkComparator.setPosition(schematic.position);
        this.sortedRendererSchematicChunk.sort(this.rendererSchematicChunkComparator);

        for (RendererSchematicChunk rendererSchematicChunk : this.sortedRendererSchematicChunk) {
            if (rendererSchematicChunk.getDirty()) {
                rendererSchematicChunk.updateRenderer();
                break;
            }
        }
    }

    public void createRendererSchematicChunks(SchematicWorld schematic) {
        int width = (schematic.getWidth() - 1) / Constants.SchematicChunk.WIDTH + 1;
        int height = (schematic.getHeight() - 1) / Constants.SchematicChunk.HEIGHT + 1;
        int length = (schematic.getLength() - 1) / Constants.SchematicChunk.LENGTH + 1;

        destroyRendererSchematicChunks();

        this.renderBlocks = new RenderBlocks(schematic);
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    this.sortedRendererSchematicChunk.add(new RendererSchematicChunk(schematic, x, y, z));
                }
            }
        }
    }

    public void destroyRendererSchematicChunks() {
        this.renderBlocks = null;
        while (!this.sortedRendererSchematicChunk.isEmpty()) {
            this.sortedRendererSchematicChunk.remove(0)
                .delete();
        }
    }

    public void refresh() {
        for (RendererSchematicChunk renderer : this.sortedRendererSchematicChunk) {
            renderer.setDirty();
        }
    }
}
