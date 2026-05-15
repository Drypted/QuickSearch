# AGENTS Guide for `quicksearch`
## Scope and source of truth
- Target stack: Minecraft `1.21.11`, Fabric Loader, Mojang mappings, Java 21 (`build.gradle`, `gradle.properties`).
- Use local decompilation as authority: `mc_decompiled/`, `fabric_decompiled/`.
- Keep Mojang names/signatures exactly; do not use Yarn names.
- External dependencies: Fabric API (`0.141.3+1.21.11`), Cloth Config (`21.11.153`), ModMenu (`17.0.0-beta.2`).
## Runtime architecture
- Entrypoints are in `src/main/resources/fabric.mod.json`; environment is client-only.
- Real initializer is `src/client/java/com/drypted/quicksearch/client/SpotlightClient.java` (AutoConfig, keybinds, lifecycle cleanup).
- `src/main/java/com/drypted/quicksearch/SpotlightEntry.java` is intentionally empty.
- `SpotlightScreen` composes specialized collaborators in `src/client/java/com/drypted/quicksearch/client/core/**` and `src/client/java/com/drypted/quicksearch/client/ui/**` (`QueryRouter`, `ResultPresenter`, `SubmitHandler`, `VisibilityController`) for routing, presentation, submit handling, and visibility state.
## Core feature flow
- Open flow: `ModKeybinds` -> `SearchHandler.requestCreativeTabRebuild()` -> `new SpotlightScreen(...)`.
- Item index flow: `CreativeModeTabsMixin` (`buildAllTabContents` tail inject) -> `SearchHandler.rebuildGameItems()`.
- Search flow: `SpotlightScreen.onTextChanged(...)` -> `SpotlightQueryRouter` -> item path (`SpotlightItemQueryRouter` -> `SearchHandler.searchAsync`) or command path (`SpotlightCommandQueryRouter` -> `CommandsHandler` + arg suggestions).
- Item actions stay in `GiveItemAction`, `ReplaceHotbarItemAction`, and `ReplaceInventoryItemAction` (creative-mode checks and slot mapping are centralized in action classes).
- Presets use `PresetStorage` under game-dir `quicksearch-storage/`: `HotbarStorage` (`hotbars.json`, vanilla `Hotbar.CODEC`) and `InventoryStorage` (`inventories.json`, `ItemStack.OPTIONAL_CODEC.listOf()`).
## UI Widget Components
- All screen widgets use builder factories: `InputWidget.builder(...)`, `ScrollBoxWidget.builder(...)`, `ResultDataWidget.builder(...)`.
- Builder patterns support fluent configuration: color, size, padding, rounding, callbacks, disabled states.
- Hotbar UI: `HotbarCollectionWidget` renders 9 slots; `HotbarSlotWidget` represents individual slots with item icons and keyboard labels.
- Hotbar widget focus/selection: shift-click for source slot selection, normal click for target slot replacements.
- Hotbar persistence: `HotbarStorage` uses `PresetStorage` (Gson + `Hotbar.CODEC`) and stores presets in `quicksearch-storage/hotbars.json` under the game directory.
## Command conventions
- Implement new commands under `src/client/java/com/drypted/quicksearch/client/core/commands/**`.
- Prefer `ArgumentedCommand` + typed `ArgumentType` classes from `core/blueprints/commands/argument/types/**`.
- Register commands only in `CommandsHandler` static block; then rebuild the command index.
- UI-visible command outcomes should return `CommandFeedback`, not thrown exceptions.
- Keep command parsing in `CommandInputParser` (`isCommandInput`, `getCommandName`, `getArgs`, `hasStartedArguments`) instead of re-implementing parsing in screens/widgets.
## Rendering and mixin integration
- Mosaic pipeline spans `MosaicBackgroundRenderer`, `RenderCommon`, `assets/quicksearch/post_effect/mosaic_background.json`, `assets/quicksearch/shaders/core/mosaic_background.fsh`.
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
- Config initialization: `SpotlightClient` calls `AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new)` on client init; retrieve via `SpotlightClient.getConfig()` and save via `SpotlightClient.saveConfig()`.
- Keybinds registered in `ModKeybinds.register()` called from `SpotlightClient.onInitializeClient()`; callbacks use `ClientTickEvents.END_CLIENT_TICK` to check state on main thread.
- Hot key bindings: `Y` opens item search, `U` opens command search, `Escape` closes, `Enter` submits (all configurable via Cloth Config).
- `ReplaceHotbarItemAction` enforces creative mode; hotbar slot mapping uses indexed inventory access to player hotbar.
- `HotbarCollectionWidget` can be disabled globally via config `hotbar.showHotbarSlots` flag.
- If changing indexing, verify both the mixin trigger and keybind-triggered manual rebuild path.
- If changing command parsing, keep quoted argument parsing behavior in `CommandInputParser.getArgs()`.
- If changing render code, keep framebuffer resize handling and `ClientLifecycleEvents.CLIENT_STOPPING` cleanup intact.
- Qodana linter config excludes `SmartSearch.java` (complex algorithm); all other code subject to standard checks.



