/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HolySpringUsedBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShaftParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Waterskin;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes.Landmark;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.Game;

public class WaterOfHealth extends WellWater {

	@Override
	protected boolean affectHero( Hero hero ) {

		if (!hero.isAlive()) return false;

		// 神圣泉水天赋：月华英雄可以选择转化
		if (hero.heroClass == HeroClasses.MOONLIGHT) {
			int points = hero.pointsInTalent(Talent.HOLY_SPRING);
			if (points > 0) {
				HolySpringUsedBuff usedBuff = hero.buff(HolySpringUsedBuff.class);
				if (usedBuff == null) {
					usedBuff = Buff.affect(hero, HolySpringUsedBuff.class);
				}

				if (usedBuff.canTransformHealth()) {
					int wellPos = hero.pos;
					Game.runOnRenderThread(() -> GameScene.show(new WndOptions(
							Messages.get(WaterOfHealth.class, "holy_spring_title"),
							Messages.get(WaterOfHealth.class, "holy_spring_desc"),
							Messages.get(WaterOfHealth.class, "holy_spring_normal"),
							Messages.get(WaterOfHealth.class, "holy_spring_transform")
					) {
						@Override
						protected void onSelect(int index) {
							if (index == 0) {
								consume(wellPos);
								normalEffect(hero);
							} else if (index == 1) {
								consume(wellPos);
								transformEffect(hero, points);
							}
						}
					}));
					return false; // 暂时不消耗泉水，等待玩家选择
				}
			}
		}

		// 正常效果
		return normalEffect(hero);
	}

	private boolean normalEffect(Hero hero) {
		Sample.INSTANCE.play( Assets.Sounds.DRINK );

		PotionOfHealing.cure( hero );
		hero.belongings.uncurseEquipped();
		hero.buff( Hunger.class ).satisfy( Hunger.STARVING );

		if (Dungeon.isChallenged(Challenges.DAMAGE_NO)){
			hero.heal(Math.min( 1 , hero.HT ));
			hero.sprite.emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 4 );
		} else {
			hero.heal(hero.HT);
			hero.sprite.emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 4 );
		}

		CellEmitter.get( hero.pos ).start( ShaftParticle.FACTORY, 0.2f, 3 );

		Dungeon.hero.interrupt();

		GLog.p( Messages.get(this, "procced") );

		return true;
	}

	private boolean transformEffect(Hero hero, int points) {
		// 标记已转化
		HolySpringUsedBuff usedBuff = hero.buff(HolySpringUsedBuff.class);
		if (usedBuff != null) {
			usedBuff.markHealthUsed();
		}

		Sample.INSTANCE.play( Assets.Sounds.DRINK );
		hero.sprite.emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 4 );

		// 掉落升级卷轴
		Dungeon.level.drop(new ScrollOfUpgrade(), hero.pos).sprite.drop();

		// +2 时额外掉落驱邪卷轴
		if (points >= 2) {
			Dungeon.level.drop(new ScrollOfRemoveCurse(), hero.pos).sprite.drop();
			Dungeon.level.drop(new ScrollOfRemoveCurse(), hero.pos).sprite.drop();
		}

		GLog.newLine();
		GLog.p(Messages.get(WaterOfHealth.class, "holy_spring_transformed"));

		QuickSlotButton.refresh();
		return true;
	}

	@Override
	protected Item affectItem( Item item, int pos ) {
		if (item instanceof Waterskin && !((Waterskin)item).isFull()) {
			((Waterskin)item).fill();
			CellEmitter.get( pos ).start( Speck.factory( Speck.HEALING ), 0.4f, 4 );
			Sample.INSTANCE.play( Assets.Sounds.DRINK );
			return item;
		} else if ( item instanceof Ankh && !(((Ankh) item).isBlessed())){
			((Ankh) item).bless();
			CellEmitter.get( pos ).start(Speck.factory(Speck.LIGHT), 0.2f, 3);
			Sample.INSTANCE.play( Assets.Sounds.DRINK );
			return item;
		} else if (ScrollOfRemoveCurse.uncursable(item)) {
			if (ScrollOfRemoveCurse.uncurse( null, item )){
				CellEmitter.get( pos ).start( ShadowParticle.UP, 0.05f, 10 );
			}
			Sample.INSTANCE.play( Assets.Sounds.DRINK );
			return item;
		}
		return null;
	}

	@Override
	protected Landmark record() {
		return Landmark.WELL_OF_HEALTH;
	}

	@Override
	public void use( BlobEmitter emitter ) {
		super.use( emitter );
		emitter.start( Speck.factory( Speck.HEALING ), 0.5f, 0 );
	}

	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}
