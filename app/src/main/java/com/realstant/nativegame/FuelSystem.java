package com.realstant.nativegame; public final class FuelSystem { public float fuel=100; public void burn(float speed,float dt){fuel=Math.max(0,fuel-speed*dt*.002f);} }
