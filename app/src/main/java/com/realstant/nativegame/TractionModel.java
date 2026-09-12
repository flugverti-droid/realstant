package com.realstant.nativegame; public final class TractionModel { public float grip=1; public float surfaceGrip(WeatherType w){return w==WeatherType.RAIN?.82f:w==WeatherType.SNOW?.58f:1f;} }
