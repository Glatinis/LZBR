<div align="center">

# LZBR: LiveZone Battle Royale

**A configuration-driven Battle Royale game mode for Paper servers.**

Shrinking play zone · randomised chest loot · low-density arena mobs · automatic arena resets.

[![Minecraft](https://img.shields.io/badge/Minecraft-Paper%2026.1.2-blue?logo=minecraft&logoColor=white)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Build](https://img.shields.io/badge/Build-Gradle-02303A?logo=gradle&logoColor=white)](https://gradle.org/)
[![Version](https://img.shields.io/badge/Version-1.0-success)](#)
[![Status](https://img.shields.io/badge/Status-Beta-yellow)](#project-status)
[![License](https://img.shields.io/badge/License-Proprietary-red)](#license)

</div>

---

## Overview

LZBR turns a Paper server into a Battle Royale arena. Players queue in a lobby, get teleported into the
arena on match start, loot randomised chests, fight through a shrinking zone, and the last player
standing wins. The arena then resets itself for the next round.

Every gameplay value is exposed in configuration. Zone pacing, loot tables and rarities, mob density
and drops, and all on-screen text and sounds are editable without touching code, and reloadable in
game with a single command.

## Features

| System | What it does |
| --- | --- |
| **Shrinking zone** | Configurable centre, start/end radius, shrink delay and duration. Renders as a real client-side border, damages players caught outside, and telegraphs each shrink with a title and sound. |
| **Match flow** | Lobby queue, pre-game countdown, scattered arena drop, live match, win screen, then automatic return to the lobby. Every title, subtitle, broadcast, timing and sound is configurable (MiniMessage formatted). |
| **Scatter spawning** | Players land at random, safety-checked spots around the zone centre instead of one shared point: spots are kept apart, never in blocks, lava or the void, and a rooftop check keeps landings on real terrain. A short freeze with a countdown lets everyone get their bearings before the fight starts. |
| **Chest loot** | Chest positions are set in game or by hand. Each match every chest is re-placed (facing preserved), emptied and re-filled with a random number of items: never brim-full, never the same twice. |
| **Loot table & rarities** | Define rarity tiers with display names and spawn percentages, then define items with amounts, custom names, lore and enchantments. One table drives both chests and mob drops. |
| **Arena mobs** | Low-density hostile spawning at configured points, weighted by mob type. Alive caps, player-distance and late-game zone cutoffs keep mobs a supporting act so PvP stays the primary loop. |
| **Mob loot drops** | Killed mobs have a configurable chance to drop items rolled from the shared loot table, with optional vanilla drops retained. |
| **Arena reset** | Pastes a WorldEdit schematic over the arena between rounds via FastAsyncWorldEdit, so each match starts from a pristine map without regenerating the world. |
| **Spectating** | Eliminated players are put into spectator mode for the remainder of the round and returned to the lobby at the end. |

## Project status

The plugin itself is **feature-complete for phase 1** and running in testing. All systems listed above
are implemented, refactored and building cleanly.

### Arena map: pending

Arena map integration is **not yet finalised**. The map supplied so far does not fully meet the
requirements of a Battle Royale layout, principally the size of the usable play area, spawn
distribution, and the space needed to place loot chests and mob spawn points at a sensible density.
Sourcing a map that suits the mode properly is taking longer than anticipated, and we would rather get
this right than build the arena configuration around a layout that will not play well.

Because of this, the arena-specific values ship as **placeholders**: zone centre and radii, spawn
scatter radius, chest positions, and mob spawn points in the configuration files are defaults rather
than tuned values.

This is a configuration gap, not a development one. Every system that depends on the map is complete
and map-agnostic. Once a suitable arena is chosen, bringing it online is a matter of following
[docs/SETUP.md](docs/SETUP.md): import the world, set the zone bounds, place chests and spawn points,
and save the reset schematic. No further plugin work is required for the map itself.

## Requirements

| | Requirement | Notes |
| --- | --- | --- |
| **Server** | Paper `26.1.2` | Must match the API version the plugin is built against. |
| **Java** | 25 | Matches the Gradle toolchain. |
| **Required** | [Multiverse-Core](https://mvplugins.org/) `5.x` | Used for all lobby and arena world switching. `/lzbr start` refuses to run without it. |
| **Optional** | [FastAsyncWorldEdit](https://intellectualsites.github.io/download/fawe.html) | Required only for automatic arena resets. Without it, the reset feature disables itself and logs a warning. Do **not** also install WorldEdit, as FAWE supersedes it. |
| **Recommended** | [LuckPerms](https://luckperms.net/) | To grant `lzbr.admin` to staff without full operator status. |

## Installation

1. Install **Multiverse-Core 5.x** (and **FastAsyncWorldEdit**, if you want arena resets) and restart.
2. Drop `LZBR.jar` into `plugins/` and restart. `config.yml`, `loot.yml` and `mobs.yml` are generated
   on first run in `plugins/LZBR/`.
3. Create the lobby and arena worlds and configure them. See **[docs/SETUP.md](docs/SETUP.md)** for the
   full server-side walkthrough, including world settings, permissions and the arena schematic workflow.

## Quick start

```
/mv create lobby normal          # lobby world
/mv create br normal             # arena world, must match worlds.br in config.yml

/lzbr loot chest add             # look at a chest, register it as loot
/lzbr mobs spawn add             # stand somewhere, register a mob spawn point
/lzbr test loot                  # fill chests now and inspect
/lzbr test mob 3                 # spawn mobs now and inspect

/lzbr join                       # each player queues (minimum 2 by default)
/lzbr start                      # admin starts the match
```

## Commands

Root command `/lzbr`, alias `/lz`. Admin commands require the `lzbr.admin` permission.

| Command | Permission | Description |
| --- | --- | --- |
| `/lzbr join` | none | Join the lobby queue. |
| `/lzbr leave` | none | Leave the lobby queue. |
| `/lzbr start` | `lzbr.admin` | Begin the pre-game countdown and start a match. |
| `/lzbr end` | `lzbr.admin` | Force the current match to end. |
| `/lzbr arena reset` | `lzbr.admin` | Paste the arena schematic immediately. |
| `/lzbr loot chest add` | `lzbr.admin` | Register the chest you are looking at as a loot chest. |
| `/lzbr loot chest remove` | `lzbr.admin` | Unregister the chest you are looking at. |
| `/lzbr loot chest list` | `lzbr.admin` | List all registered loot chests. |
| `/lzbr loot reload` | `lzbr.admin` | Reload `loot.yml` only. |
| `/lzbr mobs spawn add` | `lzbr.admin` | Register a mob spawn point at your position. |
| `/lzbr mobs spawn remove` | `lzbr.admin` | Remove the nearest mob spawn point. |
| `/lzbr mobs spawn list` | `lzbr.admin` | List all mob spawn points. |
| `/lzbr mobs reload` | `lzbr.admin` | Reload `mobs.yml` only. |
| `/lzbr reload` | `lzbr.admin` | Reload all three configuration files. |
| `/lzbr test zone` | `lzbr.admin` | Spin up a small shrinking test zone around you. |
| `/lzbr test zone stop` | `lzbr.admin` | Stop the active zone. |
| `/lzbr test loot` | `lzbr.admin` | Fill all loot chests immediately. |
| `/lzbr test mob [count]` | `lzbr.admin` | Spawn mobs immediately at the configured points. |

Full reference: **[docs/COMMANDS.md](docs/COMMANDS.md)**

## Configuration

Three files are generated in `plugins/LZBR/`:

| File | Contents |
| --- | --- |
| `config.yml` | Player limits, world names, zone behaviour, match flow (countdown, start and win screens), arena reset. |
| `loot.yml` | Chest fill settings, rarity tiers and spawn percentages, the loot table, and chest positions. |
| `mobs.yml` | Mob toggle, spawn cadence and density caps, mob type weights, drop rules, and spawn points. |

All three are hot-reloadable with `/lzbr reload`. Full key-by-key reference:
**[docs/CONFIGURATION.md](docs/CONFIGURATION.md)**

## Building from source

```bash
./gradlew build          # compile and assemble the jar
./gradlew runServer      # launch a test Paper server with the plugin loaded
```

The `jar` task is configured to output directly into a local test server's `plugins/` directory.
Adjust `destinationDirectory` in `build.gradle.kts` for your environment.

## Project structure

```
src/main/java/com/github/Glatinis/lZBR/
├── commands/          # Brigadier command tree (/lzbr) and subcommands
├── core/              # Plugin entry point and configuration repositories
├── gamestate/         # Match lifecycle, lobby queue, BR roster, event listeners
│   ├── br/            #   roster, teleport service, countdown, spawn freeze, announcements
│   ├── lobby/         #   lobby queue
│   └── listeners/     #   player quit / death / respawn handling
├── loot/              # Loot feature orchestrator
│   ├── chest/         #   chest positions, registry, and world placement/fill
│   └── table/         #   rarities, loot items, weighted table and its loader
├── mob/               # Arena mob feature orchestrator, tracking, death drops
│   └── spawn/         #   spawn points, spawner service, periodic spawn task
├── returncode/        # Typed command result enums
├── ui/                # Titles, sounds and MiniMessage helpers
├── util/              # Shared utilities (weighted random selection)
└── world/             # World control
    ├── arena/         #   schematic-based arena reset
    ├── spawn/         #   scattered, safety-checked player landing spots
    └── zone/          #   shrinking play zone, border, damage, telegraph
```

The codebase follows a consistent separation: **Manager** types own state and orchestrate,
**Service** types perform the work, **Task** types drive scheduled behaviour, **Listener** types handle
events, and **records** model immutable data.

## Documentation

| Document | Contents |
| --- | --- |
| [docs/SETUP.md](docs/SETUP.md) | Complete server-side setup: plugins, worlds, world settings, permissions, arena schematic workflow, first test run. |
| [docs/CONFIGURATION.md](docs/CONFIGURATION.md) | Every configuration key across all three files, with types, defaults and guidance. |
| [docs/COMMANDS.md](docs/COMMANDS.md) | Full command tree, arguments, permissions and usage notes. |

## License

Proprietary. All rights reserved. Not for redistribution without permission.

<sub>Developed by Glatinis.</sub>
