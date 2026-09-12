package com.realstant.nativegame.modules.render;

/** Isolated RealStant subsystem component. */
public final class Render09 {
    private float state;
    public void reset() { state = 0f; }
    public void tick(float dt) { state += dt; }
    public float value() { return state; }
}
