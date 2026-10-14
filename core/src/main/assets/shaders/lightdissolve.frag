#ifdef GL_ES
  precision highp float;
#endif

// 光解消散着色器：每个源像素块自上而下依次变白成光点、向右上缓慢飘散、收缩后渐隐消散。
// 采用"源像素块正向位移 + 反向搜索"实现：像素块真实平移并留下空洞，像素格对齐贴图源像素。

varying vec2 vUV;

uniform sampler2D uTex;
uniform vec4  uFrame;       // 当前帧 UV 范围 (left, top, w, h)
uniform float uAlpha;       // 整体透明度
uniform float uProgress;    // 消散进度 0..1
uniform vec2  uPixelSize;   // 源像素块的局部 UV 尺寸
uniform float uRandom;      // 当前特效实例的随机种子
uniform float uMoveAmount;  // 最大漂移量（相对帧宽的比例）
uniform vec2  uMoveDir;     // 主漂移方向，默认右上

// 搜索半径（格）。需 >= ceil(uMoveAmount * 最大速度 / uPixelSize.x)，
// 8 格可覆盖约 46px 宽的帧（uMoveAmount = 0.15 时）。SIDE = 2 * RADIUS + 1。
const int SIDE = 17;

float hash(vec2 p) {
  return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

// 由近及远枚举偏移量：0, ±1, ±2, ...（evenSign 决定 +1 还是 -1 先出现）
// 用 float 取模而非 %，以兼容 GLSL ES 1.00。
float radial(int i, float evenSign) {
  float fi = float(i);
  float steps = floor(fi * 0.5 + 0.5);
  return steps * ((mod(fi, 2.0) < 0.5) ? evenSign : -evenSign);
}

void main() {
  float p = clamp(uProgress, 0.0, 1.0);

  // 结束帧强制完全透明
  if (p >= 1.0 || uAlpha <= 0.0) {
    gl_FragColor = vec4(0.0);
    return;
  }

  // 1. 转换到当前帧局部 UV
  vec2 localUV = (vUV - uFrame.xy) / uFrame.zw;

  if (localUV.x < 0.0 || localUV.x > 1.0 ||
      localUV.y < 0.0 || localUV.y > 1.0) {
    gl_FragColor = vec4(0.0);
    return;
  }

  // 2. 像素网格
  vec2 ps = uPixelSize;
  if (ps.x <= 0.0 || ps.y <= 0.0) {
    ps = vec2(1.0 / 16.0);
  }

  vec2 baseCell = floor(localUV / ps);

  // 3. 归一化主漂移方向
  vec2 dir = uMoveDir;
  if (dot(dir, dir) < 0.0001) {
    dir = vec2(1.0, -0.4);
  }
  dir = normalize(dir);

  // 4. 在附近候选源格子中，寻找移动后覆盖当前片元的格子。
  //    方块向右上飞，故覆盖本片元的源格多在本格的下方(+y)与左侧(-x)，
  //    按此偏向由近及远遍历，命中即停。
  vec4 result = vec4(0.0);
  bool found = false;

  for (int i = 0; i < SIDE; i++) {
    if (found) break;
    float dy = radial(i, -1.0);   // 先查下方

    for (int j = 0; j < SIDE; j++) {
      float dx = radial(j, 1.0);  // 先查左侧

      vec2 cell = baseCell + vec2(dx, dy);
      vec2 center = (cell + 0.5) * ps;

      // 源格超出帧范围则忽略
      if (center.x < 0.0 || center.x > 1.0 ||
          center.y < 0.0 || center.y > 1.0) {
        continue;
      }

      // 每格的固定随机值（派生多个独立量，避免多次 hash 调用）
      float rnd = hash(cell + vec2(uRandom * 37.0, uRandom * 17.0));
      float rnd2 = fract(rnd * 7.13 + 0.37);
      float rnd3 = fract(rnd * 13.7 + 0.11);

      // 消散顺序：越靠上 order 越小，越早消散；叠加随机扰动
      float order = clamp(center.y + (rnd - 0.5) * 0.35, 0.0, 1.0);

      float departStart = mix(0.05, 0.52, order);
      float departEnd = departStart + 0.22;

      // 位移 0 -> 1，随后方块收缩成光点
      float travel = smoothstep(departStart, departEnd, p);
      float shrink = mix(1.0, 0.15, travel);

      // 每块轻微不同的方向与速度（小角度旋转，避免逐格三角函数开销）
      float a = (rnd2 - 0.5) * 0.65;
      float ca = 1.0 - 0.5 * a * a;
      float sa = a;
      vec2 jitterDir = vec2(dir.x * ca - dir.y * sa,
                            dir.x * sa + dir.y * ca);

      float speed = mix(0.65, 1.15, rnd3);
      vec2 offset = jitterDir * uMoveAmount * speed * travel;
      vec2 movedCenter = center + offset;

      // 5. 判断当前片元是否位于这个移动后（并收缩）的像素块内
      vec2 delta = abs(localUV - movedCenter);
      float halfX = ps.x * 0.5 * shrink;
      float halfY = ps.y * 0.5 * shrink;
      float inside = step(delta.x, halfX) * step(delta.y, halfY);

      if (inside > 0.5) {
        // 6. 始终从源格中心采样，保持像素块形状
        vec2 sampleUV = uFrame.xy + center * uFrame.zw;
        vec4 col = texture2D(uTex, sampleUV);

        // 7. 白化略早于位移：先转白，再飘散
        float whiteStart = departStart * 0.7;
        float white = smoothstep(whiteStart, whiteStart + 0.18, p);

        // 淡出在漂移开始后逐渐进行
        float fadeStart = min(departEnd + 0.12, 0.90);
        float fadeEnd = max(mix(0.72, 0.98, order), fadeStart + 0.04);
        float fade = 1.0 - smoothstep(fadeStart, fadeEnd, p);

        col.rgb = mix(col.rgb, vec3(1.0), white);
        col.a *= fade * uAlpha;

        result = col;
        found = true;
        break;
      }
    }
  }

  gl_FragColor = found ? result : vec4(0.0);
}
