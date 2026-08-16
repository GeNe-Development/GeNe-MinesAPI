package dev.gene.genemines.api;

/**
 * Why GeNe-Mines would deny a player entry to a mine.
 *
 * <p>These are the reasons the plugin's own rules can produce. An unknown mine is
 * never reported here — no access check is run for one.</p>
 */
public enum AccessDenialReason {

    /** The player is allowed in — nothing was denied. */
    NONE,

    /** The player lacks the mine's required permission. */
    PERMISSION,

    /** The player's EcoSkills level is below the mine's requirement. */
    SKILL_LEVEL
}
