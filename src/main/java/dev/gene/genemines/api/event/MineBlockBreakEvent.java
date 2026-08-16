package dev.gene.genemines.api.event;

import dev.gene.genemines.api.MineStage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Fired when a player breaks a block that is part of a mine's respawn cycle —
 * either an ore or the mine's filler block.
 *
 * <p>This event is fired on the main thread <b>before</b> GeNe-Mines applies any
 * of its own side effects (dropping items, advancing the block chain, replacing
 * the block), so your decision here always wins.</p>
 *
 * <p><b>Drops:</b> {@link #getDrops()} is a live, mutable list — add, remove or
 * replace entries to implement fortune-style bonuses, custom items or multiplied
 * yields. Whatever the list contains when the event returns is exactly what drops
 * (GeNe-Mines suppresses the vanilla drop in that case, so nothing is duplicated).
 * The list starts out as the configured drop of the mine, or the vanilla drop if
 * the mine has none configured.</p>
 *
 * <p><b>Cancelling:</b> a cancelled event stops everything — the block stays put,
 * nothing drops, and the respawn chain is not advanced.</p>
 *
 * <pre>{@code
 * @EventHandler
 * public void onMineBreak(MineBlockBreakEvent event) {
 *     if (event.getStage() != MineStage.ORE) return;
 *     if (event.getPlayer().hasPermission("myplugin.doubleore")) {
 *         for (ItemStack drop : List.copyOf(event.getDrops())) {
 *             event.getDrops().add(drop.clone());
 *         }
 *     }
 * }
 * }</pre>
 */
public class MineBlockBreakEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String mineId;
    private final String mineDisplayName;
    private final Block block;
    private final MineStage stage;
    private final Material material;
    private final List<ItemStack> drops;

    private boolean cancelled;

    /**
     * Constructed by GeNe-Mines — consumers only receive this event.
     *
     * @param player          the player breaking the block
     * @param mineId          the mine's id
     * @param mineDisplayName the mine's display name (MiniMessage format)
     * @param block           the block being broken
     * @param stage           whether an ore or a filler block was broken
     * @param material        the material of the broken block
     * @param drops           the mutable drop list
     */
    public MineBlockBreakEvent(Player player, String mineId, String mineDisplayName,
                               Block block, MineStage stage, Material material,
                               List<ItemStack> drops) {
        this.player = player;
        this.mineId = mineId;
        this.mineDisplayName = mineDisplayName;
        this.block = block;
        this.stage = stage;
        this.material = material;
        this.drops = drops;
    }

    /** The player breaking the block. */
    public Player getPlayer() {
        return player;
    }

    /** The mine's id, as configured by the server owner. */
    public String getMineId() {
        return mineId;
    }

    /** The mine's display name, in MiniMessage format. */
    public String getMineDisplayName() {
        return mineDisplayName;
    }

    /** The block being broken. Its type is still the original one. */
    public Block getBlock() {
        return block;
    }

    /** Whether an ore or the mine's filler block was broken. */
    public MineStage getStage() {
        return stage;
    }

    /** The material of the broken block. */
    public Material getMaterial() {
        return material;
    }

    /**
     * The live, mutable list of items that will drop. Modify it in place;
     * an empty list means nothing drops.
     *
     * @return the mutable drop list; never {@code null}
     */
    public List<ItemStack> getDrops() {
        return drops;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
