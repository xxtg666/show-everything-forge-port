package dev.minerslab.showeverything.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;

public final class ChatComponents {
    // Clientbound chat components are encoded as bounded JSON. Leave room for packet framing.
    public static final int SAFE_CHAT_BYTES = 240 * 1024;

    private ChatComponents() {
    }

    public static MutableComponent item(ItemStack stack) {
        MutableComponent item = itemName(stack);
        return stack.getCount() > 1
                ? new TextComponent(stack.getCount() + " * ").append(item)
                : item;
    }

    public static MutableComponent itemOmitted(ItemStack stack) {
        MutableComponent component = new TextComponent("");
        if (stack.getCount() > 1) {
            component.append(stack.getCount() + " * ");
        }
        ItemStack preview = stack.copy();
        preview.setTag(null);
        component.append(itemName(preview));
        MutableComponent hover = new TextComponent(stack.getHoverName().getString())
                .withStyle(ChatFormatting.YELLOW)
                .append(new TextComponent("\nNBT omitted for packet safety.").withStyle(ChatFormatting.GRAY));
        return component.append(" ").append(infoToken(hover));
    }

    private static MutableComponent itemName(ItemStack stack) {
        return stack.getDisplayName().copy().withStyle(style -> style.withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(stack))));
    }

    public static MutableComponent entity(Entity entity) {
        HoverEvent.EntityTooltipInfo hover = new HoverEvent.EntityTooltipInfo(
                entity.getType(), entity.getUUID(), entity.getDisplayName()
        );
        return entity.getDisplayName().copy().withStyle(style -> style.withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_ENTITY, hover)
        ));
    }

    public static MutableComponent labelValue(String label, String value) {
        return token(label.trim(), value);
    }

    public static MutableComponent position(BlockPos pos) {
        return token("pos", pos.getX() + " " + pos.getY() + " " + pos.getZ());
    }

    private static MutableComponent token(String label, String value) {
        return new TextComponent("[" + label + "]").withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, value))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(value))));
    }

    private static MutableComponent infoToken(Component hover) {
        return new TextComponent("[i]").withStyle(style -> style
                .withColor(ChatFormatting.YELLOW)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)));
    }

    public static String registryName(Item item) {
        ResourceLocation name = ForgeRegistries.ITEMS.getKey(item);
        return name == null ? "unknown" : name.toString();
    }

    public static String registryName(Block block) {
        ResourceLocation name = ForgeRegistries.BLOCKS.getKey(block);
        return name == null ? "unknown" : name.toString();
    }

    public static MutableComponent chatLine(ServerPlayer sender, Component message) {
        MutableComponent senderName = sender.getDisplayName().copy().withStyle(
                Style.EMPTY.withClickEvent(new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/msg " + sender.getGameProfile().getName() + " "
                ))
        );
        return new TextComponent("").append(senderName).append(": ").append(message.copy());
    }

    public static boolean isSafeChatComponent(Component component) {
        try {
            return Component.Serializer.toJson(component).getBytes(StandardCharsets.UTF_8).length <= SAFE_CHAT_BYTES;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public static void broadcast(MinecraftServer server, ServerPlayer sender, Component message) {
        MutableComponent line = chatLine(sender, message);
        if (!isSafeChatComponent(line)) {
            sender.sendMessage(new TextComponent("Show Everything message is too large to send safely."), sender.getUUID());
            return;
        }
        for (ServerPlayer target : server.getPlayerList().getPlayers()) {
            target.sendMessage(line, sender.getUUID());
        }
    }
}
