# matou-dev/minimap — preuve SPI côté client

Minimap from scratch : données via SPI/bridge seul, aucun remplacement du
render vanilla. **Zéro import Minecraft** (gate `check`). Modid
`matouminimap` (cf. `NAMES.md`).

M3 proof : `MinimapJob` renders overlay rows from `matou-spi` snapshots
(player + world cells + radius, namespace `minimap:`) and only rows — never
draws, never replaces vanilla render; the bridge owns blitting. Unknown
cells render as explicit void; bad backend data is refused loudly.
Self-test `java/test`, gate `tools/check.sh` (zero-MC + sibling-SPI
compile).
