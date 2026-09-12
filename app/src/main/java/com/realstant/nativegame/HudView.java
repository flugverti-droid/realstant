package com.realstant.nativegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import java.util.Locale;

public class HudView extends View {
    private final RealStantRenderer r;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF left=new RectF(), right=new RectF(), gas=new RectF(), brake=new RectF(), trick=new RectF(), cam=new RectF(), pause=new RectF(), bike=new RectF();
    private boolean ld,rd,gd,bd,td;
    private boolean gameVisible=true;

    public HudView(Context c, RealStantRenderer rr){super(c);r=rr;setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
    public void setGameVisible(boolean v){gameVisible=v;invalidate();}

    @Override protected void onDraw(Canvas c){
        if(!gameVisible)return;
        float w=getWidth(),h=getHeight();
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xB5172027);c.drawRoundRect(new RectF(20,18,185,100),20,20,p);
        c.drawRoundRect(new RectF(198,18,365,100),20,20,p);
        c.drawRoundRect(new RectF(w*.38f,18,w*.62f,70),22,22,p);
        pause.set(w-86,18,w-18,84); bike.set(w-245,18,w-173,84);p.setColor(0xB5172027);c.drawRoundRect(pause,20,20,p);

        p.setColor(0xFF8EA2AD);p.setTextSize(12);p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        c.drawText("SPEED",38,42,p);c.drawText("SCORE",216,42,p);
        p.setColor(0xFFFFFFFF);p.setTextSize(28);c.drawText(String.format(Locale.US,"%d",(int)r.getSpeedKmh()),38,76,p);
        c.drawText(String.format(Locale.US,"%d",r.getScore()),216,76,p);
        p.setColor(0xFF63E6A6);p.setTextSize(12);c.drawText("KM/H",103,76,p);
        p.setTextSize(14);c.drawText(r.getTrickText(),w*.50f,51,p);

        left.set(w*.035f,h*.72f,w*.17f,h*.92f);
        right.set(w*.185f,h*.72f,w*.32f,h*.92f);
        brake.set(w*.62f,h*.72f,w*.77f,h*.88f);
        gas.set(w*.78f,h*.63f,w*.94f,h*.92f);
        trick.set(w*.47f,h*.72f,w*.61f,h*.87f);
        cam.set(w-165,18,w-94,84);
        btn(c,left,"◀",ld);btn(c,right,"▶",rd);btn(c,brake,"ТОРМОЗ",bd);btn(c,gas,"ГАЗ",gd);btn(c,trick,"TRICK",td);
        p.setColor(0xB5172027);c.drawRoundRect(cam,20,20,p);p.setColor(Color.WHITE);p.setTextSize(12);c.drawText("CAM",w-143,53,p);
        p.setColor(0xB5172027);c.drawRoundRect(bike,20,20,p);p.setColor(Color.WHITE);p.setTextSize(11);c.drawText("BIKE",w-228,53,p);
        p.setColor(0xFF63E6A6);p.setTextSize(12);c.drawText(r.getTransportName(),w*.50f,67,p);
        c.drawRoundRect(pause,20,20,p);p.setTextSize(23);c.drawText("Ⅱ",w-63,60,p);

        if(r.isPaused()){
            p.setColor(0x88000000);c.drawRect(0,0,w,h,p);
            p.setColor(Color.WHITE);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(32);c.drawText("ПАУЗА",w/2,h/2-5,p);
            p.setTextSize(14);c.drawText("Нажми Ⅱ, чтобы продолжить",w/2,h/2+25,p);p.setTextAlign(Paint.Align.LEFT);
        }
        invalidate();
    }
    private void btn(Canvas c,RectF rr,String s,boolean down){
        p.setStyle(Paint.Style.FILL);p.setColor(down?0xE93DD79A:0xB5172027);c.drawRoundRect(rr,rr.height()/2,rr.height()/2,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xA95CE4AE);c.drawRoundRect(rr,rr.height()/2,rr.height()/2,p);
        p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(s.length()>5?11:19);
        c.drawText(s,rr.centerX(),rr.centerY()+6,p);p.setTextAlign(Paint.Align.LEFT);
    }
    @Override public boolean onTouchEvent(MotionEvent e){
        if(!gameVisible)return true;
        int action=e.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN||action==MotionEvent.ACTION_POINTER_DOWN||action==MotionEvent.ACTION_MOVE){
            boolean any=false;
            ld=rd=gd=bd=td=false;
            for(int i=0;i<e.getPointerCount();i++){
                float x=e.getX(i),y=e.getY(i);
                if(left.contains(x,y))ld=true;if(right.contains(x,y))rd=true;if(gas.contains(x,y))gd=true;
                if(brake.contains(x,y))bd=true;if(trick.contains(x,y))td=true;
                if(cam.contains(x,y)) {r.nextCamera(); any=true;}
                if(bike.contains(x,y)) {r.nextTransport(); any=true;}
            }
            if(pause.contains(e.getX(),e.getY()) && action!=MotionEvent.ACTION_MOVE){r.togglePause();any=true;}
            r.setInput(ld,rd,gd,bd,td); return true;
        }
        if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){ld=rd=gd=bd=td=false;r.setInput(false,false,false,false,false);return true;}
        return true;
    }
}
