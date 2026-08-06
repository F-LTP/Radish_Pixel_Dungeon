#ifdef GL_ES
  precision highp float;
#endif

varying vec2 vUV;
uniform sampler2D uTex;
uniform float uAlpha;
uniform float uRandom;
uniform vec4 uFrame;

// Simple noise function
float noise(vec2 p) {
  return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

float fbm(vec2 p) {
  float val = 0.5;
  float amp = 0.5;
  for (int i = 0; i < 5; i++) {
    val += amp * noise(p);
    p *= 2.0;
    amp *= 0.5;
  }
  return val;
}

void main() {
  vec4 col = texture2D(uTex, vUV);
  vec2 localUV = (vUV - uFrame.xy) / uFrame.zw;
  float noiseVal = fbm(localUV * 8.0 + uRandom * 10.0);
  
  if (noiseVal < uAlpha) {
    col.a = 0.0;
  }
  
  float colAlpha = pow(uAlpha, 1.9);
  col.rgb = col.rgb * (1.0 - colAlpha) + vec3(0.2, 0.8, 0.3) * colAlpha;
  
  gl_FragColor = col;
}
