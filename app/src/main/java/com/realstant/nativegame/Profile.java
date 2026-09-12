package com.realstant.nativegame; public final class Profile { public String nickname="Driver"; public int level=1,xp=0; public void addXp(int n){xp+=n;while(xp>=1000){xp-=1000;level++;}} }
