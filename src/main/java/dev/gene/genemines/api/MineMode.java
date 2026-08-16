package dev.gene.genemines.api;

/** How a mine gets its ores. */
public enum MineMode {

    /** The ores are already on the map; the plugin only watches them. */
    EXISTING,

    /** The plugin fills the mine with ores on the {@code generate} admin command. */
    GENERATE
}
