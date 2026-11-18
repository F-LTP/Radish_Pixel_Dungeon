package com.shatteredpixel.shatteredpixeldungeon.items.cards;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeldCardTracker;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;

public final class CardTargeter {

	private CardTargeter() {
	}

	public static void play(Hero hero, Card card) {
		if (hero == null || card == null || hero.cardDeck == null || hero.cardDeck.held != card) {
			return;
		}
		if (!hero.isAlive() || !hero.ready) return;

		if (card.type.target == CardType.Target.ENEMY) {
			Char target = QuickSlotButton.lastTarget;
			if (target != null && Actor.chars().contains(target) && target.isAlive()
					&& target.alignment != Char.Alignment.ALLY
					&& target.pos >= 0 && target.pos < Dungeon.level.length()
					&& Dungeon.level.heroFOV[target.pos]
					&& isValidTarget(hero, card, target.pos)) {
				CardEffects.play(hero, card, target.pos);
				return;
			}
		}

		// 陷阱师：空白卡可以对陷阱使用，因此改用格子选择
		if (card.type == CardType.BLANK && hero.subClass == HeroSubClasses.JOKER) {
			if (!hasValidTarget(hero, card)) {
				offerSafeReturn(hero, card);
			} else {
				GameScene.selectCell(new CellSelector.Listener() {
					@Override
					public void onSelect(Integer cell) {
						if (cell == null || !isValidTarget(hero, card, cell)) {
							if (cell != null) {
								com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
										Messages.get(CardTargeter.class, "bad_target"));
							}
							return;
						}
						CardEffects.play(hero, card, cell);
					}

					@Override
					public String prompt() {
						return Messages.get(CardTargeter.class, "prompt", card.name());
					}
				});
			}
			return;
		}

