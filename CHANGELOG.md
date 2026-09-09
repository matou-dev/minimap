# Changelog — matou-dev/minimap

Notable changes to this repo. This client proof has no FML wiring yet: it
is not a loadable Forge mod; store listings stay DRAFT (see hub `NAMES.md`).
Full notes per tag: https://github.com/matou-dev/minimap/releases.

## [Unreleased]

- CI: runner pinned (`ubuntu-24.04`), JDK 21 via `setup-java` (temurin),
  actions pinned by SHA with Dependabot, missing `spi` sibling checkout
  added (the gate compiles against it).
- Docs: README rewritten in English (R3 hygiene).

## [1.0.0] - 2026-09-09

Source release: https://github.com/matou-dev/minimap/releases/tag/v1.0.0

- Client proof: `MinimapJob` renders overlay rows from `matou-spi`
  snapshots (player + world cells + radius, namespace `minimap:`) — rows
  only, never draws, never replaces vanilla rendering.
- Unknown cells render as explicit void; bad backend data is refused
  loudly (exact 3x3 golden, player-following view).
