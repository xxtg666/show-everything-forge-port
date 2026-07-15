# Show Everything Forge 1.12.2

Forge 1.12.2 port of [MinersLab/show-everything](https://github.com/MinersLab/show-everything).

The mod lets players show their held item, the block or fluid under the crosshair, and a targeted entity in chat with hover details. Commands run on the server and are available to normal players. Clients without the mod can still join; clients with the same version receive complete oversized item hovers through the retained Forge custom packet/API, while other clients receive a bounded fallback.

Clients that also install this mod get enhanced `/show-item` rendering for very large item NBT. Clients without the mod receive a safe shortened hover instead, so oversized NBT cannot kick players through Minecraft 1.12.2's chat packet string limit.

## Requirements

- Minecraft `1.12.2`
- Forge `14.23.5.2847` or compatible 1.12.2 Forge build
- Java 8 for typical Minecraft 1.12.2 servers

## Installation

1. Build or download `showeverything-1.0.3-forge-1.12.2.jar`.
2. Put the jar in the server `mods` folder.
3. Restart the server.

Client installation is optional. The server will not reject clients just because they do not have this mod installed. Install it client-side only if you want full hover display for very large item NBT.

## Commands

All commands are available to normal players.

| Command | Description |
| --- | --- |
| `/show-item` | Shows the item in your main hand, or offhand if the main hand is empty. |
| `/show-block [x y z]` | Shows the looked-at block, or the block at the optional position. |
| `/show-fluid [x y z]` | Shows the looked-at fluid, or the fluid at the optional position. |
| `/show-entity [selector]` | Shows the looked-at entity, or the selected entity. |

Aliases without hyphens are also registered, such as `/showitem` and `/showblock`.

## Build

```bash
./gradlew build
```

The compiled mod jar is written to:

```text
build/libs/showeverything-1.0.3-forge-1.12.2.jar
```

## Compatibility

- The server and client must both use Minecraft/Forge 1.12.2 when the client mod is installed; no cross-version protocol compatibility is intended.
- A vanilla or Forge client without Show Everything can still join because the client mod is optional.
- The custom packet/API is retained for full large-NBT item hovers, with a safe reduced fallback for clients that cannot receive it.
- Block, fluid, and entity commands retain their 15-block crosshair raycasts and coordinate/selector forms.

## License

This port is maintained by xxtg666 and follows the repository license. The original mod is by MinersLab/WowStarWorld.
