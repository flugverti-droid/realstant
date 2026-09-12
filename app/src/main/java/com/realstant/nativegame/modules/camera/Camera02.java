package com.realstant.nativegame.modules.camera;

/** Isolated RealStant subsystem component. */
public final class Camera02 {
    private float state;
    public void reset() { state = 0f; }
    public void tick(float dt) { state += dt; }
    public float value() { return state; }
}
