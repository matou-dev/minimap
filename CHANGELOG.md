# Changelog — matou-dev/minimap

Notable changes to this repo. This client proof has no FML wiring yet: it
is not a loadable Forge mod; store listings stay DRAFT (see hub `NAMES.md`).
Full notes per tag: https://github.com/matou-dev/minimap/releases.

## [Unreleased]

- Authoring refactor (test-mod scaling audit): `MinimapJob` reads the
  snapshot through the shared SPI accessors (`stringOf`/`mapOf`/`longOf`)
  and parses the player position with SPI `Cell` — the 50-line bespoke
  guard chain is gone (116 to ~70 lines), generic read failures now carry
  the shared `E_MATOU_SNAPSHOT`/`E_MATOU_CELL` codes while domain rules
  keep `E_MINIMAP_*`; decided rows unchanged. `MinimapCheck` lambdas
  plus a max-radius (17x17 void) golden. `tools/check.sh` honours
  `MATOU_SPI_SRC`.
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
