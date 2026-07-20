# Server Setup Guide

Complete server-side setup for LZBR, from a bare Paper server to a playable match. Follow the sections
in order, as later steps depend on earlier ones.

> **Arena map status.** The arena map is not yet finalised. The map supplied so far does not fully meet
> the requirements of a Battle Royale layout, principally the usable play area, spawn distribution, and
> the room needed to place loot chests and mob spawn points at a sensible density. Selecting a map that
> suits the mode properly is taking longer than expected, and we would rather choose well than tune the
> arena around a layout that will not play well.
>
> Sections 1 to 6 and 10 to 12 below are final and can be completed now. Sections 7 to 9 (zone bounds
> and spawn scatter, loot chest placement, mob spawn points) are the map-dependent steps. They are documented in full and
> ready to execute, and require configuration only, not further development, once the final map is
> chosen.

---

## 1. Server foundation

| Item | Requirement |
| --- | --- |
| Server software | **Paper 26.1.2**, which must match the plugin's API version |
| Java | **25** |
| Memory | 2 GB minimum for a test server; scale with arena size and player count |

Install the dependency plugins **before** first launching with LZBR so it hooks them on boot.

## 2. Plugins

**Required**

- **Multiverse-Core 5.x**, as all lobby and arena teleporting goes through the Multiverse API.
  `/lzbr start` will refuse to run if it is not loaded.

**Optional but recommended**

- **FastAsyncWorldEdit (FAWE)**, required for automatic arena resets between rounds. Without it the
  reset feature disables itself and logs a warning; everything else still works.
  ⚠️ FAWE replaces WorldEdit. Do **not** install both, as they conflict.
- **LuckPerms**, to grant `lzbr.admin` to staff without full operator status.

Place `LZBR.jar` in `plugins/` and restart. On first run it generates `plugins/LZBR/config.yml`,
`loot.yml` and `mobs.yml`.

## 3. `server.properties`

These settings prevent problems that are difficult to diagnose later.

| Setting | Value | Why |
| --- | --- | --- |
| `spawn-protection` | `0` | **Critical.** Default spawn protection prevents non-operator players from opening chests or breaking blocks near world spawn. Loot chests placed near spawn will simply not open for players. |
| `difficulty` | `easy` or higher | **Peaceful removes all hostile mobs**, which disables the arena mob system entirely. Multiverse overrides this per world, but set a safe default. |
| `allow-nether` | `false` | Unused by the game mode. |
| `allow-end` | `false` | Unused by the game mode. |
| `view-distance` / `simulation-distance` | Sized to the arena | The zone and mobs need loaded chunks at the arena edges. Balance against server hardware. |

## 4. Creating the worlds

LZBR expects two worlds. The names must match `worlds.lobby` and `worlds.br` in `config.yml`
(defaults: `lobby` and `br`). Either name the worlds to match, or update the config.

```
/mv create lobby normal
/mv create br normal
```

If the worlds already exist as folders (built elsewhere and copied in), import instead:

```
/mv import lobby normal
/mv import br normal
```

Players should land in the **lobby** when they join the server. `/lzbr join` only adds a player to the
queue, it does not teleport them. Set the lobby as the server's spawn world via Multiverse's spawn
settings, or by setting `level-name=lobby` in `server.properties`.

### Importing a copied world folder

If a world folder was copied in from elsewhere and the import fails with
`AccessDeniedException: ./<world>/session.lock`, the server process cannot write to the folder:

1. Stop the server.
2. Delete the stale `session.lock` file inside the world folder. It is regenerated on load.
3. Grant the account running the server full read/write control over the entire world folder.
4. Confirm no other server instance is using that folder.
5. Start the server and re-run the import.

## 5. World settings

Apply per world with `/mv modify set <property> <value>` and vanilla `/gamerule`.

### Lobby world

