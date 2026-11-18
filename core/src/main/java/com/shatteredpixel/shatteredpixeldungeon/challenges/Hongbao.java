package com.shatteredpixel.shatteredpixeldungeon.challenges;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.BlobImmunity;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.RedEnvelope;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfInvisibility;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.DamageWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room;
import com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/** 红包挑战的生成、拾取与奖励逻辑。 */
public class Hongbao {

	public static final int SPAWN_INTERVAL = 15;

	public static boolean active() {
		return Dungeon.isChallenged(Challenges.HONGBAO);
	}

	public static void spawn() {
		if (!active() || Dungeon.level == null) {
			return;
		}
		for (int tries = 0; tries < 300; tries++) {
			if (placeEnvelope(Random.Int(Dungeon.level.length()))) {
				return;
			}
		}
	}

	/** 在玩家附近生成一个红包。 */
	public static void spawnNearHero() {
		if (!active() || Dungeon.level == null || Dungeon.hero == null) {
			return;
		}
		int width = Dungeon.level.width();
		int heroX = Dungeon.hero.pos % width;
		int heroY = Dungeon.hero.pos / width;
		for (int tries = 0; tries < 500; tries++) {
			int x = heroX + Random.IntRange(-4, 4);
			int y = heroY + Random.IntRange(-4, 4);
			if (x < 0 || y < 0 || x >= width || y >= Dungeon.level.height()) {
				continue;
			}
			int cell = x + y * width;
			int dist = Dungeon.level.distance(Dungeon.hero.pos, cell);
			if (dist < 1 || dist > 4) {
				continue;
			}
			if (placeEnvelope(cell)) {
				return;
			}
		}
		// 找不到随机近点时，改为从玩家周围一圈圈向外找最近的空位
		spawnNearestHero();
	}

