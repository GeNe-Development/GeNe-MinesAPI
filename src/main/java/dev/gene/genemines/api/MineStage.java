package dev.gene.genemines.api;

/**
 * A step of the mine's block cycle, as seen from the outside.
 *
 * <p>The full chain is {@code ORE → FILLER → bedrock → FILLER → ORE}. The bedrock
 * step is intentionally not part of this enum: bedrock cannot be broken by
 * players, so it never appears in a break event, and a respawn event always
 * reports the block that was just placed.</p>
 */
public enum MineStage {

    /** An ore block (one of the mine's configured ore variants). */
    ORE,

    /** The mine's filler block (the stone between ore and bedrock). */
    FILLER
}
