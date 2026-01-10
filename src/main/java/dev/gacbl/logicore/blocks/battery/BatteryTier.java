package dev.gacbl.logicore.blocks.battery;

import dev.gacbl.logicore.Config;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

public enum BatteryTier implements StringRepresentable {
    SMALL("small", Config.SMALL_BATTERY_CAPACITY, Config.SMALL_BATTERY_TRANSFER_RATE),
    MEDIUM("medium", Config.MEDIUM_BATTERY_CAPACITY, Config.MEDIUM_BATTERY_TRANSFER_RATE),
    LARGE("large", Config.LARGE_BATTERY_CAPACITY, Config.LARGE_BATTERY_TRANSFER_RATE);

    public static final StringRepresentable.StringRepresentableCodec<BatteryTier> CODEC = StringRepresentable.fromEnum(BatteryTier::values);

    private final String name;
    public final ModConfigSpec.ConfigValue<Integer> capacity;
    public final ModConfigSpec.ConfigValue<Integer> maxTransfer;

    BatteryTier(String name, ModConfigSpec.ConfigValue<Integer> capacity, ModConfigSpec.ConfigValue<Integer> maxTransfer) {
        this.name = name;
        this.capacity = capacity;
        this.maxTransfer = maxTransfer;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
