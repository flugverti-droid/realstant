package com.realstant.nativegame;
import android.content.*;import android.graphics.*;import android.view.*;
public final class ControlEditorView extends View{
 private final Paint p=new Paint(3);private float gasX=.86f,gasY=.76f,steerY=.82f;private final RectF back=new RectF();private long t;
 public ControlEditorView(Context c){super(c);setBackgroundColor(0xEE0B1117);t=System.currentTimeMillis();}
 protected void onDraw(Canvas c){float w=getWidth(),h=getHeight();p.setTextAlign(Paint.Align.CENTER);p.setColor(Color.WHITE);p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(28);c.drawText("РЕДАКТОР УПРАВЛЕНИЯ",w/2,65,p);p.setTextSize(13);p.setColor(0xFF9FB0BC);c.drawText("Перетаскивай кнопки • положение сохраняется",w/2,92,p);p.setStyle(Paint.Style.STROKE);p.setColor(0xFF3CD59A);p.setStrokeWidth(2);c.drawRoundRect(new RectF(20,115,w-20,h-80),24,24,p);p.setStyle(Paint.Style.FILL);circle(c,w*.11f,h*steerY,"◀");circle(c,w*.23f,h*steerY,"▶");circle(c,w*gasX,h*gasY,"ГАЗ");circle(c,w*.67f,h*.78f,"ТОРМ");back.set(w*.42f,h-65,w*.58f,h-18);p.setColor(0xFF26343E);c.drawRoundRect(back,18,18,p);p.setColor(Color.WHITE);p.setTextSize(14);c.drawText("ГОТОВО",back.centerX(),back.centerY()+5,p);p.setTextAlign(Paint.Align.LEFT);}
 private void circle(Canvas c,float x,float y,String s){p.setColor(0xCC1E2B34);c.drawCircle(x,y,48,p);p.setStyle(Paint.Style.STROKE);p.setColor(0x883CD59A);c.drawCircle(x,y,48,p);p.setStyle(Paint.Style.FILL);p.setColor(Color.WHITE);p.setTextSize(13);p.setTextAlign(Paint.Align.CENTER);c.drawText(s,x,y+5,p);}
 public boolean onTouchEvent(MotionEvent e){if(e.getAction()==1&&back.contains(e.getX(),e.getY()))setVisibility(GONE);return true;}
}
