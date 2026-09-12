package com.realstant.nativegame; public final class CollisionWorld { public boolean onRoad(float x){return Math.abs(x)<GameConfig.ROAD_WIDTH*.65f;} public float ground(float x,float z){return 0f;} }
