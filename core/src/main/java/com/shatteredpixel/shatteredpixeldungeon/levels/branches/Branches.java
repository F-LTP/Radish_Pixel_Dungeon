/*
 * Radish Pixel Dungeon
 * 分支系统 - 分支注册表
 */

package com.shatteredpixel.shatteredpixeldungeon.levels.branches;

import com.shatteredpixel.shatteredpixeldungeon.levels.CavesBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CavesLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.DeadEndLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.LastLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.MiningLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.SmallGrassMiniLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.ZeroLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GnollKingBossLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * 分支注册表：管理所有注册的分支
 * 
 * 在游戏启动时初始化所有分支配置
 */
public class Branches {
    
    private static final Map<String, Branch> registry = new HashMap<>();
    
    // 预定义的分支 ID 常量
    public static final String MAIN = "main";
    public static final String MOSS = "moss";
    public static final String MINING = "mining";
    
    /**
     * 初始化所有分支（在游戏启动时调用）
     */
    public static void init() {
        registry.clear();
        
        // 主线分支
        register(createMainBranch());
        
        // 苔藓分支
        register(createMossBranch());
        
        // 采矿分支
        register(createMiningBranch());
    }
    
    /**
     * 注册分支
     */
    public static void register(Branch branch) {
        registry.put(branch.id, branch);
    }
    
    /**
     * 获取分支配置
     */
    public static Branch get(String branchId) {
        if (registry.isEmpty()) {
            init();
        }
        return registry.get(branchId);
    }
    
    /**
     * 检查分支是否存在
     */
    public static boolean exists(String branchId) {
        if (registry.isEmpty()) {
            init();
        }
        return registry.containsKey(branchId);
    }
    
    /**
     * 获取所有分支 ID
     */
    public static Iterable<String> getAllIds() {
        return registry.keySet();
    }
    
    // ====== 分支创建方法 ======
    
    private static Branch createMainBranch() {
        // 主线 26 层 + 第 0 层（起始）
        @SuppressWarnings("unchecked")
        Class<? extends Level>[] levels = new Class[27];  // 索引 0-26，但 0 不用
        
        levels[0] = ZeroLevel.class;
        levels[1] = SewerLevel.class;
        levels[2] = SewerLevel.class;
        levels[3] = SewerLevel.class;
        levels[4] = SewerLevel.class;
        levels[5] = SewerBossLevel.class;
        levels[6] = PrisonLevel.class;
        levels[7] = PrisonLevel.class;
        levels[8] = PrisonLevel.class;
        levels[9] = PrisonLevel.class;
        levels[10] = PrisonBossLevel.class;
        levels[11] = CavesLevel.class;
        levels[12] = CavesLevel.class;
        levels[13] = CavesLevel.class;
        levels[14] = CavesLevel.class;
        levels[15] = CavesBossLevel.class;  // 或 GnollKingBossLevel，由 Dungeon 动态决定
        levels[16] = CityLevel.class;
        levels[17] = CityLevel.class;
        levels[18] = CityLevel.class;
        levels[19] = CityLevel.class;
        levels[20] = CityBossLevel.class;
        levels[21] = HallsLevel.class;
        levels[22] = HallsLevel.class;
        levels[23] = HallsLevel.class;
        levels[24] = HallsLevel.class;
        levels[25] = HallsBossLevel.class;
        levels[26] = LastLevel.class;
        
        return new Branch(MAIN, 26, "branch_main", levels);
    }
    
    private static Branch createMossBranch() {
        // 苔藓分支：2 层
        // 从主线 2 层进入
        @SuppressWarnings("unchecked")
        Class<? extends Level>[] levels = new Class[3];  // 索引 0-2
        levels[0] = DeadEndLevel.class;  // 占位，不使用
        levels[1] = SmallGrassMiniLevel.class;
        levels[2] = SmallGrassMiniLevel.class;  // 第2层
        
        return new Branch(MOSS, 2, "branch_moss", levels);
    }
    
    private static Branch createMiningBranch() {
        // 采矿分支：1 层（巨魔铁匠任务）
        // 从主线 11-14 层的黑smith房间进入
        @SuppressWarnings("unchecked")
        Class<? extends Level>[] levels = new Class[2];  // 索引 0-1
        levels[0] = DeadEndLevel.class;  // 占位，不使用
        levels[1] = MiningLevel.class;
        
        return new Branch(MINING, 1, "branch_mining", levels);
    }
}