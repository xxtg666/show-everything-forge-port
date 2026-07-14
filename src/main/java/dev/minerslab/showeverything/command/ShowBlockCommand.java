package dev.minerslab.showeverything.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.minerslab.showeverything.util.ChatComponents;
import dev.minerslab.showeverything.util.Raycasts;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Nameable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class ShowBlockCommand {
    private ShowBlockCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("show-block")
                .requires(source -> source.getEntity() instanceof ServerPlayer)
                .executes(context -> execute(context.getSource(), lookedAt(context.getSource().getPlayerOrException())))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> execute(
                                context.getSource(),
                                BlockPosArgument.getLoadedBlockPos(context, "pos")
                        )));
        LiteralCommandNode<CommandSourceStack> node = dispatcher.register(command);
        dispatcher.register(Commands.literal("showblock").redirect(node));
    }

    private static BlockPos lookedAt(ServerPlayer player) {
        BlockHitResult hit = Raycasts.blocks(player, 15.0D, false);
        return hit.getType() == HitResult.Type.BLOCK ? hit.getBlockPos() : player.blockPosition();
    }

    private static int execute(CommandSourceStack source, BlockPos pos) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer sender = source.getPlayerOrException();
        ServerLevel level = sender.getLevel();
        if (!level.hasChunkAt(pos)) {
            throw new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(
                    new TextComponent("Block is not loaded: " + pos.toShortString())
            ).create();
        }

        BlockState state = level.getBlockState(pos);
        ItemStack stack = state.getBlock().getCloneItemStack(level, pos, state);
        if (stack.isEmpty()) {
            stack = new ItemStack(Items.BARRIER);
        }
        applyBlockEntityName(level.getBlockEntity(pos), stack);

        MutableComponent message = ChatComponents.item(stack)
                .append(" ")
                .append(ChatComponents.labelValue("id", ChatComponents.registryName(state.getBlock())))
                .append(" ")
                .append(ChatComponents.position(pos));
        if (!ChatComponents.isSafeChatComponent(ChatComponents.chatLine(sender, message))) {
            message = ChatComponents.itemOmitted(stack)
                    .append(" ")
                    .append(ChatComponents.labelValue("id", ChatComponents.registryName(state.getBlock())))
                    .append(" ")
                    .append(ChatComponents.position(pos));
        }
        ChatComponents.broadcast(source.getServer(), sender, message);
        return 1;
    }

    private static void applyBlockEntityName(BlockEntity blockEntity, ItemStack stack) {
        if (blockEntity instanceof Nameable nameable && nameable.hasCustomName()) {
            stack.setHoverName(nameable.getDisplayName());
        }
    }
}
