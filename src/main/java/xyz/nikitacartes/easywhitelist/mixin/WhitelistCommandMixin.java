package xyz.nikitacartes.easywhitelist.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.server.Whitelist;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.dedicated.command.WhitelistCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.nikitacartes.easywhitelist.EasyWhitelist;
import xyz.nikitacartes.easywhitelist.utils.TemporaryWhitelistEntry;

@Mixin(WhitelistCommand.class)
public class WhitelistCommandMixin {
    @Redirect(method = "executeRemove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/Whitelist;remove(Lnet/minecraft/server/ServerConfigEntry;)V"))
    private static void easywhitelist$removeEntry(Whitelist whitelist, ServerConfigEntry<GameProfile> serverConfigEntry) {
        WhitelistEntry entry = whitelist.get(serverConfigEntry.getKey());
        whitelist.remove(entry);
        if(entry instanceof TemporaryWhitelistEntry){
            EasyWhitelist.removeEntry((TemporaryWhitelistEntry) entry);
        }

    }
}
