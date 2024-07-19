package xyz.nikitacartes.easywhitelist.mixin;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.server.Whitelist;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.nikitacartes.easywhitelist.EasyWhitelist;
import xyz.nikitacartes.easywhitelist.utils.TemporaryWhitelistEntry;

@Mixin(Whitelist.class)
public class WhitelistMixin {

    @Inject(method = "toString(Lcom/mojang/authlib/GameProfile;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    protected void toString(GameProfile gameProfile, CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(gameProfile.getName());
    }

    @Inject(method = "fromJson", at = @At("HEAD"), cancellable = true)
    private void easywhitelist$createTemporaryEntry(JsonObject json, CallbackInfoReturnable<ServerConfigEntry<GameProfile>> cir) {
        if(json.has("expireTimestamp")) {
            try{
                TemporaryWhitelistEntry entry = new TemporaryWhitelistEntry(json, json.get("expireTimestamp").getAsLong());
                cir.setReturnValue(entry);
            }catch (Exception e){
                EasyWhitelist.LOGGER.error("[EasyWhitelist] Error occurred while parsing temporary whitelist data.", e);
            }

        }
    }

}
