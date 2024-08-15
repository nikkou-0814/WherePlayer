package nagoya.nikkou;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class PlayerSuggestionProvider {
    public static CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        Collection<ServerPlayerEntity> players = context.getSource().getServer().getPlayerManager().getPlayerList();
        for (ServerPlayerEntity player : players) {
            builder.suggest(player.getName().getString());
        }
        return builder.buildFuture();
    }
}
