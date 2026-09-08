package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Statue;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Rlyeh extends MeleeWeapon {

    public float chance;

    public boolean chance(){
        return Random.NormalFloat(0,1)<=chance;
    }

    public boolean HeroChance(){
        return Random.NormalFloat(0,1)<=chance/2f;
    }

    {
        image = ItemSpriteSheet.RLYEH_BOOK;
        tier = 2;
    }

    @Override
    public boolean doEquip( Hero hero ) {
        Buff.affect(hero,StateProject.class);
        return super.doEquip(hero);
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        Buff.detach(hero,StateProject.class);
        return super.doUnequip(hero, collect, single);
    }


    @Override
    public int STRReq(int lvl) {
        return (9 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
    }

    @Override
    public int min(int lvl) {
        return 2 + lvl * 2;
    }
    @Override
    public int max(int lvl) {
        return 12 + lvl * 2;
    }

    @Override
    public String desc() {

        String desc;

        if(isIdentified()){
        			desc = Messages.get(this, "desc",Math.min(15+3*buffedLvl(),100));
        		} else {
        			desc = Messages.get(this, "normal_desc");
        		}

        return desc;
    }

    /**
     * 拉莱耶反弹：玩家方（英雄本体或其雕像）持有 Rlyeh 时，近战攻击可能被反弹给攻击者。
     * 订阅 {@link AttackEvent}，命中后、伤害结算前触发。触发成功则取消本次攻击，并对攻击者施加反弹伤害。
     */
    @SubscribeEvent(event = AttackEvent.class, priority = 0)
    public static void onAttack(AttackEvent event) {
        Hero player = Dungeon.hero;
        if (player == null) return;
        if (Dungeon.level == null) return;

        Char attacker = event.getAttacker();
        Char defender = event.getDefender();
        if (attacker == null || defender == null) return;

        // 只有玩家参与的攻击（玩家为攻方或守方）才可能触发反弹
        if (attacker != player && defender != player) return;

        // 玩家被攻击时用 HeroChance，玩家攻击时用 chance（保持原 behavior）
        boolean playerAttacks = (attacker == player);

        Rlyeh rlyeh = findPlayerRlyeh();
        if (rlyeh == null) return;

        boolean triggered = playerAttacks ? rlyeh.chance() : rlyeh.HeroChance();
        if (!triggered) return;

        // 反弹：攻击者显示诅咒特效 + 受到伤害
        if (attacker.sprite != null) {
            attacker.sprite.emitter().burst(ShadowParticle.CURSE, 10);
        }
        Sample.INSTANCE.play(Assets.Sounds.CURSED);
        attacker.damage(DamageInfo.physical(rlyeh.damageRoll(defender), attacker, rlyeh));

        // 取消本次攻击：原伤害不再结算
        event.cancel();
    }

    /** 查找玩家方持有的 Rlyeh：优先英雄本体武器，否则任一雕像武器。 */
    private static Rlyeh findPlayerRlyeh() {
        if (hero.belongings.weapon() instanceof Rlyeh) {
            return (Rlyeh) hero.belongings.weapon();
        }
        if (Dungeon.level.mobs != null) {
            for (Mob mob : Dungeon.level.mobs) {
                if (mob instanceof Statue && ((Statue) mob).weapon instanceof Rlyeh) {
                    return (Rlyeh) ((Statue) mob).weapon;
                }
            }
        }
        return null;
    }


    public static class StateProject extends Buff {

        {
            type=buffType.POSITIVE;
        }

        @Override
        public boolean act() {
            Rlyeh w2 = null;
            if(hero.belongings.weapon instanceof Rlyeh)
                w2 = (Rlyeh) hero.belongings.weapon;
            if(w2 != null){
            			w2.chance = 0.15f + 0.03f * w2.buffedLvl();
            		}
            spend( TICK );
            return true;
        }
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        chance = bundle.getFloat("chance");
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("chance", chance);
    }

}

