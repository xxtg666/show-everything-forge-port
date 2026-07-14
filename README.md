# Show Everything Forge 1.16.5

Show items, blocks, fluids, and entities in chat with hover details. Commands run on the server and the mod declares missing clients as optional, so vanilla/Forge clients can still join.

When an item NBT payload is too large for a normal 1.16.5 chat packet, clients with this mod receive a Forge custom packet and render the complete item hover locally. Clients without the mod receive a bounded preview with NBT omitted.

## Requirements

- Minecraft `1.16.5`
- Forge `36.2.42` (or a compatible Forge 36 build)
- Java 8 runtime for Minecraft; Java 17 is used by the build environment

## Commands

- `/show-item` (`/showitem`) shows the main-hand item, falling back to offhand.
- `/show-block` (`/showblock`) shows the block in the 15-block view ray; coordinates can be supplied.
- `/show-fluid` (`/showfluid`) shows the fluid in the view ray; coordinates can be supplied.
- `/show-entity` (`/showentity`) shows the entity in the view ray, or an entity selected by Brigadier selector.

## Build

This branch uses the official Forge `1.16.5-36.2.42` MDK and ForgeGradle 6:

```bash
./gradlew clean build
```

The reobfuscated jar is written to `build/libs/showeverything-forge-1.16.5-1.0.2.jar`. Install it on the server; client installation is optional and enables full large-NBT hovers.

## License

MIT. The original mod is by MinersLab/WowStarWorld.
