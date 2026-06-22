package dev.minerslab.showeverything.network;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dev.minerslab.showeverything.ShowEverythingMod;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

public final class NetworkHandler {
    public static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(ShowEverythingMod.MOD_ID);
    private static final Map<UUID, Boolean> CLIENT_MODS = new HashMap<UUID, Boolean>();

    private NetworkHandler() {
    }

    public static void register() {
        CHANNEL.registerMessage(ShowItemChatMessage.Handler.class, ShowItemChatMessage.class, 0, Side.CLIENT);
    }

    public static boolean hasClientMod(EntityPlayerMP player) {
        Boolean cached = CLIENT_MODS.get(player.getUniqueID());
        if (cached != null) {
            return cached;
        }
        boolean detected = detectClientMod(player);
        CLIENT_MODS.put(player.getUniqueID(), detected);
        return detected;
    }

    private static boolean detectClientMod(EntityPlayerMP player) {
        NetworkDispatcher dispatcher = NetworkDispatcher.get(player.connection.netManager);
        if (dispatcher == null) {
            return false;
        }
        String version = dispatcher.getModList().get(ShowEverythingMod.MOD_ID);
        return ShowEverythingMod.VERSION.equals(version);
    }

    public static void cacheClientMod(EntityPlayerMP player) {
        boolean hasClientMod = detectClientMod(player);
        CLIENT_MODS.put(player.getUniqueID(), hasClientMod);
        ShowEverythingMod.LOGGER.info("show-everything client: player={} installed={}", player.getName(), hasClientMod);
    }

    public static void clearClientMod(EntityPlayerMP player) {
        CLIENT_MODS.remove(player.getUniqueID());
    }

    public static class Events {
        @SubscribeEvent
        public void onLogin(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
            if (event.getHandler() instanceof net.minecraft.network.NetHandlerPlayServer) {
                EntityPlayerMP player = ((net.minecraft.network.NetHandlerPlayServer) event.getHandler()).player;
                cacheClientMod(player);
            }
        }

        @SubscribeEvent
        public void onDisconnect(FMLNetworkEvent.ServerDisconnectionFromClientEvent event) {
            if (event.getHandler() instanceof net.minecraft.network.NetHandlerPlayServer) {
                EntityPlayerMP player = ((net.minecraft.network.NetHandlerPlayServer) event.getHandler()).player;
                clearClientMod(player);
            }
        }
    }
}
