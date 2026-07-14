package dev.minerslab.showeverything;

import dev.minerslab.showeverything.command.ShowCommands;
import dev.minerslab.showeverything.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ShowEverythingMod.MOD_ID)
public final class ShowEverythingMod {
    public static final String MOD_ID = "showeverything";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ShowEverythingMod() {
        NetworkHandler.register();
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        LOGGER.info("Show Everything initialized");
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ShowCommands.register(event.getDispatcher());
    }
}
