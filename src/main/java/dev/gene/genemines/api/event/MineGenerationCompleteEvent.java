package dev.gene.genemines.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired on the main thread when a long-running mine operation finishes: an admin
 * ore fill ({@code /mines generate}) or a layout restore that runs at startup for
 * a locked mine.
 *
 * <p>Purely informational. Good for logging, notifications, or refreshing your
 * own caches after a mine's contents changed wholesale.</p>
 *
 * <pre>{@code
 * @EventHandler
 * public void onGenerated(MineGenerationCompleteEvent event) {
 *     getLogger().info(event.getMineId() + ": " + event.getBlocksPlaced()
 *             + " blocks in " + event.getDurationMillis() + " ms");
 * }
 * }</pre>
 */
public class MineGenerationCompleteEvent extends Event {

    /** Which long-running operation finished. */
    public enum Type {

        /** An admin ore fill ({@code /mines generate <mine>}). */
        GENERATE,

        /** A locked mine's saved layout was restored (runs at startup). */
        HEAL
    }

    private static final HandlerList HANDLERS = new HandlerList();

    private final String mineId;
    private final Type type;
    private final int blocksPlaced;
    private final long durationMillis;

    /**
     * @param mineId         the mine's id
     * @param type           which operation finished
     * @param blocksPlaced   how many blocks were placed (ores for {@code GENERATE},
     *                       restored blocks for {@code HEAL})
     * @param durationMillis how long the operation took, in milliseconds
     */
    public MineGenerationCompleteEvent(String mineId, Type type, int blocksPlaced, long durationMillis) {
        this.mineId = mineId;
        this.type = type;
        this.blocksPlaced = blocksPlaced;
        this.durationMillis = durationMillis;
    }

    public String getMineId() {
        return mineId;
    }

    public Type getType() {
        return type;
    }

    /** How many blocks were placed: ores for {@code GENERATE}, restored blocks for {@code HEAL}. */
    public int getBlocksPlaced() {
        return blocksPlaced;
    }

    /** How long the operation took, in milliseconds. */
    public long getDurationMillis() {
        return durationMillis;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
