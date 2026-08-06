#ifdef GL_ES
  precision highp float;
#endif

varying vec2 vUV;
uniform sampler2D uTex;
uniform float uTime;
uniform float uAlpha;
uniform vec4 uCutLine;  // xy=start(归一化), zw=direction(归一化)
uniform float uCutAlpha;
uniform float uCutSide;
uniform vec4 uFrame;

void main() {
  vec4 col = texture2D(uTex, vUV);
  
  vec2 localUV = (vUV - uFrame.xy) / uFrame.zw;
  float cutPosition = dot(localUV, normalize(uCutLine.zw)) / 1.41421356;
  float edge = cutPosition - uCutAlpha;

  if (edge < 0.0) {
    col.a = 0.0;
  } else if (edge < 0.04) {
    col.rgb = mix(col.rgb, vec3(1.0, 0.85, 0.65), 1.0 - edge / 0.04);
  }
  col.a *= uAlpha;
  
  gl_FragColor = col;
}
