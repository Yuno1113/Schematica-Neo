package com.yuno.schematicaneo.reference;

@SuppressWarnings("HardCodedStringLiteral")
public final class Names {

    public static final class Config {

        public static final class Category {

            public static final String DEBUG = "debug";
            public static final String RENDER = "render";
            public static final String PRINTER = "printer";
            public static final String PRINTER_SWAPSLOTS = "printer.swapslots";
            public static final String TOOLTIP = "tooltip";
            public static final String GENERAL = "general";
            public static final String SERVER = "server";
        }

        public static final String SHOW_DEBUG_INFO = "showDebugInfo";
        public static final String SHOW_DEBUG_INFO_DESC = "Display extra information on the debug screen (F3).";
        public static final String EXTENDED_ID_FORMAT = "useSchematicplusFormat";
        public static final String EXTENDED_ID_FORMAT_DESC = "Save and load schematics with a different format that supports a much higher number of block ids. Only schematics in schemplus format will be loaded.";
        public static final String ALPHA_ENABLED = "alphaEnabled";
        public static final String ALPHA_ENABLED_DESC = "Enable transparent textures.";
        public static final String ALPHA = "alpha";
        public static final String ALPHA_DESC = "Alpha value used when rendering the schematic (1.0 = opaque, 0.5 = half transparent, 0.0 = transparent).";
        public static final String HIGHLIGHT = "highlight";
        public static final String HIGHLIGHT_DESC = "Highlight invalid placed blocks and to be placed blocks.";
        public static final String HIGHLIGHT_AIR = "highlightAir";
        public static final String HIGHLIGHT_AIR_DESC = "Highlight blocks that should be air.";
        public static final String BLOCK_DELTA = "blockDelta";
        public static final String BLOCK_DELTA_DESC = "Delta value used for highlighting (if you experience z-fighting increase this).";
        public static final String DRAW_QUADS = "drawQuads";
        public static final String DRAW_QUADS_DESC = "Draw surface areas.";
        public static final String DRAW_LINES = "drawLines";
        public static final String DRAW_LINES_DESC = "Draw outlines.";

        public static final String PLACE_DELAY = "placeDelay";
        public static final String PLACE_DELAY_DESC = "Delay between placement attempts (in ticks).";
        public static final String TIMEOUT = "timeout";
        public static final String TIMEOUT_DESC = "Timeout before re-trying failed blocks.";
        public static final String PLACE_INSTANTLY = "placeInstantly";
        public static final String PLACE_INSTANTLY_DESC = "Place all blocks that can be placed in one tick.";
        public static final String DESTROY_BLOCKS = "destroyBlocks";
        public static final String DESTROY_BLOCKS_DESC = "The printer will destroy blocks (creative mode only).";
        public static final String DESTROY_INSTANTLY = "destroyInstantly";
        public static final String DESTROY_INSTANTLY_DESC = "Destroy all blocks that can be destroyed in one tick.";
        public static final String PLACE_ADJACENT = "placeAdjacent";
        public static final String PLACE_ADJACENT_DESC = "Place blocks only if there is an adjacent block next to them.";
        public static final String SWAP_SLOT = "swapSlot";
        public static final String SWAP_SLOT_DESC = "Allow the printer to use this hotbar slot.";

        public static final String TOOLTIP_ENABLED = "tooltipEnabled";
        public static final String TOOLTIP_ENABLED_DESC = "Display a tooltip when hovering over blocks in a schematic.";
        public static final String TOOLTIP_X = "tooltipX";
        public static final String TOOLTIP_X_DESC = "Relative tooltip X.";
        public static final String TOOLTIP_Y = "tooltipY";
        public static final String TOOLTIP_Y_DESC = "Relative tooltip Y.";

        public static final String SCHEMATIC_DIRECTORY = "schematicDirectory";
        public static final String SCHEMATIC_DIRECTORY_DESC = "Schematic directory.";
        public static final String EXTRA_AIR_BLOCKS = "extraAirBlocks";
        public static final String EXTRA_AIR_BLOCKS_DESC = "Extra blocks to consider as air for the schematic renderer.";
        public static final String SORT_TYPE = "sortType";
        public static final String SORT_TYPE_DESC = "Default sort type for the material list.";

