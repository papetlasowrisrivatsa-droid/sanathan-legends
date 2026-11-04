# SANATHAN LEGENDS v0.2.0 — Shared Worship Prototype

Target: Minecraft Java 26.3 / Fabric.

## Included in this prototype
- Six deity groups: Shiva, Vishnu, Ganesha, Hanuman, Durga and Surya.
- `/sl join <deity>` joins a shared worship group.
- `/sl offer` contributes to the group's single shared prayer total.
- `/sl status`, `/sl deities`, `/sl leave` and `/sl help`.
- No mod-imposed player cap.

## Current limitations
This is an early command-based prototype, not the complete planned mod. The current `/sl offer` command adds a fixed 10 points and does not yet consume or validate an offering item. Prayer state resets when the server restarts. Persistent saves, visible HUD, temples, blessing effects, ghosts, the Rakshasa Lord quest and Vaikuntha are not implemented yet.

## Build
- Java 25
- Gradle 9.7.1
- Fabric Loom 1.19-SNAPSHOT
- Fabric Loader 0.20.1
- Fabric API 0.162.0+26.3

Run `gradle build`. The JAR is produced in `build/libs/`.
