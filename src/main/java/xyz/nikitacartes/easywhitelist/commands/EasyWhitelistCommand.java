package xyz.nikitacartes.easywhitelist.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.Whitelist;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.command.WhitelistCommand;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import xyz.nikitacartes.easywhitelist.EasyWhitelist;
import xyz.nikitacartes.easywhitelist.utils.EasyWhitelistUtils;
import xyz.nikitacartes.easywhitelist.utils.TemporaryWhitelistEntry;

import java.util.Collection;
import java.util.Iterator;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.dedicated.command.WhitelistCommand.executeAdd;
import static net.minecraft.server.dedicated.command.WhitelistCommand.executeRemove;
import static xyz.nikitacartes.easywhitelist.EasyWhitelist.getProfileFromNickname;

public class EasyWhitelistCommand {

    private static final SuggestionProvider<ServerCommandSource> TIME_SUGGESTION_PROVIDER = (context, builder) -> {
        StringReader stringReader = new StringReader(builder.getRemaining());
        try {
            stringReader.readFloat();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            return builder.buildFuture();
        }
        return CommandSource.suggestMatching(EasyWhitelistUtils.TIME_UNITS.keySet(), builder.createOffset(builder.getStart() + stringReader.getCursor()));
    };
    public static final SimpleCommandExceptionType INVALID_TIME = new SimpleCommandExceptionType(Text.literal("Время указано не правильно"));

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register(literal("easywhitelist")
                .requires(Permissions.require("easywhitelist.commands.easywhitelist.root", 3))
                .then(literal("add")
                        .requires(Permissions.require("easywhitelist.commands.easywhitelist.add", 3))
                        .then(argument("targets", word())
                                .executes(ctx ->
                                        executeAdd(ctx.getSource(), getProfileFromNickname(getString(ctx, "targets")))
                                )
                                .then(argument("time", word()).suggests(TIME_SUGGESTION_PROVIDER).executes((ctx) ->
                                        executeAddWithTimestamp(ctx.getSource(), getProfileFromNickname(getString(ctx, "targets")), getString(ctx, "time")))))
                )
                .then(literal("remove")
                        .requires(Permissions.require("easywhitelist.commands.easywhitelist.remove", 3))
                        .then(argument("targets", word())
                                .executes(ctx ->
                                        executeRemove(ctx.getSource(), getProfileFromNickname(getString(ctx, "targets"))))
                        )
                )
                .then(literal("list").executes((ctx) -> executeList(ctx.getSource())))
        );
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("ewl").requires(Permissions.require("easywhitelist.commands.easywhitelist.root", 3))).redirect(literalCommandNode));
    }

    private static int executeList(ServerCommandSource source) {
        int playerAmount = EasyWhitelist.getTemporaryEntries().size();
        if (playerAmount == 0) {
            source.sendFeedback(() -> Text.translatable("commands.whitelist.none"), false);
        } else {
            long time = System.currentTimeMillis();
            MutableText text = Text.literal("Найдено " +playerAmount+ " временно добавленных игроков: ");
            for(TemporaryWhitelistEntry entry : EasyWhitelist.getTemporaryEntries()){
                long timeNew = Math.round((double) (entry.getExpireTimestamp() - time) / 1000);
                text.append(Text.literal(entry.getProfile().getName()+ "("+(timeNew < 0 ? 0 : timeNew)+"с), "));
            }


            source.sendFeedback(() -> text, false);
        }
        return playerAmount;
    }

    public static int executeAddWithTimestamp(ServerCommandSource source, Collection<GameProfile> targets, String timeArgument) throws CommandSyntaxException {

        if(timeArgument == null || timeArgument.isEmpty()) throw INVALID_TIME.create();
        int time;
        try{
            int modifier = EasyWhitelistUtils.TIME_UNITS.getOrDefault(timeArgument.substring(timeArgument.length() - 1), 1);
            time = Integer.parseInt(timeArgument.replaceAll("[\\D]", ""))*modifier;
        }catch (Exception e) {
            throw INVALID_TIME.create();
        }

        Whitelist whitelist = source.getServer().getPlayerManager().getWhitelist();
        int i = 0;
        Iterator<GameProfile> var4 = targets.iterator();

        while(var4.hasNext()) {
            GameProfile gameProfile = var4.next();
            if (!whitelist.isAllowed(gameProfile)) {
                WhitelistEntry whitelistEntry = new TemporaryWhitelistEntry(gameProfile, System.currentTimeMillis()+(time / 20L * 1000L));
                whitelist.add(whitelistEntry);
                source.sendFeedback(() -> Text.translatable("commands.whitelist.add.success", Texts.toText(gameProfile)), true);
                ++i;
            }
        }

        if (i == 0) {
            throw WhitelistCommand.ADD_FAILED_EXCEPTION.create();
        } else {
            return i;
        }
    }

}
