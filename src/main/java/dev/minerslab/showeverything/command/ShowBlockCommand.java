package dev.minerslab.showeverything.command;

import java.util.Collections;
import java.util.List;

import dev.minerslab.showeverything.util.ChatComponents;
import dev.minerslab.showeverything.util.Raycasts;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IWorldNameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import javax.annotation.Nullable;

public class ShowBlockCommand extends CommandBase {
    @Override
    public String getName() {
        return "show-block";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("showblock");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/show-block [x y z]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        BlockPos pos;
        if (args.length == 0) {
            RayTraceResult hit = Raycasts.blocks(player, 15.0D, false, false);
            pos = hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK ? hit.getBlockPos() : player.getPosition();
        } else if (args.length == 3) {
            pos = parseBlockPos(sender, args, 0, false);
        } else {
            throw new WrongUsageException(getUsage(sender));
        }

        World world = player.getEntityWorld();
        if (!world.isBlockLoaded(pos)) {
            throw new WrongUsageException("Block is not loaded: %s", pos.toString());
        }

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        Item item = Item.getItemFromBlock(block);
        ItemStack stack = item == Items.AIR ? new ItemStack(Blocks.BARRIER) : new ItemStack(item, 1, block.damageDropped(state));
        applyTileName(world, pos, stack);

        ITextComponent message = new TextComponentString("");
        message.appendSibling(ChatComponents.item(stack));
        message.appendText(" ");
        message.appendSibling(ChatComponents.labelValue("id ", ChatComponents.registryName(block)));
        message.appendText(" ");
        message.appendSibling(ChatComponents.position(pos));
        if (!ChatComponents.isSafeChatComponent(ChatComponents.chatLine(player, message))) {
            message = new TextComponentString("");
            message.appendSibling(ChatComponents.itemOmitted(stack, false));
            message.appendText(" ");
            message.appendSibling(ChatComponents.labelValue("id ", ChatComponents.registryName(block)));
            message.appendText(" ");
            message.appendSibling(ChatComponents.position(pos));
        }
        ChatComponents.broadcast(server, player, message);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return args.length > 0 && args.length <= 3 ? getTabCompletionCoordinate(args, 0, targetPos) : Collections.emptyList();
    }

    private static void applyTileName(World world, BlockPos pos, ItemStack stack) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof IWorldNameable) || !((IWorldNameable) tile).hasCustomName()) {
            return;
        }
        NBTTagCompound display = stack.getOrCreateSubCompound("display");
        ITextComponent name = ((IWorldNameable) tile).getDisplayName();
        display.setString("Name", ITextComponent.Serializer.componentToJson(name));
    }
}
