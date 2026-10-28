package com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Perfidy extends MissileWeapon {
    {
        image = ItemSpriteSheet.SPIRIT_ALT_ARROW;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.1f;

        bones = false;

        tier = 1;
        baseUses = 1;
        sticky = false;
    }

    @Override
    public void doThrow( Hero hero ) {
        GameScene.selectCell(throwerx);
    }

    @Override
    protected void onThrow(int cell) {

    }

    public static CellSelector.Listener throwerx = new CellSelector.Listener() {
        @Override
        public void onSelect( Integer target ) {
            if (target != null) {
                curItem.castOnlyEnemy( curUser, target );
            }
        }
        @Override
        public String prompt() {
            return Messages.get(Item.class, "prompt");
        }
    };

    @Override
    public int damageRoll(Char owner) {
        return 0;
    }

    @Override
    public int value() {
        return 0;
    }

    @Override
    public int min() {
        return 0;
    }
    @Override
    public int max() {
        return 0;
    }
}
