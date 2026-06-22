package dev.minerslab.showeverything.util;

import java.nio.charset.StandardCharsets;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public final class ChatComponents {
    public static final int CHAT_STRING_LIMIT = 32767;

    private ChatComponents() {
    }

    public static ITextComponent item(ItemStack stack) {
        ITextComponent component = new TextComponentString("");
        if (stack.getCount() > 1) {
            component.appendText(stack.getCount() + " * ");
        }
        component.appendSibling(stack.getTextComponent());
        return component;
    }

    public static ITextComponent itemOmitted(ItemStack stack, boolean clientCanShowFullNbt) {
        ITextComponent component = new TextComponentString("");
        if (stack.getCount() > 1) {
            component.appendText(stack.getCount() + " * ");
        }
        ItemStack preview = stack.copy();
        preview.setTagCompound(null);
        component.appendSibling(preview.getTextComponent());
        int nbtChars = stack.writeToNBT(new NBTTagCompound()).toString().length();
        component.appendText(" ");
        component.appendSibling(infoToken(compactItemHover(stack, nbtChars, clientCanShowFullNbt)));
        return component;
    }

    private static ITextComponent compactItemHover(ItemStack stack, int nbtChars, boolean clientCanShowFullNbt) {
        ITextComponent hover = new TextComponentString(stack.getDisplayName());
        hover.getStyle().setColor(TextFormatting.YELLOW);
        hover.appendText("\n");
        ITextComponent note = new TextComponentString(clientCanShowFullNbt
                ? "Full NBT is shown by the optional client mod."
                : "NBT too large for vanilla chat hover (" + nbtChars + " chars); omitted."
        );
        note.getStyle().setColor(TextFormatting.GRAY);
        hover.appendSibling(note);
        return hover;
    }

    public static ITextComponent entity(Entity entity) {
        ITextComponent component = entity.getDisplayName();
        ResourceLocation type = EntityList.getKey(entity);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", entity.getUniqueID().toString());
        tag.setString("type", type == null ? "unknown" : type.toString());
        tag.setString("name", entity.getDisplayName().getUnformattedText());
        component.setStyle(component.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new TextComponentString(tag.toString()))));
        return component;
    }

    public static ITextComponent labelValue(String label, String value) {
        return token(label.trim(), value);
    }

    public static ITextComponent position(BlockPos pos) {
        String shortPos = pos.getX() + " " + pos.getY() + " " + pos.getZ();
        return token("pos", shortPos);
    }

    private static ITextComponent token(String label, String value) {
        ITextComponent component = new TextComponentString("[" + label + "]");
        component.getStyle()
                .setColor(TextFormatting.GREEN)
                .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, value))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(value)));
        return component;
    }

    private static ITextComponent infoToken(ITextComponent hover) {
        ITextComponent component = new TextComponentString("[i]");
        component.getStyle()
                .setColor(TextFormatting.YELLOW)
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
        return component;
    }

    public static String registryName(Item item) {
        ResourceLocation name = item.getRegistryName();
        return name == null ? "unknown" : name.toString();
    }

    public static String registryName(Block block) {
        ResourceLocation name = block.getRegistryName();
        return name == null ? "unknown" : name.toString();
    }

    public static ITextComponent chatPrefix(EntityPlayerMP sender) {
        ITextComponent chat = new TextComponentString("");
        ITextComponent senderName = new TextComponentString(sender.getDisplayNameString());
        senderName.setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getName() + " ")));
        chat.appendSibling(senderName);
        chat.appendText(": ");
        return chat;
    }

    public static ITextComponent chatLine(EntityPlayerMP sender, ITextComponent message) {
        ITextComponent chat = chatPrefix(sender);
        chat.appendSibling(message);
        return chat;
    }

    public static boolean isSafeChatComponent(ITextComponent component) {
        return chatComponentBytes(component) <= CHAT_STRING_LIMIT;
    }

    public static int chatComponentBytes(ITextComponent component) {
        return ITextComponent.Serializer.componentToJson(component).getBytes(StandardCharsets.UTF_8).length;
    }

    public static void broadcast(MinecraftServer server, EntityPlayerMP sender, ITextComponent message) {
        ITextComponent line = chatLine(sender, message);
        if (isSafeChatComponent(line)) {
            server.getPlayerList().sendMessage(line);
        } else {
            sender.sendMessage(new TextComponentString("Show Everything message is too large to send safely."));
        }
    }
}
