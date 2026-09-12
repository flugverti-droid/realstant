package com.realstant.nativegame;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

public final class AuthView extends FrameLayout {
    public interface Listener { void onLogin(String name); }
    private final DatabaseHelper db; private final Listener listener; private final EditText name=new EditText(getContext()); private final EditText pass=new EditText(getContext());
    private final TextView title=new TextView(getContext()),hint=new TextView(getContext()); private final Button action=new Button(getContext()); private boolean register=false;
    public AuthView(Context c,Listener l){super(c);listener=l;db=new DatabaseHelper(c);setWillNotDraw(false);build();}
    private GradientDrawable bg(int color,float r){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(r);return g;}
    private void build(){
        setBackground(bg(0xFF081018,0));
        LinearLayout box=new LinearLayout(getContext());box.setOrientation(LinearLayout.VERTICAL);box.setPadding(55,40,55,40);box.setGravity(Gravity.CENTER_HORIZONTAL);box.setBackground(bg(0xEE121C26,34));
        LayoutParams bp=new LayoutParams(560,LayoutParams.WRAP_CONTENT,Gravity.CENTER);bp.setMargins(0,0,0,0);addView(box,bp);
        title.setText("REALSTANT");title.setTextColor(Color.WHITE);title.setTextSize(34);title.setGravity(Gravity.CENTER);box.addView(title,new LinearLayout.LayoutParams(-1,70));
        hint.setText("ВХОД В ПРОФИЛЬ");hint.setTextColor(0xFF70E6B1);hint.setTextSize(14);hint.setGravity(Gravity.CENTER);box.addView(hint,new LinearLayout.LayoutParams(-1,40));
        style(name,"Никнейм");style(pass,"Пароль");pass.setInputType(0x81);box.addView(name);box.addView(pass);
        action.setText("ВОЙТИ");action.setTextColor(Color.WHITE);action.setTextSize(15);action.setBackground(bg(0xFF2CCF91,22));box.addView(action,new LinearLayout.LayoutParams(-1,62));
        Button swap=new Button(getContext());swap.setText("Создать новый аккаунт");swap.setTextColor(0xFFB8C6D0);swap.setBackgroundColor(Color.TRANSPARENT);box.addView(swap,new LinearLayout.LayoutParams(-1,55));
        action.setOnClickListener(v->submit());swap.setOnClickListener(v->{register=!register;hint.setText(register?"РЕГИСТРАЦИЯ":"ВХОД В ПРОФИЛЬ");action.setText(register?"СОЗДАТЬ АККАУНТ":"ВОЙТИ");swap.setText(register?"У меня уже есть аккаунт":"Создать новый аккаунт");});
    }
    private void style(EditText e,String s){e.setHint(s);e.setHintTextColor(0xFF738391);e.setTextColor(Color.WHITE);e.setTextSize(16);e.setSingleLine();e.setPadding(20,0,20,0);e.setBackground(bg(0xFF202D38,18));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,58);p.setMargins(0,7,0,10);e.setLayoutParams(p);}
    private void submit(){String n=name.getText().toString().trim(),p=pass.getText().toString();boolean ok=register?db.register(n,p):db.login(n,p);if(ok){listener.onLogin(n);}else{Toast.makeText(getContext(),register?"Никнейм занят или данные слишком короткие":"Неверный никнейм или пароль",Toast.LENGTH_SHORT).show();}}
}
