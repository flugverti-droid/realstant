package com.realstant.nativegame;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** Native Java/OpenGL ES 3 renderer. No WebView, HTML or JavaScript. */
public class RealStantRenderer implements GLSurfaceView.Renderer {
    private final Context ctx;
    private int program, uMVP,uModel,uColor,uLight,uViewPos,uFog,uRough,uMetal,uEmissive;
    private int width,height;
    private final float[] proj=new float[16], view=new float[16], vp=new float[16], model=new float[16], mvp=new float[16];
    private Mesh cube,sphere,cyl,disc,cone;
    private final Mesh[] assetVehicles=new Mesh[7];
    private Mesh assetRamp,assetTrampoline,assetRail,assetBox,assetQuarterPipe,assetLanding,assetPlatform;
    private float carX=0, carZ=10, carYaw=0, speed=0, steer=0;
    private float camX=0,camY=3.5f,camZ=3,camLookY=1.0f;
    private float wheelSpin=0, distance=0;
    private long last;
    private boolean paused=false, snow=false;
    private boolean inLeft,inRight,inGas,inBrake,inTrick;
    private final List<Scenery> scenery=new ArrayList<>();
    private final List<Float> snowSeed=new ArrayList<>();
    private long score=0; private float stuntTime=0, bikePitch=0, bikeLean=0; private int cameraMode=0;
    private int transportType=0; private float riderY=0, verticalVel=0; private boolean airborne=false; private float spin=0;
    private String trickText="READY";

    static class Scenery { float x,z,s; int type; Scenery(float x,float z,float s,int t){this.x=x;this.z=z;this.s=s;this.type=t;} }

    public RealStantRenderer(Context c){ctx=c;}

