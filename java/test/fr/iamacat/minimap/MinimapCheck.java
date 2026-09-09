package fr.iamacat.minimap;

import fr.iamacat.spi.MatouId;
import fr.iamacat.spi.Snapshot;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * M3 client self-test (no JUnit on this gate): every violation prints
 * {@code FAIL minimap : ...} and exits 1. Lambdas are legal (the gate
 * compiles {@code --release 8}), so refusals are one-liners, never
 * anonymous classes. Run by tools/check.sh against the {@code ../spi}
 * sibling checkout.
 */
public final class MinimapCheck {
    private MinimapCheck() {}

    private static void check(boolean cond, String what) {
        if (!cond) {
            System.out.println("FAIL minimap : " + what);
            System.exit(1);
        }
        System.out.println("ok minimap : " + what);
    }

    private static void expectRefused(Runnable r, String what) {
        try {
            r.run();
        } catch (IllegalArgumentException e) {
            System.out.println("ok minimap : refused " + what
                    + " (" + e.getMessage() + ")");
            return;
        }
        System.out.println("FAIL minimap : accepted " + what);
        System.exit(1);
    }

    private static void expectNullRefused(Runnable r, String what) {
        try {
            r.run();
        } catch (NullPointerException e) {
            System.out.println("ok minimap : refused " + what
                    + " (" + e.getMessage() + ")");
            return;
        }
        System.out.println("FAIL minimap : accepted " + what);
        System.exit(1);
    }

    private static Snapshot snap(String player, Map<String, String> world,
            Object radius) {
        Map<MatouId, Object> states = new HashMap<MatouId, Object>();
        if (player != null) {
            states.put(MinimapJob.PLAYER, player);
        }
        if (world != null) {
            states.put(MinimapJob.WORLD, world);
        }
        if (radius != null) {
            states.put(MinimapJob.RADIUS, radius);
        }
        return new Snapshot(3L, states);
    }

    private static Map<String, String> world() {
        Map<String, String> world = new HashMap<String, String>();
        world.put("0,0", "#");
        world.put("1,0", "~");
        return world;
    }

    public static void main(String[] args) {
        // --- ids live in the frozen minimap namespace ---
        check(MinimapJob.PLAYER.equals(
                MatouId.of("minimap", "player")), "player id");
        check(MinimapJob.WORLD.toString().equals("minimap:world"),
                "world id");
        check(MinimapJob.RADIUS.toString().equals("minimap:radius"),
                "radius id");
        expectRefused(() -> {
            MatouId.parse("player");
        }, "bare ident");

        // --- golden 3x3: exact rows, void explicit around known cells ---
        final MinimapJob job = new MinimapJob();
        List<String> rows = job.decide(snap("0,0", world(), Long.valueOf(1L)));
        check(rows.equals(Arrays.asList("...", ".#~", "...")),
                "golden view");
        check(rows.equals(job.decide(snap("0,0", world(), Long.valueOf(1L)))),
                "view pure");

        // --- view follows the player, world stays put ---
        List<String> moved = job.decide(
                snap("1,0", world(), Long.valueOf(1L)));
        check(moved.equals(Arrays.asList("...", "#~.", "...")),
                "view follows player");
        check(moved.size() == 3 && moved.get(0).length() == 3,
                "view shaped");

        // --- empty world renders all void, never holes ---
        List<String> blank = job.decide(snap("5,5",
                new HashMap<String, String>(), Long.valueOf(1L)));
        check(blank.equals(Arrays.asList("...", "...", "...")),
                "void explicit");
        try {
            rows.add("xxx");
            check(false, "rows immutable");
        } catch (UnsupportedOperationException e) {
            System.out.println("ok minimap : rows immutable");
        }

        // --- max radius: boundary renders, shapes hold, still pure ---
        List<String> wide = job.decide(snap("0,0",
                new HashMap<String, String>(), Long.valueOf(8L)));
        check(wide.size() == 17, "max radius rows");
        boolean wideVoid = true;
        for (String row : wide) {
            wideVoid &= row.length() == 17
                    && row.equals(".................");
        }
        check(wideVoid, "max radius all void");
        check(wide.equals(job.decide(snap("0,0",
                new HashMap<String, String>(), Long.valueOf(8L)))),
                "max radius pure");

        // --- backend data: missing or malformed is refused, never guessed ---
        expectNullRefused(() -> {
            job.decide(null);
        }, "null snapshot");
        expectRefused(() -> {
            job.decide(snap(null, world(), Long.valueOf(1L)));
        }, "missing player");
        expectRefused(() -> {
            job.decide(snap("0,0", null, Long.valueOf(1L)));
        }, "missing world");
        expectRefused(() -> {
            job.decide(snap("0,0", world(), null));
        }, "missing radius");
        expectRefused(() -> {
            job.decide(snap("here", world(), Long.valueOf(1L)));
        }, "bad player shape");
        expectRefused(() -> {
            job.decide(snap("0,0", world(), Long.valueOf(0L)));
        }, "radius 0");
        expectRefused(() -> {
            job.decide(snap("0,0", world(), Long.valueOf(9L)));
        }, "radius over max");
        expectRefused(() -> {
            Map<String, String> bad = world();
            bad.put("0,0", "##");
            job.decide(snap("0,0", bad, Long.valueOf(1L)));
        }, "bad glyph");

        System.out.println("ok minimap : all");
    }
}
