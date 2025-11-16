package dev.gacbl.logicore.api.computation;

public interface ICycleProvider {
    long getCyclesAvailable();

    long getCycleCapacity();

    long extractCycles(long maxExtract, boolean simulate);

    long receiveCycles(long receive, boolean simulate);
}
