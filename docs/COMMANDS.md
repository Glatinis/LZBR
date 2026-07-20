# Command Reference

All functionality is under a single root command.

- **Root:** `/lzbr`
- **Alias:** `/lz` (for example `/lz start`)
- **Admin permission:** `lzbr.admin`, held automatically by server operators. Grant it to non-operator
  staff with LuckPerms: `/lp user <player> permission set lzbr.admin true`.

Commands are registered through Paper's Brigadier API, so arguments and subcommands tab-complete in
game, and commands you lack permission for are hidden.

---

## Command tree

```
/lzbr
├── join
├── leave
├── start
├── end
├── arena
│   └── reset
├── loot
│   ├── chest
│   │   ├── add
│   │   ├── remove
│   │   └── list
│   └── reload
├── mobs
│   ├── spawn
│   │   ├── add
│   │   ├── remove
│   │   └── list
│   └── reload
├── reload
└── test
    ├── zone
    │   └── stop
    ├── loot
    └── mob [count]
```

---

## Player commands

No permission required.

### `/lzbr join`

Adds you to the lobby queue. Players only. It does not teleport you, so you must already be in the
lobby world.

| Outcome | Message |
| --- | --- |
| Success | *Joining lobby...* |
| Already queued | *You are already in the lobby.* |
| Match in progress | *The game has already started.* |
| Queue full | *The lobby is full.* (see `players.maximum`) |

### `/lzbr leave`

Removes you from the lobby queue. Not permitted once a match has started.

| Outcome | Message |
| --- | --- |
| Success | *You have left the lobby queue.* |
| Not queued | *You are not in the lobby.* |
| Match in progress | *You cannot leave the queue once the game has started.* |

---

## Match control

Requires `lzbr.admin`.

### `/lzbr start`

Begins the pre-game countdown, then teleports the queued players into the arena and starts the match.

Requires Multiverse-Core to be loaded and at least `players.minimum` players queued.

| Outcome | Message |
| --- | --- |
| Success | *Battle Royale starting...* |
| Multiverse missing | *Multiverse-Core is not loaded... Cannot start the game without it.* |
| Already running | *The Battle Royale is already in progress.* |
| Too few players | *There are not enough players to start the match.* |

### `/lzbr end`

Force-ends the current match with no winner, then runs the normal end-of-match flow: win screen, return
to lobby, arena reset.

| Outcome | Message |
| --- | --- |
| Success | *Match ended, resetting the arena for the next round...* |
| No match running | *There is no match in progress.* |

---

## Arena

### `/lzbr arena reset`

Immediately pastes the configured arena schematic. Runs asynchronously, and the result is reported when
it completes.

Requires FastAsyncWorldEdit, `arena.reset.enabled: true`, and the schematic present in
`plugins/LZBR/schematics/`. On failure the console log states which of these is missing.

---

## Loot management

Requires `lzbr.admin`. See [CONFIGURATION.md](CONFIGURATION.md#lootyml) for the loot table itself.

### `/lzbr loot chest add`

Registers the chest you are looking at (within 6 blocks) as a loot chest. Its facing is recorded and
restored on every reset. Saved to `loot.yml` immediately.

| Outcome | Message |
| --- | --- |
| Success | *Loot chest added and saved.* |
| Already registered | *That chest is already a loot chest.* |
| Not looking at a chest | *Look at a chest within range to add it as a loot chest.* |

### `/lzbr loot chest remove`

Unregisters the chest you are looking at. The block itself is left in place.

### `/lzbr loot chest list`

Lists every registered chest with its world, coordinates and facing.

### `/lzbr loot reload`

Reloads `loot.yml` only, covering rarities, loot table and chest positions. Reports the resulting item
and chest counts.

---

## Mob management

Requires `lzbr.admin`. See [CONFIGURATION.md](CONFIGURATION.md#mobsyml) for density and drop settings.

### `/lzbr mobs spawn add`

Registers a mob spawn point at your current position. Saved to `mobs.yml` immediately. Mobs appear
within `spawning.spread-radius` blocks of the point.

### `/lzbr mobs spawn remove`

Removes the nearest spawn point within 4 blocks of you, and reports its coordinates.

| Outcome | Message |
| --- | --- |
| Success | *Removed the mob spawn point at X, Y, Z.* |
| None nearby | *No mob spawn point within 4 blocks of you.* |

### `/lzbr mobs spawn list`

Lists every registered spawn point with its world and coordinates.

### `/lzbr mobs reload`

Reloads `mobs.yml` only. Reports the resulting mob type and spawn point counts. If a match is currently
spawning mobs, the spawn task restarts with the new settings.

---

## Configuration

### `/lzbr reload`

Reloads **all three** configuration files (`config.yml`, `loot.yml` and `mobs.yml`) and reports the loot
chest and mob spawn point counts.

> Zone and match values are read as they are used, so a reload applies cleanly from the next match
> rather than altering a round already in progress. Reload between rounds for predictable results.

---

## Testing tools

Requires `lzbr.admin`. These exist to validate configuration without running a full match.

### `/lzbr test zone`

Creates a small shrinking zone centred on you (15 to 3 block radius, shrinking after 5 seconds over 15
seconds). Players only.

| Outcome | Message |
| --- | --- |
| Success | *Test zone created around you, it will start shrinking in 5 seconds.* |
| Zone already active | *A zone is already active. Stop it first with /lzbr test zone stop.* |

### `/lzbr test zone stop`

Stops the active zone and clears the border for all players.

### `/lzbr test loot`

Places, empties and re-fills every registered loot chest immediately, using the same routine that runs
at match start. Use it to inspect loot table changes without starting a round.

| Outcome | Message |
| --- | --- |
| Success | *Filled N loot chest(s) with fresh loot.* |
| Nothing filled | *No loot chests were filled, none are configured, or their world isn't loaded.* |

### `/lzbr test mob [count]`

Spawns mobs immediately at the registered spawn points. `count` defaults to `3`, and accepts `1` to `50`.

Unlike live spawning, this ignores the player-distance and zone gates so an admin standing on a spawn
point still sees a result. The `max-alive` cap is still enforced.

| Outcome | Message |
| --- | --- |
| Success | *Spawned N mob(s) at the configured spawn points.* |
| Nothing spawned | *No mobs spawned, check spawn points, mob types, the alive cap, and that mobs are enabled in mobs.yml.* |