        public static final String PRINTER_ENABLED = "printerEnabled";
        public static final String PRINTER_ENABLED_DESC = "Allow players to use the printer.";
        public static final String SAVE_ENABLED = "saveEnabled";
        public static final String SAVE_ENABLED_DESC = "Allow players to save schematics.";
        public static final String LOAD_ENABLED = "loadEnabled";
        public static final String LOAD_ENABLED_DESC = "Allow players to load schematics.";

        public static final String PLAYER_QUOTA_KILOBYTES = "playerQuotaKilobytes";
        public static final String PLAYER_QUOTA_KILOBYTES_DESC = "Amount of storage provided per-player for schematics on the server.";

        public static final String SERVERSIDE_SCHEMATICS_ENABLED = "serversideSchematicsEnabled";
        public static final String SERVERSIDE_SCHEMATICS_ENABLED_DESC = "Allow players to save schematics serverside, download schematics and see serverside schematics";

        public static final String LANG_PREFIX = Reference.MODID.toLowerCase() + ".config";
    }

    public static final class Chat {

        public static final String SAVE_COORDINATES_SUCCESS = "Schematica.chat.saveCoordinatesSuccess";
        public static final String SAVE_COORDINATES_FAIL = "Schematica.chat.saveCoordinatesFail";
    }

    public static final class Command {

        public static final class Save {

            public static final class Message {

                public static final String USAGE = "schematica.command.save.usage";
                public static final String PLAYERS_ONLY = "schematica.command.save.playersOnly";
                public static final String SAVE_STARTED = "schematica.command.save.started";
                public static final String SAVE_SUCCESSFUL = "schematica.command.save.saveSucceeded";
                public static final String SAVE_FAILED = "schematica.command.save.saveFailed";
                public static final String QUOTA_EXCEEDED = "schematica.command.save.quotaExceeded";
                public static final String PLAYER_SCHEMATIC_DIR_UNAVAILABLE = "schematica.command.save.playerSchematicDirUnavailable";
            }

            public static final String NAME = "schematicaSave";
        }

        public static final class List {

            public static final class Message {

                public static final String USAGE = "schematica.command.list.usage";
                public static final String LIST_NOT_AVAILABLE = "schematica.command.list.notAvailable";
                public static final String REMOVE = "schematica.command.list.remove";
                public static final String DOWNLOAD = "schematica.command.list.download";
                public static final String PAGE_HEADER = "schematica.command.list.header";
                public static final String NO_SUCH_PAGE = "schematica.command.list.noSuchPage";
                public static final String NO_SCHEMATICS = "schematica.command.list.noSchematics";
            }

            public static final String NAME = "schematicaList";
        }

        public static final class Remove {

            public static final class Message {

                public static final String USAGE = "schematica.command.remove.usage";
                public static final String PLAYERS_ONLY = "schematica.command.save.playersOnly";
                public static final String SCHEMATIC_REMOVED = "schematica.command.remove.schematicRemoved";
                public static final String SCHEMATIC_NOT_FOUND = "schematica.command.remove.schematicNotFound";
                public static final String ARE_YOU_SURE_START = "schematica.command.remove.areYouSure";
                public static final String YES = "gui.yes";
            }

            public static final String NAME = "schematicaRemove";
        }

        public static final class Download {

            public static final class Message {

                public static final String USAGE = "schematica.command.download.usage";
                public static final String PLAYERS_ONLY = "schematica.command.save.playersOnly";
                public static final String DOWNLOAD_STARTED = "schematica.command.download.started";
                public static final String DOWNLOAD_SUCCEEDED = "schematica.command.download.downloadSucceeded";
                public static final String DOWNLOAD_FAILED = "schematica.command.download.downloadFail";
            }

            public static final String NAME = "schematicaDownload";
        }
    }

    public static final class Gui {

        public static final class Load {

            public static final String TITLE = "schematica.gui.title";
            public static final String FOLDER_INFO = "schematica.gui.folderInfo";
            public static final String OPEN_FOLDER = "schematica.gui.openFolder";
            public static final String NO_SCHEMATIC = "schematica.gui.noschematic";
        }

        public static final class Editor {

