# GeNe-Mines API

Developer API for the [GeNe-Mines](https://genedev.hu/) Paper plugin - hook your own
plugin into the mines: change drops, override who may enter, react to respawns.

The API itself is MIT licensed (see [LICENSE](LICENSE)); the GeNe-Mines plugin is
commercial software sold separately. You only need the plugin on the server at
runtime - building against this API is free.

---

## Adding the dependency

```kotlin
repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.genedev.hu/releases")
}

dependencies {
    // compileOnly - GeNe-Mines provides these classes at runtime.
    compileOnly("dev.gene:genemines-api:1.0.0")
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
}
```

Maven:

```xml
<repository>
  <id>genedev</id>
  <url>https://maven.genedev.hu/releases</url>
</repository>

<dependency>
  <groupId>dev.gene</groupId>
  <artifactId>genemines-api</artifactId>
  <version>1.0.0</version>
  <scope>provided</scope>
</dependency>
```

> ### ⚠️ Never shade or relocate this API
>
> Use `compileOnly` / `provided` - **never** `implementation`, and **never** run it
> through shadow's `relocate`. At runtime the classes come from GeNe-Mines itself.
> If you relocate them, your `dev.yourplugin.libs.genemines.api.MinesAPI` is a
> *different class* from the one GeNe-Mines registered, the ServicesManager lookup
> returns `null`, and your event listeners are never called.

And declare the dependency in your `plugin.yml`:

```yaml
softdepend: [ GeNe-Mines ]   # or depend: [ GeNe-Mines ] if your plugin is useless without it
```

---

## Getting the API - with graceful fallback

`softdepend` means your plugin also loads when GeNe-Mines is absent, so always
null-check the lookup:

```java
package com.example.mineaddon;

import dev.gene.genemines.api.MinesAPI;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineAddon extends JavaPlugin {

    private MinesAPI mines;   // null when GeNe-Mines is not installed

    @Override
    public void onEnable() {
        mines = getServer().getServicesManager().load(MinesAPI.class);
        if (mines == null) {
            getLogger().info("GeNe-Mines not found - mine integration disabled.");
            return;
        }
        // your own listeners - see the examples below
        getServer().getPluginManager().registerEvents(new FortuneListener(), this);
        getServer().getPluginManager().registerEvents(new MiningQuestListener(mines), this);
        getLogger().info("Hooked into GeNe-Mines: " + mines.mineIds());
    }

    public MinesAPI mines() {
        return mines;
    }
}
```

Every `MinesAPI` method and every event runs on the server main thread, so don't
call the API from an async task.

---

## Events

All events live in `dev.gene.genemines.api.event` and are ordinary Bukkit events.

| Event | Cancellable | Fired when |
|---|---|---|
| `MineBlockBreakEvent` | ✅ | A player breaks an ore or filler block in a mine - **before** the plugin applies any side effect |
| `MineAccessCheckEvent` | ❌ (use `setAllowed`) | GeNe-Mines decides whether a player may enter a mine |
| `MineBlockRespawnEvent` | ❌ | A block advanced a step in the respawn chain (bedrock->filler, filler->ore) |
| `MineGenerationCompleteEvent` | ❌ | A `/mines generate` fill or a startup layout restore finished |

### `MineBlockBreakEvent`

Fires before the drop is created and before the block chain advances, so both your
cancel and your drop edits take effect.

- `getPlayer()`, `getMineId()`, `getMineDisplayName()`, `getBlock()`, `getMaterial()`
- `getStage()` - `MineStage.ORE` or `MineStage.FILLER`
- `getDrops()` - **live, mutable list**. It starts as the mine's configured drop, or
  the vanilla drop if none is configured. Whatever it contains when the event
  returns is exactly what drops (the vanilla drop is suppressed, so nothing
  duplicates). An empty list means nothing drops.
- `setCancelled(true)` - nothing happens at all: the block stays, no drops, no chain.

Drops created this way still belong to the breaking player for the configured
ownership window, exactly like normal mine drops.

### `MineAccessCheckEvent`

Fires from the GUI picker, the text list, the teleport command and
`MinesAPI.canAccess` - one central decision, so the menu and the actual teleport can
never disagree.

- `isAllowed()` / `setAllowed(boolean)` - the verdict; yours wins
- `getReason()` - why the *plugin* would deny (`PERMISSION`, `SKILL_LEVEL`, or `NONE`)
- `getRequiredPermission()`, `getRequiredSkill()`, `getRequiredLevel()`, `getCurrentLevel()`
- `setDenialMessage(String)` - MiniMessage text sent when the final verdict is "denied";
  leave it unset to keep the plugin's own message

### `MineBlockRespawnEvent`

- `getMineId()`, `getBlock()` (already holds its new type), `getNewStage()`

Not cancellable by design: the plugin's respawn bookkeeping has already advanced when
the world is touched, so a veto would leave a block permanently stuck with no
scheduled respawn. Not fired for the bulk state restore that runs at server start.

### `MineGenerationCompleteEvent`

- `getMineId()`, `getType()` (`GENERATE` or `HEAL`), `getBlocksPlaced()`, `getDurationMillis()`

---

## `MinesAPI` methods

| Method | Description |
|---|---|
| `Collection<String> mineIds()` | Ids of all loaded mines |
| `boolean isMineWorld(World world)` | Is this world a mine world? |
| `Optional<String> mineIdAt(Location loc)` | The mine governing this location (inside its area), if any |
| `boolean canAccess(Player p, String mineId)` | The plugin's verdict, including event overrides |
| `Optional<MineInfo> mineInfo(String mineId)` | Read-only config snapshot |
| `int trackedBlockCount(String mineId)` | How many blocks are currently waiting to respawn |
| `boolean teleport(Player p, String mineId, boolean bypassAccess)` | Send a player to the mine spawn |

`MineInfo` carries: `id()`, `displayName()`, `worldName()`, `mode()`
(`EXISTING`/`GENERATE`), `locked()`, `permission()`, `skill()`, `requiredSkillLevel()`,
plus the convenience `open()`.

Deliberately **not** offered: creating or deleting mines at runtime, writing the ore
layout, or manipulating the respawn queue - those would make the plugin's internal
state fragile. Mines are configured by the server owner in `config.yml`.

---

## Example 1 - Fortune-style drop bonus

Give players with a permission a chance at extra ore, and always add a rare gem.

```java
package com.example.mineaddon;

import dev.gene.genemines.api.MineStage;
import dev.gene.genemines.api.event.MineBlockBreakEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FortuneListener implements Listener {

    @EventHandler
    public void onMineBreak(MineBlockBreakEvent event) {
        if (event.getStage() != MineStage.ORE) {
            return;   // only ores get the bonus, not plain stone
        }
        if (!event.getPlayer().hasPermission("mineaddon.fortune")) {
            return;
        }

        // 25% chance to double everything that would drop
        if (ThreadLocalRandom.current().nextDouble() < 0.25) {
            for (ItemStack drop : List.copyOf(event.getDrops())) {
                event.getDrops().add(drop.clone());
            }
        }

        // 1% chance for a rare bonus item on top
        if (ThreadLocalRandom.current().nextDouble() < 0.01) {
            event.getDrops().add(new ItemStack(Material.DIAMOND));
            event.getPlayer().sendMessage(Component.text("You found a diamond in the rock!")
                    .color(NamedTextColor.AQUA));
        }
    }
}
```

## Example 2 - Your own access rule based on rank

Replace the built-in skill requirement of one mine with a rank check from your own
plugin.

```java
package com.example.mineaddon;

import dev.gene.genemines.api.event.MineAccessCheckEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RankAccessListener implements Listener {

    private final RankService ranks;   // your own plugin's service

    public RankAccessListener(RankService ranks) {
        this.ranks = ranks;
    }

    @EventHandler
    public void onAccessCheck(MineAccessCheckEvent event) {
        if (!event.getMineId().equals("iron")) {
            return;   // every other mine keeps the default rules
        }

        if (ranks.hasRank(event.getPlayer(), "miner")) {
            event.setAllowed(true);            // rank replaces the level requirement
            return;
        }

        event.setAllowed(false);
        event.setDenialMessage("<red>The Iron Mine is for <gold>Miner</gold> rank and above! "
                + "<gray>Buy it at <yellow>/ranks</yellow>.");
    }
}
```

Because the plugin routes every check through this event, the mine also shows as
locked in the `/mines` GUI for players without the rank - no extra work needed.

## Example 3 - Quest progress for mining

Count ores mined per player and complete a quest, using both the break event and the
API for context.

```java
package com.example.mineaddon;

import dev.gene.genemines.api.MineStage;
import dev.gene.genemines.api.MinesAPI;
import dev.gene.genemines.api.event.MineBlockBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MiningQuestListener implements Listener {

    private static final int QUEST_TARGET = 50;
    private static final String QUEST_MINE = "coal";

    private final MinesAPI mines;
    private final Map<UUID, Integer> progress = new HashMap<>();

    public MiningQuestListener(MinesAPI mines) {
        this.mines = mines;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMineBreak(MineBlockBreakEvent event) {
        if (event.getStage() != MineStage.ORE) {
            return;
        }
        if (!event.getMineId().equals(QUEST_MINE)) {
            return;
        }

        Player player = event.getPlayer();
        int mined = progress.merge(player.getUniqueId(), 1, Integer::sum);

        if (mined == QUEST_TARGET) {
            String mineName = mines.mineInfo(QUEST_MINE)
                    .map(info -> info.displayName())
                    .orElse(QUEST_MINE);
            player.sendMessage("§aQuest complete: 50 ores mined in " + mineName + "!");
            progress.remove(player.getUniqueId());

            // reward: let them into the iron mine regardless of level
            mines.teleport(player, "iron", true);
        } else if (mined % 10 == 0) {
            player.sendActionBar(net.kyori.adventure.text.Component.text(
                    "Quest: " + mined + " / " + QUEST_TARGET));
        }
    }
}
```

---

## Versioning

The API follows semantic versioning. New functionality is added as `default` methods
and new event classes, so a plugin compiled against `1.0.0` keeps working with later
`1.x` releases of GeNe-Mines. Breaking changes would only come with a major bump, and
never silently.

Questions or a use case the API doesn't cover? -> https://genedev.hu/
