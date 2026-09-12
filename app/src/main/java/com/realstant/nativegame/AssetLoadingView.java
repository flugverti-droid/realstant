package com.realstant.nativegame;

import android.content.Context;
import android.graphics.*;
import android.view.View;

public final class AssetLoadingView extends View {
    private int done=0,total=1; private String current="Подготовка ассетов…";
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    public AssetLoadingView(Context c){super(c); p.setTypeface(Typeface.create("sans",Typeface.NORMAL)); setBackgroundColor(0xFF081018);}
    public void setProgress(int d,int t,String n){done=d;total=Math.max(1,t);current=n;postInvalidate();}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c); float w=getWidth(),h=getHeight();
        p.setShader(new LinearGradient(0,0,w,h,0xFF081018,0xFF172A2B,Shader.TileMode.CLAMP)); c.drawRect(0,0,w,h,p); p.setShader(null);
        p.setTextAlign(Paint.Align.CENTER); p.setColor(Color.WHITE); p.setTypeface(Typeface.create("sans",Typeface.BOLD)); p.setTextSize(Math.min(w*.075f,54)); c.drawText("REALSTANT",w/2,h*.28f,p);
        p.setColor(0xFF6DE6B0); p.setTextSize(18); c.drawText("ПОДГОТОВКА ИГРОВЫХ АССЕТОВ",w/2,h*.36f,p);
        float bw=Math.min(w*.72f,820), bh=26, x=(w-bw)/2, y=h*.53f;
        p.setColor(0xFF24333D); c.drawRoundRect(x,y,x+bw,y+bh,bh/2,bh/2,p);
        p.setColor(0xFF2CD392); c.drawRoundRect(x,y,x+bw*(done/(float)total),y+bh,bh/2,bh/2,p);
        p.setColor(Color.WHITE); p.setTypeface(Typeface.create("sans",Typeface.BOLD)); p.setTextSize(22); c.drawText(Math.round(done*100f/total)+"%",w/2,y+58,p);
        p.setTypeface(Typeface.create("sans",Typeface.NORMAL)); p.setColor(0xFF9FB0BA); p.setTextSize(14); String s="Файлов: "+done+" / "+total; c.drawText(s,w/2,y+88,p);
        c.drawText(current.length()>52?current.substring(0,52)+"…":current,w/2,y+116,p);
    }
}
