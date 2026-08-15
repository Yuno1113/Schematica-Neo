package com.yuno.schematicaneo.world.schematic;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import com.yuno.schematicaneo.api.ISchematic;
import com.yuno.schematicaneo.api.event.PostSchematicCaptureEvent;
import com.yuno.schematicaneo.reference.Names;
import com.yuno.schematicaneo.reference.Reference;

public abstract class SchematicFormat {

    public static final Map<String, SchematicFormat> FORMATS = new HashMap<>();
    public static String FORMAT_DEFAULT;

    public abstract ISchematic readFromNBT(NBTTagCompound tagCompound);

    public abstract boolean writeToNBT(NBTTagCompound tagCompound, ISchematic schematic, World backupWorld);

    public static ISchematic readFromFile(File file) {
        try {
            final NBTTagCompound tagCompound = SchematicUtil.readTagCompoundFromFile(file);
            final String format = tagCompound.getString(Names.NBT.MATERIALS);
            final SchematicFormat schematicFormat = FORMATS.get(format);

            if (schematicFormat == null) {
                throw new UnsupportedFormatException(format);
            }

            return schematicFormat.readFromNBT(tagCompound);
        } catch (Exception ex) {
            Reference.logger.error("Failed to read schematic!", ex);
        }

        return null;
    }

    public static ISchematic readFromFile(File directory, String filename) {
        return readFromFile(new File(directory, filename));
    }

    public static boolean writeToFile(File file, ISchematic schematic, World backupWorld) {
        try {
            final PostSchematicCaptureEvent event = new PostSchematicCaptureEvent(schematic);
            MinecraftForge.EVENT_BUS.post(event);

            NBTTagCompound tagCompound = new NBTTagCompound();

            FORMATS.get(FORMAT_DEFAULT)
                .writeToNBT(tagCompound, schematic, backupWorld);

            try (DataOutputStream dataOutputStream = new DataOutputStream(
                new GZIPOutputStream(new FileOutputStream(file)))) {
                CompressedStreamTools.write(tagCompound, dataOutputStream);
            }

            return true;
        } catch (Exception ex) {
            Reference.logger.error("Failed to write schematic!", ex);
        }

        return false;
    }

    public static boolean writeToFile(File directory, String filename, ISchematic schematic, World backupWorld) {
        return writeToFile(new File(directory, filename), schematic, backupWorld);
    }

    static {
        FORMATS.put(Names.NBT.FORMAT_ALPHA, new SchematicAlpha());

        FORMAT_DEFAULT = Names.NBT.FORMAT_ALPHA;
    }
}
