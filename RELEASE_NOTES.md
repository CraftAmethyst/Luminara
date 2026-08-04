# Luminara 1.0.15 Release Notes
---

## New Features

- Implement player attempt-pickup event hook (`4f3e1a53`)
- Expose runtime compatibility metadata for downstream consumers (`efed2f60`)

## Bug Fixes

### Mixin & Compatibility
- Keep portal bridge outside the Mixin package to avoid loader conflicts (`663f6541`)
- Avoid Apotheosis anvil Mixin injection conflicts (`308c807f`)
- Isolate compatibility Mixins into a dedicated configuration (`debdbfe0`)
- Coexist with Radium's portal search implementation (`3d518a03`)
- Coexist with ModernFix recipe-logging hooks (`01bf4f2d`)
- Validate runtime-named Mixin targets at build time (`c748f98b`)
- Pin ejector injectors to Forge runtime mappings (`c49b69ea`)
- Target portal search radius at the runtime invocation site (`3083c3c6`)
- Preserve stem-growth event semantics under injection (`a3772716`)
- Inject entity-teleport events at the actual runtime methods (`833821b6`)

### Distribution & Build Artifacts
- Emit canonical block-style YAML configuration (`fb5ae999`)
- Use the resolved Forge version for distribution verification (`d0a2bdd9`)
- Generate the reobfuscation package mapping correctly (`12e218b7`)
- Isolate reproducibility workspaces from each other (`2bdd0fab`)
- Declare generated CraftBukkit sources as a compile input (`523c8d8b`)
- Share generated Spigot inputs across all build tasks (`f15b2ab9`)
- Separate runtime libraries from installer downloads (`dc01f651`)

### Bootstrap & Runtime
- Validate Forge bootstrap fingerprints before installation (`7e70f060`)
- Make bootstrap setup fully idempotent (`d00a827a`)
- Make EULA acceptance noninteractive (`f55317bc`)
- Harden artifact downloads with retry and checksum verification (`bd1e51af`)
- Resolve Mojang runtime libraries from correct repositories (`ba8befcd`)
- Use a field-safe Mixin annotation processor (`97f12065`)
- Use the published Mixin tools coordinate (not a shadowed copy) (`8f2d068f`)
- Resolve the pinned Spigot snapshot from the correct repository (`b0f6642c`)
- Restore synchronous world persistence (`375feec2`)
- Complete runtime metadata integration across all platforms (`9ccc53ff`)
- Use JDK `Base64` for Forge capability encoding (`11ee364b`)

## Refactoring

- Isolate compatibility Mixins into a separate configuration (`debdbfe0`)
- Migrate the configuration schema to v2 (`81402564`)
- Separate Forge installer parsing from the bootstrap flow (`6e811b71`)

## Build

- Pin and verify the Gradle wrapper (`1faafcd6`)
- Centralize repository declarations (`9e045b5b`)
- Centralize compatibility version properties (`c4c1a271`)
- Add standardized Java conventions plugin (`cde09080`)
- Modernize archive tasks with reproducible metadata (`49bf1029`)
- Add deterministic distribution assembly (`02abb8b4`)
- Lock and verify all dependencies (`be95a7c5`)
- Validate access-transformer and mapping inputs (`2457bf9e`)
- Trust locally generated Forge artifacts during assembly (`50468dcb`)
- Remove legacy publication infrastructure (`5e0d928e`)
- Track the latest Forge promotion automatically (`ab3b6cb5`)
- Tolerate platform-specific entries in the dependency lockfile (`70960b6a`)
- Verify Windows native dependency hashes (`c72e3486`)
- Verify transitive JUnit module metadata (`28158d1e`)

## Continuous Integration

- Preserve the expected launcher failure on Windows CI (`46e181e6`)
- Consolidate all verification workflows into a single pipeline (`86ad8b1e`)

## Testing

- Verify reproducible distributions byte-for-byte (`547ae5f3`)
- Add Forge mod and Bukkit plugin server smoke tests (`c7fd1180`)
- Add distribution compatibility verification (`4ae8227a`)
- Enforce registry and remapper contracts (`fe8e6590`)
- Inventory all Mixin targets to detect drift (`dc09bdc2`)

## Documentation

- Modernize contribution templates (`2951b00a`)
- Add security policy and support guidelines (`fc1b8006`)
- Refresh build and usage guidance (`16dc34bf`)

## Chores

- Add i18n YAML configuration for Luminara (`7c8e8e3f`)
- Remove obsolete community metadata (`692527e3`)
- Standardize editor settings across the project (`7716e8c1`)
- Normalize repository line endings (`da5d7fda`)
- Format all source files with Prettier (`6f09ecd7`)
