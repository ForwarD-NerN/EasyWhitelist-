package xyz.nikitacartes.easywhitelist.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class EasyWhitelistUtils {
    public static final Object2IntMap<String> TIME_UNITS = new Object2IntOpenHashMap<>();

    static {
        TIME_UNITS.put("y", 630720000);
        TIME_UNITS.put("mo", 51840000);
        TIME_UNITS.put("w", 12096000);
        TIME_UNITS.put("d", 1728000);
        TIME_UNITS.put("h", 72000);
        TIME_UNITS.put("m", 1200);
        TIME_UNITS.put("s", 20);
        TIME_UNITS.put("t", 1);
        TIME_UNITS.put("", 1);
    }
}
