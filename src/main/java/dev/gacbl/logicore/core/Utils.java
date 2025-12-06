package dev.gacbl.logicore.core;

public class Utils {
    public static String formatValues(long value) {
        if (value >= 1_000_000_000_000L) {
            return String.format("%.1fT", value / 1_000_000_000_000.0);
        } else if (value >= 1_000_000_000L) {
            return String.format("%.1fB", value / 1_000_000_000.0);
        } else if (value >= 1_000_000L) {
            return String.format("%.1fM", value / 1_000_000.0);
        } else if (value >= 1_000L) {
            return String.format("%.1fK", value / 1_000.0);
        }
        return String.valueOf(value);
    }
}
