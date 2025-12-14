package dev.gacbl.logicore.core;

public class Utils {
    public static String formatValues(long value) {
        if (value >= 1_000_000_000_000L) {
            return String.format("%.2fT", value / 1_000_000_000_000.0);
        } else if (value >= 1_000_000_000L) {
            return String.format("%.2fB", value / 1_000_000_000.0);
        } else if (value >= 1_000_000L) {
            return String.format("%.2fM", value / 1_000_000.0);
        } else if (value >= 1_000L) {
            return String.format("%.2fK", value / 1_000.0);
        }
        return String.valueOf(value);
    }
}
