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
    public static final ModConfigSpec.ConfigValue<Integer> SERVER_RACK_DATACENTER_BOOST;
    public static final ModConfigSpec.ConfigValue<Boolean> SERVER_RACK_PRODUCES_PARTICLES;

    //Computer
    public static final ModConfigSpec.ConfigValue<Integer> COMPUTER_BASE_CYCLE_GENERATION;
    public static final ModConfigSpec.ConfigValue<Integer> COMPUTER_CYCLES_PER_PROCESSOR;
    public static final ModConfigSpec.ConfigValue<Integer> COMPUTER_FE_PER_CYCLE;
    public static final ModConfigSpec.ConfigValue<Integer> COMPUTER_CYCLE_CAPACITY;
    public static final ModConfigSpec.ConfigValue<Integer> COMPUTER_FE_CAPACITY;
    public static final ModConfigSpec.ConfigValue<Integer> COMPUTER_DATACENTER_BOOST;

    //Data center
    public static final ModConfigSpec.ConfigValue<Integer> DATACENTER_MIN_MULTIBLOCK_SIZE;
    public static final ModConfigSpec.ConfigValue<Integer> DATACENTER_MAX_MULTIBLOCK_SIZE;
    public static final ModConfigSpec.ConfigValue<Boolean> DATACENTER_PRODUCES_SOUND;
    public static final ModConfigSpec.ConfigValue<Boolean> DATACENTER_PRODUCES_PARTICLES;

    //Compiler
    public static final ModConfigSpec.ConfigValue<Integer> COMPILER_CYCLES_PROCESSED_PER_TICK;
    public static final ModConfigSpec.ConfigValue<Integer> COMPILER_MAX_PROGRESS;
    public static final ModConfigSpec.ConfigValue<Integer> COMPILER_MAX_TICK_DURATION;

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

        SERVER_RACK_DATACENTER_BOOST = BUILDER
                .comment(" How much of a boost in cycle production the server rack gets from being in datacenter")
                .defineInRange("datacenter_boost", 500, 1, Integer.MAX_VALUE);

        SERVER_RACK_PRODUCES_PARTICLES = BUILDER
                .comment(" If the server rack produces particles when it's generating cycles")
                .define("produce_particles", true);

        BUILDER.pop();
        //</editor-fold>

        //<editor-fold desc="Computer">
        BUILDER.push("Computer");

        COMPUTER_BASE_CYCLE_GENERATION = BUILDER
                .comment(" How many cycles are generated per tick")
                .defineInRange("base_cycle_generation", 20, 1, 1_000_000_000);

        COMPUTER_CYCLES_PER_PROCESSOR = BUILDER
                .comment(" How many cycles are generated per cpu in the computer")
                .defineInRange("base_cycle_generation", 10, 1, 1_000_000_000);

        COMPUTER_FE_PER_CYCLE = BUILDER
                .comment(" How much FE is consumed to generate a cycle")
                .defineInRange("fe_per_cycle", 2, 1, Integer.MAX_VALUE);

        COMPUTER_CYCLE_CAPACITY = BUILDER
                .comment(" How many cycles can be stored in the computer")
                .defineInRange("cycle_capacity", 500_000, 1, 1_000_000_000);

        COMPUTER_FE_CAPACITY = BUILDER
                .comment(" How much FE can be stored in the computer")
                .defineInRange("fe_capacity", 100_000, 1, 1_000_000);

        COMPUTER_DATACENTER_BOOST = BUILDER
                .comment(" How much of a boost in cycle production the computer gets from being in datacenter")
                .defineInRange("datacenter_boost", 100, 1, Integer.MAX_VALUE);

        BUILDER.pop();
        //</editor-fold>

        //<editor-fold desc="Compiler">
        BUILDER.push("Compiler");
        COMPILER_CYCLES_PROCESSED_PER_TICK = BUILDER
                .comment(" How many cycles per tick are processed. 50 cycles per tick = 1000 cycles per second.")
                .defineInRange("cycles_processed_per_tick", 50, 20, 1000);

        COMPILER_MAX_PROGRESS = BUILDER
                .comment(" Max progress")
                .defineInRange("max_progress", 20, 20, 1000);

        COMPILER_MAX_TICK_DURATION = BUILDER
                .comment(" Cap the most expensive items to this value in ticks")
                .defineInRange("max_duration_cap", 600, 20, Integer.MAX_VALUE);
        BUILDER.pop();
        //</editor-fold>

        BUILDER.pop();
        //</editor-fold>

        //<editor-fold desc="Datacenter">
        BUILDER.push("Data center");

        DATACENTER_MIN_MULTIBLOCK_SIZE = BUILDER
                .comment(" Min dimensions for datacenter multiblock structure (ex: value: 5 is 5x5x5)")
                .defineInRange("min_datacenter_size", 7, 1, 64);

        DATACENTER_MAX_MULTIBLOCK_SIZE = BUILDER
                .comment(" Max dimensions for datacenter multiblock structure (ex: value: 5 is 5x5x5)")
                .defineInRange("max_datacenter_size", 32, 1, 64);

        DATACENTER_PRODUCES_SOUND = BUILDER
                .comment(" If the datacenter produces ambient sound when it's formed")
                .define("produce_sound", false);

        DATACENTER_PRODUCES_PARTICLES = BUILDER
                .comment(" If the datacenter produces particles when it's formed")
                .define("produce_particles", true);

        BUILDER.pop();
        //</editor-fold>

        SPEC = BUILDER.build();
    }
}
