package nagoya.nikkou;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.command.CommandRegistryAccess;

import java.util.function.Supplier;

public class whereplayer implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher, registryAccess);
        });
    }

    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("where")
                .then(CommandManager.argument("Player", StringArgumentType.string())
                        .suggests(PlayerSuggestionProvider::getSuggestions)
                        .executes(this::executeWhereCommand)));
    }

    private int executeWhereCommand(CommandContext<ServerCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "Player");
        ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(playerName);

        if (player == null) {
            context.getSource().sendError(Text.literal("そのプレイヤーは存在しません"));
            return 0;
        }

        StatusEffectInstance glowingEffect = new StatusEffectInstance(
                StatusEffects.GLOWING,
                1200,
                1,
                true,
                true
        );

        player.addStatusEffect(glowingEffect);

        int x = (int) player.getX();
        int y = (int) player.getY();
        int z = (int) player.getZ();

        Text positionMessage = Text.literal(playerName + " is at [" + x + ", " + y + ", " + z + "]").formatted(Formatting.AQUA);
        Text waypointMessage = Text.literal("xaero-waypoint:" + playerName + " is here" + ":S:" + x + ":" + y + ":" + z + ":3:false:0:Internal-overworld-waypoints");

        context.getSource().sendFeedback(
                (Supplier<Text>) () -> positionMessage,
                true
        );
        context.getSource().sendFeedback(
                (Supplier<Text>) () -> waypointMessage,
                true
        );

        return 1;
    }
}
