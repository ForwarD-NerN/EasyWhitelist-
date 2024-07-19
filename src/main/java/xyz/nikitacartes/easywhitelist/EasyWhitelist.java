package xyz.nikitacartes.easywhitelist;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Uuids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nikitacartes.easywhitelist.commands.*;
import xyz.nikitacartes.easywhitelist.utils.TemporaryWhitelistEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

public class EasyWhitelist implements ModInitializer {
    public static Logger LOGGER = LogManager.getLogger("easywhitelist");
    private static final Set<TemporaryWhitelistEntry> temporaryEntries = Sets.newHashSet();
    public static ConfigurationManager.Config config = new ConfigurationManager.Config();

    public static Collection<GameProfile> getProfileFromNickname(String name) {
        return Collections.singletonList(new GameProfile(Uuids.getOfflinePlayerUuid(name), name));
    }

    private static int checkDelay = 0;

    @Override
    public void onInitialize() {
        LOGGER.info("Whitelist is now name-based.");
        ConfigurationManager.onInit();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            EasyWhitelistCommand.registerCommand(dispatcher);
            EasyBanCommand.registerCommand(dispatcher);
            EasyPardonCommand.registerCommand(dispatcher);
            EasyOpCommand.registerCommand(dispatcher);
            EasyDeOpCommand.registerCommand(dispatcher);
        });
        ServerTickEvents.END_SERVER_TICK.register((server -> {
            checkDelay++;
            if(checkDelay >= EasyWhitelist.config.whiteListCheckDelay){
                checkDelay = 0;
                Iterator<TemporaryWhitelistEntry> iterator = temporaryEntries.iterator();

                while (iterator.hasNext()){
                    TemporaryWhitelistEntry entry = iterator.next();
                    if(entry.getExpireTimestamp() < System.currentTimeMillis()){
                        ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getProfile().getName());
                        if(player != null) {
                            server.getPlayerManager().getWhitelist().remove(entry.getProfile());
                            iterator.remove();
                            player.networkHandler.disconnect(Text.literal(EasyWhitelist.config.kickMessage));
                        }
                    }
                }
            }
        }));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            for(TemporaryWhitelistEntry entry : temporaryEntries){
                if(entry.getProfile().getName().equals(handler.getPlayer().getName().getString())){
                    temporaryEntries.remove(entry);
                }
            }
        });

    }

    public static void addTemporaryEntry(TemporaryWhitelistEntry entry){
        temporaryEntries.add(entry);
    }

    public static Set<TemporaryWhitelistEntry> getTemporaryEntries() {
        return temporaryEntries;
    }

    public static void removeEntry(TemporaryWhitelistEntry entry){
        temporaryEntries.remove(entry);
    }
}
