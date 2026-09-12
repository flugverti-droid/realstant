#version 300 es
precision mediump float;
in vec3 vNormal;
in vec3 vWorld;
out vec4 fragColor;
uniform vec3 uLight;
uniform vec3 uCamera;
void main(){
 vec3 N=normalize(vNormal); vec3 L=normalize(uLight);
 float ndl=max(dot(N,L),0.0);
 vec3 V=normalize(uCamera-vWorld);
 vec3 H=normalize(L+V);
 float spec=pow(max(dot(N,H),0.0),max(2.0,27.60));
 vec3 base=vec3(0.280,0.205,0.260);
 vec3 c=base*(.18+.82*ndl)+vec3(spec*0.130);
 float d=length(uCamera-vWorld);
 float f=1.0-exp(-d*d*0.00800);
 c=mix(c,vec3(.12,.16,.21),clamp(f,0.0,1.0));
 fragColor=vec4(c,1.0);
}