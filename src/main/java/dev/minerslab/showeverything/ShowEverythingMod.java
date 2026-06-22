package dev.minerslab.showeverything;

import dev.minerslab.showeverything.command.ShowBlockCommand;
import dev.minerslab.showeverything.command.ShowEntityCommand;
import dev.minerslab.showeverything.command.ShowFluidCommand;
import dev.minerslab.showeverything.command.ShowItemCommand;
import dev.minerslab.showeverything.network.NetworkHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = ShowEverythingMod.MOD_ID,
        name = ShowEverythingMod.NAME,
        version = ShowEverythingMod.VERSION,
        acceptedMinecraftVersions = "[1.12.2]",
        acceptableRemoteVersions = "*"
)
public class ShowEverythingMod {
    public static final String MOD_ID = "showeverything";
    public static final String NAME = "Show Everything";
    public static final String VERSION = "1.0.2";

    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkHandler.register();
        MinecraftForge.EVENT_BUS.register(new NetworkHandler.Events());
        LOGGER.info("Initialized!");
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ShowItemCommand());
        event.registerServerCommand(new ShowBlockCommand());
        event.registerServerCommand(new ShowFluidCommand());
        event.registerServerCommand(new ShowEntityCommand());
    }
}
