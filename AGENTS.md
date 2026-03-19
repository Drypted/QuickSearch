# AGENTS Guide for `spotlight`
## Scope and source of truth
- Target stack: Minecraft `1.21.11`, Fabric Loader, Mojang mappings (`build.gradle`, `gradle.properties`).
- Use local decompilation as authority: `mc_decompiled/`, `fabric_decompiled/`.
- Keep Mojang names/signatures exactly; do not use Yarn names.
## Runtime architecture
- Entrypoints are in `src/main/resources/fabric.mod.json`; environment is client-only.
- Real initializer is `src/client/java/com/drypted/spotlight/client/SpotlightClient.java` (AutoConfig, keybinds, lifecycle cleanup).
- `src/main/java/com/drypted/spotlight/SpotlightEntry.java` is intentionally empty.
- Screen orchestration and input routing live in `src/client/java/com/drypted/spotlight/client/ui/SpotlightScreen.java`.
## Core feature flow
- Open flow: `ModKeybinds` -> `SearchHandler.requestCreativeTabRebuild()` -> `new SpotlightScreen(...)`.
- Item index flow: `CreativeModeTabsMixin` (`buildAllTabContents` tail inject) -> `SearchHandler.rebuildGameItems()`.
- Search flow: `SpotlightScreen.onTextChanged(...)` dispatches to item search (`SearchHandler.searchAsync` with `SimpleSearch`/`SmartSearch`) or command search (`CommandsHandler.getCommands` + arg suggestions).
- Item actions stay in `GiveItemAction` and `ReplaceHotbarItemAction` (creative-mode checks and inventory slot mapping are centralized there).
- Hotbar presets are persisted by `HotbarStorage` to game-dir `spotlight_hotbars.json` using vanilla `Hotbar.CODEC`.
## Command conventions
- Implement new commands under `src/client/java/com/drypted/spotlight/client/core/commands/**`.
- Prefer `ArgumentedCommand` + typed `ArgumentType` classes from `core/blueprints/commands/argument/types/**`.
- Register commands only in `CommandsHandler` static block; then rebuild the command index.
- UI-visible command outcomes should return `CommandFeedback`, not thrown exceptions.
## Rendering and mixin integration
- Mosaic pipeline spans `MosaicBackgroundRenderer`, `RenderCommon`, `assets/spotlight/post_effect/mosaic_background.json`, `assets/spotlight/shaders/core/mosaic_background.fsh`.
- `SpotlightScreen.render(...)` must call `MosaicBackgroundRenderer.captureFramebuffer()` before widget rendering.
- `GameRendererAccessor` must expose `resourcePool` as `CrossFrameResourcePool`; wrong accessor type breaks mixin startup.
## Build/debug workflows
- Build: `./gradlew build`
- Run dev client: `./gradlew runClient`
- Run server profile when needed: `./gradlew runServer`
- Refresh Minecraft/Fabric decompilation context: `./gradlew genSources`
- Discover available tasks quickly: `./gradlew tasks --all`
## Project-specific patterns and safety checks
- Keep stateful registries singleton-style (`SearchHandler`, `CommandsHandler`, `ModKeybinds`).
- Preserve async search contract: cancel prior task, search off-thread, callback on main thread (`Minecraft.getInstance().execute(...)`).
- UI components consistently use builder factories (`InputWidget.builder(...)`, `ScrollBoxWidget.builder(...)`, `ResultDataWidget.builder(...)`).
- New config must go through `ModConfig` + `ModMenuIntegration` and add strings in `src/main/resources/assets/spotlight/lang/en_us.json`.
- If changing indexing, verify both the mixin trigger and keybind-triggered manual rebuild path.
- If changing command parsing, keep quoted argument parsing behavior in `SpotlightScreen.getArgs()`.
- If changing render code, keep framebuffer resize handling and `ClientLifecycleEvents.CLIENT_STOPPING` cleanup intact.



