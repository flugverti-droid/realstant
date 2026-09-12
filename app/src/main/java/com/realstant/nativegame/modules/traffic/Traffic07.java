package com.realstant.nativegame.modules.traffic;

/** Isolated RealStant subsystem component. */
public final class Traffic07 {
    private float state;
    public void reset() { state = 0f; }
    public void tick(float dt) { state += dt; }
    public float value() { return state; }
}
