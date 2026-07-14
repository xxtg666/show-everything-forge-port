package dev.minerslab.showeverything;

import dev.minerslab.showeverything.command.ShowBlockCommand;
import dev.minerslab.showeverything.command.ShowEntityCommand;
import dev.minerslab.showeverything.command.ShowFluidCommand;
import dev.minerslab.showeverything.command.ShowItemCommand;
import dev.minerslab.showeverything.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ShowEverythingMod.MOD_ID)
public final class ShowEverythingMod {
    public static final String MOD_ID = "showeverything";
    public static final String VERSION = "1.0.2";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ShowEverythingMod() {
        NetworkHandler.register();
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ShowItemCommand.register(event.getDispatcher());
        ShowBlockCommand.register(event.getDispatcher());
        ShowFluidCommand.register(event.getDispatcher());
        ShowEntityCommand.register(event.getDispatcher());
    }
}
