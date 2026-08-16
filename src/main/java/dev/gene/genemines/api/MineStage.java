package dev.gene.genemines.api;

/**
 * A step of the mine's block cycle, as seen from the outside.
 *
 * <p>The full chain is {@code ORE -> FILLER -> bedrock -> FILLER -> ORE}. Bedrock
 * is missing from this enum on purpose: players cannot break it, so it never
 * turns up in a break event, and a respawn event always reports the block that
 * was just placed.</p>
 */
public enum MineStage {

    /** An ore block (one of the mine's configured ore variants). */
    ORE,

    /** The mine's filler block (the stone between ore and bedrock). */
    FILLER
}
