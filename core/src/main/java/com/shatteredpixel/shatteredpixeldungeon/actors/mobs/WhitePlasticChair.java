package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Yamato;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WhitePlasticChairSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class WhitePlasticChair extends Mob {

	{
		spriteClass = WhitePlasticChairSprite.class;
		HP = HT = 20;
		defenseSkill = 0;
		EXP = 0;
		alignment = Alignment.ENEMY;
		state = PASSIVE;
		properties.add(Property.IMMOVABLE);
	}

	@Override
	protected boolean act() {
		spend(TICK);
		return true;
	}

	@Override
	public boolean heroShouldInteract() {
		return true;
	}

	@Override
	public boolean interact(Char c) {
		if (c == Dungeon.hero) {
			GameScene.examineObject(this);
			return true;
		}
		return super.interact(c);
	}

	public void kick(Hero hero) {
		if (!Dungeon.level.adjacent(hero.pos, pos)) {
			GLog.w(Messages.get(this, "too_far"));
			return;
		}

		int dropPos = pos;
		PixelScene.shake(3, 0.5f);
		alignment = Alignment.NEUTRAL;
		destroy();
		if (sprite != null) sprite.die();

		Yamato yamato = (Yamato) new Yamato().random();
		yamato.level(yamato.level() + 1);
		Dungeon.level.drop(yamato, dropPos).sprite.drop();
		GLog.p(Messages.get(this, "kicked"));
	}
}
