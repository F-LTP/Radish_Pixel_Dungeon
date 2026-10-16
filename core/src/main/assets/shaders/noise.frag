#ifdef GL_ES
  precision highp float;
#endif

varying vec2 vUV;
uniform sampler2D uTex;
uniform float uTime;
uniform float uAlpha;
uniform vec4 uFrame;

float noise(vec2 p) {
  return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
  vec4 col = texture2D(uTex, vUV);
  vec2 localUV = (vUV - uFrame.xy) / uFrame.zw;
  float n = noise(floor(localUV * 24.0) + uTime * 4.0);
  
  if (n < uAlpha) {
    col.a = 0.0;
  }
  
  gl_FragColor = col;
}