            public static final String PREFIX = "schematica.editor.";
            public static final String TITLE = PREFIX + "title";
            public static final String ENABLE = PREFIX + "enable";
            public static final String ENABLE_TOOLTIP = PREFIX + "enable.tooltip";
            public static final String MODE = PREFIX + "mode";
            public static final String SELECTION_SOURCE = PREFIX + "selectionSource";
            public static final String SOURCE_SCHEMATIC = PREFIX + "source.schematic";
            public static final String SOURCE_WORLD = PREFIX + "source.world";
            public static final String IMPORT = PREFIX + "import";
            public static final String SAVE_SELECTION = PREFIX + "save.selection";
            public static final String SAVE_EDITED = PREFIX + "save.edited";
            public static final String SAVE_SELECTION_TOOLTIP = PREFIX + "save.selection.tooltip";
            public static final String SAVE_EDITED_TOOLTIP = PREFIX + "save.edited.tooltip";
            public static final String REPLACE_CONFIG = PREFIX + "replace.config";
            public static final String REPLACE_TITLE = PREFIX + "replace.title";
            public static final String REPLACE_HELP = PREFIX + "replace.help";
            public static final String REPLACE_METADATA = PREFIX + "replace.metadata";
            public static final String REPLACE_TARGET = PREFIX + "replace.target";
            public static final String REPLACE_NONE = PREFIX + "replace.none";
            public static final String IMPORT_TITLE = PREFIX + "import.title";
            public static final String IMPORT_DESCRIPTION = PREFIX + "import.description";
            public static final String IMPORT_EMPTY = PREFIX + "import.empty";
            public static final String IMPORT_SELECT = PREFIX + "import.select";
            public static final String IMPORT_LOCAL_FILE = PREFIX + "import.localFile";
            public static final String IMPORT_BOOK = PREFIX + "import.book";
            public static final String IMPORT_BOOK_SINGLEPLAYER = PREFIX + "import.book.singleplayer";
            public static final String IMPORT_FILE_SAVED = PREFIX + "import.fileSaved";
            public static final String IMPORT_FORMATS = PREFIX + "import.formats";
            public static final String IMPORT_COMPLETE = PREFIX + "import.complete";
            public static final String IMPORT_FAILED = PREFIX + "import.failed";
            public static final String IMPORT_UNKNOWN_COUNT = PREFIX + "import.unknownCount";
            public static final String IMPORT_BOOK_TITLE = PREFIX + "import.bookTitle";
            public static final String IMPORT_BOOK_CREATED = PREFIX + "import.bookCreated";
            public static final String IMPORT_COMPLETE_PATH = PREFIX + "import.completePath";
            public static final String CORNER_SELECTION = PREFIX + "cornerSelection";
            public static final String CORNER_SELECTION_TOOLTIP = PREFIX + "cornerSelection.tooltip";
            public static final String SELECTION_SOURCE_TOOLTIP = PREFIX + "selectionSource.tooltip";
            public static final String IMPORT_TOOLTIP = PREFIX + "import.tooltip";
            public static final String IMPORT_FILE_SAVED_PATH = PREFIX + "import.fileSavedPath";
            public static final String IMPORT_REFERENCE_TOOLTIP = PREFIX + "import.reference.tooltip";
            public static final String HELP_CONTROLS = PREFIX + "help.controls";
            public static final String HELP_ACTION = PREFIX + "help.action";
            public static final String CURRENT_RANGE = PREFIX + "currentRange";
            public static final String RANGE_SINGLE = PREFIX + "range.single";
            public static final String RANGE_SELECTION = PREFIX + "range.selection";
            public static final String RANGE_ALL = PREFIX + "range.all";
            public static final String CHAT_PREFIX = PREFIX + "chat.prefix";
            public static final String CHAT_MODE = PREFIX + "chat.mode";
            public static final String CHAT_SOURCE = PREFIX + "chat.source";
            public static final String CHAT_EMPTY = PREFIX + "chat.empty";
            public static final String CHAT_PREVIEW = PREFIX + "chat.preview";
            public static final String CHAT_CANCEL = PREFIX + "chat.cancel";
            public static final String CHAT_COPY = PREFIX + "chat.copy";
            public static final String CHAT_CUT = PREFIX + "chat.cut";
            public static final String CHAT_NO_SCHEMATIC = PREFIX + "chat.noSchematic";
            public static final String CHAT_PASTE = PREFIX + "chat.paste";
            public static final String CHAT_REPLACE_TARGET = PREFIX + "chat.replaceTarget";
            public static final String CHAT_REPLACED = PREFIX + "chat.replaced";
            public static final String CHAT_ROTATED = PREFIX + "chat.rotated";
            public static final String CHAT_RANGE = PREFIX + "chat.range";
        }

