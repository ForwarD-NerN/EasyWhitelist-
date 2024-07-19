package xyz.nikitacartes.easywhitelist;

public interface IWhitelistEntryAccessor {
    void setExpireTimestamp(long timestamp);
    long getExpireTimestamp();
}
