package com.realstant.nativegame;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
public final class Vibration {
    private final Vibrator vibrator;
    public Vibration(Context c){ vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE); }
    public void tap(){ try{ if(vibrator!=null && Build.VERSION.SDK_INT>=26) vibrator.vibrate(VibrationEffect.createOneShot(18,60)); }catch(Exception ignored){} }
}
