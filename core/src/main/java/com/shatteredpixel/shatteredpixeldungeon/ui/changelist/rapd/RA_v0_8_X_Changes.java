package com.shatteredpixel.shatteredpixeldungeon.ui.changelist.rapd;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.changelist.ChangeInfo;
import com.watabou.noosa.Image;

import java.util.ArrayList;

import static com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet.*;

public class RA_v0_8_X_Changes {
    public static void addAllChanges(ArrayList<ChangeInfo> changeInfos) {
        add_v08_4_Changes(changeInfos);
        add_v08_3_Changes(changeInfos);
        add_v08_2_Changes(changeInfos);
        add_v08_1_Changes(changeInfos);
        add_v08_0_Changes(changeInfos);
    }

    public static void add_v08_4_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.8.4", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new ItemSprite(BONE_PILE),"一些新物品",
                ("_-_ 一些怪物会掉落新物品。\n\n" +
                        "_-_ 哥布林：简易投石索，使用投石索会消耗一颗石头投掷，将它的伤害提升4倍，此道具和石头会一并摧毁。\n\n" +
                        "_-_ 狗：狗腿，可以丢出去吸引怪物。\n\n" +
                        "_-_ 监狱内敌人：律法碎片，可以作为某一些炼金配方中卷轴的替代品，也可以令视野中的犯人和狱警短暂麻痹。\n\n" +
                        "_-_ 灵魂： 灵魂余烬，可以分解为1炼金能量。日后会有其他用途。\n\n" +
                        "_-_ 机械类敌人：机械碎片，一堆废铁能用来做啥啊……\n\n" +
                        "_-_ 矮人和豺狼人：闪晶， 用于+3以下装备，50%概率令其上升一级，50%概率令其下降一级。0级装备会被闪晶摧毁。\n\n"+
                        "_-_ 矮人炮手： 大炮，可以用来发射炸弹出去，并造成多倍的爆炸伤害。\n\n" +
                        "_目前大部分物品的效果不是很让我满意，如果有更好的想法，欢迎从首页加入群聊向开发者投稿。_")));


        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.ROGUE, 5, HeroClasses.ROGUE_SKIN_GAMBLER), "英雄皮肤系统",
                ("英雄角色现在可以换上来自Slice & Dice的新皮肤！\n\n" +
                        "_-_ 盗贼新增可选皮肤：赌徒\n\n" +
                        "_-_ 战士新增可选皮肤：流浪者\n\n" +
                        "_-_ 月华新增可选皮肤：圆球\n\n" +
                        "_-_ 新增全职业共享皮肤：杂散（Jumble）\n\n" +
                        "_-_ 这些皮肤都有独立的特殊效果，选择前请仔细查看英雄选择界面。\n\n" +
                        "_-_ 另有几个新皮肤的实现方式还没有想好，会在后续版本中缓慢添加。"
                        )));

        changes.addButton(new ChangeButton(Icons.get(Icons.DATA), "职业系统重构",
                ("将职业与子职业由枚举重构为定义式框架，为后续新英雄、新子职业与新皮肤提供统一的扩展入口。\n\n" +
                        "_-_ 职业、子职业、天赋层级、护甲技能、初始装备与皮肤统一定义,未来添加新英雄和皮肤会更加简单。\n"
                )));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);
        changes.addButton(new ChangeButton(new ItemSprite(HEADCLEAVER), "斩首者调整", "" +
                "_-_ 斩首者对大于30%血量的英雄生效时，会给英雄保留1点血量。\n\n" +
                "_-_ 小于30%的英雄如果不幸被斩首，仍会直接死亡。\n"));
        changes.addButton(new ChangeButton(new ItemSprite(LOCK_CHAIN), "锁镰兼容性修复",
                ("_-_ 攻击被追寻附魔标记的敌人时，会把不管多远的敌人都拉过来并且攻击。\n\n" +
                        "_-_ 原来不能拉拽的情况，依然不能拉拽，"))
        );
        changes.addButton(new ChangeButton(new ItemSprite(CIRCLE_SWORD), "环刃调整",
                ("_-_ 环刃增加了1攻击距离。\n\n" +
                        "_-_ 环刃现在的成长是4阶武器的成长。\n\n" +
                        "_-_ 环刃的防御转攻击效果会对树肤生效。\n\n" +
                        "_-_ 环刃会令你命中敌人时消耗所有护盾，并将其转化为伤害。\n\n" +
                        "_-_ 环刃会令残像的必定闪避效果变为一次必定命中效果。\n\n"))
        );

        changes.addButton(new ChangeButton(new TalentIcon(Talent.SCHOOL_BLADES), "骰子法师法术调整",
                ("_-_ 刃雨：获得力量加成，投射物改为纯视觉表现，伤害改为同步结算，飞刀会穿过倒下的尸体，以避免实际投射物导致的卡顿bug。在大部分情况下，这不会对这个法术的结算造成严重区别。\n\n" +
                        "_-_ 收集：效果改为将7格内的物品全部拉到自己脚下。这也会回收范围内怪物身上的投掷物。\n\n" +
                        "_-_ 预知：效果改为给予英雄20回合的魔能透视。施法不会消耗回合。\n\n" +
                        "_-_ 藤蔓：现在施法消耗0.33回合。缠绕的回合数会叠加。\n\n" +
                        "_-_ 电闪：它现在类似于电法的注能面，会随着当前法力值提高伤害。\n\n" +
                        "_-_ 恶咒：效果改为给3格内的一个目标7-15回合的虚弱、易伤、幻惑、失明、残废、禁疗，每个效果的时间会独立计算，然而对boss单位的效果是25%。冷却50回合。\n\n" +
                        "_-_ 切割：额外附加33%武器伤害与力量加成，并触发武器附魔。\n\n" +
                        "_-_ 劈砍：效果改为对扇形敌人使用手中武器进行一次高强度打击。该法术享受力量加成。\n\n" +
                        "_-_ 烧焦：效果改为令3x3范围内的敌人的火焰伤害立刻全部燃烧完毕，并再次点燃这些敌人。范围内正在燃烧的地形，会立刻燃烧完毕。\n\n" +
                        "_-_ 爆燃：消耗6魔力点，对一个正在燃烧的敌人造成100-150火焰伤害并将其引爆：目标自身的引燃立刻结束，火焰扩散到周围5×5范围的敌人与地面。不再拥有冷却时间。来自某个异世界火系法师的力量。\n\n"
                )));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),
                Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复 骰子法师学派升级时在部分设备会发生闪退的bug\n\n" +
                        "_-_ 修复 刃雨飞刀无法穿过尸体、伤害结算不稳定的bug\n\n" +
                        "_-_ 修复 黏稠刻印延迟伤害优先级错误的bug（护甲减伤全被计入延迟伤害，现改为护甲先阻挡、再对剩余伤害做延迟）\n\n" +
                        "_-_ 修复 血色哨卫能被打死的bug\n\n"+
                        "_-_ 修复 牧师信仰值是小数而非整数的bug\n\n"+
                        "_-_ 调整 圆球的动画速度加快了1.5倍\n\n"+
                        "_-_ 新增了一些没有被发现的bug")));
    }

    public static void add_v08_3_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.8.3", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new TalentIcon(Talent.SCHOOL_FIRE), "骰子法师学派系统",
                ("骰子法师的法术体系全面重做为学派系统！\n\n" +
                        "_-_ 新增火焰、刀刃、咒法、法力、血液、自然、医疗、物理、紧急、特殊十大学派，每个学派投入点数逐级习得对应法术\n" +
                        "_-_ 新增专属学派升级窗口，升级时按权重抽取两个学派二选一，也可随机提升某学派或跳过\n" +
                        "_-_ 施法界面只显示当前学派等级对应的可用法术\n" +
                        "_-_ 特殊学派每局会随机分配3个特殊法术（光束、标记、充能、热量、闪耀等）\n" +
                        "_-_ 新增魔力点冷却机制与多个学派相关Buff（标记、预言、抚慰回复、星火等）\n" +
                        "_-_ 新增 法术图鉴：日志界面新增\"骰子法师法术\"栏目，列出所有法术的图标、名称、描述与所属学派\n" +
                        "_-_ 学派天赋图标现在会显示\"即将解锁\"法术的贴图")));

        changes.addButton(new ChangeButton(new TalentIcon(Talent.SCHOOL_MANA), "骰子法师魔力系统",
                ("魔力获取、储存与消耗方式全面翻新！\n\n" +
                        "_-_ 每_20_回合自动获得_1_点魔力点；视野内的怪物死亡时获得_1_点魔力点\n" +
                        "_-_ 每_25_回合，超过_3_点以上的魔力会被清空（魔力Buff描述会显示剩余回合）\n" +
                        "_-_ 魔力不足时，可消耗背包内法杖充能：每点充能提供_1+0.33×法杖等级_点魔力，施法时会弹窗询问并列出将被消耗充能的法杖\n" +
                        "_-_ 魔力不足但法杖充能足够时，施法按钮也可点击（不再置灰）\n\n" +
                        "_-_ 新物品：魔力药水，饮用后恢复魔力点。配方为药剂+种子+卷轴，每局随机各选一个\"正确\"要素（不含力量药水与升级卷轴），命中数量决定成品等级：全对=高等(24)、中2=中等(12)、中1=下等(6)、全错=随机合剂或秘药（不含肌肉记忆合剂、龙血秘药）\n" +
                        "_-_ 只有骰子法师能炼制并饮用魔力药水，下等为暗淡低饱和贴图，中等正常，高等附带附魔彩色光环")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.DATA), "伤害系统重构",
                ("统一并重构底层伤害处理管线：\n\n" +
                        "_-_ 新增 DamageInfo 修改器管线与混合伤害类型，以支持未来可能的复杂伤害计算逻辑和来源追踪逻辑；\n\n" +
                        "_-_ 新增统一伤害事件系统（原始伤害、最终伤害、攻击、Buff生效等事件）\n\n" +
                        "_-_ 重写多处涉及伤害的物品、法杖、附魔、符石、陷阱与Boss战逻辑\n\n" +
                        "_-_ 强化伤害显示与浮动文字表现\n")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY), "素材与视觉更新",
                ("_-_ 物品图标迁移与新图集整理，SND音效图集与天赋图标调整\n")));


        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复 拥有遗忘碎片时砥砺锋芒天赋不生效的bug。\n" +
                        "_-_ 修复 替身木桩、豺狼王镜像、大地守护者、守卫雕像等NPC无法受到伤害的bug\n\n" +
                        "_-_ 修复 天狗层二阶段玩家卡墙play的bug\n\n" +
                        "_-_ 修复 猎杀直觉经验计数器未在存档中保存，读档后计数丢失的bug\n\n" +
                        "_-_ 修复 天狗层二阶段玩家卡墙play的bug\n\n" +
                        "_-_ 修复 大量文本位置不太正确的bug\n\n" +
                        "_-_ 修复 战士精巧纹章天赋受击刻印不正常触发的bug（原版本中战斗外刻印仍正常触发）\n\n" +
                        "_-_ 修复 战士破损纹章行为异常的bug\n\n" +
                        "_-_ 修复 豺狼双王不再掉落一把骷髅钥匙\n")));
    }

    public static void add_v08_2_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.8.2", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "_-_ 镜像受到伤害后不死亡的bug\n" +
                        "_-_ 哨卫受到伤害后不死亡的bug\n" +
                        "_-_ 幽灵不受到伤害的bug\n" +
                        "_-_ 拉莱耶文本反弹伤害触发时没有特效且玩家身上跳字的bug\n" +
                        "_-_ 月华NPC开局交换武器可能出顽疾刻印的bug\n" +
                        "_-_ 遗忘碎片存在时砥砺锋芒天赋不生效的bug\n" +
                        "_-_ 重型回旋镖攻击后退出重进只需一回合飞回的bug\n" +
                        "_-_ 牧师惩戒之力施法到造成伤害需20多秒的bug\n" +
                        "_-_ 天狗锁血没有生效的bug\n" +
                        "_-_ 天狗二阶段没有正确切换场景的bug\n" +
                        "_-_ 轮椅0充能时无法用弹射起步开轮椅的bug\n" +
                        "_-_ 战士受衅怒火在破盾后下一次受攻击才错误触发的bug"));
    }


    public static void add_v08_1_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.8.1", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.COMPASS), "战争迷雾寻路改为设置项",
                ("_-_ 战争迷雾寻路改为设置里的可选项，点击后立即前往目标位置。")));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_ON), "破灭之戒调整",
                ("_-_ 破灭之戒的伤害现在会让中立生物转为敌对并开始攻击玩家")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DISPLAY), "交互改进",
                ("_-_ 现在长按凳子可以触发右键效果，从而踢到凳子。")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "bugfixes"), false, null);
        changes.hardlight(CharSprite.NEGATIVE);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
                "_-_ 修复武器掌握对投掷武器生效的bug\n" +
                        "_-_ 修复武器掌握的武器切换时没有清除红色头带的bug\n" +
                        "_-_ 修复吃半个食物消耗3回合的bug\n" +
                        "_-_ 修复节制一餐天赋触发时第一次吃食物没有触发食物特殊效果的bug\n" +
                        "_-_ 修复伤害系统没有正确结算导致能电死NPC的bug\n" +
                        "_-_ 修复神圣泉水转化泉水异常闪退的bug\n" +
                        "_-_ 修复月华浮空贴图错误的bug\n" +
                        "_-_ 修复薪王形态的贴图bug\n" +
                        "_-_ 修复几个天赋的文本bug\n" +
                        "_-_ 修复种子分析无法使用的bug\n" +
                        "_-_ 移除皮甲遗留的事件订阅代码\n" +
                        "_-_ 修复震爆法杖的爆炸源最多只有1个的bug\n" +
                        "_-_ 拉莱耶文本反弹伤害触发时现在会显示诅咒特效，且玩家头上不会跳字\n" +
                        "_-_ 修复苔藓层2区没有生成赐福卷轴房间的bug\n" +
                        "_-_ 修复prison_quest相关异常\n" +
                        "_-_ 修复手术复活的boss异常\n" +
                        "_-_ 修复手机版因不能右键而踢不到凳子的bug\n" +
                        "_-_ 修复月华牧师商店卖的背包异常的bug\n" +
                        "_-_ 修复子层钥匙和主层钥匙通用的bug\n" +
                        "_-_ 修复轮椅狂飙状态死亡会崩溃的bug\n"));
    }

    public static void add_v08_0_Changes(ArrayList<ChangeInfo> changeInfos) {
        ChangeInfo changes = new ChangeInfo("v0.8.0", true, "");
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
        changes.hardlight(Window.TITLE_COLOR);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClasses.MOONLIGHT, 1), "新英雄：月华",
                ("月华作为全新的可操作英雄正式加入地牢！\n\n" +
                        "_-_ 月华初始携带多种英雄武器，切换武器不消耗回合\n" +
                        "_-_ 携带经过改装的轮椅，可以短距跳跃并获得爆发加速\n" +
                        "_-_ 拥有小骑士、骰子法师和十手冠军三条专属分支\n" +
                        "_-_ 加入完整的专属天赋、护甲技能、英雄选择界面和永久解锁流程\n" +
                        "_-_ 选择月华时，1层的月华NPC会由猫权主义者的猫代班\n")));

        changes.addButton(new ChangeButton(new TalentIcon(Talent.LEARN_BLAZE), "骰子法师",
                ("来自Slice & Dice的异世界法术体系已经完整实装！\n\n" +
                        "_-_ 使用法杖积攒魔力点，也可以在炼金台用能量兑换魔力\n" +
                        "_-_ 可以学习并施放切割、愈合、烈焰、重压、瘴气、烈酒、操作和抚慰等法术\n" +
                        "_-_ 拥有专属的暗色骰面法术窗口、击杀特效和信息面板\n" +
                        "_-_ 背包、怪物与物品介绍、选项和消息窗口采用Slice & Dice风格\n" +
                        "_-_ 使用专属像素字体、攻击音效、法术音效、死亡音效和背景音乐\n")));

        changes.addButton(new ChangeButton(new ItemSprite(ARMOR_MOONLIGHT), "玩具背包",
                ("月华的玩具背包护甲技能已经完成。发动能力可以生成玩具并附着到护甲，为冒险构筑截然不同的效果组合。\n\n" +
                        "_-_ 实装诗、伤疤、铁心、箭矢、树肤、斗篷、水银、酊剂等基础玩具\n" +
                        "_-_ 追加30种具有阶级和独特机制的进阶玩具\n" +
                        "_-_ 玩具涵盖伤害、射程、投掷物、治疗、护盾、处决、保命与资源强化\n" +
                        "_-_ 玩具槽位已满时可以选择替换，信息窗口会显示全部已附着效果\n" +
                        "_-_ 玩具“诗”会依据中文名称押韵触发最终伤害加成\n")));

        changes.addButton(new ChangeButton(new ItemSprite(SNAKE_BITED_YENDOR), "蛇咬挑战",
                ("全新的彩蛋挑战：蛇咬！\n\n" +
                        "_-_ 开启挑战后，地牢中的物品和大量视觉表现都会变成蛇\n" +
                        "_-_ 挑战拥有专属交互、结局判定和稀有成就\n" +
                        "_-_ 具体规则可以在挑战界面中查看\n")));

        changes.addButton(new ChangeButton(Icons.get(Icons.CHALLENGE_ON), "全新戒指",
                ("地牢中新增两枚机制独特的戒指，所有戒指也获得了对应的宝石外观。\n\n" +
                        "_-_ 破灭之戒：持续消耗自身生命，对视野内敌人造成基于最大生命值的伤害\n" +
                        "_-_ 挤压之戒：强化击退距离和碰撞伤害\n" +
                        "_-_ 两枚新戒指已经加入正常生成、商店、奖励与嬗变池\n")));

        changes.addButton(new ChangeButton(new ItemSprite(YAMATO), "彩蛋武器：阎魔刀",
                ("“I am the……”\n\n" +
                        "_-_ 新增不会在地牢中随机生成的5阶彩蛋武器阎魔刀\n" +
                        "_-_ 阎魔刀攻击极快，攻击满血敌人时有概率令其麻痹\n" +
                        "_-_ 连续命中两次后可以发动不消耗回合的次元斩或疾走居合\n" +
                        "_-_ 在Yendor护符层寻找白色塑料椅，长按并选择“踹一脚”即可获得\n" +
                        "_-_ 生成的阎魔刀保留正常随机附魔与诅咒，并额外提升1级\n")));

        changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
        changes.hardlight(CharSprite.WARNING);
        changeInfos.add(changes);

        changes.addButton(new ChangeButton(Icons.get(Icons.WARNING), "旧存档不兼容",
                ("v0.8.0包含大规模底层代码和资源结构升级，旧版本存档已不再兼容。\n\n" +
                        "请在更新前完成仍在进行的冒险，并在新版本中创建新的存档。\n")));

        changes.addButton(new ChangeButton(Icons.get(Icons.DATA), "底层系统大规模重构",
                ("这个新版本重置了非常多原版破碎像素地牢的代码不规范和难以扩展的系统，所以这个版本更新如此慢带来的就是今后的超快速内容更新。好日子即将来临！\n\n" +
                        "_-_ 新增统一事件系统，为移动、拾取、使用物品、升级、死亡和切层等行为提供扩展入口\n" +
                        "_-_ 重构伤害类型系统，统一物理、法术、真实和特殊伤害的处理\n" +
                        "_-_ 全面重写支线系统，移除依赖具体楼层的硬编码实现\n" +
                        "_-_ 大幅调整着色器和渲染机制，为Slice & Dice效果及后续特效提供基础\n" +
                        "_-_ 重构物品、天赋和状态贴图管线，原始图片可以通过清单自动打包\n" +
                        "_-_ 缺失物品贴图时会显示对应资源ID，方便开发和定位问题\n")));

        changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), "职业与机制调整",
                ("_-_ 重做多个职业的T4天赋，包括高端饮食、武器大师、老魔杖闪避、腐化怨灵、通识射击、药水涂镖、草地视野、光之永恒、月之辉煌、盾戳战术、骑士精神、法术强效和鸡蛋篮子\n" +
                        "_-_ 新星法杖重写为更稳定、易扩展的使用逻辑\n" +
                        "_-_ 种子查找器支持搜索饰品，并改为后台线程执行，超时后会自动停止\n" +
                        "_-_ 优化近距离战争迷雾绕路，绕远时会先询问玩家\n" +
                        "_-_ 英雄选择界面完成迭代，并加入永久物品与英雄解锁逻辑\n" +
                        "_-_ 精铁淬炼返还升级卷轴改为有等级的十手损坏时触发\n" +
                        "_-_ 钥匙栏仅在上一层仍有未使用钥匙时显示空钥匙提示\n" +
                        "_-_ 部分装备等级计算统一使用实际增益等级\n")));

        changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16),
                Messages.get(ChangesScene.class, "bugfixes"),
                ("_-_ 修复 电脑版调试时间挑战中背包按钮无法全部显示的bug\n" +
                        "_-_ 修复 苔藓层仍会生成跳楼房的bug\n" +
                        "_-_ 修复 追寻附魔标记不会在攻击者或目标死亡后消失的bug\n" +
                        "_-_ 修复 诅咒的狂怒戒指导致攻击速度异常缓慢的bug\n" +
                        "_-_ 修复 集群骷髅生成的骷髅可能与玩家重叠的bug\n" +
                        "_-_ 修复 前线旗会在怪物尚未看见英雄时将其唤醒的bug\n" +
                        "_-_ 修复 宽恕裁决在视野内有多个目标时无法正确选择目标的bug\n" +
                        "_-_ 修复 回音锤击杀敌人的一击不显示伤害的bug\n" +
                        "_-_ 修复 恶魔领主能够穿过其他角色攻击玩家的bug\n" +
                        "_-_ 修复 卸下翼剑后仍会触发攻击特效并可能导致闪退的bug\n" +
                        "_-_ 修复 复合弩投射物飞行动画错误旋转的bug\n" +
                        "_-_ 修复 全局暴击率未显示在英雄属性面板的bug\n" +
                        "_-_ 修复 种子查找器搜索不存在物品时可能卡死的bug\n" +
                        "_-_ 修复 震爆法杖无法获得塑形玻璃增益且无法命中伪装宝箱怪的bug\n" +
                        "_-_ 修复 塑形玻璃与国王之戒的虚拟等级无法叠加的bug\n" +
                        "_-_ 修复 末日守卫公式错误导致护盾量异常增加的bug\n" +
                        "_-_ 修复 背包已满时转化十手会掉落原武器并错误返还升级卷轴的bug\n" +
                        "_-_ 修复 天狗出口层部分贴图显示错误的bug\n" +
                        "_-_ 部分文案补充\n")));
    }

}
