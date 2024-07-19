package xyz.nikitacartes.easywhitelist.utils;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.WhitelistEntry;
import xyz.nikitacartes.easywhitelist.EasyWhitelist;

public class TemporaryWhitelistEntry extends WhitelistEntry {
    private final long expireTimestamp;
    public TemporaryWhitelistEntry(GameProfile profile, long expireTimestamp) {
        super(profile);
        this.expireTimestamp = expireTimestamp;
        EasyWhitelist.addTemporaryEntry(this);
    }

    public TemporaryWhitelistEntry(JsonObject json, long expireTimestamp) {
        super(WhitelistEntry.profileFromJson(json));
        this.expireTimestamp = expireTimestamp;
        EasyWhitelist.addTemporaryEntry(this);
    }

    public GameProfile getProfile(){
        return this.getKey();
    }

    public long getExpireTimestamp() {
        return this.expireTimestamp;
    }

    @Override
    protected void write(JsonObject json) {
        super.write(json);
        json.addProperty("expireTimestamp", expireTimestamp);
    }
}
