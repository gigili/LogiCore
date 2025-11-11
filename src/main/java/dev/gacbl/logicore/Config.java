package dev.gacbl.logicore;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    //CPU Core
    public static final ModConfigSpec.ConfigValue<Integer> CPU_CORE_BASE_CYCLE_GENERATION;
    public static final ModConfigSpec.ConfigValue<Integer> CPU_CORE_CYCLES_PER_PROCESSOR;
    public static final ModConfigSpec.ConfigValue<Integer> CPU_CORE_FE_PER_CYCLE;
    public static final ModConfigSpec.ConfigValue<Integer> CPU_CORE_MAX_RACKS;
    public static final ModConfigSpec.ConfigValue<Integer> CPU_CORE_SCAN_INTERVAL;
    public static final ModConfigSpec.ConfigValue<Integer> CPU_CORE_CYCLE_CAPACITY;
    public static final ModConfigSpec.ConfigValue<Integer> CPU_CORE_FE_CAPACITY;

    static {
        BUILDER.comment("LogiCore Configuration");

        //<editor-fold desc="Machines">
        BUILDER.push("Machines");

        //<editor-fold desc="CPUCore">
        BUILDER.push("CPUCore");

        CPU_CORE_BASE_CYCLE_GENERATION = BUILDER
                .comment(" How many cycles are generated per tick")
                .defineInRange("base_cycle_generation", 100, 1, 1_000_000_000);

        CPU_CORE_CYCLES_PER_PROCESSOR = BUILDER
                .comment(" How many cycles are generated per cpu in the rack")
                .defineInRange("base_cycle_generation", 50, 1, 1_000_000_000);

        CPU_CORE_FE_PER_CYCLE = BUILDER
                .comment(" How much FE is consumed to generate a cycle")
                .defineInRange("fe_per_cycle", 2, 1, Integer.MAX_VALUE);

        CPU_CORE_MAX_RACKS = BUILDER
                .comment(" How many racks can be connected to a single cpu core")
                .defineInRange("max_racks", 6, 1, 64);

        CPU_CORE_SCAN_INTERVAL = BUILDER
                .comment(" How often does cpu core scan for racks (in ticks)")
                .defineInRange("scan_interval", 20, 1, Integer.MAX_VALUE);

        CPU_CORE_CYCLE_CAPACITY = BUILDER
                .comment(" How many cycles can be stored in the cpu core")
                .defineInRange("cycle_capacity", 1_000_000, 1, 1_000_000_000);

        CPU_CORE_FE_CAPACITY = BUILDER
                .comment(" How much FE can be stored in the cpu core")
                .defineInRange("fe_capacity", 100_000, 1, 1_000_000);

        BUILDER.pop();
        //</editor-fold>

        BUILDER.pop();
        //</editor-fold>

        SPEC = BUILDER.build();
    }
}
