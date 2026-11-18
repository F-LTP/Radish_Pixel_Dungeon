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

public class RA_v0_4_X_Changes {
    public static void addAllChanges(ArrayList<ChangeInfo> changeInfos) {
        add_v04_2_Changes(changeInfos);
        add_v04_1_Changes(changeInfos);
    }
    public static void add_v04_2_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.4.9-R3->R6", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.HUNTRESS, 8), ("女猎天赋：疾风骤雨"),
                ("疾风骤雨现在可以正常使用了！")));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.WARRIOR, 8), ("战士天赋：精巧纹章"),
                ("修复 精巧纹章不生效 和 天赋2阶强化不生效")));

        changes.addButton(new ChangeButton(new Image(new RatKingSprite()), ("鼠王优化"),
                ("对于有恶魔之力的英雄，鼠王会有新的特殊对话。")));

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARTIFACT_ELTIE7), "精英证章",
                "功能修正：精英证章充能异常和其他小问题"));

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARTIFACT_CONCEAL), "匿踪斗篷",
                "功能修正：匿踪斗篷充能异常修正"));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_ON), ("挑战重制：荒芜之地"),
                ("草本身也不是很能在地牢里长的多好……\n\n-在每区，有50%/60%/70%/80%/90%的草变为枯草，额外生成的草也遵循此规律。\n\n之前的荒芜之地挑战规则全部废弃")));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "_-_V0.4.9-R6\n" +
                        "_-_ 修复英雄精英附魔特效异常问题\n" +
                        "_-_ 修复苦痛刻痕使用祝福的十字架死亡的异常\n" +
                        "_-_ 部分文案优化\n",
                "_-_V0.4.9-R5\n" +
                        "_-_ 修复顽疾诅咒的一些遗漏崩溃问题\n" +
                        "_-_ 修复重击附魔特效带来的无响应(ANR)异常\n" +
                        "_-_ 修复苦痛刻痕无法正常使用的问题\n" +
                        "_-_ 部分文案优化\n",
                "_-_V0.4.9-R4\n" +
                        "_-_ 修复战士1-1天赋，低于50%不生效异常\n" +
                        "_-_ 修复顽疾诅咒带来的各种严重闪退问题，并追加了特殊文本\n" +
                        "_-_ 修复因处理精巧纹章带来的各种底层异常，包括附魔符石&蜕变秘卷部分功能失效，以及导致符文剃刀失效\n" +
                        "_-_ 优化法师4-1恶魔天赋，使之兼容242破碎底层\n" +
                        "_-_ 移除十字弩\n" +
                        "_-_ 部分文案优化\n" +
                        "_-_ 法师天赋：充能强化部分效果异常或不生效修正\n",
                "_-_V0.4.9-R3\n" +
                        "_-_ 修复法术序列实际效果与描述不符\n" +
                        "_-_ 修复藤蔓陷阱天赋失效\n" +
                        "_-_ 修复获得小恶魔的恶魔之力后回去见鼠王并没有特殊互动\n" +
                        "_-_ 修复奇迹树脂描述有问题，但实际效果没问题\n" +
                        "_-_ 修复匿踪斗篷不随使用而升级，修复精英证章无法充能和其他异常\n" +
                        "_-_ 修复仍有一些原版武器在生成池中未被删掉，但保留【十字弩】\n" +
                        "_-_  修复盗贼一层天赋的小干粮 和 矿洞任务文本缺失"));
    }

    public static void add_v04_1_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.4.8->R2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY), ("破碎 & 萝卜 UI"),
                ("现在萝卜地牢默认萝卜UI,如果不习惯可在界面设置中调回破碎经典界面。")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.WARRIOR, 8), ("战士4层恶魔天赋"),
                ("战士恶魔天赋，完全实装，欢迎尝鲜")));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.MAGE, 8), ("法师4层恶魔天赋"),
                ("除'缠怨恶灵'天赋禁用外，其他完全实装，欢迎尝鲜")));

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SEED_CARD), "新初始物品：种子袋",
                "弥补开局关卡运营问题，可以自选一个种子，腐莓种除外，只能使用一次。"));

        changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.STONE_CRAD), "新初始物品：符石袋",
                "弥补开局关卡运营问题，可以自选一个符石，只能使用一次。"));


        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);


        changes.addButton(new ChangeButton(new Image(new GiantWormSprite()), ("巨型蠕虫平衡调整"),
                ("巨型蠕虫的吸血现在固定为1。")));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.WARRIOR, 3), ("战士初始武器强化"),
                ("战士初始武器基准提升至2-10，成长基准为1-2。")));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.MAGE, 4), ("法师初始武器强化"),
                ("法师初始武器基准提升至1-8，成长为1-2。")));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.ROGUE, 5), ("盗贼初始武器强化"),
                ("盗贼初始武器基准提升至1-9，成长为1-2。")));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.HUNTRESS, 6), ("女猎初始武器强化"),
                ("女猎灵能弓箭伤害强化，从1-6提升到2-6基准数值。")));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_ON), ("挑战平衡：精英强敌"),
                ("精英怪生成概率由原先的1/8，调整为1/10")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "1.修复DM-175护盾异常问题\n" +
                        "2.修复部分天赋不生效的问题\n" +
                        "3.修复伤痛难愈部分挑战未生效的问题"));
    }

}
