package com.realstant.nativegame;

import android.app.Activity;import android.os.Bundle;import android.view.*;import android.content.*;import android.content.pm.ActivityInfo;import android.graphics.Color;import android.widget.*;

public class MainActivity extends Activity{
 private FrameLayout root;private RealStantView game;private HudView hud;private AnimatedMenuView menu;private ControlEditorView controls;private Profile profile;private DatabaseHelper db;
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);getWindow().getDecorView().setSystemUiVisibility(5894);setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);db=new DatabaseHelper(this);showAuth();}
 private void showAuth(){root=new FrameLayout(this);AuthView auth=new AuthView(this,n->{profile=db.load(n);continueAfterLogin();});root.addView(auth);setContentView(root);}
 private void continueAfterLogin(){
   if(AssetPreloader.isReady(this)){showGame();return;}
   AssetLoadingView loading=new AssetLoadingView(this);root.removeAllViews();root.addView(loading,new FrameLayout.LayoutParams(-1,-1));setContentView(root);
   AssetPreloader.prepare(this,new AssetPreloader.Listener(){public void onProgress(int d,int t,String n){loading.setProgress(d,t,n);}public void onReady(){runOnUiThread(()->showGame());}});
 }
 private void showGame(){root=new FrameLayout(this);try{game=new RealStantView(this);root.addView(game,new FrameLayout.LayoutParams(-1,-1));}catch(Throwable ex){TextView e=new TextView(this);e.setText("Не удалось запустить 3D режим\n"+ex.getClass().getSimpleName());e.setTextColor(Color.WHITE);e.setTextSize(18);e.setGravity(Gravity.CENTER);e.setBackgroundColor(0xFF0B1117);root.addView(e);setContentView(root);return;}
  hud=new HudView(this,game.getRenderer());menu=new AnimatedMenuView(this,new AnimatedMenuView.Listener(){public void play(){menu.setVisibility(View.GONE);hud.setGameVisible(true);game.getRenderer().setPaused(false);}public void garage(){Toast.makeText(MainActivity.this,"Гараж: BMX, PITBIKE, SCOOTER, MTB, ATV, MINI BIKE, BOARD",Toast.LENGTH_LONG).show();}public void settings(){controls.setVisibility(View.VISIBLE);}});
  controls=new ControlEditorView(this);controls.setVisibility(View.GONE);root.addView(hud,new FrameLayout.LayoutParams(-1,-1));root.addView(menu,new FrameLayout.LayoutParams(-1,-1));root.addView(controls,new FrameLayout.LayoutParams(-1,-1));hud.setGameVisible(false);game.getRenderer().setPaused(true);setContentView(root);
 }
 @Override protected void onPause(){super.onPause();if(game!=null)game.onPause();}
 @Override protected void onResume(){super.onResume();if(game!=null)game.onResume();}
 @Override protected void onDestroy(){if(db!=null)db.close();super.onDestroy();}
}
