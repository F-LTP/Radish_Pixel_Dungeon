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

public class RA_v0_7_X_Changes {
    public static void addAllChanges(ArrayList<ChangeInfo> changeInfos) {
        add_v07_4_Changes(changeInfos);
        add_v07_3_Changes(changeInfos);
        add_v07_2_Changes(changeInfos);
        add_v07_1_Changes(changeInfos);
        add_v07_0_Changes(changeInfos);
    }
    public static void add_v07_4_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.7.4", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new TalentIcon(Talent.SCHOOL_FIRE), "骰子法师法术系统实装",
                ("_-_ 实装 骰子法师魔力点、炼金台兑换与法术系统\n" +
                        "_-_ 实装 骰子法师的Slice&Dice风格法术界面与法术击杀特效\n" +
                        "_-_ 修改 骰子法师的背包格子、怪物与物品介绍、二选一窗口为Slice&Dice风格UI\n" +
                        "_-_ 修改 骰子法师的通知消息为Slice&Dice风格单选项界面\n" +
                        "_-_ 修改 骰子法师游戏内文本为Slice&Dice像素化字体\n" +
                        "_-_ 修改 骰子法师的攻击、法术、击杀音效和背景音乐为Slice&Dice风格")));

        changes.addButton(new ChangeButton(new ItemSprite(ARMOR_MOONLIGHT), "月华玩具背包实装",
                ("_-_ 实装 月华玩具背包护甲技能与13种玩具效果\n" +
                        "_-_ 修改 月华玩具\"诗\"为押韵触发最终伤害加成\n" +
                        "_-_ 新增 月华\"诗\"中文押韵表生成脚本与数据文件\n" +
                        "_-_ 补全 月华玩具背包其余玩具的名称和说明文案\n" +
                        "_-_ 完善 玩具背包信息窗口，显示已附着玩具的效果描述")));

        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "职业T4天赋重做",
                ("_-_ 重做 多个职业T4天赋：战士高端饮食、角斗士武器大师、战法老魔杖闪避、术士腐化怨灵、狙击通识射击、守望药水涂镖/草地视野、月华光之永恒/月之辉煌、小骑士盾戳战术/骑士精神、骰子法师法术强效/鸡蛋篮子")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.BACKPACK), "种子查找器改进",
                ("_-_ 现在种子查找器会在后台线程运行，超时20秒后自动停止并提示用户")));

        changes.addButton(new ChangeButton(Icons.get(Icons.TALENT), "小骑士调整",
                ("_-_ 实装 小骑士投掷武器命中附加2+区域层中毒的被动效果")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY), "显示调整",
                ("_-_ 修改 月华盾贴图显示条件为剑盾骑士天赋而非手持盾类武器\n" +
                        "_-_ 修改 钥匙栏仅在上一层有未使用钥匙时显示空钥匙提示")));

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "精铁淬炼与轮椅调整",
                ("_-_ 修改 精铁淬炼返还升级卷轴改为具有等级的十手损坏时触发\n" +
                        "_-_ 轮椅升级所需移动距离调整为300+200*等级\n" +
                        "_-_ 轮椅充能恢复速度调整为每(80-2*等级)回合恢复1点\n" +
                        "_-_ 轮椅跳跃至深渊时会弹出确认对话框")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
        changes.hardlight(CharSprite.NEGATIVE);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复 种子查找器搜索不存在的物品时会导致游戏卡死的bug\n" +
                        "_-_ 修复 多处物品效果计算未使用buffedLvl导致增益效果未正确生效的bug\n" +
                        "_-_ 修复 震爆法杖无法吃到塑形玻璃效果的bug\n" +
                        "_-_ 修复 震爆法杖无法炸到伪装状态宝箱怪的bug\n" +
                        "_-_ 修复 塑形玻璃和国王之戒虚拟等级无法叠加的bug\n" +
                        "_-_ 修复 小骑士踹飞技能无伤害且击退距离仅1格的bug\n" +
                        "_-_ 修复 部分覆写描述的武器不显示剑盾骑士天赋说明的bug\n" +
                        "_-_ 修复 集群骷髅缺少毒素免疫的bug\n" +
                        "_-_ 修复 小骑士濡湿附魔天赋+2和+3效果未生效的bug\n" +
                        "_-_ 修复 背包满时转化十手会掉落原武器且错误返还升级卷轴的bug\n" +
                        "_-_ 修复 骰子法师法术击杀特效与怪物原死亡动画重叠的bug\n" +
                        "_-_ 修复 重复使用轮椅会叠加轮椅狂飙持续时间的bug\n" +
                        "_-_ 部分文案补充\n")));
    }

    public static void add_v07_3_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.7.3", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.BACKPACK), ("种子查找器饰品支持"),
                ("种子查找器现在支持搜索饰品！\n\n" +
                        "_-_ 当找到魔能触媒时，会自动生成4个饰品选项（3个具体饰品+1个随机选项）\n" +
                        "_-_ 饰品选项会在结果中标注（饰品选项）\n" +
                        "_-_ 搜索饰品名称时可以匹配到这些选项\n\n" +
                        "注意：饰品是通过魔能触媒炼金获得的，无法直接在地牢中找到。")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.COMPASS), ("战争迷雾绕路优化"),
                ("当目的地与玩家之间有战争迷雾阻挡时，若直线距离在8格以内，系统会询问玩家是否绕远路前往。\n\n" +
                        "_-_ 选择是：按照绕过迷雾的远路前进\n" +
                        "_-_ 选择否：取消本次移动\n\n" +
                        "这个改动可以避免误操作导致角色绕半个地图的尴尬情况。")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
        changes.hardlight(CharSprite.NEGATIVE);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复 地龙初始状态为被动而非睡眠，导致睡眠动画不显示的bug\n" +
                        "_-_ 修复 神圣泉水转化后获得卷轴但泉水未被消耗，可重复触发的bug\n" +
                        "_-_ 修复 弩类武器（十字弩、蝎子弩）投掷物伤害面板未显示加成来源的提示\n" +
                        "_-_ 修复 猎杀直觉天赋对击杀幽灵等0经验怪物错误计数的bug\n" +
                        "_-_ 修复 荒芜挑战中文描述与实际效果不符的bug，现在描述与英文一致\n" +
                        "_-_ 修复 残魔余卷对护甲不生效的bug，现在护甲生成时也会受残魔余卷影响\n" +
                        "_-_ 修复 十手武器耐久度未存储，小退重进会重置耐久的bug\n" +
                        "_-_ 修复 一把十手天赋只增加耐久上限，不增加当前耐久的bug\n" +
                        "_-_ 修复 十手武器格挡值未正确应用的bug\n" +
                        "_-_ 修复 十手武器投掷时未检查一把十手天赋，导致无天赋时也能触发投掷攻击的bug\n" +
                        "_-_ 部分文案补充\n")));

    }

    public static void add_v07_2_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.7.2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new MoonLightSprite(), ("月华NPC修改"),
                (
                        "_-_ 现在月华NPC和你交换的武器一定是没有等级且被鉴定的，但是仍可能有附魔和刻印。\n" +
                                "_-_ 现在如果你选择了英雄月华，NPC月华不会出现，猫权主义者的猫会来顶班。"
                )));

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.RECTOR, 2), ("牧师修改"),
                (
                        "_-_ 牧师不会再有两个卷轴筒了。\n" +
                                "_-_ 现在牧师开局的时候没有绒布包，商店会售卖绒布包给牧师。"
                )));

        changes.addButton(new ChangeButton(Icons.get(Icons.DATA), ("新星法杖重做"),
                ("_-_ 重做新星法杖，以解决老代码导致的一系列相关bug。 _注意：新星法杖的使用方式现在与之前略微有所不同。_")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DATA), ("追寻附魔调整"),
                ("_-_ 现在追寻附魔的武器造成标记后，攻击者和被攻击者其中一方死亡后，标记会消失。")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
        changes.hardlight(CharSprite.NEGATIVE);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复 苔藓层仍会生成跳楼房的bug。\n" +
                        "_-_ 修复 诅咒的狂怒戒指导致玩家的攻击极其缓慢的bug。\n" +
                        "_-_ 修复 集群骷髅死后爆的骷髅可能会生成在玩家同一格的bug。\n" +
                        "_-_ 修复 前线旗在英雄看到怪物的情况下，怪物也会苏醒的bug。现在怪物必须能够看到英雄才能立即苏醒。\n" +
                        "_-_ 修复 牧师的宽恕裁决在视野内有多个目标的情况下不能正确选择目标的bug。\n" +
                        "_-_ 修复 国王之戒和塑型玻璃没有正确叠加的bug。\n" +
                        "_-_ 修复 回音锤杀死敌人的一击不显示伤害的bug。\n" +
                        "_-_ 修复 恶魔领主穿过敌人攻击玩家的bug。\n" +
                        "_-_ 修复 装备翼剑后，即使卸下了翼剑，仍然会触发它的攻击特效，并且导致游戏闪退的bug。\n" +
                        "_-_ 修复 复合弩的投射物的视觉效果，现在它会直线飞出，而不是像投斧一样旋转。\n" +
                        "_-_ 修复 一些天赋会打印调试信息的bug。\n" +
                        "_-_ 修复 饰品萝卜的全局暴击率，没显示在玩家面板上的bug。\n" +
                        "_-_ 部分文案补充\n")));

    }

    public static void add_v07_1_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.7.1", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton((new DeminionSprite()), "烙印恶魔更新！",
                "现在恶魔层里随处可见的烙印恶魔不再是用1点伤害给你挠痒痒的小鬼了，烙印已经被各种恶魔利用并且增强自己的技能……\n\n" +
                        "_-_ 拥有烙印的玩家会受到死亡射线的额外50%伤害。\n" +
                        "_-_ 拥有烙印的玩家被重复施加烙印的时候，会受到9点真实伤害。\n" +
                        "_-_ 拥有烙印的玩家被蛇发女妖攻击的时候，会被施加双倍的石化效果。\n" +
                        "_-_ 拥有烙印的被魅惑玩家受到魅魔攻击的时候，魅魔会额外吸收10点生命值。\n" +
                        "_-_ 拥有烙印的玩家被蝎子攻击的时候，会受到6回合中毒效果。"));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new ItemSprite(KILL_BOAT), ("斩舰刀修改"),
                ("一刀两段！\n\n" +
                        "_-_ 修复 斩舰刀即使在等待后移动了也能攻击的问题。\n" +
                        "_-_ 为斩舰刀添加了全新的音效和视效。\n" +
                        "_-_ 斩舰刀现在在等待时会额外获得4+等级~8+等级的物理护甲！")));

        changes.addButton(new ChangeButton(new ItemSprite(CALLHAMR), ("回音锤修改"),
                ("教科书般的亵渎！\n\n" +
                        "_-_ 为回音锤添加全新的音效和视效。\n" +
                        "_-_ 现在回音锤的回音如果杀死了不少于一名角色，还会再次触发回音！请注意：一次杀死多只怪物也只会额外触发一次特效。\n" +
                        "_-_ 回音锤多次击杀产生的音效一定是协和音程或者大和弦。\n")));

        changes.addButton(new ChangeButton(new ItemSprite(SKYSPS), ("天象仪修改"),
                ("天生魔力！\n\n" +
                        "_-_ 天象仪的基础伤害现在是3~12点。\n" +
                        "_-_ 现在连击天象仪的连击造成的也是法术伤害。\n")));

        changes.addButton(new ChangeButton(new ItemSprite(WINGSWORD), ("翼剑修改"),
                ("俯冲攻击！\n\n" +
                        "_-_ 翼剑获取了一项新的技能，在玩家拥有漂浮状态时可以消耗漂浮状态，冲向距离为2的一只怪物，发起一次必中攻击并造成额外伤害。\n" +
                        "这次攻击的额外伤害为漂浮时长的一半+武器等级的两倍。")));

        changes.addButton(new ChangeButton(new TalentIcon(Talent.STRONGMAN), "排山倒海修改",
                "排山倒海天赋修改：\n\n" +
                        "_-_ 现在排山倒海天赋对斩舰刀有专属的适配:使用斩舰刀前等待的1回合也会被计算入排山倒海的伤害与精准加成中。\n" +
                        "_-_ 重大修改：在此前的版本中，因计算公式错误，该天赋的数值异常，在低等级时增伤和精准数值偏高，高等级时增伤偏高、精准数值偏低，_现已修复至与描述相同_。\n" +
                        "也许对战士来说这是个大事件……"));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复 天象仪会造成一次0点物理伤害，连击也造成物理伤害的bug。\n" +
                        "_-_ 修复 部分造成真实伤害的怪物的伤害仍显示为普通伤害的bug。\n" +
                        "_-_ 修复 所有武器音效都失效的bug。现在武器会正常播放它们的音效。\n" +
                        "_-_ 修复 斩舰刀等待后即使移动了也能攻击的bug。\n" +
                        "_-_ 修复 排山倒海天赋的攻击和精准加成公式错误的bug（之前真的没人发现吗！！！！）。\n" +
                        "_-_ 修复 所有武器的音效都失效的bug（之前真的没人发现吗！！！！）。\n" +
                        "_-_ 部分文案补充\n")));

    }

    public static void add_v07_0_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.7.0", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton((new MoonLightSprite()), "月华新任务",
                "月华现在常驻于1层，与之对话可以做一个任务。"));

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), ("背水一战优化"),
                ("现在背水一战可以显示被动回复的剩余回合了。")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复 塑形玻璃和祝福卷轴冲突\n" +
                        "_-_ 修复 生命壁垒负数伤害能回血，和天赋判定异常问题\n" +
                        "_-_ 部分文案补充\n" +
                        "_-_ 修复 豺狼双王 没有背水一战的被动回合问题")));

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), ("杂项修改"),
                ("1.0层移除\n" +
                        "2.修复子层含有跳楼房的问题\n" +
                        "3.修复炼金指南中部分含有赘余的合成表")));

    }

}
