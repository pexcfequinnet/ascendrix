package org.example.ascendrix;

public class MasterGravityTable {
    private record GravityEntry(int level, int gravity) {}

    private static final GravityEntry[] TABLE = {
            new GravityEntry(0,   1024),
            new GravityEntry(30,  1536),
            new GravityEntry(35,  2048),
            new GravityEntry(40,  2560),
            new GravityEntry(50,  3072),
            new GravityEntry(60,  4096),
            new GravityEntry(70,  8192),
            new GravityEntry(80,  12288),
            new GravityEntry(90,  16384),
            new GravityEntry(100, 20480),
            new GravityEntry(120, 24576),
            new GravityEntry(140, 28672),
            new GravityEntry(160, 32768),
            new GravityEntry(170, 36864),
            new GravityEntry(200, 1024),
            new GravityEntry(220, 8192),
            new GravityEntry(230, 16384),
            new GravityEntry(233, 24576),
            new GravityEntry(236, 32768),
            new GravityEntry(239, 40960),
            new GravityEntry(243, 49152),
            new GravityEntry(247, 57344),
            new GravityEntry(251, 65536),
            new GravityEntry(300, 131072),
            new GravityEntry(330, 196608),
            new GravityEntry(360, 262144),
            new GravityEntry(400, 327680),
            new GravityEntry(420, 262144),
            new GravityEntry(450, 196608),
            new GravityEntry(500, 1310720)
    };
    public static long toNs(int gravity) {
        // 1G (65536) = 1 cell per 1/60s in TGM spec
        // Time per cell in ns = (65536 / gravity) * (1_000_000_000 / 60)
        return (long)(65536.0 / gravity * (1_000_000_000.0 / 60));
    }
    public static int getGravityWithMultiplier(int level, int speedLevel) {
        int base = getGravity(level);
        // 20G is already a hard cap, don't multiply further
        if (base >= 1310720) return 1310720;
        double multiplier = switch(speedLevel) {
            case 2 -> 1.2;
            case 3 -> 1.4;
            case 4 -> 1.6;
            default -> 1.0;
        };
        return Math.min((int)(base * multiplier), 1310720);
    }

    public static boolean is20G(int level) {
        return getGravity(level) >= 1310720;
    }

    public static int getGravity(int level) {
        // Past 500 is always 20G
        if (level >= 500) return 1310720;

        for (int i = 0; i < TABLE.length - 1; i++) {
            GravityEntry current = TABLE[i];
            GravityEntry next    = TABLE[i + 1];

            if (level >= current.level() && level < next.level()) {
                // Linear interpolation between entries
                double t = (double)(level - current.level()) / (next.level() - current.level());
                return (int)(current.gravity() + t * (next.gravity() - current.gravity()));
            }
        }
        return TABLE[0].gravity();
    }
}
