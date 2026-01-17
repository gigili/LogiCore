package dev.gacbl.logicore;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    //General
    public static final ModConfigSpec.ConfigValue<Boolean> ALLOW_JEI_DRAG;
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_AE2_INTEGRATION;
    public static final ModConfigSpec.ConfigValue<Boolean> RENDER_MACHINE_INFORMATION_IN_UI;

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

    //Cloud interface
    public static final ModConfigSpec.ConfigValue<Long> CI_MAX_TRANSFER_RATE;

    // Research station
    public static final ModConfigSpec.ConfigValue<Integer> RS_MAX_RESEARCH_PROGRESS;
    public static final ModConfigSpec.ConfigValue<Integer> RS_CYCLES_PROCESSED_PER_TICK;
    public static final ModConfigSpec.ConfigValue<Integer> RS_MAX_TICK_DURATION;

    //Batteries
    public static final ModConfigSpec.ConfigValue<Integer> SMALL_BATTERY_CAPACITY;
    public static final ModConfigSpec.ConfigValue<Integer> SMALL_BATTERY_TRANSFER_RATE;
    public static final ModConfigSpec.ConfigValue<Integer> MEDIUM_BATTERY_CAPACITY;
    public static final ModConfigSpec.ConfigValue<Integer> MEDIUM_BATTERY_TRANSFER_RATE;
    public static final ModConfigSpec.ConfigValue<Integer> LARGE_BATTERY_CAPACITY;
    public static final ModConfigSpec.ConfigValue<Integer> LARGE_BATTERY_TRANSFER_RATE;

    static {
        BUILDER.comment("LogiCore Configuration");

        //<editor-fold desc="General">
        BUILDER.push("General");
        ALLOW_JEI_DRAG = BUILDER
                .comment(" If items can be dragged from JEI/EMI into compiler. Works only for researched items")
                .define("jei_drag", true);
        ENABLE_AE2_INTEGRATION = BUILDER
                .comment(" Should the AE2 integration be allowed or not")
                .define("ae2_integration", true);
        RENDER_MACHINE_INFORMATION_IN_UI = BUILDER
                .comment(" Should machine UIs show information about its process? Useful if you don't have jade/top/waila installed")
                .define("render_machine_information_in_ui", true);
        BUILDER.pop();
        //</editor-fold>

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

        // <editor-fold desc="Cloud interface">
        BUILDER.push("Cloud interface");
        CI_MAX_TRANSFER_RATE = BUILDER
                .comment(" How many cycles per tick are transferred to and from the cloud.")
                .defineInRange("cycles_processed_per_tick", 100_000L, 1L, 1_000_000_000L);
        BUILDER.pop();
        //</editor-fold>

        // <editor-fold desc="Research station">
        BUILDER.push("Research station");
        RS_CYCLES_PROCESSED_PER_TICK = BUILDER
                .comment(" How many cycles per tick are processed. 50 cycles per tick = 1000 cycles per second.")
                .defineInRange("cycles_processed_per_tick", 50, 20, 1000);

        RS_MAX_RESEARCH_PROGRESS = BUILDER
                .comment(" Max progress")
                .defineInRange("research_speed", 20, 20, Integer.MAX_VALUE);

        RS_MAX_TICK_DURATION = BUILDER
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

        //<editor-fold desc="Batteries">
        BUILDER.push("Batteries");
        BUILDER.comment(" Configuration for battery tiers and their properties");

        BUILDER.push("Small");
        SMALL_BATTERY_CAPACITY = BUILDER
                .comment(" Maximum capacity of the battery")
                .defineInRange("small_battery_capacity", 1_000_000, 1, Integer.MAX_VALUE);

        SMALL_BATTERY_TRANSFER_RATE = BUILDER
                .comment(" Maximum I/O rate of the battery")
                .defineInRange("small_battery_transfer_rate", 1_000_000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Medium");
        MEDIUM_BATTERY_CAPACITY = BUILDER
                .comment(" Maximum capacity of the battery")
                .defineInRange("medium_battery_capacity", 10_000_000, 1, Integer.MAX_VALUE);

        MEDIUM_BATTERY_TRANSFER_RATE = BUILDER
                .comment(" Maximum I/O rate of the battery")
                .defineInRange("medium_battery_transfer_rate", 10_000_000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Large");
        LARGE_BATTERY_CAPACITY = BUILDER
                .comment(" Maximum capacity of the battery")
                .defineInRange("large_battery_capacity", 100_000_000, 1, Integer.MAX_VALUE);

        LARGE_BATTERY_TRANSFER_RATE = BUILDER
                .comment(" Maximum I/O rate of the battery")
                .defineInRange("large_battery_transfer_rate", 100_000_000, 1, Integer.MAX_VALUE);

        BUILDER.pop();
        BUILDER.pop();
        //</editor-fold>

        SPEC = BUILDER.build();
    }
}
