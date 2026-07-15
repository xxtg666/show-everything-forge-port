# Show Everything Forge 1.18.2

Forge 1.18.2 port of [MinersLab/show-everything](https://github.com/MinersLab/show-everything).

Show items, blocks, fluids, and entities in chat with hover details. Commands run on the server and are available to normal players. The client mod is optional: clients without it can join and receive a safe reduced item preview when an item's NBT is too large for vanilla chat.

When both sides have the mod, oversized `/show-item` data uses a bounded Forge custom packet to preserve the full item hover. Payloads that exceed the custom packet safety limit are reduced for every client instead of risking a disconnect.

## Requirements

- Minecraft `1.18.2`
- Forge `40.2.21` or a compatible newer 1.18.2 Forge build
- Java 17

## Installation

1. Put `showeverything-1.0.3-forge-1.18.2.jar` in the server `mods` folder.
2. Restart the server.

Client installation is optional. Install the same mod version client-side only to see full hover data for items whose NBT is too large for vanilla chat.

## Commands

| Command | Description |
| --- | --- |
| `/show-item` | Shows the item in the main hand, or the offhand when the main hand is empty. |
| `/show-block [x y z]` | Shows the looked-at block, or a block at the optional position. |
| `/show-fluid [x y z]` | Shows the looked-at fluid, or a fluid at the optional position. |
| `/show-entity [selector]` | Shows the looked-at entity, or the selected entity. |

Aliases without hyphens are also registered: `/showitem`, `/showblock`, `/showfluid`, and `/showentity`.

## Build

Use the official Forge `1.18.2-40.2.21` MDK wrapper and Java 17:

```bash
./gradlew clean build
```

The compiled mod is written to `build/libs/showeverything-1.0.3-forge-1.18.2.jar`.

## License

This port is maintained by xxtg666 under the repository's MIT license. The original mod is by MinersLab/WowStarWorld.
