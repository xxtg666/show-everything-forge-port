# Show Everything NeoForge 1.21.1

NeoForge 1.21.1 port of [MinersLab/show-everything](https://github.com/MinersLab/show-everything).

The mod lets players show their held item, the block or fluid under the crosshair, and a targeted entity in chat with hover details. Commands run on the server and are available to normal players. Clients without the mod can still join; clients with the same version receive complete oversized item hovers through the retained custom payload, while other clients receive a bounded fallback.

## Requirements

- Minecraft `1.21.1`
- NeoForge `21.1.235` or newer compatible 21.1 build
- Java 21

## Installation

1. Put `showeverything-1.0.4-neoforge-1.21.1.jar` in the server `mods` folder.
2. Restart the server.

Client installation is optional. Install the same mod version client-side to receive complete hover data for items whose data components are too large for vanilla chat.

## Commands

| Command | Description |
| --- | --- |
| `/show-item` | Shows the item in the main hand, or the offhand when the main hand is empty. |
| `/show-block [x y z]` | Shows the looked-at block, or the block at the optional position. |
| `/show-fluid [x y z]` | Shows the looked-at fluid, or the fluid at the optional position. |
| `/show-entity [selector]` | Shows the looked-at entity, or the selected entity. |

Aliases without hyphens are also registered as complete commands: `/showitem`, `/showblock`, `/showfluid`, and `/showentity`.

## Build

```bash
./gradlew clean build
```

The mod jar is written to `build/libs/showeverything-1.0.4-neoforge-1.21.1.jar`.

## Compatibility

- The server and client must both use Minecraft 1.21.1 and NeoForge 21.1 when the client mod is installed; no cross-version protocol compatibility is intended.
- A client without Show Everything can still join because the client payload is optional.
- The custom payload is retained for full large-component item hovers, with a safe reduced fallback for clients that cannot receive it.
- Block, fluid, and entity commands retain their 15-block crosshair raycasts and coordinate/selector forms.

## License

This port is maintained by xxtg666 under the MIT license. The original mod is by MinersLab/WowStarWorld.