| Setting | Value | Reason |
| --- | --- | --- |
| `pvp` | `false` | No fighting in the queue area. |
| Game mode | `adventure` | Prevents players damaging the lobby build. |
| `/mv setspawn` | At the gathering point | Where players arrive and return to. |
| `/gamerule doMobSpawning` | `false` | No wandering mobs in the lobby. |

### Arena (BR) world

| Setting | Value | Reason |
| --- | --- | --- |
| `pvp` | **`true`** | Required, as PvP is the core loop. |
| `difficulty` | `easy` or higher | Peaceful disables hostile mobs. |
| `/mv setspawn` | Near the zone centre | Fallback landing spot, used only if no safe scatter location can be found for a player. |
| `/gamerule doMobSpawning` | `false` | Stops *natural* spawns. LZBR spawns its mobs explicitly, so this leaves the plugin in full control of density. |
| `/gamerule mobGriefing` | `false` | Creepers are a default spawn type, and this stops them cratering the arena. |
| `/gamerule naturalRegeneration` | `false` | Standard for Battle Royale; keeps fights decisive. |
| `/gamerule doDaylightCycle` | `false` | Consistent lighting every match. |
| `/gamerule doWeatherCycle` | `false` | Consistent conditions every match. |
| `/gamerule doInsomnia` | `false` | No phantoms. |
| `/gamerule keepInventory` | `false` | Death must drop loot and eliminate. |

> **Spawn distribution.** Players are scattered to random, safety-checked spots around the zone centre
> at match start, kept apart from one another and off rooftops, then frozen briefly while a countdown
> plays. Tune this under `spawn` in `config.yml` (see section 7).

## 6. Permissions

Admin commands check the `lzbr.admin` permission.

- **Operators** have it automatically, so `/op <player>` is enough for testing.
- For non-operator staff, with LuckPerms:
  ```
  /lp user <player> permission set lzbr.admin true
  ```
- Players need no permission for `/lzbr join` and `/lzbr leave`.

---

## 7. Zone configuration *(map-dependent)*

Once the arena is built, align the zone to it in `config.yml`:

| Key | Set to |
| --- | --- |
| `zone.center.x` / `zone.center.z` | The arena's centre, in arena-world coordinates |
| `zone.radius.initial` | Large enough to enclose the whole playable area |
| `zone.radius.final` | The end-game circle size |
| `zone.shrink.delay-seconds` | Grace period before the first shrink |
| `zone.shrink.duration-seconds` | How long the full shrink takes |

The zone border is drawn by the plugin. The vanilla `/worldborder` command is not involved.

Verify with `/lzbr test zone` (creates a small test zone around you) and `/lzbr test zone stop`.

### Player spawn scatter

Players land scattered around the zone centre, so the `spawn` section must be sized to the same arena:

| Key | Set to |
| --- | --- |
| `spawn.radius` | Comfortably inside `zone.radius.initial`, covering the area players should start across |
| `spawn.min-distance` | How far apart landing spots should be; raise for larger arenas |
| `spawn.max-height-above-ground` | Raise if the arena has legitimate terrain taller than 6 blocks; `0` disables the rooftop check |

Landing spots are safety-checked automatically (no lava, void, or landing inside blocks), and the
arena's ground height is sampled each match, so nothing else needs surveying by hand. After landing,
players are frozen for `spawn.freeze.seconds` while a countdown plays.

## 8. Loot chests *(map-dependent)*

1. Place chests throughout the arena. Facing matters, as the plugin records and restores each chest's
   facing on every reset.
2. Look directly at each chest and run `/lzbr loot chest add`.
3. Verify with `/lzbr loot chest list`.
4. Fill them immediately and inspect the result with `/lzbr test loot`.
5. Tune the rarities and loot table in `loot.yml`, then apply with `/lzbr loot reload`.

Chests do not need to be part of the arena schematic, as the plugin re-places any missing chest block at
the start of each match.

## 9. Mob spawn points *(map-dependent)*

