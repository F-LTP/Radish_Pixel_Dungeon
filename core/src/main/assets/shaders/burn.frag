#ifdef GL_ES
  precision highp float;
#endif

varying vec2 vUV;
uniform sampler2D uTex;
uniform float uTime;
uniform float uAlpha;
uniform vec4 uBounds;
uniform float uBurnProgress;
uniform float uRandom;
uniform vec4 uFrame;

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
  
  vec2 ratioPos = (vUV - uFrame.xy) / uFrame.zw;
  
  float noiseVal = clamp(fbm(ratioPos * 8.0 + uRandom * 10.0), 0.0, 1.0);
  float noiseStrength = 0.35;
  
  float burnAmt = (1.0 - ratioPos.y) - uBurnProgress * 1.5 + noiseVal * noiseStrength;
  
  if (burnAmt < 0.0) {
    col.a = 0.0;
  }
  
  float yellowStep = 0.9, orangeStep = 0.8, redStep = 0.7, sootStep = 0.6;
  float stepAlpha = 1.0 - burnAmt;
  
  float yellowAmt = smoothstep(yellowStep, 1.0, stepAlpha);
  float orangeAmt = smoothstep(orangeStep, yellowStep, stepAlpha);
  float redAmt = smoothstep(redStep, orangeStep, stepAlpha);
  float sootAmt = smoothstep(sootStep, redStep, stepAlpha);
  
  col.rgb = mix(col.rgb, vec3(0.1, 0.1, 0.1), sootAmt);
  col.rgb = mix(col.rgb, vec3(0.8, 0.2, 0.2), redAmt);
  col.rgb = mix(col.rgb, vec3(1.0, 0.5, 0.1), orangeAmt);
  col.rgb = mix(col.rgb, vec3(0.8, 0.8, 0.1), yellowAmt);
  
  col.a *= uAlpha;
  
  gl_FragColor = col;
}