	private static void spawnNearestHero() {
		if (!active() || Dungeon.level == null || Dungeon.hero == null) {
			return;
		}
		int width = Dungeon.level.width();
		int heroX = Dungeon.hero.pos % width;
		int heroY = Dungeon.hero.pos / width;
		for (int radius = 1; radius <= 12; radius++) {
			for (int dy = -radius; dy <= radius; dy++) {
				for (int dx = -radius; dx <= radius; dx++) {
					if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) {
						continue;
					}
					int x = heroX + dx;
					int y = heroY + dy;
					if (x < 0 || y < 0 || x >= width || y >= Dungeon.level.height()) {
						continue;
					}
					if (placeEnvelope(x + y * width)) {
						return;
					}
				}
			}
		}
		spawn();
	}

	private static boolean placeEnvelope(int cell) {
		if (!Dungeon.level.passable[cell] || Dungeon.level.solid[cell]) {
			return false;
		}
		if (Actor.findChar(cell) != null) {
			return false;
		}
		// 不上锁、不需要条件进入的房间才生成红包
		if (Dungeon.level instanceof RegularLevel) {
			Room room = ((RegularLevel) Dungeon.level).room(cell);
			if (room instanceof SpecialRoom) {
				return false;
			}
		}
		Heap heap = Dungeon.level.heaps.get(cell);
		if (heap != null && !heap.isEmpty()) {
			return false;
		}
		Dungeon.level.drop(new RedEnvelope(), cell);
		return true;
	}

	/** BOSS 死亡时在尸体周围掉落 5 个红包。 */
	public static void dropBossEnvelopes(int center) {
		if (!active() || Dungeon.level == null) {
			return;
		}
		int width = Dungeon.level.width();
		int cx = center % width;
		int cy = center / width;
		for (int n = 0; n < 5; n++) {
			boolean placed = placeEnvelope(center);
			if (!placed) {
				for (int radius = 1; radius <= 4 && !placed; radius++) {
					for (int dy = -radius; dy <= radius && !placed; dy++) {
						for (int dx = -radius; dx <= radius; dx++) {
							if (Math.max(Math.abs(dx), Math.abs(dy)) != radius) {
								continue;
							}
							int x = cx + dx;
							int y = cy + dy;
							if (x < 0 || y < 0 || x >= width || y >= Dungeon.level.height()) {
								continue;
							}
							if (placeEnvelope(x + y * width)) {
								placed = true;
								break;
							}
						}
					}
				}
			}
			if (!placed) {
				spawn();
			}
		}
	}

	/** 每层开局生成 5 个红包：2 个在玩家附近，3 个随机。 */
	public static void spawnFloorEnvelopes() {
		spawnNearHero();
		spawnNearHero();
		for (int i = 0; i < 3; i++) {
			spawn();
		}
	}

	/**
	 * 从当前层级可生成的怪物中随机放出一只（用于红包里的“怪物”结果）。
	 */
	public static void spawnMonster(int cell) {
		if (Dungeon.level == null || Dungeon.hero == null || !Dungeon.hero.isAlive()) {
			return;
		}
		Mob mob = Dungeon.level.createMob();
		if (mob == null) {
			return;
		}

		int pos = -1;
		if (cell >= 0 && cell < Dungeon.level.length()
				&& Dungeon.level.passable[cell] && Actor.findChar(cell) == null) {
			pos = cell;
		} else {
			for (int offset : PathFinder.NEIGHBOURS8) {
				int p = cell + offset;
				if (p >= 0 && p < Dungeon.level.length()
						&& Dungeon.level.passable[p] && Actor.findChar(p) == null) {
					pos = p;
					break;
				}
			}
		}
		if (pos == -1) {
			pos = Dungeon.level.randomRespawnCell(mob);
		}
		if (pos == -1) {
			return;
		}

		mob.pos = pos;
		mob.state = mob.WANDERING;
		GameScene.add(mob);
		Dungeon.level.occupyCell(mob);
		mob.beckon(Dungeon.hero.pos);
		GLog.w(Messages.get(Hongbao.class, "monster"));
	}

	/** 每回合给玩家视野内的红包加一点高亮粒子。 */
	public static void highlightEnvelopes() {
		if (!active() || Dungeon.level == null || GameScene.emitter() == null) {
			return;
		}
		for (int cell : Dungeon.level.heaps.keyArray()) {
			Heap heap = Dungeon.level.heaps.get(cell);
			if (heap != null && !heap.isEmpty() && heap.peek() instanceof RedEnvelope
					&& cell < Dungeon.level.heroFOV.length && Dungeon.level.heroFOV[cell]) {
				CellEmitter.get(cell).burst(Speck.factory(Speck.LIGHT), 2);
			}
		}
	}

	public static int visibleEnvelope(Char ch) {
		if (ch == null || ch.fieldOfView == null || Dungeon.level == null) {
			return -1;
		}
		// 无法移动的怪物不会受红包影响，也不会去抢红包
		if (Char.hasProp(ch, Char.Property.IMMOVABLE)) {
			return -1;
		}
		for (int cell : Dungeon.level.heaps.keyArray()) {
			Heap heap = Dungeon.level.heaps.get(cell);
			if (heap == null || heap.isEmpty() || !(heap.peek() instanceof RedEnvelope)) {
				continue;
			}
			if (Dungeon.level.distance(ch.pos, cell) <= 10 && ch.fieldOfView[cell]) {
				return cell;
			}
		}
		return -1;
	}

	public static void mobPickup(Mob mob, int cell) {
		if (mob == null || Char.hasProp(mob, Char.Property.IMMOVABLE)) {
			return;
		}
		Heap heap = Dungeon.level.heaps.get(cell);
		if (heap == null) {
			return;
		}
		Item item = heap.peek();
		if (!(item instanceof RedEnvelope)) {
			return;
		}
		heap.pickUp(item);
		mobReward(mob);
		CellEmitter.get(cell).burst(Speck.factory(Speck.LIGHT), 6);
	}

	/** 怪物开红包：35% 获得装备/道具并立即使用，65% 获得属性强化。 */
	private static void mobReward(Mob mob) {
		if (Random.Int(100) < 35 && mobGetItem(mob)) {
			return;
		}
		Buff.affect(mob, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MobEnvelope.class).addRandom(mob);
	}

	private static boolean mobGetItem(Mob mob) {
		switch (Random.Int(6)) {
			case 0: {
				for (int i = 0; i < 3; i++) {
					Item generated = Generator.random(Generator.Category.POTION);
					if (generated instanceof Potion) {
						Potion potion = (Potion) generated;
						potion.identify();
						mobUsePotion(mob, potion);
						return true;
					}
				}
				return false;
			}
			case 1: {
				for (int i = 0; i < 3; i++) {
					Item generated = Generator.random(Generator.Category.SCROLL);
					if (generated instanceof Scroll) {
						Scroll scroll = (Scroll) generated;
						scroll.identify();
						mobUseScroll(mob, scroll);
						return true;
					}
				}
				return false;
			}
			case 2:
				if (mob.attackingWeapon() == null) {
					for (int i = 0; i < 3; i++) {
						Item generated = Generator.random(Generator.Category.WEAPON);
						if (generated instanceof KindOfWeapon) {
							if (generated instanceof Weapon
									&& ((Weapon) generated).STRReq() > mobStrength()) {
								continue;
							}
							KindOfWeapon weapon = (KindOfWeapon) generated;
							weapon.identify();
							mob.mobEquipment.weapon = weapon;
							weapon.activate(mob);
							mobLog(mob, "mob_equip", mob.name(), weapon.name());
							return true;
						}
					}
				}
				return false;
			case 3:
				if (mob.armor() == null) {
					// 护甲池里混有副手护甲（如 ThePure），它们不是 Armor，不能塞进 mobEquipment.armor
					for (int i = 0; i < 3; i++) {
						Item generated = Generator.random(Generator.Category.ARMOR);
						if (generated instanceof Armor) {
							Armor armor = (Armor) generated;
							if (armor.STRReq() > mobStrength()) {
								continue;
							}
							armor.identify();
							mob.mobEquipment.armor = armor;
							armor.activate(mob);
							mobLog(mob, "mob_equip", mob.name(), armor.name());
							return true;
						}
					}
				}
				return false;
			case 4: {
				if (mob.mobEquipment.wand == null) {
					for (int i = 0; i < 3; i++) {
						Item generated = Generator.random(Generator.Category.WAND);
						if (generated instanceof Wand) {
							Wand wand = (Wand) generated;
							wand.identify();
							mob.mobEquipment.wand = wand;
							mobLog(mob, "mob_equip", mob.name(), wand.name());
							if (Dungeon.hero != null && canMobZap(mob, Dungeon.hero)) {
								mobWandAttack(mob, Dungeon.hero);
							}
							return true;
						}
					}
				}
				return false;
			}
			default:
				if (mob.mobEquipment.ring == null || mob.mobEquipment.misc == null) {
					for (int i = 0; i < 3; i++) {
						Item generated = Generator.randomUsingDefaults(Generator.Category.RING);
						if (generated instanceof Ring) {
							Ring ring = (Ring) generated;
							ring.identify();
							if (mob.mobEquipment.ring == null) {
								mob.mobEquipment.ring = ring;
							} else {
								mob.mobEquipment.misc = ring;
							}
							ring.activate(mob);
							mobLog(mob, "mob_equip", mob.name(), ring.name());
							return true;
						}
					}
				}
				return false;
		}
	}


	private static int mobStrength() {
		// 1-5 层 12 力量，之后每 5 层 +2
		return 12 + ((Math.max(1, Dungeon.depth) - 1) / 5) * 2;
	}

	public static boolean canMobZap(Mob mob, Char enemy) {
		return enemy != null && Dungeon.level != null
				&& mob.fieldOfView != null && mob.fieldOfView.length == Dungeon.level.length()
				&& mob.fieldOfView[enemy.pos]
				&& Dungeon.level.distance(mob.pos, enemy.pos) <= 10;
	}

	public static boolean mobWandAttack(Mob mob, Char enemy) {
		Wand wand = mob.mobEquipment.wand;
		if (wand == null || wand.curCharges <= 0 || enemy == null || !enemy.isAlive()) {
			return false;
		}
		// 直接伤害类法杖直接走玩家同款 onZap，不再另写一套近似效果
		if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile
				|| wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost
				|| wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast
				|| wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning
				|| wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion
				|| wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave
				|| wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight
				|| wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration) {
			return castMobWand(mob, wand, enemy);
		}

		wand.curCharges--;

		int dmg;
		if (wand instanceof DamageWand) {
			DamageWand damageWand = (DamageWand) wand;
			dmg = Random.NormalIntRange(
					damageWand.min(wand.buffedLvl()),
					damageWand.max(wand.buffedLvl()));
		} else {
			dmg = 4 + 2 * wand.buffedLvl();
		}
		final int boltDamage = Math.max(1, dmg);

		// 不同法杖使用不同的弹道/伤害类型，避免所有法杖都变成魔弹
		int boltType = MagicMissile.MAGIC_MISSILE;
		DamageType damageType = DamageType.MAGICAL;
		if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost) {
			boltType = MagicMissile.FROST;
			damageType = DamageType.FROST;
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast) {
			boltType = MagicMissile.FIRE;
			damageType = DamageType.FIRE;
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLightning) {
			boltType = MagicMissile.SHAMAN_BLUE;
			damageType = DamageType.LIGHTNING;
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion) {
			boltType = MagicMissile.CORROSION;
			damageType = DamageType.CORROSIVE;
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave) {
			boltType = MagicMissile.FORCE;
			damageType = DamageType.PHYSICAL;
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth) {
			boltType = MagicMissile.EARTH;
			damageType = DamageType.PHYSICAL;
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight) {
			boltType = MagicMissile.RAINBOW;
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfRegrowth) {
			boltType = MagicMissile.FOLIAGE;
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfWarding) {
			boltType = MagicMissile.WARD;
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTransfusion
				|| wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorruption) {
			boltType = MagicMissile.SHADOW;
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfDisintegration) {
			boltType = MagicMissile.BEACON;
		}
		final int finalBoltType = boltType;
		final DamageType finalDamageType = damageType;

		if (mob.sprite == null || mob.sprite.parent == null) {
			enemy.damage(new DamageInfo(boltDamage, finalDamageType, mob, wand, wand));
			applyMobWandStatus(mob, enemy, wand);
			return true;
		}

		Sample.INSTANCE.play(Assets.Sounds.ZAP);
		MagicMissile.boltFromChar(mob.sprite.parent, finalBoltType,
				mob.sprite, enemy.pos, new Callback() {
					@Override
					public void call() {
						if (enemy.isAlive()) {
							enemy.damage(new DamageInfo(
									boltDamage, finalDamageType, mob, wand, wand));
							applyMobWandStatus(mob, enemy, wand);
						}
					}
				});
		return true;
	}

	/** 用怪物作为施法者执行法杖原本的 onZap。 */
	private static boolean castMobWand(Mob mob, Wand wand, Char enemy) {
		if (enemy == null || !enemy.isAlive()) {
			return false;
		}
		Ballistica bolt = new Ballistica(mob.pos, enemy.pos, wand.collisionProperties(enemy.pos));
		final Char previousCaster = Wand.curUser;
		Wand.curUser = mob;

		if (mob.sprite == null || mob.sprite.parent == null) {
			wand.onZap(bolt);
			wand.curCharges = Math.max(0, wand.curCharges - 1);
			Wand.curUser = previousCaster;
			return true;
		}

		wand.fx(bolt, new Callback() {
			@Override
			public void call() {
				try {
					wand.onZap(bolt);
				} finally {
					wand.curCharges = Math.max(0, wand.curCharges - 1);
					Wand.curUser = previousCaster;
				}
			}
		});
		return true;
	}

	/** 给不同法杖补上对应的附加效果，而不只是换弹道颜色。 */
	private static void applyMobWandStatus(Mob mob, Char enemy, Wand wand) {
		if (enemy == null || !enemy.isAlive()) {
			return;
		}
		if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFrost) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill.chillOrFreeze(enemy, 5f);
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfFireblast) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
					enemy, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning.class).reignite(enemy);
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfCorrosion) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
					enemy, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion.class).set(5f, 2);
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
					enemy, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo.class, 3f);
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfPrismaticLight) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
					enemy, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness.class, 3f);
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfLivingEarth) {
			com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(
					enemy, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots.class, 3f);
		} else if (wand instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfTransfusion) {
			mob.HP = Math.min(mob.HT, mob.HP + 2);
		}
	}

	/** 有害药水先收起来，等看到玩家再丢。 */
	private static void mobUsePotion(Mob mob, Potion potion) {
		if (Potion.isHarmful(potion)) {
			mob.mobEquipment.harmfulPotion = potion;
			mobLog(mob, "mob_store", mob.name(), potion.name());
			return;
		}

		mobLog(mob, "mob_potion", mob.name(), potion.name());
		if (potion instanceof PotionOfHealing) {
			PotionOfHealing.cure(mob);
			PotionOfHealing.heal(mob);
		} else if (potion instanceof PotionOfHaste) {
			Buff.affect(mob, Haste.class, Haste.DURATION);
		} else if (potion instanceof PotionOfInvisibility) {
			Buff.affect(mob, Invisibility.class, Invisibility.DURATION);
		} else if (potion instanceof PotionOfLevitation) {
			Buff.affect(mob, Levitation.class, Levitation.DURATION);
		} else if (potion instanceof PotionOfMindVision) {
			Buff.affect(mob, MindVision.class, MindVision.DURATION);
		} else if (potion instanceof PotionOfPurity) {
			Buff.prolong(mob, BlobImmunity.class, BlobImmunity.DURATION);
			potion.mobUseAt(mob.pos);
		} else if (potion instanceof PotionOfStrength) {
			mob.HT += 2;
			mob.HP += 2;
		} else if (potion instanceof PotionOfExperience) {
			mob.HP = mob.HT;
		} else {
			potion.mobUseAt(mob.pos);
		}
	}

	/** 看到玩家时，把存着的有害药水丢出去。 */
	public static boolean throwStoredPotion(Mob mob) {
		Potion potion = mob.mobEquipment.harmfulPotion;
		if (potion == null || Dungeon.hero == null || !Dungeon.hero.isAlive()) {
			return false;
		}
		mob.mobEquipment.harmfulPotion = null;
		mobLog(mob, "mob_throw", mob.name(), potion.name());
		throwPotionAtHero(mob, potion);
		return true;
	}

	private static void throwPotionAtHero(Mob mob, Potion potion) {
		Hero hero = Dungeon.hero;
		if (hero == null || !hero.isAlive()) {
			potion.mobUseAt(mob.pos);
			return;
		}

		final int from = mob.pos;
		final int to = new Ballistica(from, hero.pos, Ballistica.PROJECTILE).collisionPos;

		if (mob.sprite == null || mob.sprite.parent == null) {
			potion.mobUseAt(to);
			return;
		}

		Sample.INSTANCE.play(Assets.Sounds.MISS, 0.6f, 0.6f, 1.5f);
		((MissileSprite) mob.sprite.parent.recycle(MissileSprite.class)).reset(
				from, to, potion, new Callback() {
					@Override
					public void call() {
						potion.mobUseAt(to);
					}
				});
	}

	/** 卷轴立即使用：能用在自己身上的照常生效，其余转化为一次红包强化。 */
	private static void mobUseScroll(Mob mob, Scroll scroll) {
		mobLog(mob, "mob_scroll", mob.name(), scroll.name());

		if (scroll instanceof ScrollOfUpgrade) {
			Item gear = null;
			if (mob.mobEquipment.weapon != null) {
				gear = mob.mobEquipment.weapon;
			} else if (mob.mobEquipment.armor != null) {
				gear = mob.mobEquipment.armor;
			} else if (mob.mobEquipment.ring != null) {
				gear = mob.mobEquipment.ring;
			}
			if (gear != null) {
				gear.upgrade();
			}
		} else if (scroll instanceof ScrollOfRemoveCurse) {
			ArrayList<Item> items = mob.mobEquipment.carriedItems();
			ScrollOfRemoveCurse.uncurse(null, items.toArray(new Item[0]));
		} else if (scroll instanceof ScrollOfTeleportation) {
			ScrollOfTeleportation.teleportChar(mob);
		} else if (scroll instanceof ScrollOfRecharging) {
			mob.HP = Math.min(mob.HT, mob.HP + Math.max(1, mob.HT / 4));
		} else if (scroll instanceof ScrollOfIdentify) {
			for (Item item : mob.mobEquipment.carriedItems()) {
				item.identify();
			}
		} else {
			// 镜像、催眠、狂怒、恐惧、报应、嬗变等卷轴以英雄为目标，怪物无法直接套用，
			// 这里改为吸收卷轴力量获得一次红包强化，避免“空读”。
		}
	}

	private static void mobLog(Mob mob, String key, Object... args) {
		if (Dungeon.level != null && Dungeon.level.heroFOV[mob.pos]) {
			GLog.i(Messages.get(Hongbao.class, key, args));
		}
	}

	public static void rewardHero(Hero hero) {
		if (Random.Int(100) < 50) {
			Item item;
			switch (Random.Int(8)) {
				case 0: item = Generator.random(Generator.Category.WEAPON); break;
				case 1: item = Generator.random(Generator.Category.ARMOR); break;
				case 2: item = Generator.random(Generator.Category.WAND); break;
				case 3: item = Generator.random(Generator.Category.POTION); break;
				case 4: item = Generator.random(Generator.Category.SCROLL); break;
				case 5: item = Generator.random(Generator.Category.STONE); break;
				case 6: item = Generator.random(Generator.Category.SEED); break;
				default:
					int gold = Random.IntRange(20 + Dungeon.depth * 8, 40 + Dungeon.depth * 16);
					Dungeon.gold += gold;
					Statistics.goldCollected += gold;
					Badges.validateGoldCollected();
					GLog.p(Messages.get(Hongbao.class, "gold", gold));
					return;
			}
			item.identify();
			if (!item.collect(hero.belongings.backpack)) {
				Dungeon.level.drop(item, hero.pos);
			}
			GLog.p("红包开出：" + item.name());
		} else {
			boolean hasFlamewalker = com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.hasFlamewalker(hero);
			boolean hasKnockback = com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.hasKnockbackOnApproach(hero);
			boolean hasAlly = com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.hasAlly(hero);
			int roll;
			do {
				roll = Random.Int(9);
			} while (roll == 8 && hasAlly);
			if (roll == 1) {
				// 法杖不消耗充能概率：每次随机 +1%~10%
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost boost =
						Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.class);
				float before = boost.wandFreeChance();
				boost.addWandFreeChance(Random.Float(0.01f, 0.10f));
				float after = boost.wandFreeChance();
				GLog.p("红包开出：法杖不消耗充能概率 " + Math.round(before * 100) + "% → " + Math.round(after * 100) + "%");
			} else if (roll == 2) {
				// 连击次数 +1，无上限
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost boost =
						Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.class);
				int before = boost.comboHits();
				boost.addComboHits(1);
				GLog.p("红包开出：连击次数 +" + (boost.comboHits() - before) + "（当前 " + boost.comboHits() + "）");
			} else if (roll == 3) {
				// 攻击距离 +1，无上限
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost boost =
						Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.class);
				boost.addReachBonus(1);
				GLog.p("红包开出：攻击距离 +1（当前 +" + boost.reachBonus() + "）");
			} else if (roll == 4) {
				// 投掷物返回概率：每次随机 +1%~10%
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost boost =
						Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.class);
				float before = boost.missileReturnChance();
				boost.addMissileReturnChance(Random.Float(0.01f, 0.10f));
				GLog.p("红包开出：投掷物返回概率 " + Math.round(before * 100) + "% → "
						+ Math.round(boost.missileReturnChance() * 100) + "%");
			} else if (roll == 5) {
				// 附魔触发概率与效果 +5%
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost boost =
						Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.class);
				boost.addEnchantPower(0.05f);
				GLog.p("红包开出：附魔触发与效果 +" + Math.round(boost.enchantPower() * 100) + "%");
			} else if (roll == 6) {
				if (!hasFlamewalker) {
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost boost =
							Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.class);
					boost.grantFlamewalker();
					GLog.p("红包开出：烈焰人（走过地格燃烧，免疫燃烧）");
				} else {
					GLog.p("红包开出：烈焰人已拥有，本次无新增");
				}
			} else if (roll == 7) {
				if (!hasKnockback) {
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost boost =
							Buff.affect(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.class);
					boost.grantKnockbackOnApproach();
					GLog.p("红包开出：怪物初次贴近时击退 1 格");
				} else {
					GLog.p("红包开出：击退已拥有，本次无新增");
				}
			} else if (roll == 8) {
				if (!hasAlly) {
					com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.grantAlly(hero);
				} else {
					GLog.p("红包开出：怪物盟友已拥有，本次无新增");
				}
			} else if (roll == 9) {
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.grantFreeScroll(hero);
			} else if (roll == 10) {
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.grantFreePotion(hero);
			} else if (roll == 11) {
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.grantFovDamage(hero);
			} else {
				com.shatteredpixel.shatteredpixeldungeon.actors.buffs.EnvelopeBoost.grantFusion(hero);
			}
		}
	}
}
