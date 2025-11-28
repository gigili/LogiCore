package dev.gacbl.logicore.core;

public class Utils {
    public static String formatValues(long energy) {
        if (energy >= 1_000_000) {
            return String.format("%.1fM", energy / 1_000_000.0);
        } else if (energy >= 1_000) {
            return String.format("%.1fK", energy / 1_000.0);
        }
        return String.valueOf(energy);
    }
}
