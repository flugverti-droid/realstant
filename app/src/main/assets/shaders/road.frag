#version 300 es
precision highp float; in vec3 vN; uniform float uWet; out vec4 o; void main(){float d=max(dot(normalize(vN),normalize(vec3(.3,1,.2))),0.0); vec3 dry=vec3(.08,.09,.1);vec3 wet=vec3(.12,.14,.16);o=vec4(mix(dry,wet,uWet)*(.25+.75*d),1.0);}
