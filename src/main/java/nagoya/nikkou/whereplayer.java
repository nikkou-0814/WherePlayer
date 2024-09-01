package nagoya.nikkou;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.util.Formatting;

public class whereplayer implements ModInitializer {

    private MinecraftServer server;

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher, registryAccess);
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            this.server = server;
        });
    }

    private void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("where")
                .then(CommandManager.argument("Player", StringArgumentType.string())
                        .suggests(PlayerSuggestionProvider::getSuggestions)
                        .executes(this::executeWhereCommand)));
    }

    private void send_text(String message) {
        if (server != null && server.getPlayerManager() != null) {
            Text coloredMessage = Text.literal(message).formatted(Formatting.AQUA);
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                player.sendMessage(coloredMessage, false);
            }
        }
    }

    private int executeWhereCommand(CommandContext<ServerCommandSource> context) {
        String playerName = StringArgumentType.getString(context, "Player");
        ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(playerName);
        ServerCommandSource source = context.getSource();
        MinecraftServer server = source.getServer();

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
        String dimension = player.getWorld().getRegistryKey().getValue().toString().replace("minecraft:", "");

        send_text(playerName + " は座標 [" + x + ", " + y + ", " + z + "] にいます。\nバイオーム: " + dimension);
        send_text("xaero-waypoint:" + playerName + " is here" + ":S:" + x + ":" + y + ":" + z + ":3:false:0:Internal-" + dimension + "-waypoints");

        return 1;
    }
}
