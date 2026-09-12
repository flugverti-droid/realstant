package com.realstant.nativegame;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class RealStantView extends GLSurfaceView {
    private final RealStantRenderer renderer;
    public RealStantView(Context c){
        super(c);
        setEGLContextClientVersion(3);
        renderer=new RealStantRenderer(c);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setFocusable(true); requestFocus();
    }
    public RealStantRenderer getRenderer(){return renderer;}
}
