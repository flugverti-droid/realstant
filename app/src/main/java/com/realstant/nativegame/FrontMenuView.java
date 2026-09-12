package com.realstant.nativegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

public final class FrontMenuView extends View {
    public interface Listener { void onPlay(); void onGarage(); void onSettings(); void onBack(); }
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Listener listener;
    private final RectF play=new RectF(), garage=new RectF(), settings=new RectF(), back=new RectF();
    private String panel=null, panelText="";

    public FrontMenuView(Context c, Listener l){ super(c); listener=l; setLayerType(View.LAYER_TYPE_SOFTWARE,null); }

    public void showMain(){ panel=null; invalidate(); }
    public void showPanel(String title,String text){ panel=title; panelText=text; invalidate(); }

    @Override protected void onDraw(Canvas c){
        float w=getWidth(), h=getHeight();
        p.setStyle(Paint.Style.FILL); p.setColor(0xE9161B21); c.drawRect(0,0,w,h,p);
        p.setTypeface(Typeface.create("sans",Typeface.BOLD));
        p.setTextAlign(Paint.Align.CENTER);
        p.setColor(0xFFFFFFFF); p.setTextSize(Math.max(34,w*.045f)); c.drawText("REALSTANT",w/2,h*.18f,p);
        p.setColor(0xFF63E6A6); p.setTextSize(Math.max(13,w*.016f)); c.drawText("REALSTANT • NATIVE 3D",w/2,h*.23f,p);

        if(panel==null){
            play.set(w*.34f,h*.33f,w*.66f,h*.46f);
            garage.set(w*.34f,h*.50f,w*.66f,h*.61f);
            settings.set(w*.34f,h*.65f,w*.66f,h*.76f);
            button(c,play,"ИГРАТЬ",true); button(c,garage,"ГАРАЖ",false); button(c,settings,"НАСТРОЙКИ",false);
            p.setColor(0xFF7F8B96); p.setTextSize(12); c.drawText("Аркадные заезды • трюки • комбо • испытания",w/2,h*.86f,p);
        } else {
            p.setColor(0xFFF4F6F8); p.setTextSize(27); c.drawText(panel,w/2,h*.34f,p);
            p.setTypeface(Typeface.create("sans",Typeface.NORMAL)); p.setTextSize(15); p.setColor(0xFFD0D7DE);
            String[] lines=panelText.split("\\n");
            float y=h*.43f; for(String s:lines){c.drawText(s,w/2,y,p);y+=27;}
            back.set(w*.40f,h*.73f,w*.60f,h*.83f); button(c,back,"НАЗАД",false);
        }
        p.setTextAlign(Paint.Align.LEFT);
    }

    private void button(Canvas c,RectF r,String s,boolean primary){
        p.setStyle(Paint.Style.FILL); p.setColor(primary?0xFF36C98A:0xFF273039);
        c.drawRoundRect(r,24,24,p);
        p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(2); p.setColor(primary?0xFF7BF0BA:0xFF44515B); c.drawRoundRect(r,24,24,p);
        p.setStyle(Paint.Style.FILL); p.setColor(Color.WHITE); p.setTextSize(16); p.setTypeface(Typeface.create("sans",Typeface.BOLD));
        c.drawText(s,r.centerX(),r.centerY()+6,p); // centered below via Paint align set in caller
    }

    @Override public boolean onTouchEvent(MotionEvent e){
        if(e.getAction()!=MotionEvent.ACTION_UP) return true;
        float x=e.getX(), y=e.getY();
        if(panel==null){
            if(play.contains(x,y)){listener.onPlay(); return true;}
            if(garage.contains(x,y)){listener.onGarage(); return true;}
            if(settings.contains(x,y)){listener.onSettings(); return true;}
        } else if(back.contains(x,y)){listener.onBack();}
        return true;
    }
}
