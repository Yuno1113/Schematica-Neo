package com.yuno.schematicaneo;


import com.yuno.schematicaneo.converter.MappingLoader;
import com.yuno.schematicaneo.handler.AutoPlaceHandler;
import com.yuno.schematicaneo.marker.BlockMarker;
import com.yuno.schematicaneo.marker.ItemBlockMarker;
import com.yuno.schematicaneo.proxy.CommonProxy;
import com.yuno.schematicaneo.reference.Reference;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.init.Items;

@Mod(
    modid = Reference.MODID,
    name = Reference.NAME,
    version = Reference.VERSION,
    dependencies = Reference.DEPENDENCIES,
    guiFactory = Reference.GUI_FACTORY)
public class SchematicaNeo {

    @Instance(Reference.MODID)
    public static SchematicaNeo instance;

    @SidedProxy(serverSide = Reference.PROXY_SERVER, clientSide = Reference.PROXY_CLIENT)
    public static CommonProxy proxy;

    public static CreativeTabs tab = new CreativeTabs("SchematicaNeo") {
        @Override
        public Item getTabIconItem() {
            return Items.paper;
        }
    };

    public static Block markerPrimary;
    public static Block markerSecondary;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        markerPrimary = new BlockMarker(true)
                .setCreativeTab(tab);
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(markerPrimary, ItemBlockMarker.class, "marker_primary");

        markerSecondary = new BlockMarker(false)
                .setCreativeTab(tab);
        cpw.mods.fml.common.registry.GameRegistry.registerBlock(markerSecondary, ItemBlockMarker.class, "marker_secondary");

        MappingLoader.init(event.getModConfigurationDirectory());
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        FMLCommonHandler.instance().bus().register(AutoPlaceHandler.getInstance());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        MappingLoader.initPhase2();
        proxy.postInit(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}
