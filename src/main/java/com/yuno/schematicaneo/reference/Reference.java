package com.yuno.schematicaneo.reference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reference {

    public static final String MODID = "schematicaneo";
    public static final String NAME = "Schematica-Neo";
    public static final String VERSION = "0.1.1";
    public static final String DEPENDENCIES = "required-after:LunatriusCore;";
    public static final String PROXY_SERVER = "com.yuno.schematicaneo.proxy.ServerProxy";
    public static final String PROXY_CLIENT = "com.yuno.schematicaneo.proxy.ClientProxy";
    public static final String LOTR_PROXY = "com.yuno.schematicaneo.compat.LOTRProxy";
    public static final String GUI_FACTORY = "com.yuno.schematicaneo.client.gui.GuiFactory";

    public static Logger logger = LogManager.getLogger(Reference.MODID);
}
