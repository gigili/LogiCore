package dev.gacbl.logicore.api.computation;

public interface ICycleConsumer {
    long getCycleDemand();

    long receiveCycles(long maxReceive, boolean simulate);

    long extractCycles(long maxExtract, boolean simulate);

    long getCyclesStored();
}
