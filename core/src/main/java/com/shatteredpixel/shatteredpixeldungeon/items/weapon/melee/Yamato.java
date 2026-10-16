package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Yamato extends MeleeWeapon {

	private static final String AC_YAMATO = "YAMATO";
	private static final String COMBO_HITS = "combo_hits";
	private static final int REQUIRED_HITS = 2;

	private int comboHits;
	private boolean usingAbility;

	{
		image = ItemSpriteSheet.YAMATO;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1.2f;
		tier = 5;
	}

	@Override
	public int min(int lvl) {
		return 3 + 2 * lvl;
	}

	@Override
	public int max(int lvl) {
		return 20 + 3 * lvl;
	}

	@Override
	public int STRReq(int lvl) {
		return 18;
	}

	@Override
	public float delayFactor(Char owner) {
		return super.delayFactor(owner) * 0.5f;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		if (!usingAbility && attacker instanceof Hero && isEquipped((Hero) attacker)) {
			comboHits = Math.min(REQUIRED_HITS, comboHits + 1);
			updateQuickslot();
		}
		if (defender.HP == defender.HT && Random.Int(2) == 0) {
			Buff.prolong(defender, Paralysis.class, 3f);
		}
		return super.proc(attacker, defender, damage);
	}

	public void onMiss() {
		if (!usingAbility && comboHits != 0) {
			comboHits = 0;
			updateQuickslot();
		}
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (isEquipped(hero) && comboHits >= REQUIRED_HITS && hero.buff(YamatoCooldown.class) == null) {
			actions.add(AC_YAMATO);
		}
		return actions;
	}

	@Override
	public String actionName(String action, Hero hero) {
		if (AC_YAMATO.equals(action)) {
			return Messages.get(this, hero.justMoved ? "ac_rapid_slash" : "ac_judgement_cut");
		}
		return super.actionName(action, hero);
	}

	@Override
	public void execute(Hero hero, String action) {
		if (!AC_YAMATO.equals(action)) {
			super.execute(hero, action);
			return;
		}
		if (!isEquipped(hero) || comboHits < REQUIRED_HITS || hero.buff(YamatoCooldown.class) != null) {
			return;
		}

		final boolean rapidSlash = hero.justMoved;
		usesTargeting = true;
		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				if (cell == null) return;
				if (rapidSlash) {
					useRapidSlash(hero, cell);
				} else {
					useJudgementCut(hero, cell);
				}
			}

			@Override
			public String prompt() {
				return Messages.get(Yamato.this, rapidSlash ? "prompt_rapid_slash" : "prompt_judgement_cut");
			}
		});
	}

	private void useJudgementCut(Hero hero, int cell) {
		Char target = Actor.findChar(cell);
		if (target == null || target == hero || target.alignment != Char.Alignment.ENEMY
				|| !Dungeon.level.heroFOV[cell]) {
			GLog.w(Messages.get(this, "invalid_target"));
			return;
		}

		hero.busy();
		hero.sprite.attack(cell, new Callback() {
			@Override
			public void call() {
				usingAbility = true;
				hero.belongings.abilityWeapon = Yamato.this;
				boolean hit = hero.attack(target, 1.33f, 0, Char.INFINITE_ACCURACY);
				hero.belongings.abilityWeapon = null;
				usingAbility = false;
				if (hit && !target.isAlive()) onAbilityKill(hero, target);
				finishAbility(hero);
			}
		});
	}

	private void useRapidSlash(Hero hero, int selectedCell) {
		int heroX = hero.pos % Dungeon.level.width();
		int heroY = hero.pos / Dungeon.level.width();
		int targetX = selectedCell % Dungeon.level.width();
		int targetY = selectedCell / Dungeon.level.width();
		int dx = Integer.compare(targetX, heroX);
		int dy = Integer.compare(targetY, heroY);
		if (dx == 0 && dy == 0) {
			GLog.w(Messages.get(this, "invalid_direction"));
			return;
		}

		ArrayList<Char> targets = new ArrayList<>();
		int destination = hero.pos;
		for (int distance = 1; distance <= 4; distance++) {
			int x = heroX + dx * distance;
			int y = heroY + dy * distance;
			if (x < 0 || x >= Dungeon.level.width() || y < 0 || y >= Dungeon.level.height()) {
				destination = hero.pos;
				break;
			}
			int cell = x + y * Dungeon.level.width();
			if (!Dungeon.level.passable[cell] && !Dungeon.level.avoid[cell]) {
				destination = hero.pos;
				break;
			}
			Char ch = Actor.findChar(cell);
			if (ch != null && ch != hero) {
				if (ch.alignment != Char.Alignment.ENEMY || distance == 4) {
					destination = hero.pos;
					break;
				}
				targets.add(ch);
			}
			destination = cell;
		}
		if (destination == hero.pos) {
			GLog.w(Messages.get(this, "blocked"));
			return;
		}

		final int finalDestination = destination;
		hero.busy();
		hero.sprite.jump(hero.pos, finalDestination, 0, 0.15f, new Callback() {
			@Override
			public void call() {
				usingAbility = true;
				hero.belongings.abilityWeapon = Yamato.this;
				for (Char target : targets) attackTwice(hero, target);
				hero.belongings.abilityWeapon = null;
				usingAbility = false;
				hero.move(finalDestination, false);
				Dungeon.observe();
				finishAbility(hero);
			}
		});
	}

	private void attackTwice(Hero hero, Char target) {
		boolean hit = hero.attack(target, 0.5f, 0, Char.INFINITE_ACCURACY);
		if (target.isAlive()) hit |= hero.attack(target, 1.2f, 0, Char.INFINITE_ACCURACY);
		if (hit && !target.isAlive()) onAbilityKill(hero, target);
	}

	private void finishAbility(Hero hero) {
		comboHits = 0;
		Buff.affect(hero, YamatoCooldown.class, 5f);
		Invisibility.dispel();
		Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
		updateQuickslot();
		hero.ready();
	}

	@Override
	public String statsInfo() {
		return Messages.get(this, "stats_desc", comboHits, REQUIRED_HITS);
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(COMBO_HITS, comboHits);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		comboHits = bundle.getInt(COMBO_HITS);
	}

	public static class YamatoCooldown extends FlavourBuff {
	}
}
