package dev.gacbl.logicore.items.processorunit;

import dev.gacbl.logicore.Config;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

public enum ProcessorUnitTier implements StringRepresentable {
    BASIC("basic", Config.BASIC_CPU_RATE),
    ADVANCED("advanced", Config.ADVANCED_CPU_RATE),
    ULTIMATE("ultimate", Config.ULTIMATE_CPU_RATE);

    public final String name;
    public final ModConfigSpec.ConfigValue<Long> cycleRate;

    public static final StringRepresentable.StringRepresentableCodec<ProcessorUnitTier> CODEC = StringRepresentable.fromEnum(ProcessorUnitTier::values);

    ProcessorUnitTier(String name, ModConfigSpec.ConfigValue<Long> cycleRate) {
        this.name = name;
        this.cycleRate = cycleRate;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
