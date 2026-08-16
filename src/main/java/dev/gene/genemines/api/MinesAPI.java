package dev.gene.genemines.api;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;

/**
 * The GeNe-Mines developer API: read-only insight into the mines plus a small,
 * safe set of actions.
 *
 * <p>GeNe-Mines registers this into the Bukkit {@code ServicesManager}; consumers
 * look it up from there:</p>
 *
 * <pre>{@code
 * MinesAPI api = getServer().getServicesManager().load(MinesAPI.class);
 * if (api == null) {
 *     getLogger().info("GeNe-Mines is not installed, mine features disabled.");
 *     return;
 * }
 * }</pre>
 *
 * <p>Call everything from the server main thread. These methods read live world
 * and plugin state, so an async call is undefined behaviour.</p>
 *
 * <p>One packaging note: this package is deliberately left out of the shade
 * relocation. Depend on it with {@code compileOnly} and don't shade or relocate
 * it yourself. At runtime the classes come from GeNe-Mines itself, so relocating
 * them breaks the ServicesManager lookup. Beyond that the contract is stable;
 * new methods arrive as {@code default} ones, and nothing existing is removed or
 * changed.</p>
 *
 * @see dev.gene.genemines.api.event.MineBlockBreakEvent
 * @see dev.gene.genemines.api.event.MineAccessCheckEvent
 */
public interface MinesAPI {

    /**
     * The ids of all currently loaded mines (as configured by the server owner).
     *
     * @return an unmodifiable collection of mine ids; never {@code null}
     */
    Collection<String> mineIds();

    /**
     * Whether a mine is configured for this world.
     *
     * @param world the world to test; {@code null} is allowed and returns {@code false}
     */
    boolean isMineWorld(World world);

    /**
     * The mine that governs this location, meaning the location sits in a mine
     * world and inside that mine's configured area (if it has one).
     *
     * @param location the location to test; {@code null} yields an empty result
     * @return the mine id, or empty if the location is not inside a mine
     */
    Optional<String> mineIdAt(Location location);

    /**
     * May this player enter the mine, going by the plugin's own rules (permission
     * and/or EcoSkills level)? Any override a
     * {@link dev.gene.genemines.api.event.MineAccessCheckEvent} listener applied
     * is already folded in, so the answer matches what the plugin itself would do.
     *
     * @param player the player; {@code null} returns {@code false}
     * @param mineId the mine id; unknown ids return {@code false}
     */
    boolean canAccess(Player player, String mineId);

    /**
     * Read-only information about a mine.
     *
     * @param mineId the mine id
     * @return the info, or empty if no such mine is loaded
     */
    Optional<MineInfo> mineInfo(String mineId);

    /**
     * How many blocks of this mine are broken right now and still waiting in the
     * respawn queue. Handy for statistics and monitoring.
     *
     * @param mineId the mine id
     * @return the number of tracked blocks; {@code 0} for an unknown mine
     */
    int trackedBlockCount(String mineId);

    /**
     * Teleports the player to the mine's configured spawn point.
     *
     * <p>With {@code bypassAccess = false} the normal requirements are checked and
     * the player gets the usual denial message when they fail. Pass {@code true}
     * only when your own plugin has already decided this player may enter.</p>
     *
     * <p>Careful with the return value: the teleport is asynchronous on Paper, so
     * {@code true} means it was <i>started</i>, not that it finished.</p>
     *
     * @param player       the player to move
     * @param mineId       the target mine id
     * @param bypassAccess {@code true} to skip the access check entirely
     * @return {@code false} if the mine is unknown, its world is not loaded, or
     *         the access check denied the player; {@code true} otherwise
     */
    boolean teleport(Player player, String mineId, boolean bypassAccess);
}
