package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.KillBoatSwordWaitBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.effects.SwordSlashEffect;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class KillBoatSword extends MeleeWeapon {

    {
        image = ItemSpriteSheet.KILL_BOAT;
        hitSound = Assets.Sounds.KILL_BOAT_SWORD_SLASH;
        hitSoundPitch = 0.6f;
        tier = 5;
    }
    public boolean delayAttack = false;

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        // CC0 斩击特效：命中时顺着攻击方向在目标身上播放
        SwordSlashEffect.showOn(attacker, defender);
        return super.proc(attacker, defender, damage);
    }

    @Override
    public int min(int lvl) {
        return 10 + lvl * 2;
    }
    @Override
    public int max(int lvl) {
        return 60 + lvl * 10;
    }

    /**
     * 斩舰刀特殊攻击逻辑：
     * 只有拥有 KillBoatSwordWaitBuff 时才能攻击
     * 如果没有 buff，则等待一回合并播放充能特效
     */
    @Override
    public boolean actAttack(Hero attacker, Char defender, HeroAction.Attack action) {
        if (attacker.buff(KillBoatSwordWaitBuff.class) == null) {
            // 没有 buff，等待一回合
            attacker.rest(false);
            attacker.sprite.operate(attacker.pos);
            attacker.interrupt();
            return false;
        }
        return true;
    }

}
