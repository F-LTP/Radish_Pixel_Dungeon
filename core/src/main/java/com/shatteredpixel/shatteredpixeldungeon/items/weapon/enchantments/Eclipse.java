package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.tweeners.Tweener;

/**
 * 日蚀附魔：装备者不处于房间边缘时，攻击会波及视野内的其他敌人。
 * 范围伤害为本次伤害的 20%，武器等级每级提高 2%。
 */
public class Eclipse extends Weapon.Enchantment {

    private static final ItemSprite.Glowing GOLD = new ItemSprite.Glowing( 0xFFD700 );

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        if (!(attacker instanceof Hero) || damage <= 0) {
            return damage;
        }
        if (Dungeon.level == null || Dungeon.level.heroFOV == null) {
            return damage;
        }
        Hero hero = (Hero) attacker;
        // 只有站在房间内部才触发，贴墙时无效
        if (atRoomEdge( hero.pos )) {
            return damage;
        }

        // 屏幕先变暗再恢复
        if (Camera.main != null) {
            final ColorBlock dark = new ColorBlock( Camera.main.width, Camera.main.height, 0xFF000000 );
            dark.camera = Camera.main;
            dark.alpha( 0f );
            GameScene.effectOverFog( dark );
            if (Game.scene() != null) {
                Game.scene().add( new Tweener( dark, 0.7f ) {
                    @Override
                    protected void updateValues( float progress ) {
                        float a = progress < 0.35f
                                ? progress / 0.35f * 0.55f
                                : (1f - progress) / 0.65f * 0.55f;
                        dark.alpha( Math.max( 0f, a ) );
                    }

                    @Override
                    protected void onComplete() {
                        dark.killAndErase();
                    }
                } );
            }
        }

        int level = Math.max( 0, weapon.buffedLvl() );
        int aoe = Math.max( 1, Math.round( damage * (0.20f + 0.02f * level) ) );

        for (Char ch : Actor.chars()) {
            if (ch == attacker || ch == defender || !ch.isAlive()) {
                continue;
            }
            if (ch.alignment != Char.Alignment.ENEMY) {
                continue;
            }
            if (Dungeon.level.heroFOV[ch.pos]) {
                ch.damage( new DamageInfo( aoe, DamageType.MAGICAL, attacker, weapon, this ) );
            }
        }
        return damage;
    }

    private static boolean atRoomEdge( int pos ) {
        int w = Dungeon.level.width();
        int x = pos % w, y = pos / w;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int nx = x + dx, ny = y + dy;
                if (nx < 0 || ny < 0 || nx >= w || ny >= Dungeon.level.height()) {
                    continue;
                }
                if (Dungeon.level.solid[ny * w + nx]) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return GOLD;
    }
}
