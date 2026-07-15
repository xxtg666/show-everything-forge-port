# Show Everything Forge 1.7.10

Forge 1.7.10 port of [MinersLab/show-everything](https://github.com/MinersLab/show-everything). The mod lets players show the item in hand and the block, fluid, or entity under their crosshair in chat with hover details. All commands run on the server and remain available to non-OP players.

## Requirements

- Minecraft `1.7.10`
- Forge `10.13.4.1614` (the recommended 1.7.10 build)
- Java 8

## Installation

1. Put `showeverything-1.0.3-forge-1.7.10.jar` in the server `mods` folder.
2. Restart the server.

Client installation is optional. Install the same mod version client-side to receive complete hover data for unusually large item NBT; clients without the mod receive a bounded preview.

## Commands

| Command | Description |
| --- | --- |
| `/show-item` | Shows the item currently held by the player. |
| `/show-block` | Shows the block looked at within 15 blocks, falling back to the player's position. |
| `/show-block <x> <y> <z>` | Shows the block at specific coordinates. |
| `/show-fluid` | Shows the fluid looked at within 15 blocks, falling back to the player's position. |
| `/show-fluid <x> <y> <z>` | Shows the fluid at specific coordinates. |
| `/show-entity` | Shows the entity looked at within 15 blocks, falling back to the player. |
| `/show-entity <player-or-selector>` | Shows a selected player entity. |

Hyphenless aliases are also registered, including `/showitem`, `/showblock`, `/showfluid`, and `/showentity`.

## Build

Use Java 8 and run:

```bash
./gradlew clean build
```

The compiled mod jar is written to:

```text
build/libs/showeverything-1.0.3-forge-1.7.10.jar
```

This branch uses the official Forge `1.7.10-10.13.4.1614-1.7.10` source distribution baseline with ForgeGradle 1.2 and Gradle 2.0.

## Compatibility

- The server and client must both use Minecraft/Forge 1.7.10 when the client mod is installed; no cross-version protocol compatibility is intended.
- A vanilla or Forge client without Show Everything can still join because the client mod is optional.
- The custom packet/API is retained for full large-NBT item hovers, with a safe reduced fallback for clients that cannot receive it.
- 1.7.10's native item chat component provides the item name, rarity color, and hover tooltip. This Minecraft version has no inline chat item-icon renderer.

## License

MIT. The original mod is by MinersLab/WowStarWorld; this port is maintained by xxtg666.
