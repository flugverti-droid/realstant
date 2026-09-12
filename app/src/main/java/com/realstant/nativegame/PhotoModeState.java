package com.realstant.nativegame;
public final class PhotoModeState {
    public boolean enabled;
    public float yaw,pitch,zoom=1f;
    public void reset(){ enabled=false; yaw=0; pitch=0; zoom=1f; }
}
