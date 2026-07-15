package dev.minerslab.showeverything;

import java.util.Map;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import dev.minerslab.showeverything.command.ShowBlockCommand;
import dev.minerslab.showeverything.command.ShowEntityCommand;
import dev.minerslab.showeverything.command.ShowFluidCommand;
import dev.minerslab.showeverything.command.ShowItemCommand;
import dev.minerslab.showeverything.network.NetworkHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = ShowEverythingMod.MOD_ID,
        name = ShowEverythingMod.NAME,
        version = ShowEverythingMod.VERSION,
        acceptedMinecraftVersions = "[1.7.10]",
        acceptableRemoteVersions = "*"
)
public class ShowEverythingMod {
    public static final String MOD_ID = "showeverything";
    public static final String NAME = "Show Everything";
    public static final String VERSION = "1.0.3";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkHandler.register();
        FMLCommonHandler.instance().bus().register(new NetworkHandler.Events());
        LOGGER.info("Initialized for Forge 1.7.10");
    }

    @NetworkCheckHandler
    public boolean acceptRemoteVersions(Map<String, String> remoteVersions, Side remoteSide) {
        return true;
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ShowItemCommand());
        event.registerServerCommand(new ShowBlockCommand());
        event.registerServerCommand(new ShowFluidCommand());
        event.registerServerCommand(new ShowEntityCommand());
    }
}
