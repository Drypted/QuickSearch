<!-- last updated: 2026-05-31 -->

* **Minecraft Version:** 1.21.11
* **Mod Loader:** Fabric (Loader `0.18.4`, API `0.141.3+1.21.11`)
* **Mappings:** Mojang mappings (never Yarn)
* **Java:** 21

## Working Strategy (implement first, verify by compiling)

Do **not** browse the decompiled sources up front by default. Instead:

1. **Implement first** using prior knowledge plus anything already captured in
   the local index/knowledge files (see below).
2. **Check for compile errors:** run the Gradle build (`./gradlew build`).
3. **Only if it does not compile** (or behavior is genuinely unclear), inspect
   the decompiled sources to find the exact Mojang-mapped API.
4. **You do not need to verify the GUI or runtime behavior** — just make sure the
   code compiles. The user will provide screenshots/output when needed.

This is a preference, not a hard rule: you *may* browse the decompilation up
front for niche/unfamiliar APIs, but don't prefer it — keep that for cases where
guessing is clearly unsafe.

## Decompiled Source Code Availability

Full decompiled sources are provided locally and remain the authoritative source
of truth **when you do consult them**:

* `mc_decompiled/src/` → Decompiled Minecraft 1.21.11 source (Mojang mappings).
  NOTE: this directory is gitignored and regenerated via `./gradlew genSources`.
* `fabric_decompiled/src/` → Decompiled Fabric API and Fabric Loader source.

## Index & Knowledge Caches (record as you go)

Whenever you DO refer to the decompilation, record **just enough** information so
the same research never has to be repeated. Persisted in plain text files:

* **Class index:** `*_decompiled/.index/{version}/{java/package/path}/{ClassName}.txt`
  - One directory per Java package, one file per inspected class.
  - `{version}` is `1.21.11` for Minecraft, `0.141.3+1.21.11` for Fabric.
  - Record only verified facts: FQ class name, the exact Mojang method/field
    signatures you used, and a one-line behavior note when non-obvious.
* **Cross-cutting knowledge:** `*_decompiled/.knowledge/{topic}.txt`
  - For findings not tied to a single class (rendering pipeline, codecs, mixin
    patterns, version quirks). One topic per file.

Rules for these files: add a line only after verifying it in the source; keep
entries terse; **plain text only, not markdown**. See each directory's
`_GUIDE.txt` for the exact convention.

## Repo Docs, Indexes & Doc Maintenance

Where docs live (don't scatter new instruction files in the repo root):

* **This file** (`.github/copilot-instructions.md`) — working strategy. Stays in `.github`.
* **`AGENTS.md`** (repo root) — architecture/flows guide. Stays at root (auto-discovered convention).
* **`.llm/`** — other agent/design docs (e.g. `.llm/implementation/mosaic.md`). New instruction-style docs go under an appropriately-named `.llm/<name>/` subdir.
* **`.index/`** (repo root) — per-file source notes, replacing the removed `file-descriptions.md`. Same `{package/path}/{ClassName}.txt` plain-text convention as the decompiled indexes, but NO version subdir; each entry carries an `Updated: YYYY-MM-DD` first line. Filled incrementally as files are touched. See `.index/_GUIDE.txt`.

**Keep docs from going stale:** every doc that tracks the codebase (`AGENTS.md`,
`.llm/**`, `.index/**` entries, and this file) carries a date. When you change
code that a doc describes, update the doc and bump its date in the same pass.
Mark parked features `DISABLED` with a date rather than deleting their docs.

## Code Expectations

* Match method names and signatures exactly to Mojang mappings.
* Do not mix mapping namespaces; never use Yarn names.
* Do not invent method names, fields, or classes; do not infer behavior from
  outdated docs or online examples — verify against the decompilation if unsure.
* Prefer reusing facts already captured in `.index`/`.knowledge` over re-reading.
