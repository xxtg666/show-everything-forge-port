package dev.minerslab.showeverything.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.minerslab.showeverything.util.ChatComponents;
import dev.minerslab.showeverything.util.Raycasts;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public final class ShowFluidCommand {
    private static final SimpleCommandExceptionType NOT_LOADED = new SimpleCommandExceptionType(
            new StringTextComponent("That block is not loaded."));
    private static final SimpleCommandExceptionType NO_FLUID = new SimpleCommandExceptionType(
            new StringTextComponent("No fluid found at that position."));

    private ShowFluidCommand() {
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(command("show-fluid"));
        dispatcher.register(command("showfluid"));
    }

    private static LiteralArgumentBuilder<CommandSource> command(String name) {
        return Commands.literal(name)
                        .executes(context -> execute(context.getSource(), null))
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(context -> execute(context.getSource(),
                                        BlockPosArgument.getLoadedBlockPos(context, "pos"))));
    }

    private static int execute(CommandSource source, BlockPos requestedPos) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrException();
        BlockPos pos = requestedPos;
        if (pos == null) {
            RayTraceResult hit = Raycasts.blocks(player, 15.0D, true);
            pos = hit.getType() == RayTraceResult.Type.BLOCK
                    ? ((BlockRayTraceResult) hit).getBlockPos()
                    : player.blockPosition();
        }

        ServerWorld world = player.getLevel();
        if (!world.isLoaded(pos)) {
            throw NOT_LOADED.create();
        }

        FluidState fluidState = world.getFluidState(pos);
        if (fluidState.isEmpty()) {
            throw NO_FLUID.create();
        }
        Fluid fluid = fluidState.getType();
        ItemStack stack = new ItemStack(fluid.getBucket());
        if (stack.isEmpty() || stack.getItem() == Items.AIR) {
            stack = new ItemStack(Items.BUCKET);
        }

        IFormattableTextComponent message = new StringTextComponent("")
                .append(ChatComponents.item(stack))
                .append(" ")
                .append(ChatComponents.labelValue("id", Registry.FLUID.getKey(fluid).toString()))
                .append(" ")
                .append(ChatComponents.position(pos));
        ChatComponents.broadcast(player.getServer(), player, message);
        return 1;
    }
}
