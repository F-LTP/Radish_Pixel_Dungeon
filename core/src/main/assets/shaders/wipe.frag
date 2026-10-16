#ifdef GL_ES
  precision highp float;
#endif

varying vec2 vUV;
uniform sampler2D uTex;
uniform float uAlpha;
uniform vec2 uDirection;
uniform vec4 uBounds;
uniform vec4 uFrame;

void main() {
  vec4 col = texture2D(uTex, vUV);
  vec2 ratioPos = (vUV - uFrame.xy) / uFrame.zw;
  
  ratioPos = ratioPos * uDirection + max(vec2(0.0, 0.0), uDirection * -1.0);
  
  float amount = dot(ratioPos, abs(uDirection));
  if (amount < uAlpha) {
    col.a = 0.0;
  }
  
  gl_FragColor = col;
}
