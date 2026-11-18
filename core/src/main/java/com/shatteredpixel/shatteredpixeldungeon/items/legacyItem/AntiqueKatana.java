package com.shatteredpixel.shatteredpixeldungeon.items.legacyItem;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.CursingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

// 古武士刀
// DoggingDog on 20250417
public class AntiqueKatana extends MeleeWeapon {

    {
        image = ItemSpriteSheet.KATANA;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;
        tier = 1;
    }

    @Override
    public int min(int lvl) {
        return 1+lvl;
    }

    @Override
    public int max(int lvl) {
        return 8+lvl*2;
    }

    @Override
    protected void onThrow( int cell ) {
        super.onThrow(cell);
        Trap trapOnTrow = Dungeon.level.traps.get(cell);
        if(trapOnTrow instanceof CursingTrap && Dungeon.level.heaps.get(cell) != null){
            Dungeon.level.heaps.get(cell).remove(this);
        }
    }

    @Override
    public Weapon enchant(Enchantment ench ) {
        if (Dungeon.hero != null){
            if(cursed){
                this.detach(Dungeon.hero.belongings.backpack);
//                Dungeon.hero.belongings.backpack.items.add((Item) new Muramasa().identify());
                new Muramasa().identify().collect();
            }
            else {
                this.detach(Dungeon.hero.belongings.backpack);
//                Dungeon.hero.belongings.backpack.items.add((Item) new Masamune().identify());
                new Masamune().enchant(Weapon.Enchantment.randomCurse()).collect();
            }
        }
        return super.enchant(ench);
    }



}
