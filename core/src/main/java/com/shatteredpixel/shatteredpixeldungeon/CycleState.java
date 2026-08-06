/*
 * Radish Pixel Dungeon
 * 循环状态 - 追踪楼梯循环落点
 */

package com.shatteredpixel.shatteredpixeldungeon;

import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * 追踪玩家在各 pairId 组的循环索引
 * 
 * 同一 pairId 的楼梯形成循环组：
 * - 2个：A → B → A → B...
 * - 3个：A → B → C → A → B → C...
 */
public class CycleState implements Bundlable {
    
    private Map<String, Integer> indices = new HashMap<>();
    
    private static final String CYCLE_KEYS = "cycle_keys";
    private static final String CYCLE_VALUES = "cycle_values";
    
    /**
     * 获取下一次落点索引
     * 
     * @param pairId 配对 ID
     * @param size 循环组大小
     * @return 当前索引（调用后自动递增）
     */
    public int next(String pairId, int size) {
        int i = indices.getOrDefault(pairId, 0);
        indices.put(pairId, (i + 1) % size);
        return i;
    }
    
    /**
     * 获取当前索引（不递增）
     */
    public int getCurrent(String pairId) {
        return indices.getOrDefault(pairId, 0);
    }
    
    /**
     * 重置指定 pairId 的循环状态
     */
    public void reset(String pairId) {
        indices.remove(pairId);
    }
    
    /**
     * 重置所有循环状态
     */
    public void resetAll() {
        indices.clear();
    }
    
    @Override
    public void storeInBundle(Bundle bundle) {
        if (indices.isEmpty()) return;
        
        String[] keys = indices.keySet().toArray(new String[0]);
        int[] values = new int[keys.length];
        for (int i = 0; i < keys.length; i++) {
            values[i] = indices.get(keys[i]);
        }
        bundle.put(CYCLE_KEYS, keys);
        bundle.put(CYCLE_VALUES, values);
    }
    
    @Override
    public void restoreFromBundle(Bundle bundle) {
        indices.clear();
        if (bundle.contains(CYCLE_KEYS)) {
            String[] keys = bundle.getStringArray(CYCLE_KEYS);
            int[] values = bundle.getIntArray(CYCLE_VALUES);
            for (int i = 0; i < keys.length; i++) {
                indices.put(keys[i], values[i]);
            }
        }
    }
}