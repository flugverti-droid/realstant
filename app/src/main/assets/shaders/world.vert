#version 300 es
layout(location=0) in vec3 aPosition; layout(location=1) in vec3 aNormal; uniform mat4 uMVP; uniform mat4 uModel; out vec3 vN; void main(){vN=mat3(uModel)*aNormal; gl_Position=uMVP*vec4(aPosition,1.0);}
