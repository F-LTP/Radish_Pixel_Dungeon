package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.BladeShield;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatshield;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RoundShield;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class LittleKnight extends HeroSubClass {

	public LittleKnight(){
		super("LITTLE_KNIGHT", HeroIcon.LITTLE_KNIGHT);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.WONT_LOSE, Talent.WET_ENCHANT, Talent.LEFT_BOW_RAPID };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.SHIELD_POKE, Talent.KNIGHT_SPIRIT };
	}

	@Override
	public void onAttackProc( Hero hero, Char enemy, int damage, boolean hit, boolean wasEnemy ) {
		int pts = hero.pointsInTalent( Talent.KNIGHT_SPIRIT );
		if (pts > 0 && hit && wasEnemy && enemy != null && enemy.isAlive()) {
			Buff.affect( hero, KnightSpirit.class ).copyFrom( hero, enemy, pts );
		}
	}

	/** 盾戳战术：小骑士拿着盾牌类武器近战时，攻击变为必定伏击的戳刺。 */
	public static boolean shieldPoke( Char ch ) {
		if (!(ch instanceof Hero) || ch != Dungeon.hero) return false;
		Hero hero = (Hero) ch;
		if (hero.subClass != HeroSubClasses.LITTLE_KNIGHT || !hero.hasTalent( Talent.SHIELD_POKE )) return false;
		KindOfWeapon w = hero.belongings.attackingWeapon();
		return w instanceof RoundShield || w instanceof Greatshield || w instanceof BladeShield;
	}

	/** 戳刺伤害 15~20/25/30/35。 */
	public static int pokeDamage( Hero hero ) {
		return Random.NormalIntRange( 15, 15 + 5 * hero.pointsInTalent( Talent.SHIELD_POKE ) );
	}

	/** 伏击伤害 +5/6/7/8。 */
	public static int ambushBonus( Char attacker ) {
		if (!(attacker instanceof Hero) || ((Hero) attacker).subClass != HeroSubClasses.LITTLE_KNIGHT) return 0;
		int pts = ((Hero) attacker).pointsInTalent( Talent.SHIELD_POKE );
		return pts > 0 ? 4 + pts : 0;
	}

	/** +4 之前戳刺不继承附魔、不吃攻速加成。 */
	public static boolean pokeIsPlain( Char ch ) {
		return shieldPoke( ch ) && ((Hero) ch).pointsInTalent( Talent.SHIELD_POKE ) < 4;
	}

	public static int spiritAccuracy( Hero hero )  { KnightSpirit s = hero.buff( KnightSpirit.class ); return s == null ? 0 : s.accuracy; }
	public static int spiritEvasion( Hero hero )   { KnightSpirit s = hero.buff( KnightSpirit.class ); return s == null ? 0 : s.evasion; }
	public static int spiritDamage( Hero hero )    { KnightSpirit s = hero.buff( KnightSpirit.class ); return s == null ? 0 : s.damage; }
	public static int spiritArmor( Hero hero )     { KnightSpirit s = hero.buff( KnightSpirit.class ); return s == null ? 0 : s.armor; }

	/**
	 * 骑士精神：命中敌人时获得它 25% 的格挡（护甲减伤）、伤害、闪避与命中，持续 1~4 回合；
	 * +4 时只要那名敌人还在攻击范围内就一直持续。
	 */
	public static class KnightSpirit extends Buff {

		{
			type = buffType.POSITIVE;
		}

		public int accuracy, evasion, damage, armor;
		private int sourceId = -1;

		void copyFrom( Hero hero, Char enemy, int pts ) {
			accuracy = Math.round( enemy.attackSkill( hero ) * 0.25f );
			evasion  = Math.round( enemy.defenseSkill( hero ) * 0.25f );
			damage   = Math.round( enemy.damageRoll() * 0.25f );
			armor    = Math.round( enemy.drRoll() * 0.25f );
			sourceId = enemy.id();
			timeToNow();
			spend( pts );
		}

		@Override
		public boolean act() {
			Char src = sourceId == -1 ? null : (Char) Actor.findById( sourceId );
			boolean keep = target instanceof Hero && ((Hero) target).pointsInTalent( Talent.KNIGHT_SPIRIT ) >= 4
					&& src != null && src.isAlive() && ((Hero) target).canAttack( src );
			if (!keep) {
				detach();
				return true;
			}
			spend( TICK );
			return true;
		}

		@Override
		public String icon() {
			return BuffIndicator.BLESS;
		}

		@Override
		public String desc() {
			return com.shatteredpixel.shatteredpixeldungeon.messages.Messages.get( this, "desc", accuracy, evasion, damage, armor );
		}

		private static final String ACC = "accuracy", EVA = "evasion", DMG = "damage", ARM = "armor", SRC = "source";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( ACC, accuracy );
			bundle.put( EVA, evasion );
			bundle.put( DMG, damage );
			bundle.put( ARM, armor );
			bundle.put( SRC, sourceId );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			accuracy = bundle.getInt( ACC );
			evasion = bundle.getInt( EVA );
			damage = bundle.getInt( DMG );
			armor = bundle.getInt( ARM );
			sourceId = bundle.getInt( SRC );
		}
	}
}
