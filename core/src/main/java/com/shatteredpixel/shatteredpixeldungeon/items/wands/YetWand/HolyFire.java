package com.shatteredpixel.shatteredpixeldungeon.items.wands.YetWand;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.HalomethaneFire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector.Belief;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class HolyFire extends Item {

    {
        image = ItemSpriteSheet.SPIRIT_ALT_ARROW;

        stackable = true;

        defaultAction = AC_THROW;
    }

    @Override
    protected void onThrow(int cell) {
        Dungeon.level.pressCell(cell);
        shatter(cell);
    }

    public void shatter(int cell) {

        if (Dungeon.level.heroFOV[cell]) {
            identify();

            Sample.INSTANCE.play( Assets.Sounds.SHATTER );
            Sample.INSTANCE.play( Assets.Sounds.BURNING );
        }

        for (int offset : PathFinder.NEIGHBOURS9){
            if (!Dungeon.level.solid[cell+offset]) {
                GameScene.add(Blob.seed(cell + offset, 2, HalomethaneFire.class));
            }
        }
        Belief creaditSkills = hero.buff(Belief.class);
        if(creaditSkills != null) creaditSkills.DownBelief(10);
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    public int value() {
        return 0;
    }
}
