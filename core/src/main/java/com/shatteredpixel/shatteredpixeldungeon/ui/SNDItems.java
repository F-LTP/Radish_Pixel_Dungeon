package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.utils.RectF;
import java.util.HashMap;
import java.util.Map;

/**
 * Slice&Dice 图标加载器
 * 优先从 snd/atlas_image.atlas 解析贴图坐标，加载 snd/atlas_image.png 中的图标。
 * 覆盖物品(item/)、法术(ability/spell/)、技能(ability/tactic/)、触发(trigger/)等全部区域。
 *
 * 若 .atlas 文件缺失或解析失败，REGIONS 将为空，调用方应自行处理。
 */
public class SNDItems {

    public static final String ATLAS = "snd/atlas_image.png";
    public static final String ATLAS_FILE = "snd/atlas_image.atlas";
    public static final int DEFAULT_SIZE = 14;

    // 从 .atlas 文件解析出的 名称 -> {x, y, w, h} 映射
    private static final Map<String, int[]> REGIONS = new HashMap<>();
    
    static {
        loadAtlas();
    }
    
    /**
     * 解析 snd/atlas_image.atlas，将所有区域坐标载入 REGIONS。
     * 物品(item/xxx)与法术(ability/spell/xxx)使用短名 xxx 作为 key，
     * 其余区域(ability/tactic/、trigger/、icon/ 等)使用完整路径名。
     */
    private static void loadAtlas() {
        FileHandle file = Gdx.files.internal(ATLAS_FILE);
        if (file == null || !file.exists()) {
            return;
        }
        try {
            String[] lines = file.readString("UTF-8").split("\\r?\\n");
            String current = null;
            Integer x = null, y = null, w = null, h = null;
            boolean rotate = false;
            for (String raw : lines) {
                boolean indented = raw.length() > 0 && Character.isWhitespace(raw.charAt(0));
                String line = raw.trim();
                if (line.isEmpty()) continue;

                if (!indented) {
                    // flush previous region before starting a new one
                    if (current != null && x != null && y != null && w != null && h != null) {
                        register(current, x, y, w, h, rotate);
                    }
                    current = null; x = null; y = null; w = null; h = null; rotate = false;
                    // skip atlas header lines (image name, size:, format:, filter:, repeat:)
                    if (line.startsWith("size:") || line.startsWith("format:")
                            || line.startsWith("filter:") || line.startsWith("repeat:")
                            || line.endsWith(".png")) {
                        continue;
                    }
                    current = line;
                } else {
                    String key = line.substring(0, line.indexOf(':')).trim();
                    String val = line.substring(line.indexOf(':') + 1).trim();
                    if (key.equals("xy")) {
                        String[] parts = val.split(",");
                        if (parts.length >= 2) {
                            x = Integer.parseInt(parts[0].trim());
                            y = Integer.parseInt(parts[1].trim());
                        }
                    } else if (key.equals("size")) {
                        String[] parts = val.split(",");
                        if (parts.length >= 2) {
                            w = Integer.parseInt(parts[0].trim());
                            h = Integer.parseInt(parts[1].trim());
                        }
                    } else if (key.equals("rotate")) {
                        rotate = "true".equalsIgnoreCase(val);
                    }
                }
            }
            // flush last region
            if (current != null && x != null && y != null && w != null && h != null) {
                register(current, x, y, w, h, rotate);
            }
        } catch (Exception ignored) {
            // atlas 解析失败时清空，后续调用返回 null/占位符
            REGIONS.clear();
        }
    }
    
    private static void register(String name, int x, int y, int w, int h, boolean rotate) {
        // rotate=true 时贴图内实际占用为 h×w
        int rw = rotate ? h : w;
        int rh = rotate ? w : h;
        int[] coords = new int[]{x, y, rw, rh};
        REGIONS.put(name, coords);
        if (name.startsWith("item/")) {
            REGIONS.put(name.substring(5), coords);
        } else if (name.startsWith("ability/spell/")) {
            REGIONS.put(name.substring(14), coords);
        }
    }
    
    /**
     * 查询某名称对应的坐标。
     */
    private static int[] coords(String name) {
        if (name == null) return null;
        return REGIONS.get(name);
    }
    
    /**
     * 获取 SND atlas 的纹理（共享缓存）
     * @return SmartTexture 对象
     */
    public static SmartTexture texture() {
        return TextureCache.get(ATLAS);
    }
    
    /**
     * 获取指定名称物品的 UV 坐标
     * @param name 物品名称
     * @return RectF UV 坐标，如果找不到则返回 null
     */
    public static RectF frame(String name) {
        int[] coords = coords(name);
        if (coords == null) return null;
        SmartTexture tex = texture();
        return tex.uvRect(coords[0], coords[1], coords[0]+coords[2], coords[1]+coords[3]);
    }
    
    /**
     * 加载指定名称的物品图标
     * @param name 物品名称（不含 item/ 前缀），如 "arrow", "longsword"
     * @return Image 对象，如果找不到则返回 null
     */
    public static Image get(String name) {
        int[] coords = coords(name);
        if (coords == null) {
            return null;
        }
        return new Image(ATLAS, coords[0], coords[1], coords[2], coords[3]);
    }
    
    /**
     * 加载指定名称的物品图标，如果找不到则返回占位符
     * @param name 物品名称
     * @return Image 对象
     */
    public static Image getOrPlaceholder(String name) {
        Image img = get(name);
        if (img != null) {
            return img;
        }
        // 返回默认占位符（第一个物品图标）
        return new Image(ATLAS, 162, 205, DEFAULT_SIZE, DEFAULT_SIZE);
    }
    
    /**
     * 检查是否存在指定名称的物品图标
     */
    public static boolean has(String name) {
        return coords(name) != null;
    }
    
    /**
     * 获取所有可用的物品名称
     */
    public static String[] names() {
        return REGIONS.keySet().toArray(new String[0]);
    }
}
