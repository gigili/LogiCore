package dev.gacbl.logicore;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    //Server rack
    public static final ModConfigSpec.ConfigValue<Integer> SERVER_RACK_BASE_CYCLE_GENERATION;
    public static final ModConfigSpec.ConfigValue<Integer> SERVER_RACK_CYCLES_PER_PROCESSOR;
    public static final ModConfigSpec.ConfigValue<Integer> SERVER_RACK_FE_PER_CYCLE;
    public static final ModConfigSpec.ConfigValue<Integer> SERVER_RACK_CYCLE_CAPACITY;
    public static final ModConfigSpec.ConfigValue<Integer> SERVER_RACK_FE_CAPACITY;

    static {
        BUILDER.comment("LogiCore Configuration");

        //<editor-fold desc="Machines">
        BUILDER.push("Machines");

        //<editor-fold desc="ServerRack">
        BUILDER.push("Server rack");

        SERVER_RACK_BASE_CYCLE_GENERATION = BUILDER
                .comment(" How many cycles are generated per tick")
                .defineInRange("base_cycle_generation", 100, 1, 1_000_000_000);

        SERVER_RACK_CYCLES_PER_PROCESSOR = BUILDER
                .comment(" How many cycles are generated per cpu in the rack")
                .defineInRange("base_cycle_generation", 50, 1, 1_000_000_000);

        SERVER_RACK_FE_PER_CYCLE = BUILDER
                .comment(" How much FE is consumed to generate a cycle")
                .defineInRange("fe_per_cycle", 2, 1, Integer.MAX_VALUE);

        SERVER_RACK_CYCLE_CAPACITY = BUILDER
                .comment(" How many cycles can be stored in the server rack")
                .defineInRange("cycle_capacity", 1_000_000, 1, 1_000_000_000);

        SERVER_RACK_FE_CAPACITY = BUILDER
                .comment(" How much FE can be stored in the server rack")
                .defineInRange("fe_capacity", 100_000, 1, 1_000_000);

        BUILDER.pop();
        //</editor-fold>

        BUILDER.pop();
        //</editor-fold>

        SPEC = BUILDER.build();
    }
}
