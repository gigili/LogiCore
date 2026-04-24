package dev.gacbl.logicore.items.processorunit;

import dev.gacbl.logicore.Config;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

public enum ProcessorUnitTier implements StringRepresentable {
    BASIC("basic", Config.BASIC_CPU_RATE, "§6"),
    ADVANCED("advanced", Config.ADVANCED_CPU_RATE, "§2"),
    ULTIMATE("ultimate", Config.ULTIMATE_CPU_RATE, "§5");

    public final String name;
    public final String tooltipPrefix;
    public final ModConfigSpec.ConfigValue<Long> cycleRate;

    public static final StringRepresentable.StringRepresentableCodec<ProcessorUnitTier> CODEC = StringRepresentable.fromEnum(ProcessorUnitTier::values);

    ProcessorUnitTier(String name, ModConfigSpec.ConfigValue<Long> cycleRate, String tooltipPrefix) {
        this.name = name;
        this.cycleRate = cycleRate;
        this.tooltipPrefix = tooltipPrefix;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
