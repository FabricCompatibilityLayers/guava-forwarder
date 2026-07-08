# guava-forwarder

A companion library for [Mod-Remapping-API](https://github.com/FabricCompatibilityLayers/Mod-Remapping-API) (a Fabric mod).

Given a Minecraft mod jar compiled against an old Guava version (`fromVersion`) that needs to run against whatever newer Guava is actually on the classpath at launch (`toVersion`), this project registers the class/member renames and ASM-style call redirects needed to bridge the gap. It does not bundle Mod-Remapping-API or Guava itself - both are provided by the host environment at runtime.

## Usage

A consuming `ModRemapper` calls the two entrypoints in `GuavaForwarder`:

- `GuavaForwarder.registerAdditionalMappings(MappingBuilder, fromVersion, toVersion)`
- `GuavaForwarder.registerVisitors(VisitorInfos, fromVersion, toVersion)`

## Supported versions

Guava `12.0.1` through `24.1.1-jre`.

## Building

- Build everything (compiles every version sourceSet, runs tests, produces the shaded/downgraded jar): `./gradlew build`
- Run tests only: `./gradlew test`
- Clean build (after sourceSet/dependency wiring changes): `./gradlew clean build`