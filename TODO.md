# SOLID Refactor Backlog

## Completed

- Extract command input parsing from `SpotlightScreen` into a dedicated parser service.
- Extract item index rebuilding from `SearchHandler` into a dedicated index builder.
- Extract command registration/search/dispatch internals from `CommandsHandler` into `CommandRegistry`.
- Split `SpotlightScreen` into dedicated collaborators:
	- Query routing: `SpotlightQueryRouter`, `SpotlightCommandQueryRouter`, `SpotlightItemQueryRouter`.
	- Result presentation: `SpotlightResultPresenter`, `SpotlightVisibilityController`, `SpotlightViewState`.
	- Submit flow: `SpotlightSubmitHandler`, `SpotlightSubmitCommandHandler`, `SpotlightSubmitItemHandler`.
	- Click/suggestion handlers: `SpotlightItemResultClickHandler`, `SpotlightCommandResultClickHandler`, `SpotlightSuggestionApplyHandler`.

## In Progress

- Continue reducing static handler dependencies behind interfaces for DIP.

## Planned

- Replace static `SearchHandler` facade with injected search/index services.
- Separate hotbar widget interaction logic from rendering in `HotbarCollectionWidget`.
- Introduce thin facades for player inventory actions to reduce direct Minecraft API coupling in UI classes.

## Existing Notes

- Show "enter" hint, when item search result is selected.
