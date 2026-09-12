package com.realstant.nativegame; public final class LoadingScreen { private float progress; public void set(float p){progress=Math.max(0,Math.min(1,p));} public float progress(){return progress;} }
