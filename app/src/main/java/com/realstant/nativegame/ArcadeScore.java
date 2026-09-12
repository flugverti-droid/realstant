package com.realstant.nativegame;
public final class ArcadeScore {
    private long score;
    public void addDistance(float km){ score += Math.max(0,(long)(km*100)); }
    public void addCleanSection(){ score += 250; }
    public void addNearMiss(){ score += 150; }
    public long value(){ return score; }
    public void reset(){ score=0; }
}
