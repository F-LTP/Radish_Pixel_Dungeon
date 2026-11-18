package com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.utils;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

abstract public class LegacyItemWeapon extends Weapon {

    {
        enchantment = null;
        cursed = false;
    }

    public String statsInfo(){
        return Messages.get(MeleeWeapon.class, "stats_desc");
    }

    @Override
    public String info() {

        String info = super.info();

        if (levelKnown) {
            info += "\n\n" + Messages.get(MeleeWeapon.class, "stats_knownx", augment.damageFactor(min()), augment.damageFactor(max()), STRReq());
            if (Dungeon.hero != null) {
                if (STRReq() > Dungeon.hero.STR()) {
                    info += " " + Messages.get(Weapon.class, "too_heavy");
                } else if (Dungeon.hero.STR() > STRReq()) {
                    info += " " + Messages.get(Weapon.class, "excess_str", Dungeon.hero.STR() - STRReq());
                }
            }
        } else {
            info += "\n\n" + Messages.get(MeleeWeapon.class, "stats_unknownx", min(0), max(0), STRReq(0));
            if (Dungeon.hero != null && STRReq(0) > Dungeon.hero.STR()) {
                info += " " + Messages.get(MeleeWeapon.class, "probably_too_heavy");
            }
        }

        String statsInfo = statsInfo();
        if (!statsInfo.equals("")) info += "\n\n" + statsInfo;

        switch (augment) {
            case SPEED:
                info += " " + Messages.get(Weapon.class, "faster");
                break;
            case DAMAGE:
                info += " " + Messages.get(Weapon.class, "stronger");
                break;
            case NONE:
        }

        if (enchantment != null && (cursedKnown || !enchantment.curse())){
            info += "\n\n" + Messages.capitalize(Messages.get(Weapon.class, "enchanted", enchantment.name()));
            if (enchantHardened) info += " " + Messages.get(Weapon.class, "enchant_hardened");
            info += " " + enchantment.desc();
        } else if (enchantHardened){
            info += "\n\n" + Messages.get(Weapon.class, "hardened_no_enchant");
        }

        if (cursed && isEquipped( Dungeon.hero )) {
            info += "\n\n" + Messages.get(Weapon.class, "cursed_worn");
        } else if (cursedKnown && cursed) {
            info += "\n\n" + Messages.get(Weapon.class, "cursed");
        } else if (!isIdentified() && cursedKnown){
            if (enchantment != null && enchantment.curse()) {
                info += "\n\n" + Messages.get(Weapon.class, "weak_cursed");
            } else {
                info += "\n\n" + Messages.get(Weapon.class, "not_cursed");
            }
        }

        return info;
    }

}
