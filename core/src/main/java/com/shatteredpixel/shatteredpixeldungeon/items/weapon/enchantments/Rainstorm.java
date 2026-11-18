package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

/**
 * 暴雨附魔：命中时有概率在目标周围降下暴雨，形成水地形并造成少量水属性伤害。
 */
public class Rainstorm extends Weapon.Enchantment {

    private static final ItemSprite.Glowing BLUE = new ItemSprite.Glowing( 0x3399FF );

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        float chance = 0.25f * procChanceMultiplier( attacker );
        if (Random.Float() >= chance) {
            return damage;
        }

        boolean flooded = false;
        for (int offset : PathFinder.NEIGHBOURS9) {
            int cell = defender.pos + offset;
            if (cell < 0 || cell >= Dungeon.level.length()) {
                continue;
            }
            if (!Dungeon.level.passable[cell] || Dungeon.level.solid[cell]) {
                continue;
            }
            if (Dungeon.level.map[cell] == Terrain.WATER) {
                continue;
            }
            if (Dungeon.level.plants.get( cell ) != null) {
                continue;
            }
            int terrain = Dungeon.level.map[cell];
            if (terrain != Terrain.EMPTY && terrain != Terrain.EMPTY_SP && terrain != Terrain.EMPTY_DECO
                    && terrain != Terrain.GRASS && terrain != Terrain.EMBERS
                    && terrain != Terrain.FURROWED_GRASS) {
                continue;
            }
            Level.set( cell, Terrain.WATER );
            Dungeon.level.water[cell] = true;
            Dungeon.level.flamable[cell] = false;
            GameScene.updateMap( cell );
            flooded = true;
        }

        if (!flooded) {
            return damage;
        }

        int splash = Math.max( 1, Math.round( damage * 0.15f ) );
        for (int offset : PathFinder.NEIGHBOURS9) {
            int cell = defender.pos + offset;
            if (cell < 0 || cell >= Dungeon.level.length()) {
                continue;
            }
            Char ch = Actor.findChar( cell );
            if (ch != null && ch != attacker && ch.isAlive()
                    && ch.alignment == Char.Alignment.ENEMY) {
                ch.damage( new DamageInfo( splash, DamageType.WATER, attacker, weapon, this ) );
            }
        }
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return BLUE;
    }
}
