package com.realstant.nativegame;

import android.graphics.*;import android.view.*;import android.content.Context;import java.util.*;

public final class AnimatedMenuView extends View {
 public interface Listener{void play();void garage();void settings();}
 private final Paint p=new Paint(3);private final Listener l;private final RectF play=new RectF(),garage=new RectF(),settings=new RectF();private long start=System.currentTimeMillis();
 public AnimatedMenuView(Context c,Listener x){super(c);l=x;setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
 protected void onDraw(Canvas c){float w=getWidth(),h=getHeight(),t=(System.currentTimeMillis()-start)/1000f;LinearGradient sky=new LinearGradient(0,0,0,h,0xFF172A50,0xFFE98B55,Shader.TileMode.CLAMP);p.setShader(sky);c.drawRect(0,0,w,h,p);p.setShader(null);
  p.setColor(0xFFFFC76A);float sx=w*.76f,sy=h*.25f+(float)Math.sin(t*.25)*8;c.drawCircle(sx,sy,Math.min(w,h)*.09f,p);
  p.setColor(0xFF10151B);Path m=new Path();m.moveTo(0,h*.63f);for(int i=0;i<=12;i++)m.lineTo(i*w/12f,h*(.55f+.09f*(float)Math.sin(i*1.7)));m.lineTo(w,h);m.lineTo(0,h);m.close();c.drawPath(m,p);
  for(int i=0;i<18;i++){float x=(i*97)%((int)w),y=h*.1f+(i*43)%((int)(h*.45f));p.setColor(0x22FFFFFF);c.drawCircle(x,y,2+(i%3),p);}
  p.setTextAlign(Paint.Align.CENTER);p.setTypeface(Typeface.create("sans",1));p.setShadowLayer(18,0,4,0x99000000);p.setColor(Color.WHITE);p.setTextSize(Math.max(38,w*.075f));c.drawText("REALSTANT",w/2,h*.20f,p);p.clearShadowLayer();p.setTextSize(13);p.setColor(0xFFE8F1F4);c.drawText("NATIVE ARCADE • RIDE • TRICKS",w/2,h*.25f,p);
  play.set(w*.36f,h*.39f,w*.64f,h*.50f);garage.set(w*.36f,h*.53f,w*.64f,h*.63f);settings.set(w*.36f,h*.66f,w*.64f,h*.76f);button(c,play,"ИГРАТЬ",true);button(c,garage,"ГАРАЖ",false);button(c,settings,"НАСТРОЙКИ",false);p.setColor(0xAAFFFFFF);p.setTextSize(11);c.drawText("BMX • PITBIKE • SCOOTER • MTB • ATV • MINI BIKE • BOARD",w/2,h*.90f,p);p.setTextAlign(Paint.Align.LEFT);invalidate();}
 private void button(Canvas c,RectF r,String s,boolean primary){p.setShader(new LinearGradient(0,r.top,0,r.bottom,primary?0xFF39E29A:0xCC1E2A35,primary?0xFF1BAF79:0xCC121A22,Shader.TileMode.CLAMP));c.drawRoundRect(r,25,25,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(primary?0xAAFFFFFF:0x6689A1AF);c.drawRoundRect(r,25,25,p);p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);p.setTextSize(16);p.setTypeface(Typeface.DEFAULT_BOLD);c.drawText(s,r.centerX(),r.centerY()+6,p);}
 public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=1)return true;float x=e.getX(),y=e.getY();if(play.contains(x,y))l.play();else if(garage.contains(x,y))l.garage();else if(settings.contains(x,y))l.settings();return true;}
}
