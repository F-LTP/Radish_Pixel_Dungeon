package com.shatteredpixel.shatteredpixeldungeon.items.cards;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Adrenaline;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HeldCardTracker;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Stamina;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.PaperCutter;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.DisintegrationTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.FlockTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GatewayTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrimTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GuardianTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.PoisonDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.SummoningTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TeleportationTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.WarpingTrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.WornDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndCardSelect;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashSet;

public final class CardEffects {

	private CardEffects() {
	}

	public static void play(Hero hero, Card card, Integer target) {
		if (hero == null || card == null || hero.cardDeck == null || hero.cardDeck.held != card) {
			return;
		}
		if (target != null && !CardTargeter.isValidTarget(hero, card, target)) {
			GLog.w(Messages.get(CardTargeter.class, "bad_target"));
			return;
		}

		int pos = target == null ? hero.pos : target;
		Char ch = Actor.findChar(pos);

		switch (card.type) {
			case BLANK: {
				// 陷阱师：对未触发的陷阱使用空白卡 -> 记录该陷阱，卡片转为陷阱卡
				Trap trap = Dungeon.level.traps.get(pos);
				// 只吸收看得见、已发现、尚未触发的陷阱（与 CardTargeter 的判定一致）。
				// 其余情况这一格按普通目标处理：敌人站在隐藏的或已触发的陷阱格上时照常打它
				if (trap != null && hero.subClass == HeroSubClasses.JOKER && CardTargeter.absorbableTrap(pos)) {
					card.type = CardType.TRAP;
					card.trapClass = trap.getClass();
					GLog.i(Messages.get(CardEffects.class, "trap_recorded", trap.name()));

					// 吸收后沉入牌堆底，而不是留在手上
					if (hero.cardDeck.held == card) {
						hero.cardDeck.held = null;
						hero.cardDeck.addToBottom(card);
						HeldCardTracker.clear(hero);
						ActionIndicator.refresh();
					}
					return;
				}
				if (ch == hero) {
					// 收藏家：对自己使用空白卡 -> 选择一件装备转化为攻击/防御卡
					collectorTransform(hero, card);
					return;
				}
				if (ch != null) {
					int dmg = Random.NormalIntRange(1, 5);
					int emp = hero.pointsInTalent(Talent.BLANK_EMPOWERMENT);
					if (emp == 1) dmg += Random.NormalIntRange(0, 2);
					else if (emp == 2) dmg += Random.NormalIntRange(1, 3);

					int wind = hero.pointsInTalent(Talent.BORROW_WIND_CUT);
					if (wind > 0) {
						int dist = Math.min(4, Math.round(Dungeon.level.trueDistance(hero.pos, ch.pos)));
						dmg += dist * (wind == 2 ? Random.NormalIntRange(2, 3) : Random.NormalIntRange(1, 2));
					}
					dealCardDamage(hero, ch, card, dmg);
				}

				// 卡组连携 +3：空白卡使用后自动转化为随机陷阱卡
				if (hero.pointsInTalent(Talent.DECK_COMBO) >= 3
						&& hero.subClass == HeroSubClasses.JOKER) {
					Class<?> trapCls = randomTrapClass();
					if (trapCls != null) {
						card.type = CardType.TRAP;
						card.trapClass = trapCls;
						GLog.i(Messages.get(CardEffects.class, "trap_recorded",
								Messages.get(trapCls, "name")));
						ActionIndicator.refresh();
					}
				}

				finish(hero, card, false, card.type.time);
				break;
			}
			case SMALL_RATION:
				eatCard(hero, card, 50);
				finish(hero, card, false, foodTime(hero, card));
				break;
			case RATION:
				eatCard(hero, card, 100);
				finish(hero, card, false, card.type.time);
				break;
			case IDENTIFY: {
				// [audit x-turns IM-4 trial fix] 鉴定挪到玩家选完之后：先鉴定再弹窗的话，窗口开着时存档读档可以白拿鉴定
				if (card.power >= 2) {
					GameScene.show(new WndOptions(
							Messages.get(CardEffects.class, "destroy_title"),
							Messages.get(CardEffects.class, "destroy_desc"),
							Messages.get(CardEffects.class, "destroy_yes"),
							Messages.get(CardEffects.class, "destroy_no")) {
						@Override
						protected void onSelect(int index) {
							identifyRandom(hero, 1, false);
							finish(hero, card, index == 0, card.type.time);
						}

						@Override
						public void onBackPressed() {
							hide();
							identifyRandom(hero, 1, false);
							finish(hero, card, false, card.type.time);
						}
					});
				} else {
					identifyRandom(hero, 1, false);
					finish(hero, card, false, card.type.time);
				}
				break;
			}
			case RIGID: {
				if (ch == null) {
					GLog.w(Messages.get(CardTargeter.class, "bad_target"));
					return;
				}
				Buff.affect(ch, Paralysis.class, 5f);
				CardDrawer.cancelCooldown(hero);
				finish(hero, card, false, card.type.time);
				break;
			}
			case HORMONE: {
				Buff.affect(hero, Adrenaline.class, 2f);
				Buff.affect(hero, Haste.class, 2f);
				CardDrawer.cancelCooldown(hero);
				finish(hero, card, false, card.type.time);
				break;
			}
			case SPIN: {
				int amount = card.power >= 3 ? 5 : card.power == 2 ? 4 : 3;
				int spun = hero.cardDeck.spinBlank(amount);
				// 文本只有一个 %d，说的是「移到牌堆底的张数」：要传实际空转掉的 spun，不是检视的 amount
				GLog.i(Messages.get(CardEffects.class, "spin_done", spun));
				if (hero.pointsInTalent(Talent.FANCY_SHUFFLE) >= 4) CardDrawer.cancelCooldown(hero);
				fancySpin(hero, spun);
				finish(hero, card, false, card.type.time);
				break;
			}
			case GRAND_FINALE: {
				if (ch == null || ch == hero || ch.alignment == Char.Alignment.ALLY) {
					GLog.w(Messages.get(CardTargeter.class, "bad_target"));
					return;
				}
				int dmg = Random.NormalIntRange(5, 10);
				for (int i = 0; i < hero.cardDeck.blankCount(); i++) {
					dmg += Random.NormalIntRange(5, 10);
				}
				if (card.power >= 3) {
					dmg += Math.round(weaponAverage(card) / 3f);
				}
				dealCardDamage(hero, ch, card, dmg);
				fancySpin(hero, hero.cardDeck.spinBlank(card.power >= 2 ? 3 : 2));
				finish(hero, card, false, card.type.time);
				break;
			}
			case PREPARED: {
				int look = card.power >= 3 ? 4 : card.power == 2 ? 3 : 2;
				int maxMove = card.power >= 3 ? 2 : 1;
				ArrayList<Card> top = hero.cardDeck.peekTop(look);
				if (top.isEmpty()) {
					finish(hero, card, false, card.type.time);
				} else {
					GameScene.show(new WndCardSelect(
							Messages.get(CardEffects.class, "prepared_title"),
							Messages.get(CardEffects.class, "prepared_desc", maxMove),
							top, maxMove, true, selected -> {
								for (Card c : selected) {
									if (hero.cardDeck.drawPile.remove(c)) {
										hero.cardDeck.addToBottom(c);
										hero.cardDeck.noteCycle(1);
									}
								}
								if (hero.pointsInTalent(Talent.FANCY_SHUFFLE) >= 4) CardDrawer.cancelCooldown(hero);
								finish(hero, card, false, card.type.time);
							}) {
						// 牌堆顶已经给玩家看过了，取消也要算打出；否则可以不花卡、不花回合地反复偷看。
						// 确认那条路在回调里已经结算过，finish 开头的「手牌还是不是这张」检查让这里变成空操作
						@Override
						public void hide() {
							super.hide();
							finish(hero, card, false, card.type.time);
						}
					});
				}
				break;
			}
			case ATTACK: {
				if (ch == null || ch == hero || ch.alignment == Char.Alignment.ALLY) {
					GLog.w(Messages.get(CardTargeter.class, "bad_target"));
					return;
				}
				// 只有身上已经有「下一张攻击卡不耗回合」时才免费。
				// Buff.affect 在没有这个状态时会当场新建一个，新建的一定能 consume，等于所有攻击卡都不耗回合
				NextAttackFree naf = hero.buff(NextAttackFree.class);
				boolean free = naf != null && naf.consume();
				// 基础伤害 1~5，再附加一份 1~5 的额外伤害
				int dmg = Random.NormalIntRange(1, 5) + Random.NormalIntRange(1, 5);

				int emp = hero.pointsInTalent(Talent.BLANK_EMPOWERMENT);
				if (emp == 1) dmg += Random.NormalIntRange(0, 2);
				else if (emp == 2) dmg += Random.NormalIntRange(1, 3);

				int wind = hero.pointsInTalent(Talent.BORROW_WIND_CUT);
				if (wind > 0) {
					int dist = Math.min(4, Math.round(Dungeon.level.trueDistance(hero.pos, ch.pos)));
					dmg += dist * (wind == 2 ? Random.NormalIntRange(2, 3) : Random.NormalIntRange(1, 2));
				}

				int vault = hero.pointsInTalent(Talent.REFINED_ATTACK_DEFENSE);
				if (vault > 0) {
					int count = hero.cardDeck.count(CardType.ATTACK);
					if (vault == 1) dmg += count;
					else if (vault == 2) dmg += count * Random.NormalIntRange(1, 2);
					else dmg += count * Random.NormalIntRange(2, 3);
				}

				int pick = hero.pointsInTalent(Talent.CARD_ENCHANT);
				if (pick > 0) {
					float frac = pick == 1 ? 0.25f : pick == 2 ? 1f / 3f : 0.5f;
					dmg += Math.round(weaponAverage(card) * frac);
				}

				// 以牙还牙：把存储的伤害一并打出
				Vendetta vend = hero.buff(Vendetta.class);
				if (vend != null && vend.stored > 0) {
					int bonus = vend.take();
					dmg += bonus;
					hero.sprite.showStatus(CharSprite.POSITIVE, "+" + bonus);
				}

				dealCardDamage(hero, ch, card, dmg);

				// 背水一战：牌堆顶仍是攻击卡时追加额外攻击
				int lastStand = hero.pointsInTalent(Talent.CT_LAST_STAND);
				if (lastStand > 0 && !hero.cardDeck.drawPile.isEmpty()) {
					Card top = hero.cardDeck.drawPile.get(hero.cardDeck.drawPile.size() - 1);
					if (top != null && top.type == CardType.ATTACK) {
						float frac = lastStand >= 3 ? 0.75f : 0.5f;
						if (lastStand >= 4) frac = 1.0f;
						int targets = lastStand >= 2 ? 2 : 1;
						ArrayList<Char> others = new ArrayList<>();
						for (Char c : Actor.chars()) {
							if (c != hero && c.alignment == Char.Alignment.ENEMY && c.isAlive()) {
								others.add(c);
							}
						}
						for (int i = 0; i < targets && !others.isEmpty(); i++) {
							Char t = others.remove(Random.Int(others.size()));
							dealCardDamage(hero, t, card, Math.max(1, Math.round(dmg * frac)));
						}
					}
				}

				finish(hero, card, false, free ? 0f : card.type.time);
				break;
			}
			case DEFENSE: {
				int shield = 3;
				int vault = hero.pointsInTalent(Talent.REFINED_ATTACK_DEFENSE);
				if (vault > 0) {
					shield += hero.cardDeck.count(CardType.DEFENSE) * vault;
				}
				int pick = hero.pointsInTalent(Talent.CARD_ENCHANT);
				if (pick > 0) {
					float frac = pick == 1 ? 0.25f : pick == 2 ? 1f / 3f : 0.5f;
					shield += Math.round(armorMax(card) * frac);
				}

				// 防御卡护盾取高者覆盖，不叠加
				Barrier barrier = Buff.affect(hero, Barrier.class);
				if (barrier.shielding() < shield) {
					barrier.setShield(shield);
				}
				hero.sprite.showStatus(CharSprite.POSITIVE, "+" + shield);

				// 以守为攻
				int guard = hero.pointsInTalent(Talent.CARD_ASSIST);
				if (guard >= 1) {
					Buff.affect(hero, Stamina.class, 1f);
				}
				if (guard >= 2) {
					Card card2 = hero.cardDeck.drawPile.isEmpty() ? null
							: hero.cardDeck.drawPile.get(hero.cardDeck.drawPile.size() - 1);
					if (card2 != null && card2.type == CardType.ATTACK) {
						CardDrawer.reduceCooldown(hero, 5);
					}
				}
				if (guard >= 3) {
					Buff.affect(hero, NextAttackFree.class);
				}

				// 以牙还牙：防御卡使用后按天赋等级存储所受伤害
				int vend = hero.pointsInTalent(Talent.CT_VENDETTA);
				if (vend > 0) {
					if (hero.buff(Vendetta.class) == null) {
						Buff.affect(hero, Vendetta.class);
					}
					hero.buff(Vendetta.class).ratio = 0.15f * vend;
				}

				CardDrawer.cancelCooldown(hero);
				finish(hero, card, false, card.type.time);
				break;
			}
			case GAZE: {
				Buff.affect(hero, MindVision.class, 20f);
				GLog.i(Messages.get(CardEffects.class, "gaze"));
				CardDrawer.cancelCooldown(hero);
				int gaze = hero.pointsInTalent(Talent.GAZE_CARD);
				if (gaze >= 4) Buff.affect(hero, DangerSense.class, 5f);
				float time = gaze >= 3 ? 0f : card.type.time;
				finish(hero, card, false, time);
				break;
			}
			case TRAP: {
				if (hero.buff(TrapCooldown.class) != null) {
					GLog.w(Messages.get(CardEffects.class, "trap_cooling", (int) hero.buff(TrapCooldown.class).cooldown()));
					return;
				}
				Class<?> trapCls = card.trapClass;
				triggerTrap(card, pos);
				trapMasteryAfterPlay(hero, trapCls);
				if (hero.pointsInTalent(Talent.CAREFUL_SELECTION) >= 1) {
					CardDrawer.reduceCooldown(hero, 5);
				}

				// 卡组连携 +2：同时把牌堆顶的空白卡打到同一落点
				if (hero.pointsInTalent(Talent.DECK_COMBO) >= 2) {
					Card topBlank = null;
					for (int i = hero.cardDeck.drawPile.size() - 1; i >= 0; i--) {
						Card c = hero.cardDeck.drawPile.get(i);
						if (c.type == CardType.BLANK) {
							topBlank = c;
							break;
						}
					}
					if (topBlank != null) {
						hero.cardDeck.drawPile.remove(topBlank);
						triggerTrap(topBlank, pos);
						// 空白卡没有记录陷阱，上面那句对它什么都不做；打出去的卡要回到牌堆底，
						// 否则每打一张陷阱卡牌组就永久少一张空白卡
						hero.cardDeck.addToBottom(topBlank);
					}
				}

				// 卡组连携 +1：要在这张卡退回空白卡之前结算。
				// 原来写在 finish 里，那时 type 已经是 BLANK，对真正的陷阱卡永远不触发，
				// 反而会被 +3「空白卡用后变成陷阱卡」触发
				if (hero.pointsInTalent(Talent.DECK_COMBO) >= 1) {
					Card next = hero.cardDeck.drawPile.isEmpty() ? null
							: hero.cardDeck.drawPile.get(hero.cardDeck.drawPile.size() - 1);
					if (next == null || next.type != CardType.TRAP) {
						rechargeWandsAndArtifacts(hero, 2f);
					}
				}

				Buff.affect(hero, TrapCooldown.class, trapCooldown(hero));
				card.type = CardType.BLANK;
				card.trapClass = null;
				boolean freeTrap = hero.pointsInTalent(Talent.TRAP_MASTERY) >= 2 && !isDirectionalTrap(trapCls);
				finish(hero, card, false, freeTrap ? 0f : CardType.TRAP.time);
				break;
			}
			case TELEPORT: {
				if (hero.rooted) return;
				int cell = target == null ? hero.pos : target;
				// 走传送卷轴的定点传送，不手写位移：它负责连通性检查（不能传进隔开的区域）、
				// 离开门格时关门、漂浮时不踩陷阱、解除缠绕、刷新视野。到不了时卡留在手上
				if (cell != hero.pos
						&& !ScrollOfTeleportation.teleportToLocation(hero, cell, false)) {
					return;
				}
				// 落点上的陷阱可能把英雄杀死，死了就不再结算这张卡（否则施法动作会盖在死亡动画上）
				if (!hero.isAlive()) return;
				CardDrawer.cancelCooldown(hero);
				int swift = hero.pointsInTalent(Talent.SWIFT_STEP);
				if (swift > 0) {
					Buff.affect(hero, Haste.class, swift);
					CardDrawer.reduceCooldown(hero, swift);
				}
				finish(hero, card, true, 0f);
				// 传送卡变回原卡
				if (hero.cardDeck.stashedCard != null) {
					hero.cardDeck.held = hero.cardDeck.stashedCard;
					hero.cardDeck.stashedCard = null;
					HeldCardTracker.sync(hero);
					ActionIndicator.refresh();
				}
				if (hero.armorAbility instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cardtrickster.CardBarrage) {
					((com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cardtrickster.CardBarrage) hero.armorAbility)
							.grantRepeat(hero.pointsInTalent(Talent.EMERGENCY_REPEAT));
				}
				break;
			}
		}
	}

