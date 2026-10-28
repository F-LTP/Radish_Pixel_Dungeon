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

public class RA_v0_5_X_Changes {
    public static void addAllChanges(ArrayList<ChangeInfo> changeInfos) {
        add_v05_8_Changes(changeInfos);
        add_v05_7_Changes(changeInfos);
        add_v05_6_Changes(changeInfos);
        add_v05_5_Changes(changeInfos);
        add_v05_0_Changes(changeInfos);
    }
    public static void add_v05_8_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.5.8-9", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.WARNING), ("异常调试日志"),
                ("在游戏卡死时，通过此系统可有效进行反馈。")));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.WAND_GNOLL)), "新法杖：豺狼法杖",
                "0.5.8--由彦木作者进行联动。\n\n" +
                        "0.5.9--修复了卡死异常"));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.SPOTOA)), "新饰品：发芽土豆",
                "0.5.8--由彦木作者进行联动。\n\n" +
                        "0.5.9--部分效果缺失修正"));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.RECTOR, 5), ("牧师相关Bug批量修正"),
                ("_-_ 修复暴击无法触发恩惠之雨与灵魂干涉\n" +
                        "_-_ 绝望祷言相关问题已修复\n" +
                        "_-_ 光能灌注相关问题已修复\n" +
                        "_-_ 祝福一餐相关问题已修复\n" +
                        "_-_ 惩戒伤害相关问题已修复\n" +
                        "_-_ 现在投掷武器不会触发灵魂干涉\n" +
                        "_-_ 现在灵魂干涉不再对精英怪生效，并且T2生效为低于当前最大生命的二分之一\n" +
                        "_-_ 将神圣护体的天赋数值下调为2/3回合，而非2/5回合\n" +
                        "_-_ 将灵魂干涉的生效条件改为生命值小于等于2/4")));

        changes.addButton(new ChangeButton(Icons.get(Icons.BACKPACK), ("背包优化"),
                ("主背包数量调整为25，其他背包格子数量调整为24")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_V0.5.8\n" +
                        "_-_ 修复惩戒伤害幂运算的异常\n" +
                        "_-_ 修复商品出售和购买价格不一致的异常\n" +
                        "_-_ 牧师的信仰值现在和最大经验挂钩\n" +
                        "_-_ 修正一些小崩溃异常\n" +
                        "_-_V0.5.7\n" +
                        "_-_ 修复牧师拾取天狗面具闪退的异常\n" +
                        "_-_ 修复上个版本FireBase报告的崩溃异常")));
    }

    public static void add_v05_7_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.5.7", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton((new ZikkSprite()), "新Boss：大蛇兹克",
                "有极小概率替换粘咕，更加狡猾，但战利品也更加丰厚。"));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.RECTOR, 5), ("牧师1-2阶开放测试"),
                ("牧师基础技能和1/2阶天赋已经可用，欢迎各位测试！\n\n" +
                        "天赋T1：餐前祈祷，心灵感应，恩惠之雨，虔诚祷告\n" +
                        "天赋T2：祝福一餐，灵魂干涉，光辉灌注，神圣护体，绝望祷言\n")));

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.ABERFORTH)), "传承武器测试",
                "可在测试时间中进行测试，后续将会渐渐正式上线到正常游玩中。"));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), ("其他调整"),
                ("1.一层刷怪数量-1\n" +
                        "2.调整伤痛难愈从1/10=>1/5\n" +
                        "3.修复弱点洞悉boss问题及描述\n" +
                        "4.每层有33%的概率额外一个食物，如果开启没入黑暗挑战，25%的概率额外一个火把\n" +
                        "5.部分生成器赘余破碎武器完全移除\n" +
                        "6.修复部分文案异常\n" +
                        "7.游戏检测更新的接口迭代")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "_-_V0.5.6\n" +
                        "_-_ 修复房间生成异常问题\n" +
                        "_-_ 修复怪物图鉴数据保存异常问题\n" +
                        "_-_ 修复0层可无限上楼的问题\n" +
                        "_-_ 修复祝福之戒未完全生效的问题\n" +
                        "_-_ 修复末日守卫导致法师天赋_储存护盾_失效的异常"));
    }

    public static void add_v05_6_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.5.6", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.SHADOW_BOOK)), "幻影之书效果优化",
                "你每阅读一次卷轴，就在你周围生成1+0.2*武器等级个镜像（向下取整）"));

        Image critImage = new Image(Assets.Effects.TEXT_ICONS, 56, 7, 7, 7);
        critImage.scale.set(PixelScene.align(1.72f));
        changes.addButton(new ChangeButton(critImage, ("暴击视觉效果调整"),
                ("暴击图标现在包含穿甲暴伤图标（白色）")));
    }

    public static void add_v05_5_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.5.4", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.RECTOR, 5), ("牧师预载"),
                ("牧师开始制作，目前已经预载")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton((new ItemSprite(ItemSpriteSheet.TAIKIG)), "太刀效果优化",
                "大太刀效果优化\n" +
                        "修复大太刀被缴械陷阱传送走冷静和必定暴击的buff仍在"));

        changes.addButton(new ChangeButton(Icons.get(Icons.GOLD), ("其他调整"),
                ("1.每区平均房间数增加1/1/2/2/2，物品生成数量增加15%，每层食物生成数量额外增加0.33机率\n" +
                        "2.挑战弱点洞悉的最低伤害上升至1/3/6/10/15\n" +
                        "3.更改地龙的贴图，让它在潜伏时更显眼。让地龙不会因为集群挑战而醒来")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "_-_V0.5.4\n" +
                        "_-_ 修复祝福之戒不生效的问题\n" +
                        "_-_ 完善稀有怪返程倍率 1.1/1.5/2.5/5/9"));
    }

    public static void add_v05_0_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.5.0-2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.HUNTRESS, 6), ("女猎恶魔天赋完全实装"),
                ("射技决斗 疾风骤雨 药镖专家 大地之心均已实装")));

        changes.addButton(new ChangeButton(new ItemSprite(KILL_BOAT), "武器增强：斩舰刃",
                "成长从2-8改为2-10。"));

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.CIRCLE_SWORD), "新武器：轮刃",
                "你的所有护甲值都会转化为此武器的攻击力。这件武器对目标周围的敌人造成溅射效果。\n\n笨重难用的武器。挥动它，几乎意味着放弃全身上下所有的防御手段。【穿戴后防御变为0】\n\n四阶，力量需求16，初始8-20，成长1-5\n\n开发组碎碎念：好像是骰杀里面武器改，不过咱也不知道啦，祝各位玩的开心。"));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.ROGUE, 6), ("盗贼恶魔天赋完全实装"),
                ("严阵以待 能量回收 动能转换 风暴奔袭均已实装")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new BuffIcon(BuffIndicator.DEGRADE, true), "降级增强",
                "修复武器/护甲降级不生效的问题，同时，在降级状态下，国王之戒的效果完全失效。"));

        changes.addButton(new ChangeButton(Icons.get(Icons.GOLD), ("商店售卖调整"),
                ("每增加一级售卖的价格就增加30%，有任何附魔加20%，出售价格从之前的3倍改为2倍。")));

        Image critImage = new Image(Assets.Effects.TEXT_ICONS, 49, 7, 7, 7);
        critImage.scale.set(PixelScene.align(1.72f));
        changes.addButton(new ChangeButton(critImage, ("暴击视觉效果调整"),
                ("暴击现在不再显示为一个文本，而是一个图标\n\n图标灵感：某农暴击图标")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "_-_V0.5.2\n" +
                        "_-_ 修复武器攻速的严重异常\n" +
                        "_-_ 修复拉莱耶文本反伤异常\n" +
                        "_-_ 修复锁镰回合结算异常\n" +
                        "_-_ 修正轮刃文本显示异常",
                "_-_V0.5.1\n" +
                        "_-_ 修复始终暴击的异常\n" +
                        "_-_ 修复轮刃可以伤害自己的异常\n" +
                        "_-_ 优化决斗家的一些赘余代码\n" +
                        "_-_ 部分文案优化\n",
                "_-_V0.5.0\n" +
                        "_-_ 修复部分文案异常\n" +
                        "_-_ 修复藤蔓陷阱天赋失效\n" +
                        "_-_ 拉莱耶文本现在不会伤害英雄\n" +
                        "_-_ 修复巨型蠕虫特殊攻击效果失效\n" +
                        "_-_ 修复气动拳套未气动时仍然在说能量不足\n" +
                        "_-_ 修复装备武力之戒之后武器会出现决斗家的充能\n" +
                        "_-_ 修复在伤痛难愈挑战下，部分食物出现1血异常效果\n" +
                        "_-_ 移除育言故事\n" +
                        "_-_ 修复一堆异常"));
    }

}
