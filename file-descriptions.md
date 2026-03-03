# File Descriptions

## src/client/java/com/drypted/spotlight/client/SpotlightEntryClient.java
- Client mod entry point that registers config, key bindings, and tick callbacks.
- Initializes `ModConfig` via AutoConfig and exposes getters/save helper.
- Defines key mappings for opening Spotlight and opening the command view.
- On key press, rebuilds search data and opens the Spotlight screen.

## src/client/java/com/drypted/spotlight/client/core/commands/ArgumentedCommand.java
- Abstract base for commands that declare typed arguments via `ArgumentType`.
- Stores immutable argument definitions and derives `requiresArgs()` from them.
- Builds usage strings and validates provided arguments with feedback.
- Generates per-argument suggestions based on current input slot.

## src/client/java/com/drypted/spotlight/client/core/commands/argument/ArgumentType.java
- Interface describing a typed command argument and its parsing contract.
- Provides validation and suggestion hooks for user input.
- Supplies a short usage hint for command signatures.
- Used by `ArgumentedCommand` to drive validation and completion.

## src/client/java/com/drypted/spotlight/client/gui/utils/Color.java
- Immutable color wrapper storing RGBA as `0xAARRGGBB` with helpers.
- Includes RGB/HSL conversion, blending, interpolation, and adjustments.
- Provides component getters, contrast-aware text color, and utilities.
- Supplies basic object overrides (`equals`, `hashCode`, `toString`).

## src/client/java/com/drypted/spotlight/client/gui/utils/Colors.java
- Central palette of common `Color` constants for UI and debug use.
- Exposes both `Color` and integer ARGB variants for convenience.
- Defines semantic colors for success/info/warn/error and selection UI.
- Includes shadow and debug overlay colors used across widgets.

## src/client/java/com/drypted/spotlight/client/core/commands/Command.java
- Core command interface with execution, validation, and suggestions.
- Extends `Searchable` to provide name/description search hooks.
- Supplies a default `/name` usage string and empty suggestions.
- Designed to run against a `LocalPlayer` context.

## src/client/java/com/drypted/spotlight/client/core/commands/CommandFeedback.java
- Immutable feedback record implementing `InputError` for commands.
- Encapsulates message and severity, with convenience constructors.
- Exposes a `NO_ERROR` constant and helper status checks.
- Used by command validation and execution flows.

## src/client/java/com/drypted/spotlight/client/core/handlers/CommandsHandler.java
- Central registry for Spotlight commands and search indexing.
- Registers built-in commands and builds a `SmartSearch` index.
- Parses input to list matching commands and execute by name.
- Provides per-command argument suggestions for UI completion.

## src/client/java/com/drypted/spotlight/client/mixin/CreativeModeTabsMixin.java
- Mixin on `CreativeModeTabs` to hook tab content rebuilding.
- Injects at the tail of `buildAllTabContents` to refresh search data.
- Delegates to `SearchHandler.rebuildGameItems()`.
- Ensures item index stays in sync with creative tabs.

## src/client/java/com/drypted/spotlight/client/gui/components/HotbarCollectionWidget.java
- Widget that renders and manages the 9-slot hotbar collection UI.
- Builds `HotbarSlotWidget` instances and handles focus/selection logic.
- Supports shift-selection to choose source slot for replacements.
- Renders help text, close button, and tooltips with styling.

## src/client/java/com/drypted/spotlight/client/gui/components/HotbarSlotWidget.java
- Widget representing a single hotbar slot with icon and key label.
- Draws rounded backgrounds, highlights, and pressed states.
- Stores `ItemsResultData` and emits click callbacks.
- Includes a builder for customization of visuals and behavior.

## src/client/java/com/drypted/spotlight/client/core/storage/HotbarStorage.java
- Reads/writes named hotbar presets to `spotlight_hotbars.json`.
- Uses the vanilla `Hotbar` codec to serialize and deserialize data.
- Exposes CRUD helpers for save, load, remove, and name listing.
- Centralizes file IO, logging, and JSON handling.

## src/client/java/com/drypted/spotlight/client/gui/models/InputError.java
- Interface for input errors with message and severity.
- Provides default color and halting behavior via `InputFeedbackType`.
- Used by widgets to render validation feedback.
- Implements a simple contract for error reporting.

## src/client/java/com/drypted/spotlight/client/gui/models/InputFeedbackType.java
- Enum of feedback severities with stop/continue semantics.
- Maps severities to UI colors and chat formatting.
- Supplies display names for user-facing messages.
- Integrates with `Styles` for consistent theming.

## src/client/java/com/drypted/spotlight/client/gui/components/InputWidget.java
- Custom text input widget with selection, caret, and scrolling.
- Handles keyboard input, clipboard operations, and mouse selection.
- Supports suggestions, validation errors, and loading indicators.
- Provides builder options, callbacks, and narration text for a11y.

## src/client/java/com/drypted/spotlight/client/models/ItemsResultData.java
- Search result model for items, wrapping icon, name, and identifier.
- Builds serialized item definitions for give/replace commands.
- Offers helpers for matching text and for empty sentinel data.
- Implements `Searchable` for query integration.

