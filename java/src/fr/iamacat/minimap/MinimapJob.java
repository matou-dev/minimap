package fr.iamacat.minimap;

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
        int[] pos = posOf(snap.get(PLAYER));
        Map<?, ?> cells = worldOf(snap.get(WORLD));
        int radius = radiusOf(snap.get(RADIUS));
        List<String> rows = new ArrayList<String>(2 * radius + 1);
        for (int dz = -radius; dz <= radius; dz++) {
            StringBuilder row = new StringBuilder(2 * radius + 1);
            for (int dx = -radius; dx <= radius; dx++) {
                row.append(glyphAt(cells, (pos[0] + dx) + "," + (pos[1] + dz)));
            }
            rows.add(row.toString());
        }
        return Collections.unmodifiableList(rows);
    }

    static int[] posOf(Object raw) {
        if (raw == null) {
            throw new IllegalArgumentException(
                    "E_MINIMAP_PLAYER:missing <minimap:player> (want \"x,z\")");
        }
        if (!(raw instanceof String)) {
            throw new IllegalArgumentException(
                    "E_MINIMAP_PLAYER:type <" + raw + "> (want \"x,z\")");
        }
        String[] parts = ((String) raw).split(",", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "E_MINIMAP_PLAYER:shape <" + raw + "> (want \"x,z\")");
        }
        try {
            return new int[]{Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1])};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "E_MINIMAP_PLAYER:shape <" + raw + "> (want \"x,z\")");
        }
    }

    static Map<?, ?> worldOf(Object raw) {
        if (raw == null) {
            throw new IllegalArgumentException(
                    "E_MINIMAP_WORLD:missing <minimap:world>");
        }
        if (!(raw instanceof Map)) {
            throw new IllegalArgumentException(
                    "E_MINIMAP_WORLD:type <" + raw + "> (want cell map)");
        }
        return (Map<?, ?>) raw;
    }

    static int radiusOf(Object raw) {
        if (raw == null) {
            throw new IllegalArgumentException(
                    "E_MINIMAP_RADIUS:missing <minimap:radius>");
        }
        if (!(raw instanceof Number)) {
            throw new IllegalArgumentException(
                    "E_MINIMAP_RADIUS:type <" + raw + "> (want u32 number)");
        }
        int radius = ((Number) raw).intValue();
        if (radius <= 0 || radius > MAX_RADIUS) {
            throw new IllegalArgumentException("E_MINIMAP_RADIUS:range <"
                    + raw + "> (want 1.." + MAX_RADIUS + ")");
        }
        return radius;
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
