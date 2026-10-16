package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wraith;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.HitCheckEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.CircleSword;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class AfterImage extends Armor{
    public int dodges=0;
    {
        image = ItemSpriteSheet.ARMOR_AFTERIMAGE;
    }

    public AfterImage() {
        super( 5 );
    }
    @Override
    protected ArmorBuff buff( ) {
        return new AfterImage.Blur();
    }
    @Override

    public int DRMax(int lvl){
        return 8 + 5 * lvl + augment.defenseFactor(lvl);
    }
    @Override
    public int DRMin(int lvl){
        return 4;
    }
    private static final String DODGES       = "dodges";

    @Override
    public void storeInBundle( Bundle bundle ) {
        bundle.put(DODGES,dodges);
        super.storeInBundle(bundle);
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        if (bundle.contains(DODGES))
            dodges = bundle.getInt(DODGES);
        else
            dodges =0;
    }
    public class Blur extends ArmorBuff {
        @Override
        public boolean act(){
            if (dodges>=3){
                dodges-=3;
                Buff.affect(target,absoluteEvasion.class);
            }
            return super.act();
        }

        public void gainDodge(){
            dodges++;
        }
    }
    public static class absoluteEvasion extends Buff{

        @Override
        public String icon() {
            return BuffIndicator.A_EVA;
        }

		@Override
		public String desc() {
			if (!(this instanceof AnotabsoluteEvasion)
					&& target != null
					&& target.attackingWeapon() instanceof CircleSword) {
				return Messages.get(this, "circle_desc");
			}
			return super.desc();
		}
    }

    public static class AnotabsoluteEvasion extends absoluteEvasion {

        @Override
        public String icon() {
            return BuffIndicator.A_EVA;
        }

        @Override
        public void tintIcon(Image icon) {
            icon.hardlight(0xFF1493);
        }

    }

    /**
     * 被近战命中时，装备者获得一次影分身计数。
     * 由 {@code Char.attack} 的命中事件驱动，装备者可能是玩家，也可能是持有装备的怪物。
     */
    @SubscribeEvent(event = AttackEvent.class, priority = 0)
    public static void onHitGainDodge(AttackEvent event) {
        Char defender = event.getDefender();
        if (defender == null) return;
        Blur blur = defender.buff(Blur.class);
        if (blur != null) {
            blur.gainDodge();
        }
    }

    /** 攻击方：身上带绝对闪避且手持轮刃时，将其转为本次必中。 */
    @SubscribeEvent(event = HitCheckEvent.class, priority = 1000)
    public static void onAttackerForceHit(HitCheckEvent event) {
        if (event.isMagic() || event.getResult() != HitCheckEvent.Result.NONE) return;
        Char attacker = event.getAttacker();
        if (attacker == null) return;
        absoluteEvasion evasion = attacker.buff(absoluteEvasion.class);
        if (evasion != null
                && attacker.buff(AnotabsoluteEvasion.class) == null
                && event.getAttackerWeapon() instanceof CircleSword) {
            evasion.detach();
            event.forceHit();
        }
    }

    /** 防御方：亡魂持有绝对闪避时必定闪避，并消耗该效果。 */
    @SubscribeEvent(event = HitCheckEvent.class, priority = 820)
    public static void onWraithForceMiss(HitCheckEvent event) {
        if (event.getResult() != HitCheckEvent.Result.NONE) return;
        Char defender = event.getDefender();
        if (defender == null) return;
        if (defender instanceof Wraith && defender.buff(absoluteEvasion.class) != null) {
            Buff.detach(defender, absoluteEvasion.class);
            event.forceMiss();
        }
    }

    /** 防御方：绝对闪避提供一次必闪并消耗（持轮刃时不消耗）。 */
    @SubscribeEvent(event = HitCheckEvent.class, priority = 810)
    public static void onDefenderForceMiss(HitCheckEvent event) {
        if (event.getResult() != HitCheckEvent.Result.NONE) return;
        Char defender = event.getDefender();
        if (defender == null) return;
        if (defender.buff(absoluteEvasion.class) != null && !defender.wieldsCircleSword()) {
            Buff.detach(defender, absoluteEvasion.class);
            event.forceMiss();
        }
    }

    /** 防御方：粉色影分身必定闪避并消耗。 */
    @SubscribeEvent(event = HitCheckEvent.class, priority = 700)
    public static void onDefenderForceMissPink(HitCheckEvent event) {
        if (event.getResult() != HitCheckEvent.Result.NONE) return;
        Char defender = event.getDefender();
        if (defender == null) return;
        if (defender.buff(AnotabsoluteEvasion.class) != null) {
            Buff.detach(defender, AnotabsoluteEvasion.class);
            event.forceMiss();
        }
    }

}
