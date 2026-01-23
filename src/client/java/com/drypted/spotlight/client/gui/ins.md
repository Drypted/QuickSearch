## 1. Screen / High-level UI flow

**Responsibility:** lifecycle, open/close logic, focus, global input

**Move into:**

* `SpotlightScreen`

**Features**

* Open/close conditions (keybind, outside click, escape)
* Focus management
* Layout positioning (center math, distances)
* Pause behavior
* Delegation of input to widgets

---

## 2. Search input handling

**Responsibility:** text editing, caret, typing rules

**Move into:**

* `SearchInputModel`
* or `SearchTextController`

**Features**

* Text buffer
* Caret position + blink timing
* Backspace, charTyped filtering
* Typing enable/disable (TAB behavior)
* Text normalization (lowercase, trim, etc.)

Right now this logic is tangled inside `SearchBarWidget`.

---

## 3. Search bar rendering

**Responsibility:** visual appearance of the search box only

**Move into:**

* `SearchBarRenderer`

**Features**

* Background rendering
* Outline rendering
* Caret rendering
* Hotbar slot rendering
* Padding/layout constants

This lets you change visuals without touching input logic.

---

## 4. Search result querying / filtering

**Responsibility:** data filtering and ranking

**Move into:**

* `SearchQueryService`

**Features**

* Query execution
* Matching rules (contains, startsWith, fuzzy later)
* Sorting (exact match > partial)
* Result limits
* Empty-query behavior

This removes logic from `SearchBarWidget.refreshResults`.

---

## 5. Search data indexing

**Responsibility:** building and caching searchable data

**Move into:**

* `SearchIndex`
* `ItemSearchIndex`
* `BlockSearchIndex` (future)

**Features**

* Registry scanning
* Lazy vs eager loading

Right now this is hardwired via `static SEARCH_DATA`.

---

## 6. Result list management

**Responsibility:** result list state, selection, scrolling

**Move into:**

* `SearchResultsModel`
* or `ResultsController`

**Features**

* Current result list
* Highlighted index
* Keyboard navigation (up/down/enter)
* Selection state
* Sync with scroll position

This will be mandatory if you add keyboard navigation.

---

## 7. Result list rendering

**Responsibility:** layout and drawing of results container

**Move into:**

* `SearchResultsRenderer`

**Features**

* Results box background
* Clipping
* Scrollbar visibility rules
* Delegating row rendering

Keeps `ScrollBoxWidget` usage isolated.

---

## 8. Individual result rendering

**Responsibility:** drawing a single search result

**Already close, but split further**

**Move into:**

* `SearchResultWidget`

**Features**

* Icon rendering
* Text rendering + scaling
* Hover / pressed / highlighted visuals
* Outline logic

Click logic should *not* live here long-term.

---

## 9. Result interaction / actions

**Responsibility:** what happens when a result is activated

**Move into:**

* `SearchResultActionHandler`

**Features**

* Give item
* Run command

This decouples UI from game logic.

---


## Suggested final structure (example)

```
gui/
 ├─ screen/
 │   └─ SpotlightScreen.java
 ├─ input/
 │   ├─ SearchInputModel.java
 │   └─ SpotlightInputRouter.java
 ├─ search/
 │   ├─ SearchEngine.java
 │   ├─ SearchIndex.java
 │   └─ SearchResultData.java
 ├─ results/
 │   ├─ SearchResultsModel.java
 │   ├─ SearchResultActionHandler.java
 │   └─ SearchResultWidget.java
 ├─ render/
 │   ├─ SearchBarRenderer.java
 │   ├─ SearchResultsRenderer.java
 │   └─ SpotlightTheme.java
 └─ layout/
     └─ SpotlightLayout.java
```
