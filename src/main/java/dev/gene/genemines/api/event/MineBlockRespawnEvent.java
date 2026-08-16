package dev.gene.genemines.api.event;

import dev.gene.genemines.api.MineStage;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired on the main thread right after a block advanced a step in the respawn
 * chain - bedrock became filler, or filler became ore. The block already has its
 * new type when the event is fired.
 *
 * <p>Useful for statistics, particle or sound effects, and live mine displays.</p>
 *
 * <p>Not cancellable, and that is deliberate. By the time the world is touched
 * the respawn bookkeeping (the tracked-block map and the time-ordered queue) has
 * already moved on, so a listener vetoing the block change would leave the two
 * permanently out of sync and strand the block with no scheduled respawn. To keep
 * players out of a mine use {@link MineAccessCheckEvent}; to stop a break, cancel
 * {@link MineBlockBreakEvent}.</p>
 *
 * <pre>{@code
 * @EventHandler
 * public void onRespawn(MineBlockRespawnEvent event) {
 *     if (event.getNewStage() != MineStage.ORE) return;
 *     event.getBlock().getWorld().spawnParticle(
 *             Particle.HAPPY_VILLAGER, event.getBlock().getLocation().add(0.5, 0.5, 0.5), 5);
 * }
 * }</pre>
 */
public class MineBlockRespawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final String mineId;
    private final Block block;
    private final MineStage newStage;

    /**
     * Called by GeNe-Mines once the block already carries its new type.
     *
     * @param mineId   the mine's id
     * @param block    the block that just changed
     * @param newStage the stage the block is now in
     */
    public MineBlockRespawnEvent(String mineId, Block block, MineStage newStage) {
        this.mineId = mineId;
        this.block = block;
        this.newStage = newStage;
    }

    public String getMineId() {
        return mineId;
    }

    /** The block that just changed; it already holds its new type. */
    public Block getBlock() {
        return block;
    }

    public MineStage getNewStage() {
        return newStage;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
