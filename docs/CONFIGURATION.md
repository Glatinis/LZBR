# Configuration Reference

LZBR generates three files in `plugins/LZBR/` on first run. All are hot-reloadable. Apply changes with
`/lzbr reload`, or reload an individual file with `/lzbr loot reload` or `/lzbr mobs reload`.

Text fields marked *MiniMessage* accept [MiniMessage formatting](https://docs.advntr.dev/minimessage/format.html),
for example `<red><bold>TEXT</bold></red>`. Sound keys accept any Minecraft sound id (such as
`minecraft:block.note_block.pling`); an empty string plays nothing.

> Values marked **map-dependent** are placeholders until the final arena map is selected. See
> [SETUP.md](SETUP.md).

---

## `config.yml`

Core game mode settings: players, worlds, the zone, match presentation, and arena reset.

### Players

| Key | Type | Default | Description |
| --- | --- | --- | --- |
| `players.minimum` | int | `2` | Players required before a match can start. |
| `players.maximum` | int | `25` | Lobby queue capacity. |

### Worlds

| Key | Type | Default | Description |
| --- | --- | --- | --- |
| `worlds.lobby` | string | `lobby` | Multiverse name of the lobby world. Must match the actual world name. |
| `worlds.br` | string | `br` | Multiverse name of the arena world. Must match the actual world name. |

### Zone

The shrinking play area, rendered as a genuine client-side border.

| Key | Type | Default | Description |
| --- | --- | --- | --- |
| `zone.center.x` | double | `0` | **Map-dependent.** Zone centre X, in arena-world coordinates. |
| `zone.center.z` | double | `0` | **Map-dependent.** Zone centre Z. |
| `zone.radius.initial` | double | `500` | **Map-dependent.** Starting radius in blocks; should enclose the playable area. |
| `zone.radius.final` | double | `50` | **Map-dependent.** Radius at the end of the shrink. |
| `zone.shrink.delay-seconds` | int | `60` | Grace period after match start before shrinking begins. |
| `zone.shrink.duration-seconds` | int | `300` | Time taken to shrink from initial to final radius. |
| `zone.damage.amount` | double | `1.0` | Damage per interval to players outside the zone (`2.0` is one heart). |
| `zone.damage.interval-ticks` | int | `20` | Ticks between damage applications (20 ticks is 1 second). |
| `zone.warning.distance-blocks` | int | `5` | Distance from the edge at which the client shows its red warning tint. |
| `zone.warning.time-seconds` | int | `10` | Seconds of approaching border that triggers the warning tint. |

#### Shrink telegraph

A centre-screen warning shown shortly before each shrink. `{seconds}` is replaced with the countdown.

| Key | Type | Default | Description |
| --- | --- | --- | --- |
| `zone.shrink.telegraph.enabled` | bool | `true` | Master toggle for the warning. |
| `zone.shrink.telegraph.lead-seconds` | int | `10` | How far ahead of the shrink to warn. Clamped to the shrink delay. |
| `zone.shrink.telegraph.title` | *MiniMessage* | `<red><bold>ZONE CLOSING IN</bold></red>` | Title line. |
| `zone.shrink.telegraph.subtitle` | *MiniMessage* | `<yellow>The zone shrinks in {seconds}s</yellow>` | Subtitle line. |
| `zone.shrink.telegraph.fade-in-ticks` | int | `10` | Title fade-in. |
| `zone.shrink.telegraph.stay-ticks` | int | `40` | Title hold time. |
| `zone.shrink.telegraph.fade-out-ticks` | int | `10` | Title fade-out. |
| `zone.shrink.telegraph.sound.key` | string | `minecraft:block.note_block.pling` | Sound id, or `""` for silence. |
| `zone.shrink.telegraph.sound.volume` | double | `1.0` | Volume. |
| `zone.shrink.telegraph.sound.pitch` | double | `1.0` | Pitch. |

### Player spawn

Where players land when a match starts. Instead of everyone arriving on one point, each player is
scattered to a random spot around the zone centre. Candidate spots are checked for safety (never inside
a block, over lava or the void, or on hazards like magma and cactus), kept a minimum distance from other
players' spots, and rejected if they sit too far above the arena's typical ground height, which keeps
players off rooftops. Ground height is sampled automatically each match, so no manual survey is needed.

| Key | Type | Default | Description |
| --- | --- | --- | --- |
| `spawn.radius` | double | `400` | **Map-dependent.** Players scatter within this many blocks of the zone centre. Keep it comfortably inside `zone.radius.initial`. |
| `spawn.min-distance` | double | `15` | Try to keep landing spots at least this many blocks apart. |
| `spawn.max-attempts` | int | `30` | Tries per player to find a spot that is safe and far enough from others. If none qualifies, the best safe spot found is used instead. |
| `spawn.max-height-above-ground` | int | `6` | Reject spots higher than this above the arena's typical ground height, so players do not land on rooftops. Raise it if the arena has legitimate hills taller than this; `0` disables the check. |

#### Spawn freeze

After landing, players are frozen in place (they can still look around) while a short countdown plays,
so everyone gets their bearings before the match goes live. `{seconds}` is replaced with the number
remaining.

| Key | Type | Default |
| --- | --- | --- |
| `spawn.freeze.seconds` | int | `5` |
| `spawn.freeze.title` | *MiniMessage* | `<green><bold>Get Ready!</bold></green>` |
| `spawn.freeze.subtitle` | *MiniMessage* | `<yellow>Moving in <bold>{seconds}</bold>s</yellow>` |
| `spawn.freeze.fade-in-ticks` | int | `0` |
| `spawn.freeze.stay-ticks` | int | `22` |
| `spawn.freeze.fade-out-ticks` | int | `4` |
| `spawn.freeze.sound.key` | string | `minecraft:block.note_block.hat` |
| `spawn.freeze.sound.volume` | double | `1.0` |
| `spawn.freeze.sound.pitch` | double | `1.0` |

### Match flow

#### Pre-game countdown

Shown in the lobby after an admin starts. Players are teleported when it finishes. `{seconds}` is
replaced with the number remaining.

| Key | Type | Default |
| --- | --- | --- |
| `game.countdown.seconds` | int | `10` |
| `game.countdown.title` | *MiniMessage* | `<yellow>Match starting</yellow>` |
| `game.countdown.subtitle` | *MiniMessage* | `<red><bold>{seconds}</bold></red>` |
| `game.countdown.sound.key` | string | `minecraft:block.note_block.hat` |
| `game.countdown.sound.volume` | double | `1.0` |
| `game.countdown.sound.pitch` | double | `1.0` |

#### Match start banner

Shown the moment the match goes live.

| Key | Type | Default |
| --- | --- | --- |
| `game.start.title` | *MiniMessage* | `<green><bold>GO!</bold></green>` |
| `game.start.subtitle` | *MiniMessage* | `<gray>Last player standing wins</gray>` |
| `game.start.fade-in-ticks` | int | `5` |
| `game.start.stay-ticks` | int | `30` |
| `game.start.fade-out-ticks` | int | `10` |
| `game.start.sound.key` | string | `minecraft:entity.ender_dragon.growl` |
| `game.start.sound.volume` | double | `1.0` |
| `game.start.sound.pitch` | double | `1.0` |

#### Win screen

Shown when the match ends. `{winner}` is replaced with the winner's name.

| Key | Type | Default | Description |
| --- | --- | --- | --- |
| `game.win.title` | *MiniMessage* | `<gold><bold>VICTORY</bold></gold>` | |
| `game.win.subtitle` | *MiniMessage* | `<yellow>{winner} won the match</yellow>` | |
| `game.win.broadcast` | *MiniMessage* | `<gold>{winner}</gold> <yellow>won the Battle Royale!</yellow>` | |
| `game.win.no-winner-broadcast` | *MiniMessage* | `<yellow>The match ended with no winner.</yellow>` | |
| `game.win.fade-in-ticks` | int | `10` | |
| `game.win.stay-ticks` | int | `60` | |
| `game.win.fade-out-ticks` | int | `20` | |
| `game.win.sound.key` | string | `minecraft:ui.toast.challenge_complete` | |
| `game.win.sound.volume` | double | `1.0` | |
| `game.win.sound.pitch` | double | `1.0` | |
| `game.win.return-delay-seconds` | int | `5` | Time on the win screen before players return to the lobby. |

### Arena reset

Pastes a schematic over the arena between rounds. Requires FastAsyncWorldEdit. See
[SETUP.md section 10](SETUP.md#10-arena-reset-schematic) for the schematic workflow. The origin must
match the position you stood on when running `//copy`.

| Key | Type | Default | Description |
| --- | --- | --- | --- |
| `arena.reset.enabled` | bool | `true` | Master toggle. |
| `arena.reset.schematic` | string | `arena.schem` | Filename inside `plugins/LZBR/schematics/`. |
| `arena.reset.origin.x` | int | `0` | **Map-dependent.** Paste origin X. |
| `arena.reset.origin.y` | int | `64` | **Map-dependent.** Paste origin Y. |
| `arena.reset.origin.z` | int | `0` | **Map-dependent.** Paste origin Z. |
| `arena.reset.paste-air` | bool | `true` | `true` fully overwrites the area, clearing player builds. `false` leaves existing blocks where the schematic has air. |
| `arena.reset.paste-entities` | bool | `false` | Restore entities saved in the schematic (item frames, armour stands, and similar). |

---

## `loot.yml`

Chest loot, ordered as: fill settings, rarities, loot table, then chest positions.

### Fill settings

| Key | Type | Default | Description |
| --- | --- | --- | --- |
| `settings.items-per-chest.minimum` | int | `3` | Lower bound on item stacks placed per chest. |
| `settings.items-per-chest.maximum` | int | `7` | Upper bound. Kept below capacity deliberately so chests are never brim-full. |
| `settings.scatter-items` | bool | `true` | `true` scatters items across random slots; `false` packs them into the first slots. |

### Rarities

Each entry under `rarities` defines a tier. When a slot is filled, a rarity is rolled first, then a
random item of that rarity is chosen. Rarities with no items in the loot table are skipped
automatically.

| Key | Type | Description |
| --- | --- | --- |
| `rarities.<id>.display-name` | *MiniMessage* | Shown in each item's lore so players can gauge a drop's value. |
| `rarities.<id>.chance` | double | Relative spawn weight. Values need not total 100, but keeping them on a 0 to 100 scale makes them read as percentages. |

Defaults: `common` 55, `uncommon` 25, `rare` 13, `epic` 5, `legendary` 2.

### Loot table

`loot` is a list of item entries. Unknown materials, rarities or enchantments are logged and skipped
rather than breaking the file.

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `material` | string | **yes** | Any Minecraft material name, such as `IRON_SWORD`. |
| `rarity` | string | **yes** | One of the keys defined under `rarities`. |
| `amount.minimum` / `amount.maximum` | int | no | Stack size range. Defaults to `1`. |
| `name` | *MiniMessage* | no | Custom item name. |
| `lore` | list of *MiniMessage* | no | Extra lore lines. |
| `enchantments` | map | no | Enchantment name to level, such as `SHARPNESS: 3`. |
| `show-rarity` | bool | no | Append the rarity display name to the lore. Defaults to `true`. |

```yaml
loot:
  - material: NETHERITE_SWORD
    rarity: legendary
    name: "<gold>Zone Breaker</gold>"
    lore:
      - "<gray>Forged in the eye of the storm.</gray>"
    enchantments:
      SHARPNESS: 5
      FIRE_ASPECT: 2
```

### Chest locations

**Map-dependent.** Best managed in game with `/lzbr loot chest add`, `remove` and `list`; entries are
written here automatically. Can also be edited by hand.

| Field | Type | Description |
| --- | --- | --- |
| `world` | string | World name the chest is in. |
| `x` / `y` / `z` | int | Block coordinates. |
| `facing` | string | `NORTH`, `EAST`, `SOUTH` or `WEST`, the direction the chest opening points. Re-applied every match so chests always look correct after a reset. Any other value normalises to `NORTH`. |

---

## `mobs.yml`

Arena mobs, ordered as: toggle, spawn cadence and density, mob types, drops, then spawn points.

### Settings

| Key | Type | Default | Description |
| --- | --- | --- | --- |
| `settings.enabled` | bool | `true` | Master switch for the entire mob system. |

### Spawning

These are the levers that keep mob density low so PvP remains the primary loop.

| Key | Type | Default | Description |
| --- | --- | --- | --- |
| `spawning.interval-seconds` | int | `20` | How often a spawn attempt runs. |
| `spawning.per-tick.minimum` | int | `0` | Lower bound on mobs attempted per interval. |
| `spawning.per-tick.maximum` | int | `2` | Upper bound per interval. |
| `spawning.max-alive` | int | `12` | Hard cap on LZBR-spawned mobs alive at once. The primary density control. |
| `spawning.spread-radius` | int | `6` | Mobs spawn within this many blocks horizontally of a spawn point. `0` spawns exactly on the point. |
| `spawning.min-player-distance` | int | `12` | Never spawn within this many blocks of a living player, so mobs do not appear on top of a fight. |
| `spawning.stop-below-zone-radius` | int | `60` | Once the zone shrinks below this radius, stop spawning so the endgame is pure PvP. `0` spawns for the whole match. |

### Mob types

Each entry under `mob-types` is a spawnable living entity name with a relative weight. Non-living or
unrecognised entity names are logged and skipped.

| Key | Type | Description |
| --- | --- | --- |
| `mob-types.<ENTITY_TYPE>.weight` | double | Relative spawn weight; need not total anything. |

Defaults: `ZOMBIE` 40, `SKELETON` 30, `SPIDER` 20, `CREEPER` 10.

### Drops

Items are pulled from the loot table in `loot.yml`, so mob loot uses the same items and probabilities as
chests and is configured in one place. Only LZBR-spawned mobs are affected, so player deaths and PvP
drops are untouched.

| Key | Type | Default | Description |
| --- | --- | --- | --- |
| `drops.chance` | double | `25.0` | Percentage chance (0 to 100) that a killed mob drops any LZBR loot. |
| `drops.rolls.minimum` | int | `1` | Lower bound on loot-table rolls when a drop occurs. |
| `drops.rolls.maximum` | int | `1` | Upper bound. |
| `drops.keep-vanilla-drops` | bool | `true` | Keep the mob's normal vanilla drops as well. |

### Spawn locations

**Map-dependent.** Best managed in game with `/lzbr mobs spawn add`, `remove` and `list`; entries are
written here automatically.

| Field | Type | Description |
| --- | --- | --- |
| `world` | string | World name the spawn point is in. |
| `x` / `y` / `z` | int | Block coordinates. Mobs appear within `spawning.spread-radius` of this point, at this height. |
