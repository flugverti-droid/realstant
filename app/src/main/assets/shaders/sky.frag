#version 300 es
precision highp float; uniform vec3 uTop; uniform vec3 uBottom; in vec3 vN; out vec4 o; void main(){float t=clamp(vN.y*.5+.5,0.,1.);o=vec4(mix(uBottom,uTop,t),1.);}
