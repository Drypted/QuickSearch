# CLAUDE.md — instructions for Claude Code on `quicksearch`

<!-- last updated: 2026-05-31 -- tracks the codebase; refresh (and bump the date) when the workflow/doc layout below changes. -->

This is the committed, pushable copy of the working instructions for this repo.
The detailed sources of truth (keep these authoritative; this file just points
to them and states the essentials):

- **`AGENTS.md`** (repo root) — architecture, runtime flows, project-specific patterns.
- **`.github/copilot-instructions.md`** — full working strategy.
- **`.llm/`** — agent/design docs (e.g. `.llm/implementation/mosaic.md`).
- **`.index/`** — per-file source notes (replaces the old `file-descriptions.md`).

## Stack (see `AGENTS.md` for detail)

- Minecraft `1.21.11`, Fabric (Loader `0.18.4`, API `0.141.3+1.21.11`), Mojang mappings (never Yarn), Java 21.
- Decompiled sources vendored locally: `mc_decompiled/src/` (gitignored, regen via `./gradlew genSources`), `fabric_decompiled/src/`.

## How to work here (essentials)

1. **Implement first** from prior knowledge + the local caches (`.index/`,
   `*_decompiled/.index/`, `*_decompiled/.knowledge/`). Do NOT browse decompiled
   sources up front by default.
2. **Verify by compiling:** `./gradlew build`. No need to verify GUI/runtime —
   the user provides screenshots/output when needed.
3. **Only inspect `*_decompiled/src/` if it does not compile** or behavior is
   genuinely unclear. Up-front browsing is allowed for niche APIs, but not preferred.
4. **Cache as you go:** when you DO consult sources, record just-enough verified
   facts (plain text, terse) so the research isn't repeated:
   - MC/Fabric: `*_decompiled/.index/{version}/{package/path}/{ClassName}.txt`
     (version `1.21.11` for MC, `0.141.3+1.21.11` for Fabric) and
     `*_decompiled/.knowledge/{topic}.txt`. See each dir's `_GUIDE.txt`.
   - This repo's own code: `.index/{package/path}/{ClassName}.txt`, each entry
     stamped `Updated: YYYY-MM-DD`. See `.index/_GUIDE.txt`.
5. **Match Mojang mappings exactly**; never use Yarn names or mix namespaces;
   don't invent APIs — verify against the decompilation if unsure.

## Keep docs fresh

Every doc that tracks the codebase (`AGENTS.md`, this file, `.llm/**`,
`.index/**` entries, `.github/copilot-instructions.md`) carries a date. When you
change code a doc describes, update the doc and bump its date in the same pass.
Park features as `DISABLED` with a date instead of deleting their docs (e.g.
mosaic is currently disabled — `.llm/implementation/mosaic.md`).
