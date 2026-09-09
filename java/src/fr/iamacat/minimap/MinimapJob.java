package fr.iamacat.minimap;

import fr.iamacat.spi.Cell;
import fr.iamacat.spi.MatouId;
import fr.iamacat.spi.MatouJob;
import fr.iamacat.spi.Snapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * M3 client proof: renders minimap overlay rows from the snapshot, and only
 * rows. The job never draws, never touches vanilla render buffers, never
 * reads the live world: the bridge builds one snapshot per tick (player pos,
 * world cells, radius) and owns every side effect including blitting.
 *
 * <p>Same snapshot in, equal rows out. Unknown cells render as explicit
 * {@link #VOID}, never as garbage. Absent or malformed backend data is
 * refused loudly, never defaulted. Java 8, zero deps beyond matou-spi.
 */
public final class MinimapJob implements MatouJob<List<String>> {
    public static final MatouId PLAYER = MatouId.parse("minimap:player");
    public static final MatouId WORLD = MatouId.parse("minimap:world");
    public static final MatouId RADIUS = MatouId.parse("minimap:radius");

    /** Explicit glyph for unknown cells (no silent hole, no guess). */
    public static final String VOID = ".";

    /** Overlay stays bounded: radius above this is refused, not clamped. */
    static final int MAX_RADIUS = 8;

    public List<String> decide(Snapshot snap) {
        if (snap == null) {
            throw new NullPointerException("E_MINIMAP_SNAPSHOT:null");
        }
        Cell player = Cell.parsePlane(snap.stringOf(PLAYER));
        Map<?, ?> cells = snap.mapOf(WORLD);
        long wide = snap.longOf(RADIUS);
        if (wide <= 0 || wide > MAX_RADIUS) {
            throw new IllegalArgumentException("E_MINIMAP_RADIUS:range <"
                    + wide + "> (want 1.." + MAX_RADIUS + ")");
        }
        int radius = (int) wide;
        List<String> rows = new ArrayList<String>(2 * radius + 1);
        for (int dz = -radius; dz <= radius; dz++) {
            StringBuilder row = new StringBuilder(2 * radius + 1);
            for (int dx = -radius; dx <= radius; dx++) {
                row.append(glyphAt(cells,
                        (player.x + dx) + "," + (player.z + dz)));
            }
            rows.add(row.toString());
        }
        return Collections.unmodifiableList(rows);
    }

    /** One cell: known glyph verbatim, unknown as explicit void. */
    static String glyphAt(Map<?, ?> cells, String key) {
        Object glyph = cells.get(key);
        if (glyph == null) {
            return VOID;
        }
        if (!(glyph instanceof String)
                || ((String) glyph).length() != 1) {
            throw new IllegalArgumentException(
                    "E_MINIMAP_GLYPH:type <" + glyph + "> at <" + key
                            + "> (want single char)");
        }
        return (String) glyph;
    }
}
