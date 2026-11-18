/*
 * Radish Pixel Dungeon
 * 分支系统 - 分支配置类
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.branches;

import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

/**
 * 分支配置：定义一个独立的地牢分支
 * 
 * 每个分支有唯一 ID、层数范围、每层的 Level 类型等
 */
public class Branch {
    
    public final String id;              // 分支唯一标识（如 "main", "moss", "mining"）
    public final int maxDepth;           // 最大层数（0 = 无限/特殊）
    public final String displayNameKey;  // 国际化显示名称的 key
    
    // 每层的 Level 类（索引从 1 开始，所以数组大小是 maxDepth + 1）
    private final Class<? extends Level>[] levelClasses;
    
    public Branch(String id, int maxDepth, String displayNameKey, Class<? extends Level>[] levelClasses) {
        this.id = id;
        this.maxDepth = maxDepth;
        this.displayNameKey = displayNameKey;
        this.levelClasses = levelClasses;
    }
    
    /**
     * 获取分支的本地化名称
     */
    public String getLocalizedName() {
        return Messages.get(Branch.class, id);
    }
    
    /**
     * 创建指定层的 Level 实例
     * @param depth 层数（从 1 开始）
     * @return Level 实例，如果超出范围返回 null
     */
    public Level createLevel(int depth) {
        if (depth < 1 || depth > maxDepth) {
            return null;
        }
		try {
			return levelClasses[depth].getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create level for branch=" + id + " depth=" + depth, e);
        }
    }
    
    /**
     * 检查是否还有下一层
     */
    public boolean hasMoreDepth(int currentDepth) {
        return currentDepth < maxDepth;
    }
    
    /**
     * 获取指定层的 Level 类
     */
    public Class<? extends Level> getLevelClass(int depth) {
        if (depth < 1 || depth > maxDepth) {
            return null;
        }
        return levelClasses[depth];
    }
}
