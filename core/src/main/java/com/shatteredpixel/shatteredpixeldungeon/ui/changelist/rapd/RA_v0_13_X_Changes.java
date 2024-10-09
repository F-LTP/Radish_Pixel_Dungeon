package com.shatteredpixel.shatteredpixeldungeon.ui.changelist.rapd;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeInfo;
import com.watabou.noosa.Image;

import java.util.ArrayList;

public class RA_v0_13_X_Changes {

    public static void addAllChanges( ArrayList<ChangeInfo> changeInfos ){
        add_v03_5_Changes(changeInfos);
        add_v03_4_Changes(changeInfos);
        add_v03_3_Changes(changeInfos);
        add_v03_2_Changes(changeInfos);
        add_v03_1_Changes(changeInfos);
    }

    public static void add_v03_5_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.4.0", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.STATS), ("全局优化"),
                ("每层的初始出怪数量增加2，物品刷新率增加个10%，楼层大小增加15%。")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY_LAND), ("随机图层"),
                ("每层可以出现隐藏图块，但怪物不变。你也许能在新图块环境中有更好的作战积极性！")));

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), ("全新3大附魔登场"),
                ("汲能，狂热，重击 附魔登场")));

        changes.addButton(new ChangeButton(Icons.get(Icons.WARNING), ("0层相关问题修复"),
                ("吞力量，升级，魔能触媒问题修正")));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "修复了以下Bug:\n\n" +
                        "_来自于 v0.4.0:_\n\n" +
                        "[修复者：JDSALing]：\n"+
                        "_-_ 0.修复 斩舰刃 和 两书的问题\n" +
                        "_-_ 1.移除决斗家\n" +
                        "_-_ 2.修复 上个版本的一些崩溃问题" ));
    }

    public static void add_v03_4_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.3.9-RC", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.WARRIOR, 8), ("战士改动：角斗士更新"),
                ("角斗士连携技大改")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DEPTH), ("0层回归"),
                ("萝卜地牢0层回归！")));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "修复了以下Bug:\n\n" +
                        "_来自于 v0.3.9:_\n\n" +
                        "[修复者：JDSALing]：\n"+
                        "_-_ 0.修复 恶魔天赋 重进失效的问题\n" +
                        "_-_ 1.修复角斗士连击技能面板全英文；你现在已有X块暗金矿英文；被魅惑后攻击怪物提示英文的文本丢失\n" +
                        "_-_ 2.修复 火印恶魔火印不可见但仍能触发 的异常\n" +
                        "_-_ 3.二次修复 蜂巢 武器的功能缺失异常\n" +
                        "_-_ 4.修复 恶魔领主 伤害异常问题\n" +
                        "_-_ 5.修复 刻印者 与 火印恶魔的素材异常问题\n" +
                        "_-_ 6.修复部分情况下，每次退回主菜单，鉴定天赋会退回升级" ));
    }

    public static void add_v03_3_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.3.8", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY_LAND), ("界面更新"),
                ("过渡界面已迁移到2.5.0风格")));

        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "测试时间加强",
                "_-_ 追加属性生成器，更方便您的调试"));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "修复了以下Bug:\n\n" +
                        "_来自于 v0.3.8:_\n\n" +
                        "[修复者：JDSALing]：\n"+
                        "_-_ 0.优化萝卜更新记录的界面的显示问题\n" +
                        "_-_ 1.修复各种文本丢失的异常\n" +
                        "_-_ 2.修复 自然之覆 的功能异常\n" +
                        "_-_ 3.战术，法师，盗贼，女猎的T1-T3天赋迁移完成\n" +
                        "_-_ 4.修复 守卫者陷阱 崩溃异常\n" +
                        "_-_ 5.修复穿戴护甲必定崩溃游戏的异常\n" +
                        "_-_ 6.修复全局伤害翻倍异常\n" +
                        "_-_ 7.修复 蜂巢 武器的功能缺失异常\n" +
                        "_-_ 8.修复 战士 部分护甲状态下素材显示错误\n" +
                        "_-_ 9.修复 石像 给予Buff闪退的严重异常\n\n" +
                        "[修复者：Doge]：\n" +
                        "_-_ 10.修复 护盾 伤害失效异常\n" +
                        "_-_ 11.修复 地底亚龙 的各种异常\n" +
                        "_-_ 12.修复 Buff基类 的一些迁移迭代异常"));
    }

    public static void add_v03_2_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.3.6", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.CHANGES), ("重大更新"),
                ("萝卜现已更新底层到破碎V2.4.2版本，迁移者：JDSALing\n\n" +
                        "注意：迁移后可能还含有各种问题，请积极反馈！")));

        changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "杂项生成器",
                "_-_ 同步 魔绫像素地牢的 生成器，让你的测试更加简单。"));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "修复了以下Bug:\n" +
                        "_来自于 v0.3.5-MD3:_\n" +
                        "_-_ 修复石像Buff状态异常问题\n" +
                        "_-_ 部分素材贴图校准\n" +
                        "_-_ 部分效果失效修正"));
    }


    public static void add_v03_1_Changes( ArrayList<ChangeInfo> changeInfos ) {
        ChangeInfo changes = new ChangeInfo("v0.3.5", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo("内部测试-MD3", false, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.RING_EMERALD), "戒指生成",
                "内部测试1版：\n\n" +
                        "部分戒指的生成异常已经修正"));

        changes = new ChangeInfo("内部测试-MD2", false, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(ChangesScene.class, "misc"),
                "1.迁移后的贴图定位错乱已经修正--Thanks(过去的事)\n\n" +
                        "2.部分界面的显示异常已经修正"));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "修复了以下Bug:\n" +
                        "_来自于 v0.3.5-MD1:_\n" +
                        "_-_ 修复DM175脱战护盾异常问题\n" +
                        "_-_ 修复豺狼祭司的一些潜在问题\n" +
                        "_-_ 修复迁移后的导致的部分贴图错乱\n" +
                        "\n" +
                        "_来自于 v0.3.5-MD0:_\n" +
                        "_-_ 修复石像的调查文本异常\n" +
                        "_-_ 修复迁移后的护甲生成异常"));
    }

}
