package com.realstant.nativegame.modules.ai;

/** Isolated RealStant subsystem component. */
public final class Ai05 {
    private float state;
    public void reset() { state = 0f; }
    public void tick(float dt) { state += dt; }
    public float value() { return state; }
}
