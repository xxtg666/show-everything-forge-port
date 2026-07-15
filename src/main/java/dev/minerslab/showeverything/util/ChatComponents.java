package dev.minerslab.showeverything.util;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.nio.charset.StandardCharsets;

public final class ChatComponents {
    // Clientbound chat components are decoded with a 262144-character limit in 1.16.5.
    private static final int CHAT_COMPONENT_SAFETY_LIMIT = 240000;

    private ChatComponents() {
    }

    public static ITextComponent item(ItemStack stack) {
        ITextComponent item = itemName(stack);
        return stack.getCount() > 1
                ? new StringTextComponent(stack.getCount() + " * ").append(item)
                : item;
    }

    public static ITextComponent itemOmitted(ItemStack stack) {
        IFormattableTextComponent component = new StringTextComponent("");
        if (stack.getCount() > 1) {
            component.append(stack.getCount() + " * ");
        }
        ItemStack preview = stack.copy();
        preview.setTag(null);
        component.append(itemName(preview)).append(" ");
        IFormattableTextComponent hover = new StringTextComponent(stack.getHoverName().getString())
                .withStyle(TextFormatting.YELLOW)
                .append("\n")
                .append(new StringTextComponent("NBT too large for vanilla chat hover; omitted.")
                        .withStyle(TextFormatting.GRAY));
        return component.append(new StringTextComponent("[i]").withStyle(style -> style
                .withColor(TextFormatting.YELLOW)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))));
    }

    private static ITextComponent itemName(ItemStack stack) {
        return stack.getDisplayName().copy().withStyle(style -> style.withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemHover(stack))));
    }

    public static ITextComponent entity(Entity entity) {
        return entity.getDisplayName();
    }

    public static ITextComponent labelValue(String label, String value) {
        return token(label, value);
    }

    public static ITextComponent position(net.minecraft.util.math.BlockPos pos) {
        return token("pos", pos.getX() + " " + pos.getY() + " " + pos.getZ());
    }

    private static ITextComponent token(String label, String value) {
        return new StringTextComponent("[" + label + "]").withStyle(style -> style
                .withColor(TextFormatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, value))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(value))));
    }

    public static String registryName(Item item) {
        return item.getRegistryName() == null ? "unknown" : item.getRegistryName().toString();
    }

    public static String registryName(Block block) {
        return block.getRegistryName() == null ? "unknown" : block.getRegistryName().toString();
    }

    public static ITextComponent chatLine(ServerPlayerEntity sender, ITextComponent message) {
        IFormattableTextComponent senderName = sender.getDisplayName().copy().withStyle(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                        "/msg " + sender.getGameProfile().getName() + " ")));
        return new StringTextComponent("").append(senderName).append(": ").append(message);
    }

    public static boolean isSafeChatComponent(ITextComponent component) {
        try {
            return chatComponentBytes(component) <= CHAT_COMPONENT_SAFETY_LIMIT;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    public static int chatComponentBytes(ITextComponent component) {
        return ITextComponent.Serializer.toJson(component).getBytes(StandardCharsets.UTF_8).length;
    }

    public static void broadcast(MinecraftServer server, ServerPlayerEntity sender, ITextComponent message) {
        ITextComponent line = chatLine(sender, message);
        if (isSafeChatComponent(line)) {
            broadcastLine(server, sender, line);
        } else {
            sender.sendMessage(new StringTextComponent(
                    "Show Everything message is too large to send safely."), sender.getUUID());
        }
    }

    public static void broadcastLine(MinecraftServer server, ServerPlayerEntity sender, ITextComponent line) {
        server.getPlayerList().broadcastMessage(line, ChatType.CHAT, sender.getUUID());
    }
}
