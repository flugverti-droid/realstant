package com.realstant.nativegame;
public final class ControlLayout {
    public float leftX=.08f,leftY=.80f,rightX=.23f,rightY=.80f;
    public float gasX=.86f,gasY=.78f,brakeX=.71f,brakeY=.78f;
    public float buttonScale=1.0f,opacity=.82f;
    public ControlLayout copy(){ ControlLayout c=new ControlLayout();
        c.leftX=leftX;c.leftY=leftY;c.rightX=rightX;c.rightY=rightY;
        c.gasX=gasX;c.gasY=gasY;c.brakeX=brakeX;c.brakeY=brakeY;
        c.buttonScale=buttonScale;c.opacity=opacity; return c; }
}
