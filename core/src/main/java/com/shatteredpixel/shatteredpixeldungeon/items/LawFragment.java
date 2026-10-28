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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

/**
 * 律法残页 (Law Fragment)
 * 监狱守卫/拷问者/囚犯 15%/20%/30% 掉落。
 * 阅读之后，使视野内的所有狱警（监狱守卫、拷问者、新boss）和犯人（囚犯、天狗、副本boss）麻痹4回合，
 * 并将其沿远离自身的方向击退5格。
 * 可分解为6点炼金能量。
 */
public class LawFragment extends Item {

	public static final String AC_READ = "READ";

	private static final float TIME_TO_READ = 1f;
	private static final float PARALYZE_DURATION = 4f;
	private static final int KNOCKBACK_DIST = 5;

	{
		defaultAction = AC_READ;

		image = ItemSpriteSheet.LAW_FRAGMENT;
		stackable = true;

		bones = true;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_READ);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_READ)) {
			curUser = hero;
			curItem = this;
			doRead();
		}
	}

	public void doRead() {
		detach(curUser.belongings.backpack);

		curUser.spend(TIME_TO_READ);
		curUser.busy();
		((HeroSprite)curUser.sprite).read();

		Sample.INSTANCE.play(Assets.Sounds.READ);
		curUser.sprite.emitter().start(Speck.factory(Speck.NOTE), 0.1f, 10);
		Buff.affect(curUser, Light.class, 10f);
		if (curUser.sprite != null) new Flare(6, 32).color(0xFFFFFF, true).show(curUser.sprite, 2f);

		// 使视野内的所有狱警与犯人麻痹并将其击退5格
		int paralyzed = 0;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
			if (mob.isAlive()
					&& Dungeon.level.heroFOV[mob.pos]
					&& isGuardOrPrisoner(mob)) {
				Buff.affect(mob, Paralysis.class, PARALYZE_DURATION);
				paralyzed++;

				knockBackAway(mob);
			}
		}
	}

	protected void knockBackAway(Mob mob) {
		if (mob.rooted || mob.properties().contains(Char.Property.IMMOVABLE)) {
			return;
		}
		// 从英雄指向敌人的方向
		Ballistica heroToMob = new Ballistica(curUser.pos, mob.pos, Ballistica.PROJECTILE);
		int mobIndex = heroToMob.path.indexOf(mob.pos);
		// 敌人须位于轨迹上，且轨迹尽头在该方向还有延伸空间
		if (mobIndex < 0 || mobIndex >= heroToMob.path.size() - 1) {
			return;
		}
		int targetPos = heroToMob.path.get(heroToMob.path.size() - 1);
		Ballistica knockbackPath = new Ballistica(mob.pos, targetPos, Ballistica.MAGIC_BOLT);
		WandOfBlastWave.throwChar(mob, knockbackPath, KNOCKBACK_DIST, true, true, this);
	}

	protected boolean isGuardOrPrisoner(Mob mob) {
		return mob.properties().contains(Char.Property.JAIL_INHABITANT);
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 30 * quantity;
	}

	@Override
	public int energyVal() {
		return 6 * quantity;
	}

	@Override
	public String name() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc");
	}
}
