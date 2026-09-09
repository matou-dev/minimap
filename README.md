# matou-dev/minimap — SPI proof on the client side

From-scratch minimap: data via SPI/bridge only, no vanilla render
replacement. **Zero Minecraft import** (`check` gate). Modid
`matouminimap` (see `NAMES.md`).

M3 proof: `MinimapJob` renders overlay rows from `matou-spi` snapshots
(player + world cells + radius, namespace `minimap:`) and only rows — never
draws, never replaces vanilla render; the bridge owns blitting. Unknown
cells render as explicit void; bad backend data is refused loudly.
Self-test `java/test`, gate `tools/check.sh` (zero-MC + sibling-SPI
compile). Writing another client mod? Start at `../spi/AUTHORING.md`.
