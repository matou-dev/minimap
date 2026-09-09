#!/bin/sh
# Gate zero-MC-import (Q2 strict) : aucun import Minecraft/Forge hors bridge.
set -eu
cd "$(dirname "$0")/.."
hits=$(rg -n --no-heading "net\.minecraft|cpw\.mods\.|net\.minecraftforge" \
  --glob '!tools/**' --glob '!.git/**' --glob '!*.md' --glob '!java/build/**' . || true)
if [ -n "$hits" ]; then
  echo "FAIL zero-mc-import :"
  echo "$hits"
  exit 1
fi
echo "ok (zero-mc-import)"
# M3 client gate : compile against the sibling ../spi checkout (convention
# siblings, cf. hub README). Loud refusal when absent. MATOU_SPI_SRC
# overrides the path (same layout expected underneath).
SPI=${MATOU_SPI_SRC:-../spi/java/src}
[ -d "$SPI" ] || { echo "FAIL minimap : spi sibling absent (cloner hub+spi+minimap en siblings)"; exit 1; }
mkdir -p java/build
javac --release 8 -d java/build $(find "$SPI" java/src -name '*.java')
javac --release 8 -cp java/build -d java/build $(find java/test -name '*.java')
java -cp java/build fr.iamacat.minimap.MinimapCheck
