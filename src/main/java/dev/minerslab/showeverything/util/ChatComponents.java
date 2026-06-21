package dev.minerslab.showeverything.util;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
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
import net.minecraft.entity.player.EntityPlayerMP;

public final class ChatComponents {
    private ChatComponents() {
    }

    public static ITextComponent item(ItemStack stack) {
        ITextComponent component = new TextComponentString("");
        if (stack.getCount() > 1) {
            component.appendText(stack.getCount() + " * ");
        }
        component.appendSibling(stack.getTextComponent());
        component.setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new TextComponentString(stack.writeToNBT(new NBTTagCompound()).toString()))));
        return component;
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

    public static String registryName(Item item) {
        ResourceLocation name = item.getRegistryName();
        return name == null ? "unknown" : name.toString();
    }

    public static String registryName(Block block) {
        ResourceLocation name = block.getRegistryName();
        return name == null ? "unknown" : name.toString();
    }

    public static void broadcast(MinecraftServer server, EntityPlayerMP sender, ITextComponent message) {
        ITextComponent chat = new TextComponentString("");
        ITextComponent senderName = new TextComponentString(sender.getDisplayNameString());
        senderName.setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/msg " + sender.getName() + " ")));
        chat.appendSibling(senderName);
        chat.appendText(": ");
        chat.appendSibling(message);
        server.getPlayerList().sendMessage(chat);
    }
}
