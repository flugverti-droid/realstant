package com.realstant.nativegame.modules.input;

/** Isolated RealStant subsystem component. */
public final class Input09 {
    private float state;
    public void reset() { state = 0f; }
    public void tick(float dt) { state += dt; }
    public float value() { return state; }
}
