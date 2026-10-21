#ifdef GL_ES
  precision highp float;
#endif

// 光蒸发着色器：参考《挺进地牢》Combine Rifle 击杀普通敌人时的消散效果。
// 与原作一致：每个源像素格用固定 hash 决定消失顺序，整只精灵的 emissive
// 强度随进度上升，像素随机消失；整体漂移由 ShaderEffect 在 CPU 侧完成。

varying vec2 vUV;

uniform sampler2D uTex;
uniform vec4  uFrame;      // 当前帧 UV 范围 (left, top, w, h)
uniform float uAlpha;      // 整体透明度
uniform float uProgress;   // 蒸发进度 0..1
uniform vec2  uPixelSize;  // 源像素块的局部 UV 尺寸
uniform float uRandom;     // 当前特效实例的随机种子
uniform float uEmissive;   // 当前 emissive 强度（原作从 1 升到 10）
uniform float uCellScale;  // 1 = 每个源像素一格；>1 用于小尺寸精灵放大格子

// 与 ETG 的 burn 相同的逐像素 hash，保证同一格在整个动画中只取一次随机值。
float hash(vec2 p) {
  return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

void main() {
  float p = clamp(uProgress, 0.0, 1.0);

  // 结束帧强制完全透明
  if (p >= 1.0 || uAlpha <= 0.0) {
    gl_FragColor = vec4(0.0);
    return;
  }

  vec4 col = texture2D(uTex, vUV);

  // 与原作一致：burn 后 alpha 过低的像素直接丢弃
  if (col.a < 0.1) {
    discard;
  }

  // 转换到当前帧局部 UV
  vec2 localUV = (vUV - uFrame.xy) / uFrame.zw;
  if (localUV.x < 0.0 || localUV.x > 1.0 ||
      localUV.y < 0.0 || localUV.y > 1.0) {
    gl_FragColor = vec4(0.0);
    return;
  }

  // 像素网格
  vec2 ps = uPixelSize;
  if (ps.x <= 0.0 || ps.y <= 0.0) {
    ps = vec2(1.0 / 16.0);
  }

  // 对齐源贴图像素；rnd 越小越早消失。
  vec2 cellSize = ps * max(uCellScale, 1.0);
  vec2 cell = floor(localUV / cellSize);
  float rnd = hash(cell + vec2(uRandom * 37.0, uRandom * 17.0));

  // ETG 的核心规则：随机阈值 + 进度裁切
  float alive = step(p, rnd);

  // ETG 的 emissive：整只精灵随进度变亮，而不是只点亮消失边缘
  col.rgb = min(col.rgb * max(uEmissive, 1.0), vec3(1.0));
  col.a *= alive * uAlpha;

  if (col.a < 0.1) {
    discard;
  }

  gl_FragColor = col;
}
