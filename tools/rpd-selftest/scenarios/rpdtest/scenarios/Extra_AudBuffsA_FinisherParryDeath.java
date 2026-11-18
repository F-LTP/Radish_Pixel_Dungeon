package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ShieldBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;

import rpdtest.H;
import rpdtest.Reflect;
import rpdtest.Scenario;

/**
 * audit-buffs-a：终结技「格挡」的 ParryTracker 在 detach 时直接解引用 target.buff(Combo.class)。
 * 英雄在格挡的那一回合里死掉时，身上的状态按挂上去的先后顺序逐个移除，连击先于格挡被移除，这里就是空指针。
 */
public class Extra_AudBuffsA_FinisherParryDeath implements Scenario {

	@Override
	public String describe() {
		return "角斗士用终结技格挡的那一回合被烧死：正常死亡，不崩溃";
	}

	/** 英雄身上每个状态和它还要等多久才行动，用来在记录里看清这一回合的先后顺序。 */
	private static String buffState() {
		StringBuilder sb = new StringBuilder();
		sb.append( "now=" ).append( Actor.now() ).append( " heroCooldown=" ).append( Dungeon.hero.cooldown() ).append( " |" );
		for (Buff b : Dungeon.hero.buffs()) {
			sb.append( ' ' ).append( b.getClass().getSimpleName() ).append( '@' ).append( b.cooldown() );
		}
		return sb.toString();
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.world( "gladiator with 7 combo", () -> {
			Dungeon.hero.subClass = HeroSubClasses.GLADIATOR;
			Combo c = Buff.affect( Dungeon.hero, Combo.class );
			for (int i = 0; i < 7; i++) c.hit();
			Reflect.set( c, "comboTime", 500f );
		} );
		Combo combo = h.get( () -> Dungeon.hero.buff( Combo.class ) );
		h.require( combo != null && h.get( () -> combo.canUseMove( Combo.ComboMove.FINISH ) ), "7 连击时可以使用「终结」" );

		h.input( "combo: finish", () -> combo.useMove( Combo.ComboMove.FINISH ) );
		h.closeWindows();
		h.require( h.get( () -> combo.isFinish ), "选了「终结」之后连击处于终结技状态" );

		h.world( "hero at 1 HP, no shield, no regeneration, on fire", () -> {
			Hero hero = Dungeon.hero;
			for (ShieldBuff s : hero.buffs( ShieldBuff.class )) s.detach();
			// 自然回复和燃烧在同一时刻行动，先后顺序每次运行都不一样：回复先动会先 +1 生命，
			// 燃烧只掷出 1 点伤害时英雄就活下来了。拿掉回复，让这一回合必死
			Buff.detach( hero, Regeneration.class );
			hero.HP = 1;
			Buff.affect( hero, Burning.class ).reignite( hero );
		} );
		h.require( h.get( () -> Dungeon.hero.HP == 1 && Dungeon.hero.shielding() == 0
						&& Dungeon.hero.buff( Burning.class ) != null && Dungeon.hero.buff( Regeneration.class ) == null ),
				"英雄 1 点生命、没有护盾、没有自然回复、正在燃烧" );

		h.note( "before parry: " + h.get( Extra_AudBuffsA_FinisherParryDeath::buffState ) );
		h.input( "combo: finisher parry", () -> combo.useMove( Combo.ComboMove.PARRY ) );
		H.Status st = h.waitReady();

		h.note( "status after the parry turn: " + st + ", hero alive=" + h.heroAlive() + " HP=" + h.get( () -> Dungeon.hero.HP ) );
		h.note( "after parry: " + h.get( Extra_AudBuffsA_FinisherParryDeath::buffState ) );
		h.check( st == H.Status.DEAD && !h.heroAlive(), "英雄在终结格挡的那一回合里被烧死，游戏正常进入死亡流程（实际状态 " + st + "）" );
	}
}
