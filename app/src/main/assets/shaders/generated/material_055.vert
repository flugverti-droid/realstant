#version 300 es
precision mediump float;
layout(location=0) in vec3 aPosition;
layout(location=1) in vec3 aNormal;
uniform mat4 uMVP; uniform mat4 uModel;
out vec3 vNormal; out vec3 vWorld;
void main(){ vec4 w=uModel*vec4(aPosition,1.0); vWorld=w.xyz;
vNormal=mat3(uModel)*aNormal; gl_Position=uMVP*w; }