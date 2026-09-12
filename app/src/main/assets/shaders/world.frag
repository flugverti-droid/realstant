#version 300 es
precision highp float; in vec3 vN; uniform vec4 uColor; uniform vec3 uLight; out vec4 o; void main(){float d=max(dot(normalize(vN),normalize(uLight)),0.0); vec3 c=uColor.rgb*(.22+.78*d); o=vec4(c,uColor.a);}
