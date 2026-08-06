package com.shatteredpixel.shatteredpixeldungeon.items.legacyItem;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.utils.LegacyItemArmor;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

// 荒原
// Doggingdog on 20250517
public class Wastelandew extends LegacyItemArmor {
    {
        image = ItemSpriteSheet.WASTELANDEW;
    }
    public Wastelandew() {
        super(1);
    }
    @Override
    public int DRMax(int lvl){
        return 8 + Math.max(lvl + augment.defenseFactor(lvl), lvl);
    }

    @Override
    public int DRMin(int lvl){
        return 2 + lvl;
    }

    @Override
    public float evasionFactor(Char owner, float evasion ){
        return super.evasionFactor(owner,evasion) + buffedLvl();
    }

    @Override
    public float speedFactor( Char owner, float speed ){
        if(Dungeon.level.map[owner.pos] == Terrain.GRASS && Random.Int(0,100) < 50+buffedLvl()){
            Dungeon.level.drop(new Dewdrop(), owner.pos).sprite.drop();
            Level.set(owner.pos, Terrain.EMPTY);
            GameScene.updateMap( owner.pos );
            CellEmitter.get( owner.pos ).burst( Speck.factory( Speck.BUBBLE ), 10 );
        }
        return super.speedFactor(owner,speed);
    }
}
