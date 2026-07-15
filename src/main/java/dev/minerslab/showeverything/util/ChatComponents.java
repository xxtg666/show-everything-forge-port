package dev.minerslab.showeverything.util;

import java.nio.charset.StandardCharsets;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class ChatComponents {
    private static final int CHAT_STRING_LIMIT = 32767;

    private ChatComponents() {
    }

    public static MutableComponent item(ItemStack stack) {
        MutableComponent item = nativeItemComponent(stack);
        return stack.getCount() > 1
                ? Component.literal(stack.getCount() + " * ").append(item)
                : item;
    }

    /**
     * Keep the vanilla item name styling while making the native SHOW_ITEM
     * payload explicit for clients that render item chat components.
     */
    private static MutableComponent nativeItemComponent(ItemStack stack) {
        return stack.getDisplayName().copy().withStyle(style -> style.withHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(stack))));
    }

    public static MutableComponent itemMessage(ItemStack stack, Component suffix, boolean omitted) {
        MutableComponent message = omitted ? itemOmitted(stack) : item(stack);
        return message.append(suffix.copy());
    }

    private static MutableComponent itemOmitted(ItemStack stack) {
        ItemStack preview = stack.copy();
        CompoundTag tag = preview.getTag();
        int nbtChars = tag == null ? 0 : tag.toString().length();
        preview.setTag(null);
        MutableComponent component = item(preview).append(" ");
        Component hover = Component.literal(stack.getHoverName().getString()).withStyle(ChatFormatting.YELLOW)
                .append(Component.literal("\nNBT too large for vanilla chat hover (" + nbtChars + " chars); omitted.")
                        .withStyle(ChatFormatting.GRAY));
        return component.append(Component.literal("[i]").withStyle(style -> style
                .withColor(ChatFormatting.YELLOW)
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))));
    }

    public static MutableComponent token(String label, String value) {
        return Component.literal("[" + label + "]").withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, value))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(value))));
    }

    public static MutableComponent position(BlockPos pos) {
        return token("pos", pos.getX() + " " + pos.getY() + " " + pos.getZ());
    }

    public static MutableComponent chatLine(Component senderDisplayName, String senderCommandName, Component message) {
        return senderDisplayName.copy().withStyle(style -> style.withClickEvent(
                        new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + senderCommandName + " ")))
                .append(": ").append(message);
    }

    public static boolean isSafe(Component component) {
        return Component.Serializer.toJson(component).getBytes(StandardCharsets.UTF_8).length <= CHAT_STRING_LIMIT;
    }

    public static void broadcast(ServerPlayer sender, Component message) {
        Component line = chatLine(sender.getDisplayName(), sender.getGameProfile().getName(), message);
        if (isSafe(line)) {
            sender.getServer().getPlayerList().broadcastSystemMessage(line, false);
        } else {
            sender.sendSystemMessage(Component.literal("Show Everything message is too large to send safely.")
                    .withStyle(ChatFormatting.RED));
        }
    }

    public static String registryName(Item item) {
        net.minecraft.resources.ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        return id == null ? "unknown" : id.toString();
    }
}
