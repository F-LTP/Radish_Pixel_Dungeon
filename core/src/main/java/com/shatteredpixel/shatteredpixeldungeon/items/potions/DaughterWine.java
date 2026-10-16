/*
 * Radish Pixel Dungeon
 * 女儿红陈酿
 */

package com.shatteredpixel.shatteredpixeldungeon.items.potions;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AdrenalineSurge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

/**
 * 女儿红陈酿：东方神秘酒饮。
 * 饮用：恢复少量生命，并在 1000 回合内提升力量。
 * 投掷进火区：触发一次伤害更高的爆炸。
 */
public class DaughterWine extends Potion {

	private static final int HEAL = 20;

	{
		image = ItemSpriteSheet.DAUGHTER_WINE;
	}

	@Override
	public boolean isKnown() {
		return true;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public void apply( Hero hero ) {
		identify();

		hero.HP = Math.min( hero.HT, hero.HP + HEAL );
		hero.sprite.showStatus( CharSprite.POSITIVE, "+" + HEAL );
		Buff.affect( hero, AdrenalineSurge.class ).reset( 2, 1000f );

		GLog.p( Messages.get(this, "drink") );
	}

	@Override
	public void shatter( int cell ) {
		splash( cell );
		if (Dungeon.level.heroFOV[cell]) {
			identify();
			Sample.INSTANCE.play( Assets.Sounds.SHATTER );
		}

		Fire fire = (Fire) Dungeon.level.blobs.get( Fire.class );
		boolean burning = false;
		for (int i : PathFinder.NEIGHBOURS9) {
			int c = cell + i;
			if (Dungeon.level.insideMap(c) && fire != null && fire.volume > 0 && fire.cur[c] > 0) {
				burning = true;
				break;
			}
		}

		if (!burning) {
			return;
		}

		// 投掷进火区：更高伤害的爆炸（150%）
		Sample.INSTANCE.play( Assets.Sounds.BLAST );
		if (Dungeon.level.heroFOV[cell]) {
			CellEmitter.center(cell).burst( BlastParticle.FACTORY, 30 );
		}

		for (int i : PathFinder.NEIGHBOURS9) {
			int c = cell + i;
			if (!Dungeon.level.insideMap(c) || Dungeon.level.solid[c]) {
				continue;
			}
			GameScene.add( Blob.seed( c, 3, Fire.class ) );

			Char ch = Actor.findChar( c );
			if (ch != null && ch.isAlive()) {
				int dmg = Math.round( Char.combatRoll( 5 + Dungeon.scalingDepth(),
						10 + Dungeon.scalingDepth() * 2 ) * 1.5f );
				if (ch.pos != cell) {
					dmg = Math.round( dmg * 0.67f );
				}
				ch.damage( new DamageInfo( dmg, DamageType.FIRE, curUser, this, this ) );
				if (ch.isAlive()) {
					Buff.affect( ch, Burning.class ).reignite( ch );
				}
			}
		}
	}

	@Override
	public int value() {
		return isKnown() ? 30 * quantity : super.value();
	}
}
