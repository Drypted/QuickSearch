.llm/ -- LLM / agent-facing documentation
=========================================

Home for instruction & reference docs meant for AI agents working on this repo,
consolidated out of the project root.

Subdirectories:
  implementation/   Feature design / implementation notes (e.g. mosaic.md).

Conventions:
  - Docs that track the codebase carry a "last updated YYYY-MM-DD" line and must
    be refreshed when the related code changes (do not leave them stale).
  - If a feature is parked, mark the doc DISABLED with the date rather than
    deleting it.

NOT moved here (intentionally):
  - AGENTS.md stays at repo root (conventional auto-discovered agent guide).
  - .github/copilot-instructions.md stays in .github (conventional location).
  - The per-file source index lives in the repo-root .index/ directory.
