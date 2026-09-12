package com.realstant.nativegame;

import android.content.Context;
import java.io.*;
import java.util.*;

/** Tiny mobile-friendly OBJ loader: positions + triangles, normals are calculated once at startup. */
public final class ObjMeshLoader {
    private ObjMeshLoader() {}
    public static RealStantRenderer.Mesh load(Context c, String asset) throws IOException {
        ArrayList<float[]> v=new ArrayList<>(); ArrayList<int[]> f=new ArrayList<>();
        try(BufferedReader br=new BufferedReader(new InputStreamReader(c.getAssets().open(asset)))){
            String line; while((line=br.readLine())!=null){line=line.trim(); if(line.isEmpty()||line.startsWith("#")) continue;
                String[] a=line.split("\\s+");
                if("v".equals(a[0])&&a.length>=4) v.add(new float[]{Float.parseFloat(a[1]),Float.parseFloat(a[2]),Float.parseFloat(a[3])});
                else if("f".equals(a[0])&&a.length>=4){int i1=idx(a[1],v.size()),i2=idx(a[2],v.size()); for(int k=3;k<a.length;k++){int i3=idx(a[k],v.size()); f.add(new int[]{i1,i2,i3}); i2=i3;}}
            }
        }
        float[] p=new float[f.size()*9], n=new float[p.length]; int at=0;
        for(int[] q:f){float[] A=v.get(q[0]),B=v.get(q[1]),C=v.get(q[2]); float ux=B[0]-A[0],uy=B[1]-A[1],uz=B[2]-A[2], vx=C[0]-A[0],vy=C[1]-A[1],vz=C[2]-A[2]; float nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx, len=(float)Math.sqrt(nx*nx+ny*ny+nz*nz); if(len<1e-6f){nx=0;ny=1;nz=0;}else{nx/=len;ny/=len;nz/=len;} for(float[] P:new float[][]{A,B,C}){p[at]=P[0];p[at+1]=P[1];p[at+2]=P[2];n[at]=nx;n[at+1]=ny;n[at+2]=nz;at+=3;}}
        return new RealStantRenderer.Mesh(p,n);
    }
    private static int idx(String s,int size){int slash=s.indexOf('/'); String q=slash<0?s:s.substring(0,slash); int i=Integer.parseInt(q); return i<0?size+i:i-1;}
}
