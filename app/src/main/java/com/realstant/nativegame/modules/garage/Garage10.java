package com.realstant.nativegame.modules.garage;

/** Isolated RealStant subsystem component. */
public final class Garage10 {
    private float state;
    public void reset() { state = 0f; }
    public void tick(float dt) { state += dt; }
    public float value() { return state; }
}
