package com.realstant.nativegame;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.security.MessageDigest;

public final class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB="realstant.db";
    public DatabaseHelper(Context c){super(c,DB,null,1);}
    @Override public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT,nickname TEXT UNIQUE NOT NULL,password_hash TEXT NOT NULL,level INTEGER NOT NULL DEFAULT 1,xp INTEGER NOT NULL DEFAULT 0,coins INTEGER NOT NULL DEFAULT 100)");
    }
    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){}
    public boolean register(String name,String pass){
        if(name==null||pass==null||name.trim().length()<3||pass.length()<4)return false;
        SQLiteDatabase db=getWritableDatabase(); ContentValues v=new ContentValues();
        v.put("nickname",name.trim());v.put("password_hash",hash(pass));
        try{return db.insertOrThrow("users",null,v)>0;}catch(Exception e){return false;}
    }
    public boolean login(String name,String pass){
        SQLiteDatabase db=getReadableDatabase(); Cursor c=db.query("users",new String[]{"id"},"nickname=? AND password_hash=?",new String[]{name.trim(),hash(pass)},null,null,null);
        boolean ok=c.moveToFirst();c.close();return ok;
    }
    public Profile load(String name){
        Profile p=new Profile();SQLiteDatabase db=getReadableDatabase();Cursor c=db.query("users",new String[]{"level","xp"},"nickname=?",new String[]{name},null,null,null);
        if(c.moveToFirst()){p.nickname=name;p.level=c.getInt(0);p.xp=c.getInt(1);}c.close();return p;
    }
    public void save(Profile p){SQLiteDatabase db=getWritableDatabase();ContentValues v=new ContentValues();v.put("level",p.level);v.put("xp",p.xp);db.update("users",v,"nickname=?",new String[]{p.nickname});}
    private static String hash(String s){try{byte[] b=MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"));StringBuilder x=new StringBuilder();for(byte q:b)x.append(String.format("%02x",q));return x.toString();}catch(Exception e){return s;}}
}
