package dev.gacbl.logicore.client;

public class ClientCycleData {
    private static long playerCycles = 0;

    public static void setCycles(long cycles) {
        playerCycles = cycles;
    }

    public static long getCycles() {
        return playerCycles;
    }
}
