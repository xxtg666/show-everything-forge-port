package dev.minerslab.showeverything;

import com.mojang.brigadier.CommandDispatcher;
import dev.minerslab.showeverything.command.ShowBlockCommand;
import dev.minerslab.showeverything.command.ShowEntityCommand;
import dev.minerslab.showeverything.command.ShowFluidCommand;
import dev.minerslab.showeverything.command.ShowItemCommand;
import dev.minerslab.showeverything.network.NetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ShowEverythingMod.MOD_ID)
public final class ShowEverythingMod {
    public static final String MOD_ID = "showeverything";
    public static final String NAME = "Show Everything";
    public static final String VERSION = "1.0.3";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public ShowEverythingMod() {
        NetworkHandler.register();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
        ShowItemCommand.register(dispatcher);
        ShowBlockCommand.register(dispatcher);
        ShowFluidCommand.register(dispatcher);
        ShowEntityCommand.register(dispatcher);
    }
}
