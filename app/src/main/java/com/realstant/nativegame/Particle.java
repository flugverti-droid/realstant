package com.realstant.nativegame; public final class Particle { public float x,y,z,vx,vy,vz,life,size; public void update(float dt){x+=vx*dt;y+=vy*dt;z+=vz*dt;life-=dt;} }