    @Override public void onSurfaceCreated(GL10 gl,EGLConfig config){
        GLES30.glClearColor(0.42f,0.62f,0.76f,1f);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST); GLES30.glDepthFunc(GLES30.GL_LEQUAL);
        GLES30.glEnable(GLES30.GL_CULL_FACE); GLES30.glCullFace(GLES30.GL_BACK);
        try { program=buildProgram(VS,FS); }
        catch(Throwable ex) {
            // Never crash the app because of a shader/device capability issue.
            program=0; GLES30.glClearColor(0.08f,0.10f,0.12f,1f); return;
        }
        uMVP=GLES30.glGetUniformLocation(program,"uMVP"); uModel=GLES30.glGetUniformLocation(program,"uModel");
        uColor=GLES30.glGetUniformLocation(program,"uColor"); uLight=GLES30.glGetUniformLocation(program,"uLight");
        uViewPos=GLES30.glGetUniformLocation(program,"uViewPos"); uFog=GLES30.glGetUniformLocation(program,"uFog");
        uRough=GLES30.glGetUniformLocation(program,"uRough"); uMetal=GLES30.glGetUniformLocation(program,"uMetal"); uEmissive=GLES30.glGetUniformLocation(program,"uEmissive");
        if(program==0)return; cube=Mesh.cube(); sphere=Mesh.sphere(24,14); cyl=Mesh.cylinder(24); disc=Mesh.disc(32); cone=Mesh.cone(24);
        loadBundledAssets();
        buildWorld();
        for(int i=0;i<80;i++) snowSeed.add((float)i);
        last=System.nanoTime();
    }


    private void loadBundledAssets(){
        String[] names={"bmx","pitbike","scooter","mtb","atv","minibike","board"};
        for(int i=0;i<names.length;i++){try{assetVehicles[i]=ObjMeshLoader.load(ctx,"models/vehicles/"+names[i]+".obj");}catch(Throwable ignored){assetVehicles[i]=null;}}
        try{assetRamp=ObjMeshLoader.load(ctx,"models/park/ramp.obj");}catch(Throwable ignored){}
        try{assetTrampoline=ObjMeshLoader.load(ctx,"models/park/trampoline.obj");}catch(Throwable ignored){}
        try{assetRail=ObjMeshLoader.load(ctx,"models/park/rail.obj");}catch(Throwable ignored){}
        try{assetBox=ObjMeshLoader.load(ctx,"models/park/box.obj");}catch(Throwable ignored){}
        try{assetQuarterPipe=ObjMeshLoader.load(ctx,"models/park/quarter_pipe.obj");}catch(Throwable ignored){}
        try{assetLanding=ObjMeshLoader.load(ctx,"models/park/landing.obj");}catch(Throwable ignored){}
        try{assetPlatform=ObjMeshLoader.load(ctx,"models/park/platform.obj");}catch(Throwable ignored){}
    }

    private void buildWorld(){
        for(int z=0;z<220;z+=12){
            float off=(float)Math.sin(z*0.19)*2.2f;
            scenery.add(new Scenery(-12-off,z+4,1.0f,0));
            scenery.add(new Scenery(12+off,z+7,1.15f,0));
            if(z%36==0){scenery.add(new Scenery(-8,z+2,1.0f,1)); scenery.add(new Scenery(8,z+10,.9f,1));}
        }
    }

    @Override public void onSurfaceChanged(GL10 gl,int w,int h){
        width=w;height=h; GLES30.glViewport(0,0,w,h);
        Matrix.perspectiveM(proj,0,62f,(float)w/h,.08f,300f);
    }

    @Override public void onDrawFrame(GL10 gl){
        long now=System.nanoTime(); float dt=Math.min(.033f,(now-last)/1e9f); last=now;
        if(!paused) update(dt);
        render();
    }

    private void update(float dt){
        if(program==0)return;
        float throttle=inGas?1f:0f;
        float brake=inBrake?1f:0f;
        if(throttle>0) speed += 16.0f*dt;
        else speed -= 2.6f*dt;
        if(brake>0) speed -= 26.0f*dt;
        speed=Math.max(0,Math.min(31,speed));
        float steerInput=(inRight?1f:0f)-(inLeft?1f:0f);
        float steerResponse=1.9f + speed*0.035f;
        steer += (steerInput-steer)*Math.min(1,dt*8f);
        carYaw += steer*steerResponse*dt*(0.32f+speed/31f);
        float dx=(float)Math.sin(carYaw)*speed*dt;
        float dz=(float)Math.cos(carYaw)*speed*dt;
        carX += dx; carZ += dz; distance += speed*dt;
        wheelSpin += speed*dt*2.8f;
        // Arcade stunt-park jump physics: ramps launch the vehicle into a short controllable flight.
        float groundY=0f;
        boolean rampZone = Math.abs(carX) < 6.8f && ((carZ>24&&carZ<31)||(carZ>62&&carZ<69)||(carZ>108&&carZ<116)||(carZ>168&&carZ<176));
        if(!airborne && rampZone && speed>6f){ airborne=true; verticalVel=7.0f + speed*.08f; riderY=.02f; trickText="AIR TIME"; }
        if(airborne){
            verticalVel -= 15.5f*dt; riderY += verticalVel*dt;
            spin += (inRight?1f:0f)-(inLeft?1f:0f);
            bikePitch += ((inTrick?1.6f:0f) + (inGas?.55f:0f))*dt;
            if(riderY<=groundY){ riderY=groundY; airborne=false; verticalVel=0; score += (long)Math.max(0, stuntTime*450); trickText=stuntTime>.35f?"CLEAN LANDING":"LANDING"; stuntTime=0; bikePitch*=.45f; }
        }
        bikeLean += (((inRight?0.32f:0f)-(inLeft?0.32f:0f))-bikeLean)*Math.min(1,dt*7f);
        if(inTrick && speed>5f && (airborne || rampZone)){
            stuntTime += dt; score += 7;
            trickText = stuntTime>1.0f ? "COMBO x" + (1+(int)(stuntTime*1.5f)) : (airborne?"TRICK":"SETUP");
        } else if(!airborne) {
            if(stuntTime>0.65f){ score += (long)(stuntTime*250); trickText="CLEAN LANDING"; }
            stuntTime=0; bikePitch += (0-bikePitch)*Math.min(1,dt*5f);
            trickText = speed>4 ? "RIDE" : "READY";
        }
        if(inGas && speed>18) score += 1;
        if(carX>13)carX=13; if(carX<-13)carX=-13;
        if(carZ>235){carZ=5; distance=0;}
        // Smooth chase camera: stable and responsive, without the old "barely turns" feeling.
        float desiredX=carX-(float)Math.sin(carYaw)*8.4f;
        float desiredZ=carZ-(float)Math.cos(carYaw)*8.4f;
        camX += (desiredX-camX)*Math.min(1,dt*5.5f);
        camZ += (desiredZ-camZ)*Math.min(1,dt*5.5f);
        camY += ((3.25f+speed*.035f+riderY*.55f)-camY)*Math.min(1,dt*4.5f);
        camLookY += ((1.05f)-camLookY)*Math.min(1,dt*5f);
    }

    private void render(){
        GLES30.glClearColor(0.44f,0.66f,0.80f,1f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT|GLES30.GL_DEPTH_BUFFER_BIT);
        float cx=camX, cy=camY, cz=camZ, ly=camLookY;
        if(cameraMode==1){cx=carX-(float)Math.sin(carYaw)*2.3f;cz=carZ-(float)Math.cos(carYaw)*2.3f;cy=1.7f;ly=.95f;}
        else if(cameraMode==2){cx=carX-(float)Math.sin(carYaw)*11f;cz=carZ-(float)Math.cos(carYaw)*11f;cy=5.6f;ly=1.0f;}
        Matrix.setLookAtM(view,0,cx,cy,cz,carX,ly,carZ+5.5f,0,1,0);
        Matrix.multiplyMM(vp,0,proj,0,view,0);
        GLES30.glUseProgram(program);
        GLES30.glUniform3f(uLight,-0.35f,1.15f,0.25f);
        GLES30.glUniform3f(uViewPos,camX,camY,camZ);
        GLES30.glUniform4f(uFog,0.44f,0.66f,0.80f,0.0048f);

        // Road and natural shoulder.
        drawBox(0,-.45f,120,14,.5f,250,0x3D463E,.92f,.0f);
        drawBox(0,-.13f,120,8.7f,.16f,250,0x292C2D,.86f,.08f);
        drawBox(-5.0f,-.04f,120,.13f,.06f,250,0xB7B9AE,.35f,.0f);
        drawBox(5.0f,-.04f,120,.13f,.06f,250,0xB7B9AE,.35f,.0f);
        for(int z=0;z<250;z+=8) drawBox(0,.035f,z,.09f,.025f,3.4f,0xE5E2D6,.55f,.0f);
        for(int z=0;z<250;z+=18){ drawBox(-4.35f,.05f,z,.07f,.11f,6.0f,0xD5D5CF,.45f,.0f); drawBox(4.35f,.05f,z,.07f,.11f,6.0f,0xD5D5CF,.45f,.0f); }

        // Mountains / distant skyline.
        drawMountain(-28,20,20,0x526A63); drawMountain(28,34,24,0x4A625C); drawMountain(0,58,28,0x60776F);

        for(Scenery s:scenery){
            if(Math.abs(s.z-carZ)>85) continue;
            if(s.type==0) drawTree(s.x,s.z,s.s); else drawRock(s.x,s.z,s.s);
        }
        drawGuardRails();
        drawStuntPark();
        drawShadow(carX,.035f+riderY,carZ,1.45f,2.5f,0x14191A);
        drawBike();
        if(snow) drawSnow();
    }


    private void drawStuntPark(){
        // Dedicated fictional arcade stunt plaza: ramps, quarter pipes, boxes, rails and landing decks.
        drawBox(-10,.02f,42,2.8f,.04f,18,0x30383D,.95f,.05f);
        drawBox(10,.02f,42,2.8f,.04f,18,0x30383D,.95f,.05f);
        drawRamp(0,27,5.8f,3.2f,1.8f);
        drawRamp(0,65,5.8f,3.4f,2.0f);
        drawRamp(-4.5f,111,3.5f,2.8f,1.5f);
        drawRamp(4.5f,172,3.5f,2.8f,1.5f);
        drawBox(-3.2f,.55f,88,2.2f,.55f,1.0f,0x5B666B,.72f,.12f);
        drawBox(3.2f,.38f,94,1.7f,.38f,1.0f,0x667177,.72f,.12f);
        drawRail(-6.2f,.62f,83,5.0f); drawRail(6.2f,.62f,101,5.0f);
        drawRail(-2.5f,.72f,128,3.6f); drawRail(2.5f,.72f,143,3.6f);
        drawBox(0,.10f,145,5.8f,.10f,8.0f,0x262D31,.95f,.08f);
        drawAsset(assetQuarterPipe,-5.4f,.02f,151,1.0f,0x59656B,.78f,.10f); drawAsset(assetQuarterPipe,5.4f,.02f,151,1.0f,0x59656B,.78f,.10f);
        drawAsset(assetPlatform,0,.02f,195,1.0f,0x59656B,.70f,.10f);
        drawBox(-7.8f,.04f,198,1.0f,.04f,30,0x343C40,.9f,.04f);
        drawBox(7.8f,.04f,198,1.0f,.04f,30,0x343C40,.9f,.04f);
        drawAsset(assetTrampoline,0,.14f,132,2.2f,0x3BC7A5,.35f,.15f);
        drawAsset(assetTrampoline,-5.8f,.14f,132,1.5f,0x3BC7A5,.35f,.15f);
        drawAsset(assetRamp,0,0.02f,27,1.25f,0x657178,.78f,.12f);
        drawAsset(assetRamp,0,0.02f,65,1.25f,0x657178,.78f,.12f);
    }

    private void drawRamp(float x,float z,float width,float length,float height){
        pushModel(x,height*.5f,z); Matrix.scaleM(model,0,width,height,length); drawMesh(wedgeMesh(),0x58636A,.78f,.10f,0); pop();
    }
    private void drawQuarterPipe(float x,float z,float width,float height){
        pushModel(x,height*.45f,z); Matrix.scaleM(model,0,width,height,2.2f); drawMesh(wedgeMesh(),0x59656B,.78f,.10f,0); pop();
    }
    private void drawRail(float x,float y,float z,float len){
        drawBox(x,y,z,.06f,.06f,len,0xB7BEC1,.30f,.70f);
        drawBox(x,y*.5f,z-len*.82f,.05f,y*.5f,.05f,0x7C8589,.45f,.45f);
        drawBox(x,y*.5f,z+len*.82f,.05f,y*.5f,.05f,0x7C8589,.45f,.45f);
    }
    private Mesh wedgeCache;
    private Mesh wedgeMesh(){
        if(wedgeCache!=null)return wedgeCache;
        float[] p={-1,0,-1, 1,0,-1, 1,0,1, -1,0,-1, 1,0,1, -1,0,1, -1,0,-1, 1,0,-1, 1,2,1, -1,0,-1, 1,2,1, -1,2,1, 1,0,-1, -1,0,-1, -1,2,1, 1,0,-1, -1,2,1, 1,2,1};
        float[] n=new float[p.length]; for(int i=0;i<n.length;i+=3){n[i]=0;n[i+1]=1;n[i+2]=0;}
        return wedgeCache=new Mesh(p,n);
    }

    private void drawMountain(float x,float z,float size,int color){
        pushModel(x,1,z); Matrix.scaleM(model,0,size,size*.7f,size*.45f); drawMesh(cone,color,.98f,.0f,0); pop();
    }

    private void drawGuardRails(){
        for(int z=15;z<235;z+=12){
            drawBox(-7.1f,1.0f,z,.09f,.09f,.35f,0x8E9594,.9f,.0f);
            drawBox(-7.1f,.7f,z,.08f,.55f,.08f,0x6E7474,.9f,.0f);
            drawBox(7.1f,1.0f,z,.09f,.09f,.35f,0x8E9594,.9f,.0f);
            drawBox(7.1f,.7f,z,.08f,.55f,.08f,0x6E7474,.9f,.0f);
        }
    }

    private void drawTree(float x,float z,float s){
        drawShadow(x,.02f,z,.8f,1.1f,0x182019);
        pushModel(x,.9f*s,z); Matrix.scaleM(model,0,.22f*s,.9f*s,.22f*s); drawMesh(cyl,0x5B4431,.95f,.0f,0); pop();
        pushModel(x,2.0f*s,z); Matrix.scaleM(model,0,1.25f*s,1.55f*s,1.25f*s); drawMesh(cone,0x315D3B,.92f,.0f,0); pop();
        pushModel(x,.95f*s,z); Matrix.scaleM(model,0,.95f*s,1.25f*s,.95f*s); drawMesh(cone,0x264B32,.94f,.0f,0); pop();
    }

    private void drawRock(float x,float z,float s){ pushModel(x,.35f*s,z); Matrix.scaleM(model,0,1.1f*s,.5f*s,.8f*s); drawMesh(sphere,0x626865,.96f,.0f,0); pop(); }

    private void drawBike(){
        Mesh m=assetVehicles[Math.max(0,Math.min(6,transportType))];
        if(m!=null){
            float y=(transportType==6?0.34f:0.58f)+riderY;
            float scale=(transportType==4?0.72f:transportType==6?0.85f:1.0f);
            int color; switch(transportType){case 0:color=0xD83E58;break;case 1:color=0xE04858;break;case 2:color=0x35D5A0;break;case 3:color=0x2C9FEA;break;case 4:color=0xE39A35;break;case 5:color=0x668FFF;break;default:color=0xE9BF55;}
            pushModel(carX,y,carZ); rotateCar(); Matrix.rotateM(model,0,(float)Math.toDegrees(bikePitch),1,0,0); Matrix.rotateM(model,0,(float)Math.toDegrees(bikeLean),0,0,1); Matrix.scaleM(model,0,scale,scale,scale); drawMesh(m,color,.42f,.22f,0); pop();
            return;
        }
        if(transportType==0){ drawBmx(); return; } if(transportType==2){ drawScooter(); return; } if(transportType==3){ drawMtb(); return; } if(transportType==4){ drawAtv(); return; } if(transportType==5){ drawMiniBike(); return; } drawBoard();
    }

    private void drawAsset(Mesh m,float x,float y,float z,float scale,int color,float rough,float metal){if(m==null)return;pushModel(x,y,z);Matrix.scaleM(model,0,scale,scale,scale);drawMesh(m,color,rough,metal,0);pop();}

    private void drawBmx(){
        float y=.52f+riderY;
        for(int side=-1;side<=1;side+=2){
            pushModel(carX+side*.36f,y,carZ+1.02f); rotateCar(); Matrix.rotateM(model,0,90,0,0,1); Matrix.scaleM(model,0,.50f,.10f,.50f); drawMesh(cyl,0x17191A,.88f,.02f,0); pop();
            pushModel(carX+side*.36f,y,carZ-1.02f); rotateCar(); Matrix.rotateM(model,0,90,0,0,1); Matrix.scaleM(model,0,.50f,.10f,.50f); drawMesh(cyl,0x17191A,.88f,.02f,0); pop();
        }
        pushModel(carX,y+.55f,carZ); rotateCar(); Matrix.rotateM(model,0,(float)Math.toDegrees(bikePitch),1,0,0); Matrix.scaleM(model,0,.08f,.62f,.08f); drawMesh(cyl,0xD6DADF,.35f,.55f,0); pop();
        pushModel(carX,y+.72f,carZ-.05f); rotateCar(); Matrix.scaleM(model,0,.10f,.08f,.55f); drawMesh(cube,0xE23C55,.45f,.05f,0); pop();
        pushModel(carX,y+.55f,carZ+1.02f); rotateCar(); Matrix.scaleM(model,0,.48f,.06f,.06f); drawMesh(cube,0xC4CCD0,.35f,.65f,0); pop();
        pushModel(carX,y+.95f,carZ+.62f); rotateCar(); Matrix.scaleM(model,0,.10f,.10f,.28f); drawMesh(cube,0x15191C,.3f,.3f,0); pop();
    }

    private void drawScooter(){
        float y=.55f+riderY;
        pushModel(carX,y,carZ); rotateCar(); Matrix.scaleM(model,0,.24f,.06f,1.15f); drawMesh(cube,0x42D6A2,.42f,.25f,0); pop();
        for(int side=-1;side<=1;side+=2){
            pushModel(carX+side*.27f,y,carZ+1.0f); rotateCar(); Matrix.rotateM(model,0,90,0,0,1); Matrix.scaleM(model,0,.34f,.09f,.34f); drawMesh(cyl,0x151719,.9f,.0f,0); pop();
            pushModel(carX+side*.27f,y,carZ-1.0f); rotateCar(); Matrix.rotateM(model,0,90,0,0,1); Matrix.scaleM(model,0,.34f,.09f,.34f); drawMesh(cyl,0x151719,.9f,.0f,0); pop();
        }
        pushModel(carX,y+.72f,carZ+.75f); rotateCar(); Matrix.rotateM(model,0,(float)Math.toDegrees(bikePitch),1,0,0); Matrix.scaleM(model,0,.055f,.75f,.055f); drawMesh(cyl,0xBFC7CB,.3f,.6f,0); pop();
        pushModel(carX,y+1.43f,carZ+.75f); rotateCar(); Matrix.scaleM(model,0,.38f,.06f,.06f); drawMesh(cube,0xD7DEE1,.25f,.7f,0); pop();
    }

    private void drawMtb(){
        float y=.52f+riderY; for(int side=-1;side<=1;side+=2){pushModel(carX+side*.36f,y,carZ+1.05f);rotateCar();Matrix.rotateM(model,0,90,0,0,1);Matrix.scaleM(model,0,.48f,.08f,.48f);drawMesh(cyl,0x15191B,.9f,.0f,0);pop();}
        pushModel(carX,y+.48f,carZ);rotateCar();Matrix.rotateM(model,0,(float)Math.toDegrees(bikePitch),1,0,0);Matrix.scaleM(model,0,.06f,.58f,.06f);drawMesh(cyl,0x8FA1A7,.35f,.65f,0);pop();
        pushModel(carX,y+.62f,carZ-.05f);rotateCar();Matrix.scaleM(model,0,.09f,.08f,.62f);drawMesh(cube,0x2AA6E8,.38f,.18f,0);pop();
    }
    private void drawAtv(){
        float y=.62f+riderY;pushModel(carX,y,carZ);rotateCar();Matrix.scaleM(model,0,.72f,.18f,1.28f);drawMesh(cube,0xD88728,.3f,.25f,0);pop();
        for(int side=-1;side<=1;side+=2)for(int z=-1;z<=1;z+=2){pushModel(carX+side*.72f,.48f+riderY,carZ+z*.82f);rotateCar();Matrix.rotateM(model,0,90,0,0,1);Matrix.scaleM(model,0,.28f,.14f,.28f);drawMesh(cyl,0x121517,.92f,.02f,0);pop();}
        pushModel(carX,.96f+riderY,carZ-.15f);rotateCar();Matrix.scaleM(model,0,.55f,.06f,.85f);drawMesh(cube,0x303A40,.35f,.4f,0);pop();
    }
    private void drawMiniBike(){
        float y=.56f+riderY;pushModel(carX,y+.22f,carZ);rotateCar();Matrix.scaleM(model,0,.38f,.20f,1.28f);drawMesh(cube,0x5D8BFF,.3f,.35f,0);pop();
        for(int z=-1;z<=1;z+=2){pushModel(carX,.48f+riderY,carZ+z*.92f);rotateCar();Matrix.rotateM(model,0,90,0,0,1);Matrix.scaleM(model,0,.42f,.11f,.42f);drawMesh(cyl,0x111315,.92f,.02f,0);pop();}
        pushModel(carX,.92f+riderY,carZ+.62f);rotateCar();Matrix.scaleM(model,0,.06f,.55f,.06f);drawMesh(cyl,0xB9C1C4,.3f,.7f,0);pop();
    }
    private void drawBoard(){
        float y=.28f+riderY;pushModel(carX,y,carZ);rotateCar();Matrix.scaleM(model,0,.28f,.06f,1.32f);drawMesh(cube,0xE6B74D,.45f,.05f,0);pop();
        for(int side=-1;side<=1;side+=2)for(int z=-1;z<=1;z+=2){pushModel(carX+side*.28f,.16f+riderY,carZ+z*.65f);rotateCar();Matrix.rotateM(model,0,90,0,0,1);Matrix.scaleM(model,0,.11f,.07f,.11f);drawMesh(cyl,0x151719,.9f,.0f,0);pop();}
    }
    private void drawPitBikeModel(){
        pushModel(carX,.72f+riderY,carZ); rotateCar(); Matrix.rotateM(model,0,(float)Math.toDegrees(bikeLean),0,0,1); Matrix.rotateM(model,0,(float)Math.toDegrees(bikePitch),1,0,0);
        Matrix.scaleM(model,0,.42f,.16f,1.55f); drawMesh(cube,0xD62D4F,.28f,.58f,0); pop();

        pushModel(carX,1.05f+riderY,carZ-.20f); rotateCar(); Matrix.rotateM(model,0,(float)Math.toDegrees(bikeLean),0,0,1);
        Matrix.scaleM(model,0,.30f,.30f,.58f); drawMesh(cube,0x171C20,.22f,.35f,0); pop();

        pushModel(carX,1.38f+riderY,carZ-.62f); rotateCar(); Matrix.rotateM(model,0,(float)Math.toDegrees(bikeLean),0,0,1);
        Matrix.scaleM(model,0,.22f,.18f,.42f); drawMesh(cube,0x111518,.18f,.60f,0); pop();

        pushModel(carX,.98f+riderY,carZ+1.18f); rotateCar();
        Matrix.scaleM(model,0,.48f,.08f,.08f); drawMesh(cube,0x8B969B,.45f,.72f,0); pop();

        for(int side=-1;side<=1;side+=2){
            pushModel(carX+side*.42f,.55f+riderY,carZ+1.12f); rotateCar();
            Matrix.rotateM(model,0,90,0,0,1); Matrix.scaleM(model,0,.52f,.14f,.52f); drawMesh(cyl,0x101315,.90f,.02f,0); pop();
            pushModel(carX+side*.42f,.55f+riderY,carZ-1.05f); rotateCar();
            Matrix.rotateM(model,0,90,0,0,1); Matrix.scaleM(model,0,.48f,.13f,.48f); drawMesh(cyl,0x101315,.90f,.02f,0); pop();
        }
        // fork/handlebar and headlight
        pushModel(carX,.98f+riderY,carZ+1.28f); rotateCar(); Matrix.scaleM(model,0,.08f,.45f,.08f); drawMesh(cube,0xB7C0C3,.3f,.72f,0); pop();
        pushModel(carX,.98f+riderY,carZ+1.42f); rotateCar(); Matrix.scaleM(model,0,.18f,.13f,.10f); drawMesh(sphere,0xFFF3B0,.12f,.05f,.7f); pop();
        drawShadow(carX,.03f,carZ,1.0f,1.75f,0x111415);
    }

    private void drawCar(){
        // Main metallic body, long hood and cabin.
        pushModel(carX,.93f,carZ); rotateCar(); Matrix.scaleM(model,0,1.18f,.42f,2.15f); drawMesh(cube,0x1682D4,.28f,.72f,0); pop();
        pushModel(carX,1.25f,carZ+.25f); rotateCar(); Matrix.scaleM(model,0,1.02f,.20f,1.08f); drawMesh(cube,0x237FC1,.38f,.62f,0); pop();
        pushModel(carX,1.47f,carZ-.35f); rotateCar(); Matrix.scaleM(model,0,.78f,.38f,.82f); drawMesh(cube,0x15202A,.12f,.82f,0); pop();
        // windshield/roof highlight
        pushModel(carX,1.72f,carZ-.25f); rotateCar(); Matrix.scaleM(model,0,.70f,.10f,.70f); drawMesh(cube,0x0C2838,.08f,.72f,0); pop();
        // bumpers and grille
        pushModel(carX,.72f,carZ+2.13f); rotateCar(); Matrix.scaleM(model,0,1.08f,.12f,.12f); drawMesh(cube,0x202427,.45f,.78f,0); pop();
        pushModel(carX,.78f,carZ+2.25f); rotateCar(); Matrix.scaleM(model,0,.38f,.12f,.04f); drawMesh(cube,0x050607,.28f,.7f,0); pop();
        // lamps
        for(int side=-1;side<=1;side+=2){
            pushModel(carX+side*.62f,.98f,carZ+2.12f); rotateCar(); Matrix.scaleM(model,0,.25f,.12f,.08f); drawMesh(cube,0xFFF1B0,.15f,.05f,1); pop();
            pushModel(carX+side*.68f,.87f,carZ-2.12f); rotateCar(); Matrix.scaleM(model,0,.18f,.10f,.06f); drawMesh(cube,0xD51F1F,.22f,.05f,.5f); pop();
        }
        // mirrors
        for(int side=-1;side<=1;side+=2){ pushModel(carX+side*.96f,1.45f,carZ+.55f); rotateCar(); Matrix.scaleM(model,0,.12f,.10f,.24f); drawMesh(cube,0x11181C,.35f,.55f,0); pop(); }
        // wheels + rims
        for(int side=-1;side<=1;side+=2) for(int front=-1;front<=1;front+=2){
            float wx=carX+side*1.02f, wz=carZ+front*1.30f;
            drawWheel(wx,.62f,wz,side,front);
        }
        drawShadow(carX,.03f,carZ,1.6f,2.35f,0x111415);
    }

    private void drawWheel(float x,float y,float z,int side,int front){
        pushModel(x,y,z); rotateCar(); Matrix.rotateM(model,0,90,0,0,1); Matrix.rotateM(model,0,(float)Math.toDegrees(wheelSpin),1,0,0); Matrix.scaleM(model,0,.36f,.22f,.36f); drawMesh(cyl,0x111314,.95f,.02f,0); pop();
        pushModel(x+side*.20f,y,z); rotateCar(); Matrix.rotateM(model,0,90,0,0,1); Matrix.scaleM(model,0,.17f,.24f,.17f); drawMesh(cyl,0xB9C0BE,.22f,.65f,0); pop();
    }

    private void rotateCar(){ Matrix.rotateM(model,0,(float)Math.toDegrees(carYaw),0,1,0); }

    private void drawShadow(float x,float y,float z,float sx,float sz,int color){
        GLES30.glEnable(GLES30.GL_BLEND); GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA,GLES30.GL_ONE_MINUS_SRC_ALPHA); GLES30.glDepthMask(false);
        pushModel(x,y+.005f,z); Matrix.scaleM(model,0,sx,.012f,sz); drawMesh(disc,color,.98f,.0f,0); pop();
        GLES30.glDepthMask(true); GLES30.glDisable(GLES30.GL_BLEND);
    }

    private void drawSnow(){
        GLES30.glEnable(GLES30.GL_BLEND); GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA,GLES30.GL_ONE_MINUS_SRC_ALPHA); GLES30.glDepthMask(false);
        for(int i=0;i<snowSeed.size();i++){
            float t=snowSeed.get(i); float x=(float)Math.sin(t*9.71)*12f; float z=carZ-15+(t*3.3f)%32f; float y=1.2f+(t%8)*.55f;
            pushModel(x,y,z); Matrix.scaleM(model,0,.045f,.045f,.045f); drawMesh(sphere,0xF7FBFF,.8f,.0f,0); pop();
        }
        GLES30.glDepthMask(true); GLES30.glDisable(GLES30.GL_BLEND);
    }

    private void pushModel(float x,float y,float z){ Matrix.setIdentityM(model,0); Matrix.translateM(model,0,x,y,z); }
    private void pop(){}
    private void drawBox(float x,float y,float z,float sx,float sy,float sz,int color,float rough,float metal){ pushModel(x,y,z); Matrix.scaleM(model,0,sx,sy,sz); drawMesh(cube,color,rough,metal,0); }
    private void drawMesh(Mesh m,int color,float rough,float metal,float emissive){
        Matrix.multiplyMM(mvp,0,vp,0,model,0);
        GLES30.glUniformMatrix4fv(uMVP,1,false,mvp,0); GLES30.glUniformMatrix4fv(uModel,1,false,model,0);
        GLES30.glUniform3f(uColor,((color>>16)&255)/255f,((color>>8)&255)/255f,(color&255)/255f);
        GLES30.glUniform1f(uRough,rough); GLES30.glUniform1f(uMetal,metal); GLES30.glUniform1f(uEmissive,emissive);
        m.draw();
    }

    public void setInput(boolean left,boolean right,boolean gas,boolean brake,boolean trick){
        inLeft=left;inRight=right;inGas=gas;inBrake=brake;inTrick=trick;
    }
    public void setInput(boolean left,boolean right,boolean gas,boolean brake){setInput(left,right,gas,brake,false);}
    public long getScore(){return score;}
    public String getTrickText(){return trickText;}
    public void nextCamera(){cameraMode=(cameraMode+1)%3;}
    public void nextTransport(){transportType=(transportType+1)%7; trickText=getTransportName();}
    public String getTransportName(){switch(transportType){case 0:return "BMX";case 1:return "PITBIKE";case 2:return "SCOOTER";case 3:return "MTB";case 4:return "ATV";case 5:return "MINI BIKE";default:return "BOARD";}}
    public void setPaused(boolean p){paused=p;last=System.nanoTime();}
    public void togglePause(){paused=!paused; last=System.nanoTime();}
    public boolean isPaused(){return paused;}
    public float getSpeedKmh(){return speed*3.6f;}
    public float getDistanceKm(){return distance/1000f;}

    private int buildProgram(String v,String f){
        int vs=compile(GLES30.GL_VERTEX_SHADER,v), fs=compile(GLES30.GL_FRAGMENT_SHADER,f);
        int p=GLES30.glCreateProgram(); GLES30.glAttachShader(p,vs); GLES30.glAttachShader(p,fs); GLES30.glLinkProgram(p);
        int[] ok=new int[1]; GLES30.glGetProgramiv(p,GLES30.GL_LINK_STATUS,ok,0); if(ok[0]==0) throw new RuntimeException(GLES30.glGetProgramInfoLog(p));
        GLES30.glDeleteShader(vs); GLES30.glDeleteShader(fs); return p;
    }
    private int compile(int type,String s){int sh=GLES30.glCreateShader(type); GLES30.glShaderSource(sh,s); GLES30.glCompileShader(sh); int[] ok=new int[1]; GLES30.glGetShaderiv(sh,GLES30.GL_COMPILE_STATUS,ok,0); if(ok[0]==0) throw new RuntimeException(GLES30.glGetShaderInfoLog(sh)); return sh;}

    static class Mesh{
        FloatBuffer pos,nor; int count,vbo,nbo;
        Mesh(float[] p,float[] n){count=p.length/3;pos=fb(p);nor=fb(n);}
        static FloatBuffer fb(float[] a){ByteBuffer b=ByteBuffer.allocateDirect(a.length*4).order(ByteOrder.nativeOrder());FloatBuffer f=b.asFloatBuffer();f.put(a).position(0);return f;}
        void init(){int[] x=new int[2];GLES30.glGenBuffers(2,x,0);vbo=x[0];nbo=x[1];GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vbo);GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,pos.capacity()*4,pos,GLES30.GL_STATIC_DRAW);GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,nbo);GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,nor.capacity()*4,nor,GLES30.GL_STATIC_DRAW);}
        void draw(){if(vbo==0)init();GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,vbo);GLES30.glEnableVertexAttribArray(0);GLES30.glVertexAttribPointer(0,3,GLES30.GL_FLOAT,false,0,0);GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,nbo);GLES30.glEnableVertexAttribArray(1);GLES30.glVertexAttribPointer(1,3,GLES30.GL_FLOAT,false,0,0);GLES30.glDrawArrays(GLES30.GL_TRIANGLES,0,count);GLES30.glDisableVertexAttribArray(0);GLES30.glDisableVertexAttribArray(1);}
        static Mesh cube(){
            float[] p={-1,-1,-1,1,-1,-1,1,1,-1,-1,-1,-1,1,1,-1,-1,1,-1, -1,-1,1,1,1,1,1,1,-1,-1,-1,1,1,1,-1,1,-1,-1, -1,-1,-1,-1,-1,1,-1,1,1,-1,-1,-1,1,1,-1,1,-1,1,1,1,1,1,1,-1,-1,-1,-1,1,-1,-1,-1,1,-1,1,1,-1,-1,-1,1,1,-1,1,-1,-1,1,-1,1,1,1,-1,-1,1,1,1,1,1,-1,1,-1,-1,-1,-1,1,-1,-1,-1,1,-1,1,1,-1,1,1,-1,-1, -1,-1,-1,-1,-1,1,1,-1,1,-1,-1,-1,-1,1,1,-1,1,-1,-1,-1,1,-1,-1,-1,1,1,1,1,-1,1,1,1,1,-1,-1,1};
            float[] n=new float[p.length]; for(int i=0;i<n.length;i+=18){float nx=0,ny=0,nz=0;if(i<18)nz=-1;else if(i<36)nz=1;else if(i<54)ny=1;else if(i<72)ny=-1;else if(i<90)nx=1;else nx=-1;for(int j=0;j<18;j+=3){n[i+j]=nx;n[i+j+1]=ny;n[i+j+2]=nz;}} return new Mesh(p,n);
        }
        static Mesh sphere(int seg,int rings){ArrayList<Float>p=new ArrayList<>(),n=new ArrayList<>();for(int y=0;y<rings;y++){float t0=(float)Math.PI*y/rings,t1=(float)Math.PI*(y+1)/rings;for(int x=0;x<seg;x++){float a0=2f*(float)Math.PI*x/seg,a1=2f*(float)Math.PI*(x+1)/seg;tri(p,n,t0,a0,t1,a0,t1,a1);tri(p,n,t0,a0,t1,a1,t0,a1);}}return new Mesh(to(p),to(n));}
        static void tri(ArrayList<Float>p,ArrayList<Float>n,float t1,float a1,float t2,float a2,float t3,float a3){float[][]q={{(float)(Math.sin(t1)*Math.cos(a1)),(float)Math.cos(t1),(float)(Math.sin(t1)*Math.sin(a1))},{(float)(Math.sin(t2)*Math.cos(a2)),(float)Math.cos(t2),(float)(Math.sin(t2)*Math.sin(a2))},{(float)(Math.sin(t3)*Math.cos(a3)),(float)Math.cos(t3),(float)(Math.sin(t3)*Math.sin(a3))}};for(float[]v:q){for(float c:v){p.add(c);n.add(c);}}}
        static Mesh cylinder(int seg){ArrayList<Float>p=new ArrayList<>(),n=new ArrayList<>();for(int i=0;i<seg;i++){float a=2f*(float)Math.PI*i/seg,b=2f*(float)Math.PI*(i+1)/seg;float[]p1={(float)Math.cos(a),-1,(float)Math.sin(a)},p2={(float)Math.cos(b),-1,(float)Math.sin(b)},p3={(float)Math.cos(b),1,(float)Math.sin(b)},p4={(float)Math.cos(a),1,(float)Math.sin(a)};float[]na={(float)Math.cos(a),0,(float)Math.sin(a)},nb={(float)Math.cos(b),0,(float)Math.sin(b)};triV(p,n,p1,na,p2,nb,p3,nb);triV(p,n,p1,na,p3,nb,p4,na);}return new Mesh(to(p),to(n));}
        static Mesh disc(int seg){ArrayList<Float>p=new ArrayList<>(),n=new ArrayList<>();for(int i=0;i<seg;i++){double a=i*2*Math.PI/seg,b=(i+1)*2*Math.PI/seg;triV(p,n,new float[]{0,0,0},new float[]{0,1,0},new float[]{(float)Math.cos(a),0,(float)Math.sin(a)},new float[]{0,1,0},new float[]{(float)Math.cos(b),0,(float)Math.sin(b)},new float[]{0,1,0});}return new Mesh(to(p),to(n));}
        static Mesh cone(int seg){ArrayList<Float>p=new ArrayList<>(),n=new ArrayList<>();for(int i=0;i<seg;i++){double a=i*2*Math.PI/seg,b=(i+1)*2*Math.PI/seg;float[]ap={(float)Math.cos(a),0,(float)Math.sin(a)},bp={(float)Math.cos(b),0,(float)Math.sin(b)},top={0,2,0};float[]nn={0.3f,0.9f,0.3f};triV(p,n,ap,nn,bp,nn,top,nn);}return new Mesh(to(p),to(n));}
        static void triV(ArrayList<Float>p,ArrayList<Float>n,float[]a,float[]an,float[]b,float[]bn,float[]c,float[]cn){for(int i=0;i<3;i++){p.add(a[i]);n.add(an[i]);}for(int i=0;i<3;i++){p.add(b[i]);n.add(bn[i]);}for(int i=0;i<3;i++){p.add(c[i]);n.add(cn[i]);}}
        static float[]to(ArrayList<Float>a){float[]r=new float[a.size()];for(int i=0;i<r.length;i++)r[i]=a.get(i);return r;}
    }

    private static final String VS="#version 300 es\nlayout(location=0) in vec3 aPos;layout(location=1) in vec3 aNormal;uniform mat4 uMVP;uniform mat4 uModel;out vec3 N;out vec3 W;void main(){W=(uModel*vec4(aPos,1.0)).xyz;N=normalize(mat3(uModel)*aNormal);gl_Position=uMVP*vec4(aPos,1.0);} ";
    private static final String FS="#version 300 es\nprecision highp float;in vec3 N;in vec3 W;uniform vec3 uColor,uLight,uViewPos;uniform vec4 uFog;uniform float uRough,uMetal,uEmissive;out vec4 frag;void main(){vec3 V=normalize(uViewPos-W);vec3 L=normalize(uLight);vec3 H=normalize(V+L);float ndl=max(dot(N,L),0.0);float spec=pow(max(dot(N,H),0.0),mix(90.0,8.0,uRough));float fres=pow(1.0-max(dot(N,V),0.0),5.0);vec3 base=uColor;vec3 diff=base*(0.13+0.82*ndl);vec3 metalTint=mix(vec3(1.0),base,uMetal);vec3 col=diff+metalTint*spec*(0.15+0.7*uMetal)+base*uEmissive;float fog=exp(-uFog.w*distance(W,uViewPos));fog=clamp(fog,0.0,1.0);col=mix(uFog.xyz,col,fog);col=col/(col+vec3(1.0));col=pow(col,vec3(0.92));frag=vec4(col,1.0);} ";
}
