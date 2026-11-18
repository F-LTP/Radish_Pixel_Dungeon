package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
import static com.shatteredpixel.shatteredpixeldungeon.items.Item.curItem;
import static com.shatteredpixel.shatteredpixeldungeon.items.Item.curUser;
import static com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot;
import static com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfWealth.genMidValueConsumable;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AnkhInvulnerability;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionHero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.VitaeBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.DeathAnimations;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.WondrousResin;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.YetWand.HolyFire;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.YetWand.HolyLand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.YetWand.WandOfCorret;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EndGuard;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Perfidy;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Belief extends Buff implements ActionIndicator.Action {

    public float credibility;
    public boolean not_link = false;

    {
        type = buffType.POSITIVE;
        revivePersists = true;
    }

    public enum SkillList {
        CORRECT, LIGHTIMUEE, CLEAN, PRAYERS,
        BATTLE, BLESS, HOLYFIRE, HOLYLAND, DEADKILL,
        BACK, ENDDEAD;
        public String desc(){
            switch (this){
                case CORRECT:
                case LIGHTIMUEE:
                case CLEAN:
                case PRAYERS:
                case BLESS:
                case BATTLE:
                case HOLYFIRE:
                case HOLYLAND:
                case DEADKILL:
                case BACK:
                case ENDDEAD:
                    return Messages.get(this, name()+"desc");
                default:
                    return Messages.get(this, desc());
            }
        }
        public String title(){
            return Messages.get(this, name() + ".name");
        }
    }

    public void useSkills(Belief.SkillList skillList){
        switch (skillList){
            case CORRECT:
                if(hero.pointsInTalent(Talent.IRON_SUN)>=2){
                    Buff.affect(hero, Barrier.class).setShield( hero.lvl );
                }
                curUser = hero;
                curItem = new WandOfCorret();
                GameScene.selectCell( zapper );
                int altFixedDamage = 12 + Dungeon.depth;
                int altFixedDamagePlus;

                if(hero.subClass == HeroSubClasses.BATTLEPREIST){
                    ArrayList<Mob> visibleTargets = new ArrayList<>();

                    for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])){
                        // 说明写的是「视野内的随机目标」：要看英雄此刻看不看得见（heroFOV），
                        // 不是这一格有没有探索过（visited）——后者会隔着墙劈到地图另一头的怪。
                        // 友方阵营（被腐化、魅惑过来的怪）不在候选里。
                        if(Dungeon.level.heroFOV[mob.pos] && mob.alignment != Char.Alignment.ALLY
                                && !(mob instanceof NPC || mob instanceof Mimic && mob.alignment == Char.Alignment.NEUTRAL) && !(mob.properties().contains(Char.Property.IMMOVABLE ))){
                            visibleTargets.add(mob);
                        } else {
                            visibleTargets.remove(mob);
                        }
                    }
                    if(!visibleTargets.isEmpty()){
                        Mob mob = visibleTargets.get(Random.Int(visibleTargets.size()));
                        // 这里添加对目标的具体操作逻辑
                        altFixedDamagePlus = (int) (altFixedDamage * 1.5f);
                        hero.sprite.parent.add(new Beam.LightRay(hero.sprite.center(), DungeonTilemap.raisedTileCenterToWorld( mob.pos )));
                        if (mob.properties().contains(Char.Property.DEMONIC) || mob.properties().contains(Char.Property.UNDEAD)) {
                            altFixedDamage = (int) (altFixedDamage * 1.5f);
                        }
                        if (hero.pointsInTalent(Talent.ACT_GODPROGRESS) >= 1
                                && hero.buff(Talent.NoBeliefUsedCooldown.class) == null
                                && credibility<5) {
                            float cooldown;
                            switch (hero.pointsInTalent(Talent.ACT_GODPROGRESS)){
                                default:
                                case 1:
                                    cooldown = 500f;
                                    break;
                                case 2:
                                    cooldown = 425f;
                                    break;
                                case 3:
                                    cooldown = 350f;
                                    break;
                            }
                            Buff.affect(hero, Talent.NoBeliefUsedCooldown.class, cooldown);
                            if(hero.pointsInTalent(Talent.IRON_SUN)>=2){
                                Buff.affect(hero, Barrier.class).setShield( hero.lvl );
                            }
                        } else {
                            DownBelief(5);
                        }

                        float x = mob.sprite.center().x;
                        float y = mob.sprite.center().y;
                        mob.sprite.parent.add(new Lightning(mob.sprite.center(), new PointF( x, y-300f),null));
                        mob.sprite.parent.add(new Lightning(new PointF(x-5f, y), new PointF( x-5f, y-300f),null));
                        mob.sprite.parent.add(new Lightning(new PointF(x+5f, y), new PointF( x+5f, y-300f),null));
                        Sample.INSTANCE.play( Assets.Sounds.LIGHTNING, 1.5f);
                        mob.damage(DamageInfo.of(altFixedDamage + altFixedDamagePlus, DamageType.HOLY, null, this));
                        mob.sprite.centerEmitter().burst( SparkParticle.FACTORY, 32 );
                        mob.sprite.flash();
                    } else {
                        GLog.n(Messages.get(Belief.class,"no_target"));
                    }
                }
                break;
            case LIGHTIMUEE:
                if(hero.pointsInTalent(Talent.IRON_SUN)>=2){
                    Buff.affect(hero, Barrier.class).setShield( hero.lvl );
                }
                if (hero.hasTalent(Talent.NOHOPE_LANG) && Dungeon.hero.HP < Dungeon.hero.HT/4){
                    int originStamina = 10 + Dungeon.depth/5 - 1;
                    int originVitae = RectorSkills.LIGHTIMUEE.baseVitae(false) + 8;

                    int originVitaePlus = RectorSkills.LIGHTIMUEE.baseVitae(true) + 8;
                    int adrenaline =  Dungeon.depth/5 * 4 - 1 ;

                    if(hero.subClass == HeroSubClasses.BATTLEPREIST){
                        Buff.affect(hero, VitaeBuff.class).addVitae((int) (originVitaePlus * 1.5f));
                        Buff.affect(target, Adrenaline.class,adrenaline);
                    } else {
                        Buff.affect(hero, VitaeBuff.class).addVitae((int) (originVitae * 1.5f));
                    }

                    Buff.affect(hero, Stamina.class,originStamina  * 1.5f);
                } else {
                    if(hero.subClass == HeroSubClasses.BATTLEPREIST){
                        Buff.affect(hero, VitaeBuff.class).addVitae(RectorSkills.LIGHTIMUEE.baseVitae(true));
                        Buff.affect(target, Adrenaline.class,Dungeon.depth/5 * 4-1);
                    } else {
                        Buff.affect(hero, VitaeBuff.class).addVitae(RectorSkills.LIGHTIMUEE.baseVitae(false));
                    }
                    Buff.affect(hero, Stamina.class, Dungeon.depth+10-1);
                }
                GLog.p(Messages.get(Belief.class, "lightimuee_success"));
                break;
            case CLEAN:
                if(hero.pointsInTalent(Talent.IRON_SUN)>=2){
                    Buff.affect(hero, Barrier.class).setShield( hero.lvl );
                }
                curUser = hero;
                // 绝望祷言：+1 只强化惩戒和光能灌注，净化和祷告的强化是 +2 的内容。
                // 这里和下面的祷告原来都只判断「有没有点」，点 1 级就全拿到了，第 2 级等于白点。
                if (hero.pointsInTalent(Talent.NOHOPE_LANG) >= 2 && Dungeon.hero.HP < Dungeon.hero.HT/4){
                    ArrayList<Item> items = hero.belongings.getAllItems(Item.class);
                    for (Item w : items.toArray(new Item[0])){
                       w.cursed = false;
                    }
                    Buff.prolong( curUser, Light.class, 200f);
                    Buff.prolong( curUser, Bless.class, 40f);
                } else {
                    Scroll s = new ScrollOfRemoveCurse();
                    s.anonymize();
                    curItem = s;
                    s.doRead();
                    Buff.prolong( curUser, Light.class, 100f);
                    Buff.prolong( curUser, Bless.class, 20f);
                }

                if (hero.hasTalent(Talent.DEVOTIONAL)){
                    Buff.affect(hero, Barrier.class).setShield(4 * hero.pointsInTalent(Talent.DEVOTIONAL));
                    hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(4 * hero.pointsInTalent(Talent.DEVOTIONAL)), FloatingText.SHIELDING );
                }
                break;
            case PRAYERS:
                if(hero.pointsInTalent(Talent.IRON_SUN)>=2){
                    Buff.affect(hero, Barrier.class).setShield( hero.lvl );
                }
                if (hero.pointsInTalent(Talent.NOHOPE_LANG) >= 2 && Dungeon.hero.HP < Dungeon.hero.HT/4){
                    grantPrayer();
                }
                grantPrayer();
                switch (hero.pointsInTalent(Talent.LIGHT_WASH)){
                    case 1:
                        hero.HP = Math.min(hero.HP+3, hero.HT);
                        hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 3);
                    break;
                    case 2:
                        hero.HP = Math.min(hero.HP+6, hero.HT);
                        hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 6);
                        break;
                    case 3:
                        hero.HP = Math.min(hero.HP+9, hero.HT);
                        hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 9);
                        break;
                }
                break;
            case BATTLE:
                if(hero.pointsInTalent(Talent.IRON_SUN)>=2){
                    Buff.affect(hero, Barrier.class).setShield( hero.lvl );
                }
                ChampionHero.getElite(hero,6, 60f);
                break;
            case HOLYFIRE:
                curUser = hero;
                curItem = new HolyFire();
                GameScene.selectCell(Item.thrower);
                break;
            case HOLYLAND:
                curUser = hero;
                curItem = new HolyLand();
                GameScene.selectCell(Item.thrower);
                break;
            case DEADKILL:
                GameScene.selectCell(deadkillTargeter);
                break;
            case BACK:
                curUser = hero;
                curItem = new Perfidy();
                GameScene.selectCell(Perfidy.throwerx);
                break;
            case ENDDEAD:
                int count = 0;
                int expCost = 0;

                for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
                    if (count >= 5) break;
                    if (mob.alignment == Char.Alignment.ENEMY && !(mob.properties().contains(Char.Property.BOSS) || mob.properties().contains(Char.Property.MINIBOSS)) && Dungeon.level.heroFOV[mob.pos]) {
                        CellEmitter.center(mob.pos).start(ShadowParticle.UP, 0.05f, 10);
                        mob.die(true);
                        expCost += mob.EXP;
                        count++;
                    }
                }

                if (expCost > 0) {
                   hero.exp -= Math.min(hero.exp, expCost);
                }
                new Flare( 5, 32 ).color( 0xFF0000, true ).show( hero.sprite, 2f );
                Sample.INSTANCE.play( Assets.Sounds.READ );
                GLog.w(Messages.get(this,"enddead_success",count));
                break;
            default:
                break;
        }
    }

    // 虔诚祷告的一次随机结果。绝望祷言会让祷告触发两次，所以单独成一个方法。
    private void grantPrayer() {
        switch (Random.Int(4)){
            //财富蓝色掉落
            case 0:
                Item i = genMidValueConsumable();
                Dungeon.level.drop(i,target.pos);
                new Flare( 6, 32 ).color(0x00AAFF, true).show( hero.sprite, 2f );
                GLog.p(Messages.get(Belief.class, "prayers_success",i.name()));
                break;
            case 1:
                // 先算好这次给多少，飘字、发放、日志用同一个数：
                // 给完经验可能当场升级，那之后 maxExp() 已经是下一级的了
                int expGain = hero.maxExp()/2;
                hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(expGain), FloatingText.EXPERIENCE);
                hero.earnExp( expGain, getClass() );
                new Flare( 6, 32 ).color(0xFFFF00, true).show( hero.sprite, 2f );
                GLog.p(Messages.get(Belief.class, "experience_success",expGain));
                break;
            case 2:
                ArrayList<Item> belongingsItems = hero.belongings.getAllItems(Item.class);
                for (Item w : belongingsItems.toArray(new Item[0])){
                    ScrollOfRemoveCurse.uncurse( hero, w );
                    Sample.INSTANCE.play( Assets.Sounds.RAY );
                }
                new Flare( 6, 32 ).color(0x111111, true).show( hero.sprite, 2f );
                GLog.p(Messages.get(Belief.class, "curse_remove_success"));
                break;
            case 3:
                // 说明是「回复 1/3 的最大血量，并获得与恢复血量数同等回合的祝福」
                int healed = hero.heal(hero.HT / 3);
                if (healed > 0) {
                    Buff.prolong(hero, Bless.class, healed);
                }
                new Flare( 6, 32 ).color(0xFF1493, true).show( hero.sprite, 2f );
                GLog.p(Messages.get(Belief.class, "heal_success"));
                break;
        }
    }

    @Override
    public boolean act() {
        FaithObstruction failed = Dungeon.hero.buff(FaithObstruction.class);
        // 动作按钮只有一个槽位。信仰之力是常驻按钮，每回合都会来这里刷新一次；
        // 别的状态的按钮（比如次元撕裂者只开 3 回合的裂隙窗口）正占着槽位时让给它，
        // 否则每回合都把人家顶掉。对方用完会清掉自己的按钮，下一回合这里再补回来。
        // 同理，清按钮时只清自己的。
        // 代行神权冷却期间按钮照常显示：信仰够就正常付费释放，不够时窗口里提示「还在冷却中」（用户定 D35-B）。
        if (credibility >= 0f){
            if (!slotHeldByOtherBuff()) {
                ActionIndicator.setAction(this);
            }
        } else {
            ActionIndicator.clearAction(this);
        }
        not_link = failed != null;
        spend(TICK);
        return true;
    }

    // 槽位里是不是「这个英雄身上另一个还挂着的状态」的按钮。
    // 只给这种让位：上一局遗留的、或者已经解除却没清掉的按钮不算，照常接管，
    // 免得信仰之力的按钮被一个已经不存在的东西永久挡住。
    private boolean slotHeldByOtherBuff() {
        ActionIndicator.Action current = ActionIndicator.action;
        return current != null && current != this
                && current instanceof Buff && target.buff(((Buff) current).getClass()) == current;
    }

    /**
     * 增加信仰值
     * @param value 信仰值
     */
    public void getBelief(float value) {
        boolean[] cursedItemsProcessed = new boolean[5];
        // 保留两位小数。除数必须是浮点：两个整数相除会把小数整个丢掉，
        // 0.33（灵活信仰）变成 0，1.8（深层的击杀）变成 1
        float roundedValue = Math.round(value * 100) / 100f;
        credibility += roundedValue;

        int cursedItems = 0;
        if (hero.belongings.weapon != null && !cursedItemsProcessed[0]) {
            if (hero.belongings.weapon.cursed) {
                cursedItems++;
                cursedItemsProcessed[0] = true; // 标记武器已经被处理
            }
        }
        if (hero.belongings.armor != null && !cursedItemsProcessed[1]) {
            if (hero.belongings.armor.cursed) {
                cursedItems++;
                cursedItemsProcessed[1] = true; // 标记盔甲已经被处理
            }
        }
        if (hero.belongings.ring != null && !cursedItemsProcessed[2]) {
            if (hero.belongings.ring.cursed) {
                cursedItems++;
                cursedItemsProcessed[2] = true; // 标记戒指已经被处理
            }
        }
        if (hero.belongings.artifact != null && !cursedItemsProcessed[3]) {
            if (hero.belongings.artifact.cursed) {
                cursedItems++;
                cursedItemsProcessed[3] = true; // 标记神器已经被处理
            }
        }
        if (hero.belongings.misc != null && !cursedItemsProcessed[4]) {
            if (hero.belongings.misc.cursed) {
                cursedItems++;
                cursedItemsProcessed[4] = true; // 标记辅助物品已经被处理
            }
        }

        float beliefExpert = 0f;
        switch (hero.pointsInTalent(Talent.DEAD_POWER)) {
            case 1:
                beliefExpert = 0.16f;
                break;
            case 2:
                beliefExpert = 0.32f;
                break;
            case 3:
                beliefExpert = 0.50f;
                break;
        }
        float exp = 0f;
        if (hero.hasTalent(Talent.DEAD_POWER)) {
            // 殉道之力是「获得的信仰值增加 16% / 32% / 50%」，加成要乘上这次的获得量。
            // 不乘的话每次固定多给 0.16 / 0.32 / 0.5：获得量为 0 的时候（越级的怪、
            // 星界沟通没攒够经验的那几次）也照给，获得 15 点的时候又只多给零点几。
            exp = roundedValue * cursedItems * beliefExpert;
            credibility += exp;
        }

        // 这次什么都没得到（越级的怪、星界沟通没攒够经验的那几次）就不飘字，
        // 否则每打死一只怪头顶都多飘一个「0」
        float gained = roundedValue + exp;
        if (gained > 0) {
            // 飘字也带上小数，否则获得 0.33 时头顶飘的是「0」
            hero.sprite.showStatusWithIcon(Window.TITLE_COLOR,
                    Messages.decimalFormat("#.##", gained), FloatingText.BELIEF);
        }
    }


    /**
     * 减少信仰值
     * @param value 信仰值
     */
    public void DownBelief(float value) {
        float actualValue = (float) (Math.floor(value * 100) / 100);
        credibility = Math.max(0, credibility - actualValue);
        hero.sprite.showStatus(Window.RADISH, "-" + (int) Math.floor(actualValue));

        // Rector armor skill : taichi poise
        // DoggingDog 20260116
        if(hero != null){
            Buff buff = hero.buff(Soulstaker.class);
            if (buff != null && !hero.hasTalent(Talent.TAI_CHI_POISE)){
                buff.detach();
            }
            else if(buff != null){
                if(value > hero.pointsInTalent(Talent.TAI_CHI_POISE) * 5f){
                    buff.detach();
                }
            }
        }

        // rector armor skill : gods possession
        // 20260119 by DoggingDog
        if(hero != null){
            Buff buff = hero.buff(GodsPossessionBuff.class);
            if (buff != null && hero.hasTalent(Talent.GODHOOD)){
                Buff.prolong(hero, AnkhInvulnerability.class, hero.pointsInTalent(Talent.GODHOOD) + 1);
            }
        }

    }


    @Override
    public String iconTextDisplay() {
        return Integer.toString(displayedCredibility());
    }

    private int displayedCredibility() {
        return (int) Math.floor(credibility);
    }

    public static String CREDIBILITY = "credibility";
    public static String NOT_LINK = "not_link";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(CREDIBILITY, credibility);
        bundle.put(NOT_LINK, not_link);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        credibility = bundle.getFloat(CREDIBILITY);
        not_link    = bundle.getBoolean(NOT_LINK);

        // 和 act() 用同一个条件：读档后立刻就有按钮，不用等下一回合。
        // 不再用无参的 clearAction()，那会把别的状态刚恢复的按钮一起清掉。
        if (credibility >= 0f){
            ActionIndicator.setAction(this);
        }
    }

    @Override
    public String desc(){
        if(hero.pointsInTalent(Talent.IRON_SUN)>=1){
            int buffCnt = 0;
            for(Object i: hero.buffs(Buff.class).toArray()) {
                if(!BuffIndicator.NONE.equals(((Buff) i).icon())){
                    buffCnt+=3;
                }
            }
            return Messages.get(this, "desc2", displayedCredibility(), buffCnt);
        }
        return Messages.get(this, "desc", displayedCredibility());
    }

    @Override
    public String icon() {
        return not_link ? BuffIndicator.NONE : BuffIndicator.BELIEF_LINK;
    }

    @Override
    public String actionName() {
        return Messages.get(this, "action_name");
    }

    @Override
    public String actionIcon() {
        return HeroIcon.BLESS;
    }

    @Override
    public int indicatorColor() {
        if(credibility>=20){
            return Window.TITLE_COLOR;
        } else if(credibility>=15){
            return Window.SHPX_COLOR;
        } else if(credibility>=12){
            return 0x99ccbb;
        } else {
            return 0x55AAFF;
        }
    }

    @Override
    public void doAction() {
        if(not_link){
            GLog.w(Messages.get(this, "not_link_error"));
        } else {
            GameScene.show(new WndBless(this));
        }

    }


    protected static CellSelector.Listener zapper = new  CellSelector.Listener() {

        @Override
        public void onSelect( Integer target ) {

            if (target != null) {

                //FIXME this safety check shouldn't be necessary
                //it would be better to eliminate the curItem static variable.
                final Wand curWand;
                if (curItem instanceof Wand) {
                    curWand = (Wand) curItem;
                } else {
                    return;
                }

                final Ballistica shot = new Ballistica( curUser.pos, target, curWand.collisionProperties(target));
                int cell = shot.collisionPos;

                if (target == curUser.pos || cell == curUser.pos) {
                    if (target == curUser.pos && curUser.hasTalent(Talent.SHIELD_BATTERY)){
                        float shield = curUser.HT * (0.04f*curWand.curCharges);
                        if (curUser.pointsInTalent(Talent.SHIELD_BATTERY) == 2) shield *= 1.5f;

                        if(hero.belongings.weapon() instanceof EndGuard) {
                            EndGuard w2 = (EndGuard) hero.belongings.weapon;
                            if (w2 != null) {
                                Buff.affect(curUser, Barrier.class).setShield((int) (Math.round(shield) + (0.2f * ( w2.level() +1 ))));
                            }
                        } else {
                            Buff.affect(curUser, Barrier.class).setShield(Math.round(shield));
                        }

                        curWand.curCharges = 0;
                        curUser.sprite.showStatusWithIcon(
                                com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite.POSITIVE,
                                Integer.toString(Math.round(shield)),
                                com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText.SHIELDING);
                        curUser.sprite.operate(curUser.pos);
                        Sample.INSTANCE.play(Assets.Sounds.CHARGEUP);
                        ScrollOfRecharging.charge(curUser);
                        updateQuickslot();
                        curUser.spendAndNext(Actor.TICK);
                        return;
                    }
                    GLog.i( Messages.get(Wand.class, "self_target") );
                    return;
                }

                curUser.sprite.zap(cell);

                //attempts to target the cell aimed at if something is there, otherwise targets the collision pos.
                if (Actor.findChar(target) != null)
                    QuickSlotButton.target(Actor.findChar(target));
                else
                    QuickSlotButton.target(Actor.findChar(cell));

                if (curWand.tryToZap(curUser, target)) {

                    curUser.busy();

                    if (curWand.cursed){
                        if (!curWand.cursedKnown){
                            GLog.n(Messages.get(Wand.class, "curse_discover", curWand.name()));
                        }
                        CursedWand.cursedZap(curWand,
                                curUser,
                                new Ballistica(curUser.pos, target, Ballistica.MAGIC_BOLT),
                                new Callback() {
                                    @Override
                                    public void call() {
                                        curWand.wandUsed();
                                    }
                                });
                    } else {
                        curWand.fx(shot, new Callback() {
                            public void call() {
                                curWand.onZap(shot);
                                if (Random.Float() < WondrousResin.extraCurseEffectChance()){
                                    WondrousResin.forcePositive = true;
                                    CursedWand.cursedZap(curWand,
                                            curUser,
                                            new Ballistica(curUser.pos, target, Ballistica.MAGIC_BOLT), new Callback() {
                                                @Override
                                                public void call() {
                                                    WondrousResin.forcePositive = false;
                                                    curWand.wandUsed();
                                                }
                                            });
                                } else {
                                    curWand.wandUsed();
                                }
                            }
                        });
                    }
                    curWand.cursedKnown = true;

                }

            }
        }

        @Override
        public String prompt() {
            return Messages.get(Wand.class, "prompt");
        }
    };

    protected static CellSelector.Listener deadkillTargeter = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer target) {
            if (target == null) return;

            Char ch = Actor.findChar(target);
            if (ch == null) {
                GLog.w(Messages.get(Belief.class, "deadkill_nomobs"));
                return;
            }

            if (ch == hero || ch.alignment != Char.Alignment.ENEMY) {
                GLog.w(Messages.get(Belief.class, "deadkill_nomobs"));
                return;
            }

            // 检查是否在攻击范围内
            boolean inRange = false;
            if (hero.belongings.weapon != null && hero.belongings.weapon.canReach(hero, ch.pos)) {
                inRange = true;
            } else if (Dungeon.level.distance(hero.pos, ch.pos) <= 1) {
                inRange = true;
            }

            if (!inRange) {
                GLog.w(Messages.get(Belief.class, "deadkill_noattack"));
                return;
            }

            // 检查是否符合击杀条件：HP < 60% 且非Boss/MiniBoss
            if (ch.HP >= ch.HT * 0.6f) {
                GLog.w(Messages.get(Belief.class, "deadkill_noattack"));
                return;
            }

            if (ch.properties().contains(Char.Property.BOSS) || ch.properties().contains(Char.Property.MINIBOSS)) {
                GLog.w(Messages.get(Belief.class, "deadkill_noattack"));
                return;
            }

            ch.die(new DeathAnimations.HolyDeath());
            GLog.n(Messages.get(Belief.class, "deadkill_success", ch.name()));

            // 扣减信仰值（如果不在冷却中且有足够信仰）
            Belief belief = hero.buff(Belief.class);
            if (belief != null) {
                if (hero.buff(Talent.NoBeliefUsedCooldown.class) == null && belief.credibility >= 4) {
                    belief.DownBelief(4);
                }
            }
        }

        @Override
        public String prompt() {
            return Messages.get(Belief.class, "deadkill_prompt");
        }
    };
}