	/**
	 * 收藏家：空白卡指定自己，选择一件已鉴定非诅咒的武器/护甲，把这张空白卡就地变成攻击/防御卡。
	 */
	public static void collectorTransform(Hero hero, Card card) {
		GameScene.selectItem(new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return Messages.get(CardEffects.class, "collect_prompt");
			}

			@Override
			public boolean itemSelectable(Item item) {
				return item.isIdentified() && !item.cursed
						&& (item instanceof MeleeWeapon || item instanceof Armor);
			}

			@Override
			public void onSelect(Item item) {
				if (item == null) return;
				if (hero.cardDeck == null || hero.cardDeck.held != card) return;

				if (item.isEquipped(hero) && item instanceof EquipableItem) {
					// [audit x-turns IM-5 trial fix] single = true：卸装备当场花掉这 1 回合（spendAndNext），而不是只记账、英雄还能接着行动
					if (!((EquipableItem) item).doUnequip(hero, false, true)) return;
				}
				item.detachAll(hero.belongings.backpack);

				if (item instanceof MeleeWeapon) {
					card.type = CardType.ATTACK;
					card.gearTier = ((MeleeWeapon) item).tier;
					card.gearLevel = item.trueLevel();
					card.enchantment = ((Weapon) item).enchantment;
				} else {
					card.type = CardType.DEFENSE;
					card.gearTier = ((Armor) item).tier;
					card.gearLevel = item.trueLevel();
				}
				GLog.i(Messages.get(CardEffects.class, "collected", item.name(), card.name()));
				ActionIndicator.refresh();
				Item.updateQuickslot();
			}
		});
	}

	/** 随机取一个陷阱类型（卡组连携 +3）。优先用当前楼层已生成的陷阱。 */
	private static Class<?> randomTrapClass() {
		java.util.ArrayList<Class<?>> types = new java.util.ArrayList<>();
		for (Trap t : Dungeon.level.traps.values()) {
			if (t != null && !types.contains(t.getClass())) types.add(t.getClass());
		}
		if (types.isEmpty()) {
			// 退化：给一组常见陷阱
			types.add(com.shatteredpixel.shatteredpixeldungeon.levels.traps.AlarmTrap.class);
			types.add(com.shatteredpixel.shatteredpixeldungeon.levels.traps.ToxicTrap.class);
			types.add(com.shatteredpixel.shatteredpixeldungeon.levels.traps.BurningTrap.class);
			types.add(com.shatteredpixel.shatteredpixeldungeon.levels.traps.ChillingTrap.class);
		}
		return types.get(Random.Int(types.size()));
	}

	/** 给英雄身上所有法杖与神器充能（卡组连携 +1）。 */
	private static void rechargeWandsAndArtifacts(Hero hero, float amount) {
		if (hero == null) return;
		com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand wand =
				hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand.class);
		if (wand != null) wand.gainCharge(amount);
		for (com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact art
				: hero.belongings.getAllItems(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact.class)) {
			if (art != null) art.charge(hero, amount);
		}
	}

	private static void triggerTrap(Card card, int pos) {
		if (card.trapClass == null) return;
		try {
			Trap trap = (Trap) card.trapClass.getDeclaredConstructor().newInstance();
			trap.set(pos);
			trap.fromJokerCard = true;
			trap.activate();
			GLog.i(Messages.get(CardEffects.class, "trap_released", trap.name()));
		} catch (Exception e) {
			GLog.w(Messages.get(CardEffects.class, "trap_failed"));
		}
	}

	// 陷阱精通：会自己找目标打的陷阱算「指向性」，其余算「非指向性」
	static boolean isDirectionalTrap(Class<?> cls) {
		return cls != null && (PoisonDartTrap.class.isAssignableFrom(cls) || cls == WornDartTrap.class
				|| cls == DisintegrationTrap.class || cls == GrimTrap.class);
	}

	static boolean isSummoningTrap(Class<?> cls) {
		return cls == SummoningTrap.class || cls == FlockTrap.class || cls == GuardianTrap.class;
	}

	static boolean isTeleportTrap(Class<?> cls) {
		return cls == TeleportationTrap.class || cls == WarpingTrap.class || cls == GatewayTrap.class;
	}

	private static void trapMasteryAfterPlay(Hero hero, Class<?> cls) {
		int mastery = hero.pointsInTalent(Talent.TRAP_MASTERY);
		if (mastery >= 3 && isSummoningTrap(cls)) {
			for (Char ch : Actor.chars()) {
				if (ch.alignment == Char.Alignment.ENEMY && ch.isAlive() && Dungeon.level.heroFOV[ch.pos]) {
					Buff.prolong(ch, Blindness.class, 5f);
				}
			}
		}
		if (mastery >= 4 && isTeleportTrap(cls)) {
			Buff.prolong(hero, MindVision.class, 5f);
		}
	}

	private static int trapCooldown(Hero hero) {
		int cd = 25;
		int craft = hero.pointsInTalent(Talent.TRAP_CRAFT);
		if (craft > 0) {
			HashSet<Class<?>> types = new HashSet<>();
			for (Card c : hero.cardDeck.allCards()) {
				if (c.type == CardType.TRAP && c.trapClass != null) types.add(c.trapClass);
			}
			int min = craft == 1 ? 20 : craft == 2 ? 15 : 10;
			cd = Math.max(min, 25 - types.size() * craft);
		}
		return cd;
	}

	private static void dealCardDamage(Hero hero, Char ch, Card card, int dmg) {
		// 伤害型卡片等效投掷武器：相邻命中下降、远距离命中加成；不享受神射戒指。
		float acc = Dungeon.level.adjacent(hero.pos, ch.pos) ? 0.5f : 1.5f;
		if (!Char.hit(hero, ch, acc, false)) {
			ch.sprite.showStatus(CharSprite.NEUTRAL, Messages.get(CardEffects.class, "miss"));
			return;
		}

		// 伏击加成：隐身攻击时按准备等级提升伤害
		boolean sneaking = hero.invisible > 0 && hero.canSurpriseAttack();
		if (sneaking) {
			float bonus = 0.10f; // 默认最低档
			Preparation prep = hero.buff(Preparation.class);
			if (prep != null) {
				// attackLevel() 返回 1~4 的准备等级
				int lvlIndex = Math.max(1, Math.min(4, prep.attackLevel()));
				bonus = new float[]{0.10f, 0.20f, 0.35f, 0.50f}[lvlIndex - 1];
			}
			dmg = Math.round(dmg * (1f + bonus));
			ch.sprite.showStatus(CharSprite.POSITIVE, Messages.get(CardEffects.class, "ambush"));
		}

		ch.damage(new DamageInfo(dmg, DamageType.PHYSICAL, hero, null, card));

		// 让目标走一次受击反应（蝇群分裂等逻辑挂在 defenseProc 里）
		if (ch.isAlive()) {
			ch.defenseProc(hero, dmg);
		}

		if (!ch.isAlive()) {
			BoneToGold gold = hero.buff(BoneToGold.class);
			if (gold != null) gold.onCardKill(hero, ch.pos);
		}
	}

	private static void finish(Hero hero, Card card, boolean exhaust, float time) {
		if (hero.cardDeck == null || hero.cardDeck.held != card) return;

		// 卡牌护体：打出任意卡获得不可叠加护盾
		int prot = hero.pointsInTalent(Talent.BLANK_PROTECTION);
		if (prot > 0) {
			int shield = prot == 1 ? 2 : 3;
			Barrier barrier = Buff.affect(hero, Barrier.class);
			if (barrier.shielding() < shield) {
				barrier.setShield(shield);
				hero.sprite.showStatus(CharSprite.POSITIVE, "+" + shield);
			}
		}

		// 僵化/激素强化：每打出一张卡，前置一格
		if (card.type != CardType.RIGID && card.type != CardType.HORMONE) {
			advanceEnhanced(hero);
		}

		hero.sprite.operate(hero.pos);
		if (time > 0) hero.busy();
		if (exhaust) {
			hero.cardDeck.held = null;
		} else if (card.fromStorage) {
			card.fromStorage = false;
			int eff = hero.pointsInTalent(Talent.EFFECTIVE_USE);
			if (eff > 0 && Random.Float() < 0.1f * eff) {
				hero.cardDeck.held = null;
				hero.cardDeck.stored.add(card);
			} else {
				hero.cardDeck.playDone(card);
			}
		} else if (hero.pointsInTalent(Talent.FANCY_SHUFFLE) >= 3
				&& (card.type == CardType.SPIN || card.type == CardType.GRAND_FINALE
					|| card.type == CardType.PREPARED)) {
			// 花式切牌 +3：打出后随机插入牌组
			hero.cardDeck.held = null;
			hero.cardDeck.insertRandom(card);
		} else {
			hero.cardDeck.playDone(card);
		}
		HeldCardTracker.clear(hero);
		ActionIndicator.refresh();
		Item.updateQuickslot();
		hero.spendAndNext(time);
	}

	private static void advanceEnhanced(Hero hero) {
		ArrayList<Card> pile = hero.cardDeck.drawPile;
		for (int i = pile.size() - 2; i >= 0; i--) {
			Card c = pile.get(i);
			if ((c.type == CardType.RIGID || c.type == CardType.HORMONE) && c.power >= 2) {
				pile.remove(i);
				pile.add(i + 1, c);
			}
		}
	}

	private static float foodTime(Hero hero, Card card) {
		return hero.pointsInTalent(Talent.ADVANCED_FOOD) >= 1 ? 0f : card.type.time;
	}

	private static void eatCard(Hero hero, Card card, float energy) {
		if (card.type == CardType.SMALL_RATION && hero.buff(RationCooldown.class) != null) {
			GLog.w(Messages.get(CardEffects.class, "ration_cooling"));
			return;
		}
		float val = energy;
		if (Dungeon.isChallenged(Challenges.NO_FOOD)) val /= 3f;
		Buff.affect(hero, Hunger.class).satisfy(val);
		if (card.type == CardType.SMALL_RATION) {
			Buff.affect(hero, RationCooldown.class, 100f);
		}
		if (hero.pointsInTalent(Talent.ADVANCED_FOOD) >= 2) {
			CardDrawer.cancelCooldown(hero);
		}
		hero.sprite.operate(hero.pos);
		SpellSprite.show(hero, SpellSprite.FOOD);
		GLog.i(Messages.get(CardEffects.class, "eaten", (int) energy));
	}

	/** 临时管理：把存储槽内的卡打出。 */
	public static void playStored(Hero hero, Card card) {
		CardDeck deck = hero.cardDeck;
		if (deck == null || card == null || deck.held != null) return;
		if (!deck.stored.contains(card)) return;
		deck.stored.remove(card);
		deck.held = card;
		card.fromStorage = true;
		HeldCardTracker.sync(hero);
		CardTargeter.play(hero, card);
	}

	/** 存储槽到期：把存储的卡放回牌堆底（不等玩家选目标）。 */
	public static void releaseStored(Hero hero) {
		CardDeck deck = hero.cardDeck;
		if (deck == null || deck.stored.isEmpty()) return;
		// 逐张放回牌堆底，避免手上已有卡时被阻塞
		for (Card c : new ArrayList<>(deck.stored)) {
			deck.stored.remove(c);
			deck.addToBottom(c);
			GLog.i(Messages.get(CardEffects.class, "storage_to_bottom", c.name()));
		}
		HeldCardTracker.sync(hero);
		ActionIndicator.refresh();
	}

	/** 花式切牌 +1：空转出的空白卡中随机一张打到视野内随机敌人。 */
	public static void fancySpin(Hero hero, int spunBlanks) {
		if (spunBlanks <= 0 || hero.pointsInTalent(Talent.FANCY_SHUFFLE) < 1) return;
		ArrayList<Char> targets = new ArrayList<>();
		for (Char c : Actor.chars()) {
			if (c.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[c.pos]) targets.add(c);
		}
		if (targets.isEmpty()) return;
		Char ch = targets.get(Random.Int(targets.size()));
		int dmg = Random.NormalIntRange(1, 5);
		int emp = hero.pointsInTalent(Talent.BLANK_EMPOWERMENT);
		if (emp == 1) dmg += Random.NormalIntRange(0, 2);
		else if (emp == 2) dmg += Random.NormalIntRange(1, 3);
		int wind = hero.pointsInTalent(Talent.BORROW_WIND_CUT);
		if (wind > 0) {
			int dist = Math.min(4, Math.round(Dungeon.level.trueDistance(hero.pos, ch.pos)));
			dmg += dist * (wind == 2 ? Random.NormalIntRange(2, 3) : Random.NormalIntRange(1, 2));
		}
		dealCardDamage(hero, ch, null, dmg);
		GLog.i(Messages.get(CardEffects.class, "fancy_spin", ch.name()));
	}

	/** 陷阱师「灵力协调」：使用法杖后减少陷阱卡与抽卡冷却。 */
	public static void onWandUsed(Hero hero) {
		int p = hero.pointsInTalent(Talent.SPIRIT_COORDINATION);
		if (p <= 0) return;
		CardDrawer.reduceCooldown(hero, p);
		TrapCooldown cd = hero.buff(TrapCooldown.class);
		if (cd != null) {
			cd.reduce(p);
		}
	}

	public static void onDeckCycled(Hero hero) {		if (hero.subClass != HeroSubClasses.SPIN_MASTER || hero.cardDeck == null) return;
		int dmg = Math.max(1, hero.lvl);
		for (Char ch : Actor.chars()) {
			if (ch.alignment == Char.Alignment.ENEMY && Dungeon.level.heroFOV[ch.pos]) {
				ch.damage(new DamageInfo(dmg, DamageType.PHYSICAL_NO_ARMOR, hero, null, hero.cardDeck));
			}
		}
		GLog.i(Messages.get(CardEffects.class, "cycle_damage", dmg));
	}

	public static void identifyRandom(Hero hero, int count) {
		identifyRandom(hero, count, true);
	}

	private static void identifyRandom(Hero hero, int count, boolean createWhenEmpty) {
		for (int i = 0; i < count; i++) {
			Item item = findUnidentified(hero);
			if (item == null) {
				if (!createWhenEmpty) {
					GLog.i(Messages.get(CardEffects.class, "identified"));
					continue;
				}
				int roll = Random.Int(3);
				Generator.Category cat = roll == 0 ? Generator.Category.SCROLL
						: roll == 1 ? Generator.Category.POTION : Generator.Category.RING;
				item = Generator.random(cat);
				if (item != null) {
					// 先记录名字再鉴定，便于提示玩家具体是什么
					String itemName = item.name();
					item.identify();
					if (!item.collect(hero.belongings.backpack)) {
						Dungeon.level.drop(item, hero.pos).sprite.drop();
					}
					GLog.p(Messages.get(CardEffects.class, "identified_one", item.name()));
					continue;
				}
			} else {
				item.identify();
				GLog.p(Messages.get(CardEffects.class, "identified_one", item.name()));
				continue;
			}
			GLog.i(Messages.get(CardEffects.class, "identified"));
		}
	}

	private static Item findUnidentified(Hero hero) {
		for (Item item : hero.belongings) {
			if (!item.isIdentified() && (item instanceof Scroll || item instanceof Potion || item instanceof Ring)) {
				return item;
			}
		}
		return null;
	}

	private static float weaponAverage(Card card) {
		if (card.gearTier <= 0) return 0;
		int tier = Math.max(1, card.gearTier);
		int min = tier + card.gearLevel;
		int max = 5 * (tier + 1) + card.gearLevel * (tier + 1);
		return (min + max) / 2f;
	}

	private static float armorMax(Card card) {
		if (card.gearTier <= 0) return 0;
		return card.gearTier * (2 + card.gearLevel);
	}

	/** 临时管理：存储槽计时。 */
	public static class CardStorage extends FlavourBuff {
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.45f, 0.65f, 1f); }

		@Override
		public boolean act() {
			if (cooldown() <= 0) {
				releaseStored((Hero) target);
				detach();
				return true;
			}
			spend(TICK);
			return true;
		}
	}

	/** 点骨成金：卡牌击杀额外掉落。 */
	public static class BoneToGold extends FlavourBuff {
		public String icon() { return BuffIndicator.UPGRADE; }
		public void tintIcon(Image icon) { icon.hardlight(1f, 0.85f, 0.3f); }

		public int wealthLevel(Hero hero) {
			return 2 + hero.pointsInTalent(Talent.HEAVY_REWARD);
		}

		public void onCardKill(Hero hero, int pos) {
			int gold = Math.max(1, Dungeon.depth * 6 * wealthLevel(hero));
			Dungeon.level.drop(new com.shatteredpixel.shatteredpixeldungeon.items.Gold(gold), pos).sprite.drop();
			int heal = hero.pointsInTalent(Talent.EXTRA_BLESSING);
			if (heal > 0) {
				int amount = Math.round(hero.HT * (0.02f + 0.01f * heal));
				hero.HP = Math.min(hero.HT, hero.HP + amount);
				hero.sprite.showStatus(CharSprite.POSITIVE, "+" + amount);
			}
		}
	}

	/** 小口粮卡的 100 回合内部冷却。 */	public static class RationCooldown extends FlavourBuff {
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.8f, 0.6f, 0.2f); }
	}

	/** 防御卡护盾取高者覆盖用。 */
	public static class NextAttackFree extends Buff {
		public boolean consumed;

		@Override
		public boolean act() {
			spend(TICK);
			return true;
		}

		public boolean consume() {
			if (consumed) return false;
			consumed = true;
			detach();
			return true;
		}

		@Override
		public String icon() { return BuffIndicator.NONE; }
	}

	/** 陷阱卡独立冷却。 */
	public static class TrapCooldown extends FlavourBuff {
		public String icon() { return BuffIndicator.TIME; }
		public void tintIcon(Image icon) { icon.hardlight(0.45f, 0.3f, 0.1f); }
		public float iconFadePercent() { return Math.max(0, visualcooldown() / 25f); }

		public void reduce(float amount) {
			if (cooldown() <= amount) detach();
			else spend(-amount);
		}
	}

	/** 危险感知（隧穿凝视 +4）。 */
	public static class DangerSense extends FlavourBuff {
		public String icon() { return BuffIndicator.MIND_VISION; }
		@Override
		public boolean act() {
			for (int cell = 0; cell < Dungeon.level.length(); cell++) {
				Trap t = Dungeon.level.traps.get(cell);
				if (t != null && Dungeon.level.heroFOV[cell]) {
					t.reveal();
				}
			}
			return super.act();
		}
	}

	/** 卡牌刻印强化（旧幻术师遗留，防御卡不再使用，保留以兼容旧存档）。 */
	public static class CardGlyphBoost extends FlavourBuff {
		private float strength = 1.5f;

		{
			type = buffType.POSITIVE;
		}

		public CardGlyphBoost setStrength(float value) {
			strength = value;
			return this;
		}

		public float strength() {
			return strength;
		}

		@Override
		public String icon() {
			return BuffIndicator.ARMOR;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.54f, 0.23f, 0.72f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", Math.round(strength * 100));
		}

		private static final String STRENGTH = "strength";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(STRENGTH, strength);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			strength = bundle.getFloat(STRENGTH);
		}
	}

	/**
	 * 以牙还牙：使用防御卡后，按天赋等级把受到的伤害按比例存入。
	 * 下一次攻击卡命中时一并打出。
	 */
	public static class Vendetta extends Buff {

		{
			type = buffType.POSITIVE;
			announced = false;
		}

		/** 已存储的伤害。 */
		public int stored = 0;

		/** 存储比例：0.15 / 0.30 / 0.45 / 0.60。 */
		public float ratio = 0.15f;

		@Override
		public void onDamageTaken(int dealt, DamageInfo info) {
			if (dealt <= 0) return;
			stored += Math.max(1, Math.round(dealt * ratio));
		}

		/** 取出并清空，用于攻击卡结算。 */
		public int take() {
			int v = stored;
			stored = 0;
			return v;
		}

		@Override
		public String icon() {
			return BuffIndicator.INVERT_MARK;
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", Math.round(ratio * 100), stored);
		}

		private static final String STORED = "stored";
		private static final String RATIO = "ratio";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(STORED, stored);
			bundle.put(RATIO, ratio);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			stored = bundle.getInt(STORED);
			ratio = bundle.getFloat(RATIO);
		}
	}

	/**
	 * 背水一战：攻击卡打出时，若牌堆顶仍是攻击卡，追加若干次额外攻击。
	 * 效果直接写在 ATTACK 结算里，不需要独立 Buff。
	 */
}
