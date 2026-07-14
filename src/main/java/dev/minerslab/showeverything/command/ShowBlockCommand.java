package dev.minerslab.showeverything.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.minerslab.showeverything.util.ChatComponents;
import dev.minerslab.showeverything.util.Raycasts;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.INameable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public final class ShowBlockCommand {
    private static final SimpleCommandExceptionType NOT_LOADED = new SimpleCommandExceptionType(
            new StringTextComponent("That block is not loaded."));

    private ShowBlockCommand() {
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        com.mojang.brigadier.tree.LiteralCommandNode<CommandSource> command = dispatcher.register(
                Commands.literal("show-block")
                        .executes(context -> execute(context.getSource(), null))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> execute(context.getSource(),
                                        BlockPosArgument.getLoadedBlockPos(context, "pos")))));
        dispatcher.register(Commands.literal("showblock").redirect(command));
    }

    private static int execute(CommandSource source, BlockPos requestedPos) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrException();
        BlockPos pos = requestedPos;
        if (pos == null) {
            RayTraceResult hit = Raycasts.blocks(player, 15.0D, false);
            pos = hit.getType() == RayTraceResult.Type.BLOCK
                    ? ((BlockRayTraceResult) hit).getBlockPos()
                    : player.blockPosition();
        }

        ServerWorld world = player.getLevel();
        if (!world.isLoaded(pos)) {
            throw NOT_LOADED.create();
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        ItemStack stack = block.getCloneItemStack(world, pos, state);
        if (stack.isEmpty()) {
            stack = new ItemStack(Items.BARRIER);
        }
        applyTileName(world, pos, stack);

        IFormattableTextComponent message = new StringTextComponent("")
                .append(ChatComponents.item(stack))
                .append(" ")
                .append(ChatComponents.labelValue("id", ChatComponents.registryName(block)))
                .append(" ")
                .append(ChatComponents.position(pos));
        if (!ChatComponents.isSafeChatComponent(ChatComponents.chatLine(player, message))) {
            message = new StringTextComponent("")
                    .append(ChatComponents.itemOmitted(stack))
                    .append(" ")
                    .append(ChatComponents.labelValue("id", ChatComponents.registryName(block)))
                    .append(" ")
                    .append(ChatComponents.position(pos));
        }
        ChatComponents.broadcast(player.getServer(), player, message);
        return 1;
    }

    private static void applyTileName(ServerWorld world, BlockPos pos, ItemStack stack) {
        TileEntity tile = world.getBlockEntity(pos);
        if (!(tile instanceof INameable) || !((INameable) tile).hasCustomName()) {
            return;
        }
        CompoundNBT display = stack.getOrCreateTagElement("display");
        ITextComponent name = ((INameable) tile).getDisplayName();
        display.putString("Name", ITextComponent.Serializer.toJson(name));
    }
}
