package dev.gene.genemines.api;

import java.util.Optional;

/**
 * Read-only snapshot of a mine's configuration. Values reflect the state at the
 * moment {@link MinesAPI#mineInfo(String)} was called; re-query after a
 * {@code /mines reload} or a lock change.
 *
 * @param id                 the mine id (also the {@code /mines <id>} shortcut)
 * @param displayName        the display name, in MiniMessage format
 * @param worldName          the name of the mine's world
 * @param mode               how the mine gets its ores
 * @param locked             whether the ore layout has been locked in by an admin
 * @param requiredPermission the permission needed to enter, or {@code null} if none
 * @param requiredSkill      the EcoSkills skill id needed to enter, or {@code null} if none
 * @param requiredSkillLevel the minimum skill level, or {@code 0} if no skill is required
 */
public record MineInfo(String id,
                       String displayName,
                       String worldName,
                       MineMode mode,
                       boolean locked,
                       String requiredPermission,
                       String requiredSkill,
                       int requiredSkillLevel) {

    /** The required permission, if the mine has one. */
    public Optional<String> permission() {
        return Optional.ofNullable(requiredPermission);
    }

    /** The required EcoSkills skill id, if the mine has one. */
    public Optional<String> skill() {
        return Optional.ofNullable(requiredSkill);
    }

    /** Can anyone enter this mine (no permission and no skill requirement)? */
    public boolean open() {
        return requiredPermission == null && requiredSkill == null;
    }
}
