package dev.minerslab.showeverything.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import dev.minerslab.showeverything.ShowEverythingMod;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

/** Tracks the optional client through an explicit hello packet, including vanilla clients. */
public final class NetworkHandler {
    public static final SimpleNetworkWrapper CHANNEL =
            NetworkRegistry.INSTANCE.newSimpleChannel(ShowEverythingMod.MOD_ID);
    static final Map<UUID, Boolean> CLIENT_MODS = new HashMap<UUID, Boolean>();

    private NetworkHandler() {
    }

    public static void register() {
        CHANNEL.registerMessage(ShowItemChatMessage.Handler.class, ShowItemChatMessage.class, 0, Side.CLIENT);
        CHANNEL.registerMessage(ClientHelloMessage.Handler.class, ClientHelloMessage.class, 1, Side.SERVER);
    }

    public static boolean hasClientMod(EntityPlayerMP player) {
        Boolean installed = CLIENT_MODS.get(player.getUniqueID());
        return installed != null && installed.booleanValue();
    }

    static void markClientMod(EntityPlayerMP player, boolean installed) {
        CLIENT_MODS.put(player.getUniqueID(), installed);
        ShowEverythingMod.LOGGER.info("show-everything client: player={} installed={}",
                player.getCommandSenderName(), installed);
    }

    private static void clear(EntityPlayerMP player) {
        CLIENT_MODS.remove(player.getUniqueID());
    }

    public static class Events {
        @SubscribeEvent
        public void onClientConnected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
            CHANNEL.sendToServer(new ClientHelloMessage(ShowEverythingMod.VERSION));
        }

        @SubscribeEvent
        public void onDisconnect(FMLNetworkEvent.ServerDisconnectionFromClientEvent event) {
            if (event.handler instanceof NetHandlerPlayServer
                    && ((NetHandlerPlayServer) event.handler).playerEntity != null) {
                clear(((NetHandlerPlayServer) event.handler).playerEntity);
            }
        }
    }
}