1. Stand where mobs should appear and run `/lzbr mobs spawn add`. Repeat around the arena.
2. Verify with `/lzbr mobs spawn list`.
3. Spawn some immediately to check placement with `/lzbr test mob 5`.
4. Tune density in `mobs.yml`. The `max-alive`, `per-tick`, `min-player-distance` and
   `stop-below-zone-radius` keys are the levers that keep mobs supporting PvP rather than competing with
   it. Apply with `/lzbr mobs reload`.

---

## 10. Arena reset schematic

The arena reset pastes a schematic over the arena between rounds. The origin is the part that most
commonly goes wrong, so follow this exactly.

1. With FAWE in the arena world, run `//wand` and select the two opposite corners of the arena region.
2. **Stand on a reference block you will remember** (the arena's origin corner is a good choice), then
   run `//copy`. WorldEdit records the clipboard's origin as *your position at the moment of copying*.
3. Save it: `//schem save arena`
4. Move the file from `plugins/FastAsyncWorldEdit/schematics/arena.schem` to
   **`plugins/LZBR/schematics/arena.schem`**, as the plugin reads from its own folder.
5. In `config.yml`, set `arena.reset.origin.x/y/z` to **the exact block you were standing on in step 2**.
   If the origin does not match the copy position, the arena pastes back offset.
6. Confirm the remaining reset options:
   - `arena.reset.enabled: true`
   - `arena.reset.paste-air: true` fully overwrites the area, clearing anything players built
   - `arena.reset.paste-entities: false`, set to `true` only if the schematic contains item frames,
     armour stands or similar that should be restored
7. Test with `/lzbr arena reset` and confirm the arena returns exactly to its saved state.

## 11. Match settings

In `config.yml`:

| Key | Purpose |
| --- | --- |
| `players.minimum` | Players required to start (default `2`) |
| `players.maximum` | Lobby capacity (default `25`) |
| `game.countdown.seconds` | Pre-game countdown length (in the lobby, before teleport) |
| `spawn.freeze.seconds` | How long players stay frozen after landing, before the match goes live |
| `game.win.return-delay-seconds` | How long the win screen lingers before returning players to the lobby |

All on-screen text uses [MiniMessage](https://docs.advntr.dev/minimessage/format.html) formatting.
`{seconds}` and `{winner}` are substituted where documented in the file's comments.

## 12. First test run

1. Two accounts in the lobby world (minimum player count is `2`).
2. Both run `/lzbr join`.
3. An admin runs `/lzbr start`.
4. Confirm, in order:
   - Countdown appears, then players are teleported into the arena
   - Players land scattered at separate spots, frozen while the get-ready countdown plays
   - Loot chests are stocked
   - Mobs spawn sparsely and do not overwhelm the fight
   - The zone telegraphs, shrinks, and damages players outside it
   - PvP works
5. Play the round out and confirm the win screen, the return to the lobby, and the arena reset.
6. After any configuration change, apply it with `/lzbr reload`.

## Troubleshooting

| Symptom | Cause and fix |
| --- | --- |
| `/lzbr start` says Multiverse is not loaded | Multiverse-Core 5.x missing or failed to enable. Check the startup log. |
| Arena reset does nothing | FAWE not installed, `arena.reset.enabled: false`, or the schematic is missing from `plugins/LZBR/schematics/`. The console log states which. |
| Arena pastes back offset | `arena.reset.origin` does not match the position you stood on when running `//copy`. |
| Players cannot open loot chests | `spawn-protection` is not `0` in `server.properties`. |
| No mobs spawn | Arena world difficulty is `peaceful`, `settings.enabled` is `false` in `mobs.yml`, or no spawn points are registered. |
| Chest or spawn positions not saving | The server process cannot write to `plugins/LZBR/`. Check folder permissions. |
| Console: "BR world '...' is not loaded" | The world name in `worlds.br` does not match the imported world's name. |
