package com.realstant.nativegame.modules.ui;

/** Isolated RealStant subsystem component. */
public final class Ui06 {
    private float state;
    public void reset() { state = 0f; }
    public void tick(float dt) { state += dt; }
    public float value() { return state; }
}
