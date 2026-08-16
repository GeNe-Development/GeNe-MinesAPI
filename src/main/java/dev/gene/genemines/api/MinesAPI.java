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
 *     getLogger().info("GeNe-Mines is not installed — mine features disabled.");
 *     return;
 * }
 * }</pre>
 *
 * <p><b>Threading:</b> every method must be called from the server main thread —
 * they read live world and plugin state. Calling from an async task is undefined
 * behaviour.</p>
 *
 * <p><b>Stability:</b> this package ({@code dev.gene.genemines.api}) is
 * deliberately NOT relocated. Depend on it with {@code compileOnly} and NEVER
 * shade or relocate it — at runtime the classes are provided by GeNe-Mines
 * itself, and relocating them breaks the ServicesManager lookup. The contract is
 * stable: new methods may be added as {@code default} methods, existing ones are
 * never removed or changed.</p>
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
     * Is this world a mine world?
     *
     * @param world the world to test; {@code null} is allowed and returns {@code false}
     * @return {@code true} if a mine is configured for this world
     */
    boolean isMineWorld(World world);

    /**
     * The mine that governs this location — i.e. the location is inside a mine
     * world AND inside that mine's configured area (if it has one).
     *
     * @param location the location to test; {@code null} yields an empty result
     * @return the mine id, or empty if the location is not inside a mine
     */
    Optional<String> mineIdAt(Location location);

    /**
     * May this player enter the mine, according to the plugin's own rules
     * (permission and/or EcoSkills level)? The result already includes any
     * override applied by a
     * {@link dev.gene.genemines.api.event.MineAccessCheckEvent} listener, so it
     * matches exactly what the plugin itself would decide.
     *
     * @param player the player; {@code null} returns {@code false}
     * @param mineId the mine id; unknown ids return {@code false}
     * @return {@code true} if the player may enter
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
     * How many blocks of this mine are currently waiting in the respawn queue
     * (i.e. are broken and will come back). Useful for statistics and monitoring.
     *
     * @param mineId the mine id
     * @return the number of tracked blocks; {@code 0} for an unknown mine
     */
    int trackedBlockCount(String mineId);

    /**
     * Teleports the player to the mine's configured spawn point.
     *
     * <p>With {@code bypassAccess = false} the normal requirements are checked and
     * the player receives the usual denial message when they fail. Pass
     * {@code true} only when your own plugin has already decided the player may
     * enter (rank, quest, party, …).</p>
     *
     * <p>The teleport itself is asynchronous on Paper; a {@code true} return means
     * the teleport was <i>started</i>, not that it has completed.</p>
     *
     * @param player       the player to move
     * @param mineId       the target mine id
     * @param bypassAccess {@code true} to skip the access check entirely
     * @return {@code false} if the mine is unknown, its world is not loaded, or
     *         the access check denied the player; {@code true} otherwise
     */
    boolean teleport(Player player, String mineId, boolean bypassAccess);
}
