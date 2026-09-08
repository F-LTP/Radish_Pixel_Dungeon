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
  vec2 start = uCutLine.xy;
  vec2 dir = normalize(uCutLine.zw);

  // 计算起点到帧四角沿方向的最大带符号投影距离，用于归一化到整张贴图
  float d00 = dot(vec2(0.0, 0.0) - start, dir);
  float d10 = dot(vec2(1.0, 0.0) - start, dir);
  float d01 = dot(vec2(0.0, 1.0) - start, dir);
  float d11 = dot(vec2(1.0, 1.0) - start, dir);
  float halfSpan = max(max(d00, d10), max(d01, d11));
  halfSpan = max(halfSpan, -min(min(d00, d10), min(d01, d11)));

  // 以起点为中心的带符号距离，取绝对值使裂痕向两侧对称扩散
  float dist = dot(localUV - start, dir) / halfSpan;
  float edge = abs(dist) - uCutAlpha;

  if (edge < 0.0) {
    col.a = 0.0;
  } else if (edge < 0.04) {
    col.rgb = mix(col.rgb, vec3(1.0, 0.85, 0.65), 1.0 - edge / 0.04);
  }
  col.a *= uAlpha;
  
  gl_FragColor = col;
}
