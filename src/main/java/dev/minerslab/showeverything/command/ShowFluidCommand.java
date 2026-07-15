package dev.minerslab.showeverything.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.minerslab.showeverything.util.ChatComponents;
import dev.minerslab.showeverything.util.Raycasts;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;

public final class ShowFluidCommand {
    private ShowFluidCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(command("show-fluid"));
        dispatcher.register(command("showfluid"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> command(String name) {
        return Commands.literal(name)
                .executes(context -> execute(context.getSource(), lookedAt(context.getSource().getPlayerOrException())))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> execute(
                                context.getSource(),
                                BlockPosArgument.getLoadedBlockPos(context, "pos")
                        )));
    }

    private static BlockPos lookedAt(ServerPlayer player) {
        BlockHitResult hit = Raycasts.blocks(player, 15.0D, true);
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

        Fluid fluid = level.getFluidState(pos).getType();
        if (fluid == Fluids.EMPTY) {
            throw new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(
                    new TextComponent("No fluid found at " + pos.toShortString())
            ).create();
        }
        Item bucket = fluid.getBucket();
        ItemStack stack = new ItemStack(bucket == Items.AIR ? Items.BUCKET : bucket);
        ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(fluid);

        MutableComponent message = ChatComponents.item(stack)
                .append(" ")
                .append(ChatComponents.labelValue("id", fluidId == null ? "unknown" : fluidId.toString()))
                .append(" ")
                .append(ChatComponents.position(pos));
        ChatComponents.broadcast(source.getServer(), sender, message);
        return 1;
    }
}
