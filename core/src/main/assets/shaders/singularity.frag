#ifdef GL_ES
  precision highp float;
#endif

varying vec2 vUV;
uniform sampler2D uTex;
uniform float uAlpha;
uniform float uScaleX;
uniform float uScaleY;
uniform vec4 uBounds;
uniform vec4 uFrame;

void main() {
  vec4 col = texture2D(uTex, vUV);
  vec2 ratioPos = (vUV - uFrame.xy) / uFrame.zw;
  vec2 distanceFromCenter = abs(ratioPos - 0.5);
  float remaining = max(0.0, 0.5 * (1.0 - uAlpha));

  if (distanceFromCenter.x > remaining || distanceFromCenter.y > remaining) {
    col.a = 0.0;
  }

  float factor = pow(uAlpha, 3.0);
  col.r += factor * 0.6;
  col.g += factor * 0.7;
  col.b += factor * 0.9;
  
  gl_FragColor = col;
}