		switch (card.type.target) {
			case NONE:
			case SELF:
				CardEffects.play(hero, card, hero.pos);
				break;
			case ENEMY:
			case ANY:
				if (!hasValidTarget(hero, card)) {
					offerSafeReturn(hero, card);
				} else {
					GameScene.selectCell(new CellSelector.Listener() {
						@Override
						public void onSelect(Integer cell) {
							if (cell == null || !isValidTarget(hero, card, cell)) {
								if (cell != null) {
									com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
											Messages.get(CardTargeter.class, "bad_target"));
								}
								return;
							}
							CardEffects.play(hero, card, cell);
						}

						@Override
						public String prompt() {
							return Messages.get(CardTargeter.class, "prompt", card.name());
						}
					});
				}
				break;
			case CELL:
				if (!hasValidTarget(hero, card)) {
					offerSafeReturn(hero, card);
				} else {
					GameScene.selectCell(new CellSelector.Listener() {
						// 弹窗时 GameScene.show 会再取消一次选格、再回调一次 null
						private boolean cancelOffered = false;

						@Override
						public void onSelect(Integer cell) {
							if (cell == null && card.type == CardType.TRAP
									&& hero.pointsInTalent(Talent.CAREFUL_SELECTION) >= 2) {
								if (!cancelOffered) {
									cancelOffered = true;
									offerTrapCancel(hero, card);
								}
								return;
							}
							if (cell == null || !isValidTarget(hero, card, cell)) {
								if (cell != null) {
									com.shatteredpixel.shatteredpixeldungeon.utils.GLog.w(
											Messages.get(CardTargeter.class, "bad_target"));
								}
								return;
							}
							CardEffects.play(hero, card, cell);
						}

						@Override
						public String prompt() {
							return Messages.get(CardTargeter.class, "prompt", card.name());
						}
					});
				}
				break;
		}
	}

	/**
	 * 看得见，并且从英雄到目标格的直线没有被墙挡住（中间的其他生物不挡）。
	 * 弹道必须带 STOP_TARGET：只给 STOP_SOLID 时它会穿过目标一直飞到撞墙，
	 * collisionPos 是墙前最后一格，只有目标正好贴着墙站时才等于目标格。
	 */
	private static boolean inSight(Hero hero, int cell) {
		return Dungeon.level.heroFOV[cell]
				&& new Ballistica(hero.pos, cell, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID).collisionPos == cell;
	}

	/** 陷阱师的空白卡能吸收的陷阱：看得见、已经被发现、还没触发。隐藏的陷阱不能算，否则对着任意一格点一下就是免费探雷。 */
	static boolean absorbableTrap(int cell) {
		Trap trap = Dungeon.level.traps.get(cell);
		return trap != null && trap.visible && trap.active && Dungeon.level.heroFOV[cell];
	}

	public static boolean isValidTarget(Hero hero, Card card, int cell) {
		Char ch = Actor.findChar(cell);
		switch (card.type.target) {
			case NONE:
				return true;
			case SELF:
				return ch == hero;
			case ENEMY:
				if (ch == null || ch == hero || ch.alignment == Char.Alignment.ALLY) return false;
				// 卡牌攻击不能穿墙：需要视线可达
				return inSight(hero, ch.pos);
			case ANY:
				if (card.type == CardType.BLANK && ch == hero) {
					// 空白卡指定自己：仅收藏家可用（转化为攻击/防御卡）。
					return hero.subClass == HeroSubClasses.ILLUSIONIST;
				}
				if (card.type == CardType.BLANK && hero.subClass == HeroSubClasses.JOKER
						&& absorbableTrap(cell)) {
					// 陷阱师：陷阱格
					return true;
				}
				if (ch == null) return false;
				if (ch == hero) return true;
				// 与攻击卡一样不能隔墙、不能打看不见的目标
				return ch.alignment != Char.Alignment.ALLY && inSight(hero, ch.pos);
			case CELL:
				return isValidCell(hero, card, cell);
			default:
				return false;
		}
	}

	private static boolean isValidCell(Hero hero, Card card, int cell) {
		if (Dungeon.level.solid[cell]) return false;
		if (card.type == CardType.TELEPORT) {
			int range = card.teleportRange > 0 ? card.teleportRange : 2;
			if (Dungeon.level.distance(hero.pos, cell) > range) return false;
			if (!Dungeon.level.passable[cell]) return false;
			// 允许传送到自己脚下以外的空地；有生物站着的格子不可落点
			if (cell != hero.pos && Actor.findChar(cell) != null) return false;
			// 传送不需要视线，只要能走到的平面即可
			return true;
		}
		// 陷阱卡：视野内 8 格，只需可达且非实体墙（不被生物/软障碍阻挡）
		if (Dungeon.level.distance(hero.pos, cell) > 8) return false;
		// 必须带 STOP_TARGET | STOP_SOLID：只给 IGNORE_SOFT_SOLID 时弹道没有任何停下来的条件，
		// 一路飞到地图边缘，collisionPos 不可能等于玩家点的格子，陷阱卡就没有任何合法落点
		Ballistica b = new Ballistica(hero.pos, cell,
				Ballistica.STOP_TARGET | Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID);
		return b.collisionPos == cell;
	}

	public static boolean hasValidTarget(Hero hero, Card card) {
		if (card.type.target == CardType.Target.SELF || card.type.target == CardType.Target.NONE) {
			return true;
		}
		if (card.type.target == CardType.Target.CELL) {
			for (int i = 0; i < Dungeon.level.length(); i++) {
				if (Dungeon.level.heroFOV[i] && isValidCell(hero, card, i)) return true;
			}
			return false;
		}
		if (card.type == CardType.BLANK && hero.subClass == HeroSubClasses.JOKER) {
			for (int i = 0; i < Dungeon.level.length(); i++) {
				if (absorbableTrap(i)) return true;
			}
		}
		for (Char ch : Actor.chars()) {
			if (isValidTarget(hero, card, ch.pos)) return true;
		}
		return false;
	}

	public static void offerSafeReturn(Hero hero, Card card) {
		GameScene.show(new WndOptions(
				Messages.get(CardTargeter.class, "no_target_title"),
				Messages.get(CardTargeter.class, "no_target_desc"),
				Messages.get(CardTargeter.class, "safe_return"),
				Messages.get(CardTargeter.class, "cancel")) {
			@Override
			protected void onSelect(int index) {
				if (index == 0) {
					if (hero.cardDeck != null && hero.cardDeck.held == card) {
						hero.cardDeck.putHeldBack(card);
						HeldCardTracker.clear(hero);
						hero.spendAndNext(1f);
						com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator.refresh();
					}
				}
			}
		});
	}

	// 精挑细选 +2：取消释放陷阱卡，放回牌堆底；+3 同时取消抽卡冷却；+4 不消耗回合
	private static void offerTrapCancel(Hero hero, Card card) {
		GameScene.show(new WndOptions(
				Messages.get(CardTargeter.class, "trap_cancel_title"),
				Messages.get(CardTargeter.class, "trap_cancel_desc"),
				Messages.get(CardTargeter.class, "safe_return"),
				Messages.get(CardTargeter.class, "cancel")) {
			@Override
			protected void onSelect(int index) {
				if (index != 0 || hero.cardDeck == null || hero.cardDeck.held != card) return;
				int points = hero.pointsInTalent(Talent.CAREFUL_SELECTION);
				hero.cardDeck.putHeldBack(card);
				HeldCardTracker.clear(hero);
				if (points >= 3) CardDrawer.cancelCooldown(hero);
				if (points < 4) hero.spendAndNext(1f);
				com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator.refresh();
			}
		});
	}

	public static void offerSafeReturnIfNone(Hero hero) {
		if (hero.cardDeck != null && hero.cardDeck.held != null) {
			Card card = hero.cardDeck.held;
			if (card.type.needsTarget() && !hasValidTarget(hero, card)) {
				offerSafeReturn(hero, card);
			}
		}
	}

	/** Convenience for the deck window. */
	public static void playHeld(Hero hero) {
		if (hero.cardDeck != null && hero.cardDeck.held != null) {
			play(hero, hero.cardDeck.held);
		}
	}

	public static int heldCount(Hero hero) {
		return hero.cardDeck == null || hero.cardDeck.held == null ? 0 : 1;
	}

	public static boolean inCombat(Hero hero) {
		for (Char ch : Actor.chars()) {
			if (ch.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[ch.pos]) return true;
		}
		return false;
	}
}
