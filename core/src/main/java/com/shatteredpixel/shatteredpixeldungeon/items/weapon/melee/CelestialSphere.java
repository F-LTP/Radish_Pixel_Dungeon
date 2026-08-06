package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;

public class CelestialSphere extends MeleeWeapon {

    {
        image = ItemSpriteSheet.SKYSPS;
        tier = 4;

        // See Weapon.java ReachFactor()
        RCH = 1;
    }

    @Override
    public int STRReq(int lvl) {
        return (7 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
    }

    @Override
    	public int min(int lvl) {
    		int wandTotalLevel = 0;
    		if(Dungeon.hero != null) {
    			ArrayList<Wand> wands = hero.belongings.getAllItems(Wand.class);
    			for (Wand w : wands.toArray(new Wand[0])) {
    				wandTotalLevel += w.buffedLvl();
    			}
    		}

    		return 3 + lvl + wandTotalLevel;
    	}
    @Override
    	public int max(int lvl) {
    		int wandTotalLevel = 0;
    		if(Dungeon.hero != null) {
    			ArrayList<Wand> wands = hero.belongings.getAllItems(Wand.class);
    			for (Wand w : wands.toArray(new Wand[0])) {
    				wandTotalLevel += w.buffedLvl();
    			}
    		}

    		return 12 + lvl * 3 + wandTotalLevel * 2;
    	}
}
