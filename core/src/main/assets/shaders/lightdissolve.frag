#ifdef GL_ES
  precision highp float;
#endif

// 光解消散着色器：每个源像素块从右上到左下依次变白成方形像素光点，沿右上方向持续飞行，
// 直到越过帧边界被裁掉。采用"源像素块正向位移 + 反向扫描"实现：
// 像素块保持原尺寸真实平移并留下空洞，像素格对齐贴图源像素。

varying vec2 vUV;

uniform sampler2D uTex;
uniform vec4  uFrame;       // 当前帧 UV 范围 (left, top, w, h)
uniform float uAlpha;       // 整体透明度
uniform float uProgress;    // 消散进度 0..1
uniform vec2  uPixelSize;   // 源像素块的局部 UV 尺寸
uniform float uRandom;      // 当前特效实例的随机种子
uniform float uFlySpeed;    // 单位进度内沿方向飞行的局部 UV 距离
uniform vec2  uMoveDir;     // 已按翻转/旋转换算到帧 UV 空间的右上方向

// 反向扫描步数上限。每步每轴最多前进半格，384 步可覆盖约 192 格。
const int MAX_SCAN = 384;

// 廉价 hash（无 sin）
float hash(vec2 p) {
  vec3 p3 = fract(vec3(p.xyx) * 0.1031);
  p3 += dot(p3, p3.yzx + 33.33);
  return fract((p3.x + p3.y) * p3.z);
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

  // 3. 归一化主漂移方向
  vec2 dir = uMoveDir;
  if (dot(dir, dir) < 0.0001) {
    dir = vec2(0.7071, -0.7071);
  }
  dir = normalize(dir);

  // 4. 所有源格沿同一方向平移，覆盖当前片元的源格必定在 -dir 方向的后方线上。
  //    从片元位置反向扫描即可，无需二维搜索；步长保证每个轴单步不超过半格。
  vec4 result = vec4(0.0);
  bool found = false;

  float stepUV = 0.5 * min(ps.x / max(abs(dir.x), 0.0001),
                            ps.y / max(abs(dir.y), 0.0001));
  vec2 scan = localUV;

  for (int i = 0; i < MAX_SCAN; i++) {
    if (found) break;

    vec2 cell = floor(scan / ps);
    vec2 center = (cell + 0.5) * ps;

    // 反向扫描在单个轴上单调移动；离开帧范围后不可能再有源格
    if (center.x < 0.0 || center.x > 1.0 ||
        center.y < 0.0 || center.y > 1.0) {
      break;
    }

    // 每格的固定随机值（派生多个独立量，避免多次 hash 调用）
    float rnd = hash(cell + vec2(uRandom * 37.0, uRandom * 17.0));
    float rnd2 = fract(rnd * 7.13 + 0.37);
    float rnd3 = fract(rnd * 13.7 + 0.11);

    // 消散顺序：从右上到左下推进（右上 order 最小，最早变白、脱落）；叠加随机扰动
    float order = clamp(((1.0 - center.x) + center.y) * 0.5 + (rnd - 0.5) * 0.35, 0.0, 1.0);

    // 白化进度：所有像素最终都会白化成光点
    float whiteStart = mix(0.03, 0.26, order);
    float white = smoothstep(whiteStart, whiteStart + 0.34, p);

    // 白化达到 75% 后才开始脱落（smoothstep 在 t≈0.625 时到达 75%）
    float moveStart = whiteStart + 0.34 * 0.625;

    // 每格移动速度随机（0 - uFlySpeed），平均速度为满速的一半；
    // 最远距离不超过原来的满速移动。
    float moveSpeed = uFlySpeed * rnd2;

    float flyTime = max(0.0, p - moveStart);
    vec2 movedCenter = center + dir * moveSpeed * flyTime;

    // 5. 当前片元是否位于这个移动后（保持原尺寸）的方形像素块内
    vec2 delta = abs(localUV - movedCenter);
    if (delta.x <= ps.x * 0.5 && delta.y <= ps.y * 0.5) {
      // 6. 始终从源格中心采样，保持像素块形状
      vec2 sampleUV = uFrame.xy + center * uFrame.zw;
      vec4 col = texture2D(uTex, sampleUV);

      // 7. 白光点从 0 透明度淡入，最终达到 50% - 80% 的不透明度；
      //    原贴图同时淡出，避免白化开始前整块贴图消失。
      float lightAlpha = white * mix(0.5, 0.8, rnd3) * col.a;

      // 8. 开始移动后逐渐变得更透明
      float fade = 1.0 - smoothstep(moveStart, moveStart + 0.45, p);

      // 白化上限为 70% 白 + 30% 原色
      col.rgb = mix(col.rgb, vec3(1.0), white * 0.7);
      col.a = (col.a * (1.0 - white) + lightAlpha) * fade * uAlpha;
      result = col;
      found = true;
    }

    scan -= dir * stepUV;
  }

  gl_FragColor = found ? result : vec4(0.0);
}
