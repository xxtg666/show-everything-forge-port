package dev.minerslab.showeverything.util;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;

public final class ChatComponents {
    public static final int CHAT_STRING_LIMIT = 32767;

    private ChatComponents() {
    }

    public static IChatComponent item(ItemStack stack) {
        IChatComponent component = new ChatComponentText("");
        if (stack.stackSize > 1) {
            component.appendText(stack.stackSize + " * ");
        }
        component.appendSibling(stack.func_151000_E());
        return component;
    }

    public static IChatComponent itemOmitted(ItemStack stack) {
        IChatComponent component = new ChatComponentText("");
        if (stack.stackSize > 1) {
            component.appendText(stack.stackSize + " * ");
        }
        ItemStack preview = stack.copy();
        preview.setTagCompound(null);
        component.appendSibling(preview.func_151000_E());
        int nbtChars = stack.writeToNBT(new NBTTagCompound()).toString().length();
        component.appendText(" ");
        IChatComponent hover = new ChatComponentText(stack.getDisplayName());
        hover.getChatStyle().setColor(EnumChatFormatting.YELLOW);
        hover.appendText("\n");
        IChatComponent note = new ChatComponentText(
                "NBT too large for vanilla chat hover (" + nbtChars + " chars); omitted.");
        note.getChatStyle().setColor(EnumChatFormatting.GRAY);
        hover.appendSibling(note);
        component.appendSibling(infoToken(hover));
        return component;
    }

    public static IChatComponent entity(Entity entity) {
        IChatComponent component = entity.func_145748_c_();
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("id", entity.getUniqueID().toString());
        tag.setString("type", entityId(entity));
        tag.setString("name", entity.getCommandSenderName());
        component.setChatStyle(component.getChatStyle().setChatHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(tag.toString()))));
        return component;
    }

    public static String entityId(Entity entity) {
        String id = EntityList.getEntityString(entity);
        return id == null ? "unknown" : id;
    }

    public static IChatComponent labelValue(String label, String value) {
        return token(label, value);
    }

    public static IChatComponent position(int x, int y, int z) {
        return token("pos", x + " " + y + " " + z);
    }

    private static IChatComponent token(String label, String value) {
        IChatComponent component = new ChatComponentText("[" + label + "]");
        component.getChatStyle()
                .setColor(EnumChatFormatting.GREEN)
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, value))
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(value)));
        return component;
    }

    private static IChatComponent infoToken(IChatComponent hover) {
        IChatComponent component = new ChatComponentText("[i]");
        component.getChatStyle()
                .setColor(EnumChatFormatting.YELLOW)
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
        return component;
    }

    public static String registryName(Item item) {
        Object name = Item.itemRegistry.getNameForObject(item);
        return name == null ? "unknown" : name.toString();
    }

    public static String registryName(Block block) {
        Object name = Block.blockRegistry.getNameForObject(block);
        return name == null ? "unknown" : name.toString();
    }

    public static IChatComponent chatPrefix(EntityPlayerMP sender) {
        IChatComponent chat = new ChatComponentText("");
        IChatComponent senderName = new ChatComponentText(sender.getDisplayName());
        senderName.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(
                ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getCommandSenderName() + " ")));
        chat.appendSibling(senderName);
        chat.appendText(": ");
        return chat;
    }

    public static IChatComponent chatLine(EntityPlayerMP sender, IChatComponent message) {
        IChatComponent chat = chatPrefix(sender);
        chat.appendSibling(message);
        return chat;
    }

    public static boolean isSafeChatComponent(IChatComponent component) {
        return chatComponentChars(component) <= CHAT_STRING_LIMIT;
    }

    public static int chatComponentChars(IChatComponent component) {
        return IChatComponent.Serializer.func_150696_a(component).length();
    }

    public static void broadcast(EntityPlayerMP sender, IChatComponent message) {
        IChatComponent line = chatLine(sender, message);
        if (isSafeChatComponent(line)) {
            MinecraftServer.getServer().getConfigurationManager().sendChatMsg(line);
        } else {
            sender.addChatMessage(new ChatComponentText("Show Everything message is too large to send safely."));
        }
    }
}
