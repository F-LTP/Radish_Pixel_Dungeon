package com.shatteredpixel.shatteredpixeldungeon.ui.changelist.rapd;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
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
        add_v03_3_Changes(changeInfos);
        add_v03_2_Changes(changeInfos);
        add_v03_1_Changes(changeInfos);
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
                        "_-_ 7.修复 蜂巢 武器的功能缺失异常\n\n" +
                        "[修复者：Doge]：\n" +
                        "_-_ 8.修复 护盾 伤害失效异常\n" +
                        "_-_ 9.修复 地底亚龙 的各种异常\n" +
                        "_-_ 10.修复 效果基类 的一些异常"));
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
