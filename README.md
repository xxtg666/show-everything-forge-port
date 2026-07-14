# Show Everything Forge 1.20.1

Forge 1.20.1 port of [MinersLab/show-everything](https://github.com/MinersLab/show-everything).

The mod adds `/show-item`, `/show-block`, `/show-fluid`, and `/show-entity`, plus aliases without hyphens. Commands run on the server. Clients may join without the mod; clients that install it receive oversized item hover data through a Forge custom packet, while other clients receive a safe compact fallback.

## Requirements

- Minecraft `1.20.1`
- Forge `47.4.10` or a compatible Forge 47 build
- Java 17

## Build

```bash
./gradlew clean build
```

The mod jar is written to `build/libs/showeverything-forge-1.20.1-1.0.2.jar`.

## License

This port is maintained by xxtg666 under the MIT license. The original mod is by MinersLab/WowStarWorld.
