package dev.minerslab.showeverything;

import dev.minerslab.showeverything.command.ShowCommands;
import dev.minerslab.showeverything.network.NetworkHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ShowEverythingMod.MOD_ID)
public final class ShowEverythingMod {
    public static final String MOD_ID = "showeverything";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ShowEverythingMod(IEventBus modEventBus) {
        modEventBus.addListener(NetworkHandler::register);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        LOGGER.info("Show Everything initialized");
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ShowCommands.register(event.getDispatcher());
    }
}
