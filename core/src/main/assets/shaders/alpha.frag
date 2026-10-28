#ifdef GL_ES
  precision highp float;
#endif

varying vec2 vUV;
uniform sampler2D uTex;
uniform float uAlpha;
uniform vec4 uBounds;

void main() {
  vec4 col = texture2D(uTex, vUV);
  col.a *= (1.0 - uAlpha);
  col.r += uAlpha / 5.0;
  gl_FragColor = col;
}