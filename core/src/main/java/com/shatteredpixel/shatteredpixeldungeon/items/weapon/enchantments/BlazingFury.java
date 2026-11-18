package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

/**
 * 炽炎附魔：命中后在目标身后划出一条 1 格宽的火径，点燃路径上的敌人。
 * 火径使用会自行熄灭的火团，不会永久留下余烬。
 */
public class BlazingFury extends Weapon.Enchantment {

    private static final ItemSprite.Glowing ORANGE = new ItemSprite.Glowing( 0xFF6600 );

    private static final int PATH_LENGTH = 3;

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        int w = Dungeon.level.width();
        int ax = attacker.pos % w, ay = attacker.pos / w;
        int dx = defender.pos % w, dy = defender.pos / w;

        int stepX = Integer.signum( dx - ax );
        int stepY = Integer.signum( dy - ay );
        if (Math.abs( dx - ax ) >= Math.abs( dy - ay )) {
            stepY = 0;
        } else {
            stepX = 0;
        }
        if (stepX == 0 && stepY == 0) {
            return damage;
        }

        int cell = defender.pos + stepY * w + stepX;
        for (int i = 0; i < PATH_LENGTH; i++) {
            if (cell < 0 || cell >= Dungeon.level.length()) {
                break;
            }
            if (Dungeon.level.solid[cell]) {
                break;
            }
            if (!Dungeon.level.water[cell]) {
                //必须交给 GameScene.add：Blob.seed 只把火放进关卡数据，本层此前没有过火焰时，
                //新建的火既没进回合系统也没有贴图，看不见、不烧、也不熄灭，要等读档才突然烧起来
                GameScene.add( Blob.seed( cell, 1, Fire.class ) );
                CellEmitter.get( cell ).burst( Speck.factory( Speck.LIGHT ), 2 );
            }

            Char ch = Actor.findChar( cell );
            if (ch != null && ch != attacker) {
                Buff.affect( ch, Burning.class ).reignite( ch );
            }

            cell += stepY * w + stepX;
        }
        return damage;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return ORANGE;
    }
}
