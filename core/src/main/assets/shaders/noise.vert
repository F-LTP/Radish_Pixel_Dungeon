#ifdef GL_ES
  precision highp float;
#endif

uniform mat4 uCamera;
attribute vec4 aXYZW;
attribute vec2 aUV;
varying vec2 vUV;

void main() {
  gl_Position = uCamera * aXYZW;
  vUV = aUV;
}