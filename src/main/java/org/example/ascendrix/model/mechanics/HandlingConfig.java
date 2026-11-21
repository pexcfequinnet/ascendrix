package org.example.ascendrix.model.mechanics;

public class HandlingConfig {

    public static final long DEFAULT_DAS_NS = 135_000_000L; // 135 ms
    public static final long DEFAULT_ARR_NS = 33_000_000L;  // 33 ms
    public static final long DEFAULT_SDF_NS = 50_000_000L;  // 50 ms (20x at 1G)

    private long dasNs;
    private long arrNs;
    private long sdfNs;
    private boolean instantArr;
    private boolean instantSdf;

    public HandlingConfig() {
        this(DEFAULT_DAS_NS, DEFAULT_ARR_NS, DEFAULT_SDF_NS, false, false);
    }

    public HandlingConfig(long dasNs,
                          long arrNs,
                          long sdfNs,
                          boolean instantArr,
                          boolean instantSdf) {
        this.dasNs = Math.max(0L, dasNs);
        this.arrNs = Math.max(0L, arrNs);
        this.sdfNs = Math.max(0L, sdfNs);
        this.instantArr = instantArr;
        this.instantSdf = instantSdf;
    }

    public static HandlingConfig defaultConfig() {
        return new HandlingConfig();
    }

    public static HandlingConfig ofMs(double dasMs,
                                      double arrMs,
                                      double sdfMs,
                                      boolean instantArr,
                                      boolean instantSdf) {
        return new HandlingConfig(
                Math.round(dasMs * 1_000_000.0),
                Math.round(arrMs * 1_000_000.0),
                Math.round(sdfMs * 1_000_000.0),
                instantArr,
                instantSdf
        );
    }

    public HandlingConfig copy() {
        return new HandlingConfig(dasNs, arrNs, sdfNs, instantArr, instantSdf);
    }

    public long getDasNs()                      { return dasNs; }
    public void setDasNs(long dasNs)            { this.dasNs = Math.max(0L, dasNs); }

    public long getArrNs()                      { return arrNs; }
    public void setArrNs(long arrNs)            { this.arrNs = Math.max(0L, arrNs); }

    public long getSdfNs()                      { return sdfNs; }
    public void setSdfNs(long sdfNs)            { this.sdfNs = Math.max(0L, sdfNs); }

    public boolean isInstantArr()               { return instantArr; }
    public void setInstantArr(boolean instant)  { this.instantArr = instant; }

    public boolean isInstantSdf()               { return instantSdf; }
    public void setInstantSdf(boolean instant)  { this.instantSdf = instant; }
}