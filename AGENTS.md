# AGENTS.md

## Project Snapshot
- Project type: Fabric mod/client for modern Minecraft, built with Gradle + Fabric Loom.
- Language/runtime: Java 25 (`options.release = 25`, source/target compatibility 25).
- Mod id: `nexus-client`.
- Source sets are split by environment:
  - `src/main/java`: common/main entrypoint code.
  - `src/client/java`: client-only code, GUI, HUD, modules, mixins, commands, config.
  - `src/main/resources` and `src/client/resources`: mod metadata and mixin configs.

## Build And Validation
- Preferred validation command:
  - `./gradlew.bat compileJava compileClientJava`
- This command succeeds in the current repo state and is the safest fast check after code changes.
- There is no test suite in the repository right now. Do not claim test coverage; use compile verification unless you explicitly add tests.
- Only run full client launches when necessary for UI or runtime-only behavior.

## Runtime Architecture
- Main mod entrypoint: `src/main/java/nexus/zigliix/com/NexusClient.java`
- Client entrypoint: `src/client/java/nexus/zigliix/com/client/NexusClientClient.java`
- Client initialization currently wires:
  - `ModuleManager.init()`
  - `NexusConfigManager.load()`
  - `NexusCommandManager.register()`
  - Discord RPC startup/shutdown
  - per-tick orchestration via `ClientFeatureController`
  - HUD rendering via `HudElementRegistry`

## Important Systems
- Modules:
  - Base type: `src/client/java/nexus/zigliix/com/client/module/Module.java`
  - Registry: `src/client/java/nexus/zigliix/com/client/module/ModuleManager.java`
  - Categories currently in use: `HUD`, `VISUAL`, `MOVEMENT`, `MISC`
  - Adding a module requires registering it in `ModuleManager.init()`
- Settings:
  - Located under `src/client/java/nexus/zigliix/com/client/module/setting/`
  - Settings serialize through `Setting#toJson()` / `fromJson()` and are persisted automatically through the config manager.
- Config:
  - Managed by `src/client/java/nexus/zigliix/com/client/config/NexusConfigManager.java`
  - Stored in Fabric config dir as `nexus-client.json`
  - Module toggles/keybinds/settings and global flags persist here.
- Commands:
  - Managed by `src/client/java/nexus/zigliix/com/client/command/NexusCommandManager.java`
  - Chat command prefix is `.nexus`
- Notifications/HUD:
  - Runtime tick fan-out lives in `src/client/java/nexus/zigliix/com/client/runtime/ClientFeatureController.java`
  - HUD code lives under `src/client/java/nexus/zigliix/com/client/hud/`
- Mixins:
  - Client mixin config: `src/client/resources/nexus-client.client.mixins.json`
  - Current mixins handle keyboard input, mouse clicks, and custom title screen redirect.

## Input And UI Conventions
- The Click GUI toggle key is `Right Shift` via `KeybindUtil.GUI_TOGGLE_KEY`.
- Do not allow modules to bind to the reserved GUI toggle key.
- Keyboard handling goes through `KeyboardMixin`; module keybind toggles are only processed while no screen is open.
- The title screen is intentionally replaced by `MainMenuScreen` through `TitleScreenMixin`.

## Editing Rules For This Repo
- Preserve the current architecture. New client features should usually fit into one of:
  - module
  - setting
  - HUD renderer/component
  - command/config plumbing
  - mixin hook when no cleaner API exists
- Prefer extending existing systems over adding parallel managers.
- When adding a new module:
  - keep the constructor metadata clean and user-facing
  - add settings through `addSetting(...)`
  - ensure config persistence works without special cases
  - register the module in `ModuleManager.init()`
- When editing mixins:
  - keep injections narrow
  - avoid cancellable injections unless behavior actually needs interception
  - verify the target method signatures match the current Minecraft mappings in this repo
- When changing config or commands:
  - maintain backward-tolerant parsing
  - prefer preserving invalid config files via the existing backup behavior rather than hard failing

## Known Repo State
- The git worktree is currently dirty. Do not revert unrelated user changes.
- There is generated or analysis output in `graphify-out/` and also duplicated `graphify-out` content under `src/client/java/nexus/zigliix/graphify-out/`. Treat both as non-source noise unless the task is specifically about them.
- There is also a stray Windows shortcut file: `gradlew.bat - Raccourci.lnk`. Ignore it unless the user asks for repo cleanup.
- `fabric.mod.json` still contains example-template metadata in description/contact/license fields. If a task touches project metadata, update those fields intentionally rather than copying template values forward.

## Good Agent Workflow
1. Inspect the relevant module, setting, mixin, or GUI path before editing.
2. Make the smallest coherent change that fits the current architecture.
3. Run `./gradlew.bat compileJava compileClientJava` after meaningful code edits.
4. In your final note, state whether you only compiled or also manually exercised runtime/UI behavior.
