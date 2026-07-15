package dev.minerslab.showeverything.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.minerslab.showeverything.util.ChatComponents;
import dev.minerslab.showeverything.util.Raycasts;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

public final class ShowEntityCommand {
    private ShowEntityCommand() {
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(command("show-entity"));
        dispatcher.register(command("showentity"));
    }

    private static LiteralArgumentBuilder<CommandSource> command(String name) {
        return Commands.literal(name)
                        .executes(context -> execute(context.getSource(), null))
                        .then(Commands.argument("target", EntityArgument.entity())
                                .executes(context -> execute(context.getSource(),
                                        EntityArgument.getEntity(context, "target"))));
    }

    private static int execute(CommandSource source, Entity requestedEntity) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrException();
        Entity entity = requestedEntity;
        if (entity == null) {
            RayTraceResult hit = Raycasts.entity(player, 15.0D);
            entity = hit != null && hit.getType() == RayTraceResult.Type.ENTITY
                    ? ((EntityRayTraceResult) hit).getEntity()
                    : player;
        }

        ResourceLocation entityId = entity.getType().getRegistryName();
        IFormattableTextComponent message = new StringTextComponent("")
                .append(ChatComponents.entity(entity))
                .append(" ")
                .append(ChatComponents.labelValue("id", entityId == null ? "unknown" : entityId.toString()))
                .append(" ")
                .append(ChatComponents.labelValue("uuid", entity.getUUID().toString()))
                .append(" ")
                .append(ChatComponents.position(entity.blockPosition()));
        ChatComponents.broadcast(player.getServer(), player, message);
        return 1;
    }
}
