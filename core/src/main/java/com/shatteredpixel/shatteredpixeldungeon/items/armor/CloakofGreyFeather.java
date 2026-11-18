package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackMissEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class CloakofGreyFeather extends Armor{
    {
        image = ItemSpriteSheet.ARMOR_GREYFEATHER;
    }

    public CloakofGreyFeather() {
        super( 4 );
    }
    @Override
    protected ArmorBuff buff( ) {
        return new CloakofGreyFeather.hexDodge();
    }
    @Override
    public int DRMax(int lvl){
        return 10 + 4 * lvl + augment.defenseFactor(lvl);
    }
    @Override
    public int DRMin(int lvl){
        return 0;
    }
    public class hexDodge extends ArmorBuff {
    }

    /** 未命中时，若防御者装备灰羽斗篷，则向周围敌人扩散诅咒（由攻击未命中事件驱动）。 */
    @SubscribeEvent(event = AttackMissEvent.class, priority = 0)
    public static void onAttackMiss(AttackMissEvent event) {
        Char defender = event.getDefender();
        if (defender == null) return;
        CloakofGreyFeather.hexDodge hex = defender.buff(CloakofGreyFeather.hexDodge.class);
        if (hex == null) return;
        //怪物也会穿护甲（「智能怪物」挑战里随机配装、奇械师改装给友军），而怪物的视野要等它自己行动过一次才有，
        //刚生成、刚读档、刚进层时是空的。这时候被打空了没有视野可查，就不扩散；
        //不判断的话这里会抛空指针，虽然会被事件分发接住，但每次都往日志里打一条异常栈
        boolean[] fov = defender.fieldOfView;
        if (fov == null) return;
        for (Char ch : Actor.chars()) {
            if (ch.alignment != defender.alignment && ch.alignment != Char.Alignment.NEUTRAL
                    && ch.pos >= 0 && ch.pos < fov.length && fov[ch.pos]) {
                Buff.affect(ch, Hex.class, 2f + 0.75f * hex.buffedLvl());
            }
        }
    }
}
