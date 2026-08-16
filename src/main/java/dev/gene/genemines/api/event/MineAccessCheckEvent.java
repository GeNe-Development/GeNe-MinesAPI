package dev.gene.genemines.api.event;

import dev.gene.genemines.api.AccessDenialReason;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired whenever GeNe-Mines decides whether a player may enter a mine — from the
 * GUI picker, the teleport command, the text list and
 * {@link dev.gene.genemines.api.MinesAPI#canAccess}. The plugin uses the final
 * (possibly overridden) decision everywhere, so the GUI, the list and the actual
 * teleport always agree with each other.
 *
 * <p>Fired on the main thread. It carries the plugin's own verdict; override it
 * with {@link #setAllowed(boolean)} to implement your own rule (rank, quest,
 * party, playtime, …) instead of — or on top of — the built-in permission and
 * EcoSkills checks.</p>
 *
 * <p>When you deny access, set a message with {@link #setDenialMessage(String)};
 * leave it {@code null} to keep the plugin's own message for
 * {@link #getReason()}. The message is sent in MiniMessage format.</p>
 *
 * <p>This event is not cancellable by design: "cancelled" has no meaning for a
 * yes/no question. Use {@link #setAllowed(boolean)}.</p>
 *
 * <pre>{@code
 * @EventHandler
 * public void onAccessCheck(MineAccessCheckEvent event) {
 *     if (!event.getMineId().equals("iron")) return;
 *     if (myRankPlugin.hasRank(event.getPlayer(), "miner")) {
 *         event.setAllowed(true);            // rank replaces the level requirement
 *     } else {
 *         event.setAllowed(false);
 *         event.setDenialMessage("<red>The Iron Mine is for Miner rank and above!");
 *     }
 * }
 * }</pre>
 */
public class MineAccessCheckEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String mineId;
    private final AccessDenialReason reason;
    private final String requiredPermission;
    private final String requiredSkill;
    private final int requiredLevel;
    private final int currentLevel;

    private boolean allowed;
    private String denialMessage;

    /**
     * Constructed by GeNe-Mines — consumers only receive this event.
     *
     * @param player             the player being checked
     * @param mineId             the mine's id
     * @param allowed            the plugin's own verdict
     * @param reason             why the plugin would deny entry ({@code NONE} if allowed)
     * @param requiredPermission the permission the mine requires, or {@code null}
     * @param requiredSkill      the EcoSkills skill id the mine requires, or {@code null}
     * @param requiredLevel      the required skill level, or {@code 0}
     * @param currentLevel       the player's current skill level, or {@code -1} if unknown
     */
    public MineAccessCheckEvent(Player player, String mineId, boolean allowed,
                                AccessDenialReason reason, String requiredPermission,
                                String requiredSkill, int requiredLevel, int currentLevel) {
        this.player = player;
        this.mineId = mineId;
        this.allowed = allowed;
        this.reason = reason;
        this.requiredPermission = requiredPermission;
        this.requiredSkill = requiredSkill;
        this.requiredLevel = requiredLevel;
        this.currentLevel = currentLevel;
    }

    /** The player trying to enter. */
    public Player getPlayer() {
        return player;
    }

    /** The mine's id. */
    public String getMineId() {
        return mineId;
    }

    /** The current verdict — the plugin's own, or yours if you already changed it. */
    public boolean isAllowed() {
        return allowed;
    }

    /**
     * Overrides the verdict. GeNe-Mines uses the value left here when the event
     * returns.
     *
     * @param allowed {@code true} to let the player in, {@code false} to deny
     */
    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    /**
     * Why the <b>plugin</b> would deny entry. This always describes the built-in
     * decision and does not change when you call {@link #setAllowed(boolean)}.
     */
    public AccessDenialReason getReason() {
        return reason;
    }

    /** The permission the mine requires, or {@code null} if it requires none. */
    public String getRequiredPermission() {
        return requiredPermission;
    }

    /** The EcoSkills skill id the mine requires, or {@code null} if it requires none. */
    public String getRequiredSkill() {
        return requiredSkill;
    }

    /** The skill level the mine requires, or {@code 0} if no skill is required. */
    public int getRequiredLevel() {
        return requiredLevel;
    }

    /**
     * The player's current level in the required skill, or {@code -1} when it is
     * unknown (no skill requirement, or EcoSkills is not installed).
     */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /** The denial message you set, or {@code null} to use the plugin's own. */
    public String getDenialMessage() {
        return denialMessage;
    }

    /**
     * Sets the message sent to the player when access is denied. Only used when
     * the final verdict is "denied".
     *
     * @param denialMessage a MiniMessage string, or {@code null} for the plugin's default
     */
    public void setDenialMessage(String denialMessage) {
        this.denialMessage = denialMessage;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