        public static final class Control {

            public static final String SAVE_COORDINATES = "schematica.gui.savecoordinates";
            public static final String MOVE_SCHEMATIC = "schematica.gui.moveschematic";
            public static final String MATERIALS = "schematica.gui.materials";
            public static final String PRINTER = "schematica.gui.printer";
            public static final String OPERATIONS = "schematica.gui.operations";

            public static final String NAME = "schematica.gui.name";

            public static final String UNLOAD = "schematica.gui.unload";
            public static final String MODE_ALL = "schematica.gui.all";
            public static final String MODE_LAYERS = "schematica.gui.layers";
            public static final String HIDE = "schematica.gui.hide";
            public static final String SHOW = "schematica.gui.show";
            public static final String MOVE_HERE = "schematica.gui.movehere";
            public static final String FLIP = "schematica.gui.flip";
            public static final String ROTATE = "schematica.gui.rotate";
            public static final String VISIBILITY_SELECTION = "schematicaneo.gui.visibility.selection";
            public static final String VISIBILITY_BLOCK = "schematicaneo.gui.visibility.block";
            public static final String VISIBILITY_BLOCK_CONFIG = "schematicaneo.gui.visibility.block.config";
            public static final String VISIBILITY_BLOCK_TITLE = "schematicaneo.gui.visibility.block.title";
            public static final String VISIBILITY_BLOCK_HELP = "schematicaneo.gui.visibility.block.help";
            public static final String PRINT_SPEED_PREFIX = "schematicaneo.gui.printSpeed.";
            public static final String PRINT_SPEED_TOOLTIP = PRINT_SPEED_PREFIX + "tooltip";
            public static final String TRANSFORM_PREFIX = "schematica.gui.";

            public static final String MATERIAL_NAME = "schematica.gui.materialname";
            public static final String MATERIAL_AMOUNT = "schematica.gui.materialamount";
            public static final String MATERIAL_REQUIRED = "schematica.gui.materialrequired";
            public static final String MATERIAL_AVAILABLE = "schematica.gui.materialavailable";

            public static final String SORT_PREFIX = "schematica.gui.material";
            public static final String DUMP = "schematica.gui.materialdump";
        }

        public static final String X = "schematica.gui.x";
        public static final String Y = "schematica.gui.y";
        public static final String Z = "schematica.gui.z";
        public static final String ON = "schematica.gui.on";
        public static final String OFF = "schematica.gui.off";
        public static final String DONE = "schematica.gui.done";
    }

    public static final class ModId {

        public static final String MINECRAFT = "minecraft";
    }

    public static final class Keys {

        public static final String CATEGORY = "Schematica-Neo";
        public static final String LOAD = "schematicaneo.key.load";
        public static final String SAVE = "schematicaneo.key.save";
        public static final String CONTROL = "schematicaneo.key.control";
        public static final String LAYER_INC = "schematicaneo.key.layerInc";
        public static final String LAYER_DEC = "schematicaneo.key.layerDec";
    }

    public static final class NBT {

        public static final String ROOT = "Schematic";

        public static final String MATERIALS = "Materials";
        public static final String FORMAT_CLASSIC = "Classic";
        public static final String FORMAT_ALPHA = "Alpha";

        public static final String ICON = "Icon";
        public static final String BLOCKS = "Blocks";
        public static final String DATA = "Data";
        public static final String ADD_BLOCKS = "AddBlocks";
        public static final String ADD_BLOCKS_SCHEMATICA = "Add";
        public static final String WIDTH = "Width";
        public static final String LENGTH = "Length";
        public static final String HEIGHT = "Height";
        public static final String MAPPING = "..."; // TODO: use this once MCEdit adds support for it
        public static final String MAPPING_SCHEMATICA = "SchematicaMapping";
        public static final String TILE_ENTITIES = "TileEntities";
        public static final String ENTITIES = "Entities";
        public static final String EXTENDED_METADATA = "ExtendedMetadata";
    }

    public static final class SBC {

        public static final String DISABLE_PRINTER = "\u00a70\u00a72\u00a70\u00a70\u00a7e\u00a7f";
        public static final String DISABLE_SAVE = "\u00a70\u00a72\u00a71\u00a70\u00a7e\u00a7f";
        public static final String DISABLE_LOAD = "\u00a70\u00a72\u00a71\u00a71\u00a7e\u00a7f";
    }
}
