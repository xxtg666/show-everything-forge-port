package dev.minerslab.showeverything.command;

import java.util.Collections;
import java.util.List;

import dev.minerslab.showeverything.util.ChatComponents;
import dev.minerslab.showeverything.util.Raycasts;
import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class ShowBlockCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "show-block";
    }

    @Override
    public List getCommandAliases() {
        return Collections.singletonList("showblock");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/show-block [x y z]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        int x;
        int y;
        int z;
        if (args.length == 0) {
            MovingObjectPosition hit = Raycasts.blocks(player, 15.0D, false, false);
            if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                x = hit.blockX;
                y = hit.blockY;
                z = hit.blockZ;
            } else {
                x = floor_double(player.posX);
                y = floor_double(player.posY);
                z = floor_double(player.posZ);
            }
        } else if (args.length == 3) {
            x = parseCoordinate(sender, args[0], floor_double(player.posX));
            y = parseCoordinate(sender, args[1], floor_double(player.posY));
            z = parseCoordinate(sender, args[2], floor_double(player.posZ));
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        World world = player.getEntityWorld();
        if (!world.blockExists(x, y, z)) {
            throw new CommandException("Block is not loaded: %s %s %s", x, y, z);
        }

        Block block = world.getBlock(x, y, z);
        int metadata = world.getBlockMetadata(x, y, z);
        Item item = Item.getItemFromBlock(block);
        ItemStack stack;
        if (item == null) {
            stack = new ItemStack(Items.paper);
            stack.setStackDisplayName(block.getLocalizedName());
        } else {
            stack = new ItemStack(item, 1, block.damageDropped(metadata));
        }

        IChatComponent message = new ChatComponentText("");
        message.appendSibling(ChatComponents.item(stack));
        message.appendText(" ");
        message.appendSibling(ChatComponents.labelValue("id", ChatComponents.registryName(block)));
        message.appendText(" ");
        message.appendSibling(ChatComponents.position(x, y, z));
        ChatComponents.broadcast(player, message);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        return null;
    }

    private static int parseCoordinate(ICommandSender sender, String value, int origin) {
        boolean relative = value.startsWith("~");
        String number = relative ? value.substring(1) : value;
        int parsed = number.length() == 0 ? 0 : parseInt(sender, number);
        return relative ? origin + parsed : parsed;
    }

    private static int floor_double(double value) {
        int integer = (int) value;
        return value < integer ? integer - 1 : integer;
    }
}
