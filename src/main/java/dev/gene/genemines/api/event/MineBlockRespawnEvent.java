package dev.gene.genemines.api.event;

import dev.gene.genemines.api.MineStage;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired on the main thread right after a block advanced a step in the respawn
 * chain — bedrock became filler, or filler became ore. The block already has its
 * new type when the event is fired.
 *
 * <p>Useful for statistics, particle or sound effects, and live mine displays.</p>
 *
 * <p><b>Not cancellable, by design.</b> The plugin's respawn bookkeeping (the
 * tracked-block map and the time-ordered queue) has already advanced by the time
 * the world is touched; letting a listener veto the block change would leave the
 * world and that bookkeeping permanently out of sync — the block would be stuck
 * with no scheduled respawn. To keep players out of a mine, use
 * {@link MineAccessCheckEvent}; to stop a break, cancel
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
     * Constructed by GeNe-Mines — consumers only receive this event.
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

    /** The mine's id. */
    public String getMineId() {
        return mineId;
    }

    /** The block that just changed; it already holds its new type. */
    public Block getBlock() {
        return block;
    }

    /** The stage the block is now in. */
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
