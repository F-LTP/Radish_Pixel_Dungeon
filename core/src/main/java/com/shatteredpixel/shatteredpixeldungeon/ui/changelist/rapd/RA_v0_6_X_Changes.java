package com.shatteredpixel.shatteredpixeldungeon.ui.changelist.rapd;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.DeminionSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RadishEnemySprite.GiantWormSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeInfo;
import com.watabou.noosa.Image;

import java.util.ArrayList;

import static com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.*;

public class RA_v0_6_X_Changes {
    public static void addAllChanges(ArrayList<ChangeInfo> changeInfos) {
        add_v06_9_Changes(changeInfos);
        add_v06_8_Changes(changeInfos);
        add_v06_4_Changes(changeInfos);
        add_v06_3_Changes(changeInfos);
        add_v06_2_Changes(changeInfos);
        add_v06_1_Changes(changeInfos);
        add_v06_0_Changes(changeInfos);
    }
    public static void add_v06_9_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.6.9", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton((new GnollKingSprite()), "新Boss：豺狼大酋长",
                "15层B面Boss，击败获得1500金币，必定掉落一个升级卷轴。\n\n注意：此为双Boss，因此必须全部击败后才会掉落奖励。"));

        changes.addButton(new ChangeButton((new GnollShamanKingSprite()), "新Boss：豺狼大祭司",
                "15层B面Boss，击败获得1500金币，必定掉落一个升级卷轴。\n\n注意：此为双Boss，因此必须全部击败后才会掉落奖励。"));

        changes.addButton(new ChangeButton((new FrogSprite()), "新敌人：青蛙",
                "栖息于苔藓洞穴，其危险性比啮齿小鼠更大。"));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISTANT_WELL), ("新地形：苔藓洞穴"),
                ("在一区2层必定生成，一个迷你副本，里面不会生成力量药水和升级卷轴，但据说有一个较为珍贵的宝藏在这里……")));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.BLESS_SCROLL)), "新物品：赐福卷轴",
                "可为你提供护甲/武器临时+1升级，击败核心Boss后自动失效。"));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.RECTOR, 6), ("牧师护甲天赋全面实装"),
                ("牧师三大天赋技能：终末奇迹，暗影咒文，凡体受神全面实装，欢迎各位游玩。")));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复 隐没于人天赋不生效\n" +
                        "_-_ 修复 激素涌动攻速异常\n" +
                        "_-_ 修复 上一个版本的相关游戏崩溃问题")));

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), ("杂项修改"),
                ("1.现在Boss血条支持多血条，至多支持4个\n" +
                        "2.现在子层跳楼将自动返回到入口处\n" +
                        "3.部分素材优化迭代")));


    }


    public static void add_v06_8_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.6.8", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.REPLACE_POINT)), "新武器：改锥",
                "4阶，力量需求16\n" +
                        "初始2-10，成长1-2，攻速0.4\n" +
                        "一对锋利的锥子，可以捅向敌人的伤患处，越来越深。\n\n在上回合每造成一次物理伤害，此武器的伤害就越致命。"));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.WAND_NEWSTAR)), "新法杖：新星法杖",
                "使用这根法杖可以对自己或者友军使用，随后以那个位置为中心，3*3圆形范围内的所有敌人受到2+等级-5+等级*4点伤害，友军获得法杖等级的护盾。等级每提升4级就会使范围扩大一圈。\n" +
                        "\n" +
                        "战法特效为使用新星法杖进行近战攻击时，有概率触发_新星治疗_，它会将_老魔杖_和_所有法杖_的总等级综合，并迅速回馈给自己的主人。\n\n" +
                        "新星法杖的元素风暴战技：没有特殊效果，但是它的伤害倍率是元素风暴基础伤害的2倍。\n\n" +
                        "谁知道制作这根法杖的家伙和牧师做了什么交易，但是不用祈祷就能使用的神圣法术可是许多人梦寐以求的，nova！"));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.WAND_BOMBWAVES)), "新法杖：新星法杖",
                "使用这根法杖会先指定一处位置，在下回合以那个位置为中心，5×5圆形范围内的所有敌人受到3+等级-10+等级*4点伤害。并击退范围内的友军2格。\n" +
                        "\n" +
                        "战法特效为有1+等级/4+等级的概率向目标脚下放置一个爆炸源。\n\n" +
                        "震爆法杖的元素风暴战技：将会在英雄脚下生成一个威力十分巨大的震爆范围，此效果远超直接释放。\n\n" +
                        "此类法杖延迟生效和范围过大的特点，一直被魔法学会列为缺点，直到如今也没有人能替它翻案。"));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.MAGNETIC_CROWN)), "新神器：磁力王冠",
                "带上这顶王冠时，你感受到了空间中微弱的磁力，这种力量也许能把你和其他生物拖向某一地点……\n\n" +
                        "升级方式：在陷阱地块上获得经验。"));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.LIGHT_KING)), "新饰品：光明之冠",
                "你能感受得到这枚闪烁的皇冠中蕴藏着力量，不过需要以你良好的状态诱发之。\n" +
                        "\n" +
                        "在当前的等级下，当你的当前生命值大于或等于最大生命值的90%/85%/80%/75%时，这件饰物会使你造成的所有伤害增加25%/33%/41%/50%，反之则降低如上值。"));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.RIVER_GLASS)), "新饰品：塑形玻璃",
                "这件饰品有很强的延展性，你能感受到它给你的所有装备都镀了一层性能近似但更柔软的膜。\n" +
                        "\n" +
                        "在当前的等级下，这件饰物每级会使所有可被升级的装备获得1级虚拟升级，但也会使装备在发挥效用时多进行1次判定并取其中最小值结算。"));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.RECTOR, 6), ("牧师护甲第一天赋实装"),
                ("牧师首个护甲技能---终末圣祷，现已正式实装！")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复 嬗变饰品萝卜有概率会闪退\n" +
                        "_-_ 修复 天赋大地之心+1的加速未生效，+2 踩在草上就有露珠，并且扔下水袋后，踩不了草游戏异常\n" +
                        "_-_ 修复 钢铁烈阳会使自己释放的法术神罚不给护盾\n" +
                        "_-_ 修复 巨人杀手的暴击增益对精英强敌的精英无效\n" +
                        "_-_ 修复 幻影雾剑在攻击时未击中敌人时自身隐形\n" +
                        "_-_ 修复 使用回音锤攻击巨型精英时未击杀也可以产生特效\n" +
                        "_-_ 修复 狂战士怒气获取问题\n" +
                        "_-_ 修复 部分测试工具异常\n" +
                        "_-_ 修复 新星法杖的一堆问题\n" +
                        "_-_ 修复 护甲部分渲染素材异常")));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.STONE_DISARM)), "符石重做：探测符石",
                "原先为拆除符石，现在二合一。"));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY_LAND), ("主界面优化"),
                ("现在 日志 界面，可在游戏主界面打开。\n" +
                        "升级 界面 迭代新版")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DATA), ("网络协议迭代"),
                ("从0.6.7-FD开始，迭代网络协议，重启游戏内部自动更新")));
    }

    public static void add_v06_4_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.6.4", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.WHITE_KING_GOD_SWORD)), "新武器：白帝圣剑",
                "三阶，力量需求14\n" +
                        "初始3-16，成长2-3\n" +
                        "在每位敌人首次出现在你视野中时，立刻对其造成一次相当于攻击力60%+10%*等级的伤害。\n" +
                        "御剑跟着我！\n\n现在可以被磨重或减轻效果影响。"));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复星界沟通和神赐之礼生效异常问题\n" +
                        "_-_ 修复复仇怒号未正确显示的问题\n" +
                        "_-_ 修复战斗牧师的极效疗愈会在处于冷却时错误的触发并增加冷却时间，并且会被护甲格挡的零伤害触发\n" +
                        "_-_ 现在治疗飞镖和治疗炸弹会被挑战-【伤痛难愈】-的1/5效率影响\n" +
                        "_-_ 修复光能灌注给的临时生命值少了八点")));

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.LENGDS_PAGE), "育言故事正式回归",
                "育言故事回归，在探索地牢时阅读一些睡前小故事！"));
    }

    public static void add_v06_3_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.6.3", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.DARK_SHADOW_SWORD)), "新武器：暗影之刃",
                "二阶，力量需求12\n" +
                        "初始2-12，成长1-2，精准1.2\n" +
                        "视野内的每位敌人都会为这把武器提供20%+5%*等级的攻击速度。\n" +
                        "敌人越多，这把剑的思绪也就越多。"));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.WHITE_KING_GOD_SWORD)), "新武器：白帝圣剑",
                "三阶，力量需求14\n" +
                        "初始3-16，成长2-3\n" +
                        "在每位敌人首次出现在你视野中时，立刻对其造成一次相当于攻击力60%+10%*等级的伤害。\n" +
                        "御剑跟着我！"));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.RECTOR, 5), ("牧师恶魔天赋实装"),
                ("除执行者恶魔天赋尚未完成，其他均已实装。")));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.RADISH)), "新饰品：萝卜",
                "一株不应该在炼金锅里存在的蔬菜，似乎是整个地牢的精神象征，冥冥之中有人这么告诉你。\n" +
                        "萝卜地牢怎么能没有萝卜呢？\n" +
                        "在当前等级下，这件饰物会为你提供5%/10%/15%/20%的全局暴击率。"));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.GOLD_RADISH)), "新饰品：黄金萝卜",
                "这枚神秘纪念品闪烁着灿金色的光辉，并非暗金那种货色可比。你一定是把探索的运气全用到这上面了才能获得它。\n" +
                        "这件饰品的获取概率是其他饰品的1/10\n" +
                        "在当前等级下，这件饰物会使你所装备的非神器非传承装备等级固定为+1/+2/+3/+4。"));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复执行者的经验灌注天赋没有触发\n" +
                        "_-_ 修复战斗牧师的物理祈祷天赋有问题，投掷武器和复合弩的射击都能触发\n" +
                        "_-_ 修复圣地会把上下楼梯覆盖掉")));

        changes.addButton(new ChangeButton((new Image(new KingSprite())), "矮人国王调整",
                "如通过牧师击败矮人国王，直接掉落强化天赋书。\n\n牧师护甲技能还未完成，所以先用着恶魔4阶强化天赋。"));

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.LENGDS_PAGE), "育言故事回归",
                "育言故事回归，在探索地牢时阅读一些睡前小故事！"));
    }

    public static void add_v06_2_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.6.2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.RECTOR, 5), ("牧师转职-执行者"),
                ("新增红衣主教T3天赋 和 转职后的效果，欢迎各位尝鲜\n\n" +
                        "代行神权，灵活信仰【公用天赋】\n\n" +
                        "战斗牧师专属天赋：黑暗奉献，殉道之力，经验灌注")));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.APOWER)), "新技能：宽恕裁决",
                "立刻击杀攻击范围内一名_生命值低于60%的敌人_。\n\n这个技能需要消耗_4点信仰值_。\n\n释放失败不会扣减信仰值。"));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.BACKMESSAGE)), "新技能：背信弃义",
                "指定视野范围内的一点，其_3*3范围_内的所有生物获得初始值为_楼层数+3的流血_。\n\n这个技能需要消耗_12点信仰值_。"));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复牧师额外惩戒不消耗信仰的异常\n" +
                        "_-_ 修复一些小的崩溃异常\n" +
                        "_-_ 修复0层会掉饥饿的异常")));

        changes.addButton(new ChangeButton(Icons.get(Icons.BACKPACK), ("背包优化"),
                ("现在电脑端所有界面都已支持新布局")));
    }

    public static void add_v06_1_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.6.1", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.RECTOR, 5), ("牧师转职-红衣主教"),
                ("新增红衣主教T3天赋 和 转职后的效果，欢迎各位尝鲜\n\n" +
                        "代行神权，灵活信仰【公用天赋】\n\n" +
                        "战斗牧师专属天赋：圣火燎原，圣光洗礼，通天圣塔")));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.HOLYFIRE)), "新技能：圣火审判",
                "指定一个地格，在其3×3区域生成圣火场，火场内的所有生物受到的伤害增加1.3倍且沾染上圣火。"));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.HOLYLAND)), "新技能：圣地领域",
                "指定一个地格，并以其为中心生成一片5*5圆形的圣地区域。" +
                        "\n\n在圣地区域内的非英雄非飞行角色都会减少33%移速，对亡灵与恶魔类怪物则是减少50%移速，并且每回合它们受到区域数点伤害。"));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复牧师额外惩戒不消耗信仰的异常\n" +
                        "_-_ 修复一些小的崩溃异常\n" +
                        "_-_ 修复0层会掉饥饿的异常")));

        changes.addButton(new ChangeButton(Icons.get(Icons.BACKPACK), ("背包优化"),
                ("现在电脑端所有界面都已支持新布局")));
    }

    public static void add_v06_0_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.6.0", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.RECTOR, 5), ("牧师转职-战斗牧师"),
                ("新增战斗牧师T3天赋 和 转职后的效果，欢迎各位尝鲜\n\n" +
                        "代行神权，灵活信仰【公用天赋】\n\n" +
                        "战斗牧师专属天赋：钢铁烈阳，物理祈祷，极效疗愈")));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.CORRECT)), "技能Plus：神罚时刻",
                "战斗牧师的惩戒伤害会增加50%并会额外指定一个视野内的随机目标。"));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.BLESS)), "新技能：圣光之耀",
                "转职后自动替换_虔诚祈祷_，获得此技能。\n\n" +
                        "效果：获得25%伤害加成 + 25%的伤害减免，持续60回合。"));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.LIGHTIMUEE)), "技能Plus：光明领域",
                "战斗牧师的光能灌注的效果变更至获得区域数*12点临时生命与区域数*4回合激素涌动，其他不变。"));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY_LAND), ("UI优化"),
                ("现在牧师的临时血条可在血条上显示出来")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复牧师临时血条可以抵挡超出伤害的异常\n" +
                        "_-_ 灵魂干涉优化")));

        changes.addButton(new ChangeButton(Icons.get(Icons.BACKPACK), ("背包优化"),
                ("现在电脑端所有界面都已支持新布局")));
    }

}
