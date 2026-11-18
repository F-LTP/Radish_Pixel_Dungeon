package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class RottenLance extends MeleeWeapon{// idea is from relic pd
    private static final String AC_WEAPONSKILL="WS";
    {
        image = ItemSpriteSheet.ROTTEN_LANCE;
        hitSound = Assets.Sounds.HIT_STAB;
        hitSoundPitch=1f;

        tier=2;
        RCH=2;
        defaultAction=AC_WEAPONSKILL;
    }
    @Override
    public int max(int lvl) {
        return  14 +
                lvl*2;
    }
    public boolean isRoot(Hero hero){
        return Dungeon.hero != null && hero.buff(Root.class) != null;
    }

    @Override
    public int damageRoll(Char owner) {
        int damage = super.damageRoll(owner);
        // 蓄根加成：按实际持有者是否处于 Root 状态结算（英雄/怪物统一）
        if (owner != null && owner.buff(Root.class) != null) {
            damage += augment.damageFactor(11 + 2 * buffedLvl());
        }
        return damage;
    }
    @Override public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_WEAPONSKILL);
        return actions;
    }
    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if(action.equals(AC_WEAPONSKILL)) {
            if (!isEquipped(hero)) {
                GLog.w(Messages.get(this, "not_equipped"));
            } else {
                if (!this.cursed) {
                    if (isRoot(hero)) {
                        if (hero.buff(Root.class) != null) {
                            hero.buff(Root.class).detach();
                        }
                        hero.sprite.operate(hero.pos);
                        hero.spendAndNext(Actor.TICK);
                    } else {
                        if ((Dungeon.level.map[hero.pos] != Terrain.GRASS && Dungeon.level.map[hero.pos] != Terrain.HIGH_GRASS && Dungeon.level.map[hero.pos] != Terrain.FURROWED_GRASS) || hero.buff(Levitation.class) != null) {
                            GLog.w(Messages.get(this, "need"));
                            return;
                        }
                        onRoot(hero);
                    }
                    modeSwitch();
                } else {
                    cursedKnown = true;
                    updateQuickslot();
                    GLog.n(Messages.get(this, "cursed"));
                }
            }
        }
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        if(isRoot(hero)){
            GLog.n(Messages.get(this,"root"));
            return false;
        }
        return super.doUnequip(hero, collect, single);
    }

    protected void onRoot(Hero hero){

        if (hero.buff(MagicImmune.class)!=null){
            GLog.i( Messages.get(MeleeWeapon.class, "magic_immune") );
            return;
        }
        hero.sprite.operate( hero.pos );
        Buff.affect(hero,Root.class);
    }

    @Override
    public int defenseFactor(Char owner) {
        if(owner.buff(Root.class)!=null){
            return 1+buffedLvl() ;
        }else {
            return super.defenseFactor(owner);
        }
    }
    public void modeSwitch() {
        if (Dungeon.hero!=null) {
            if (isRoot(Dungeon.hero)) {
                DLY = 1.5f;
                RCH = 3;
            } else {
                DLY = 1f;
                RCH = 2;
            }
        }
    }

    // 扎根形态的攻速和攻击距离要跟着「持有者此刻有没有扎根」走，每次用到时现同步。
    // 只靠 modeSwitch() 不够：它在读档时被调用，可那一刻 Dungeon.hero 还是空的，什么都改不了；
    // 扎根被别的途径解除（比如着火）时也没有人调它
    private void syncMode(Char owner) {
        boolean rooted = owner != null && owner.buff(Root.class) != null;
        DLY = rooted ? 1.5f : 1f;
        RCH = rooted ? 3 : 2;
    }

    @Override
    public float delayFactor(Char owner) {
        syncMode(owner);
        return super.delayFactor(owner);
    }

    @Override
    public int reachFactor(Char owner) {
        syncMode(owner);
        return super.reachFactor(owner);
    }

    @Override
    public String info() {
        // 说明里的「攻速慢／额外攻击距离」特性句读的也是这两个字段
        syncMode(isEquipped(Dungeon.hero) ? Dungeon.hero : null);
        return super.info();
    }
    @Override
    public String statsInfo() {
        if(isIdentified()){
            return  Messages.get(this, "stats_desc",1+buffedLvl());
        }else {
            return  Messages.get(this, "typical_stats_desc",1);
        }

    }
    public static class Root extends Buff{
        {
            announced = true;
        }
        @Override
        public String icon() {
            return BuffIndicator.ARMOR;
        }

        @Override
        public boolean act() {
            // 每回合都要检查。以前这里返回 super.act()，而 Buff.act() 会让 buff 停止行动，
            // 于是只在扎根的那一刻检查一次，之后着火也烧不掉，人被钉在火里
            if (target.buff(Burning.class)!=null){
                detach();
            } else {
                spend(TICK);
            }
            return true;
        }@Override
        public boolean attachTo( Char target ) {
            if (super.attachTo( target )) {
                target.rooted = true;
                if (target.buff(Levitation.class)!=null) Buff.detach(target, Levitation.class);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void detach() {
            target.rooted = false;
            super.detach();
        }
    }
    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        modeSwitch();
    }
}