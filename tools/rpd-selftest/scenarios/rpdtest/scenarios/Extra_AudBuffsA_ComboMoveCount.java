package rpdtest.scenarios;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat;

import rpdtest.H;
import rpdtest.Probe;
import rpdtest.Reflect;
import rpdtest.Scenario;

/**
 * audit-buffs-a：角斗士「每次命中 +1 连击」的子职业钩子现在挂在 Hero.attackProc 里，
 * 战技自己发起的 hero.attack() 也会经过它，而战技内部本来就自己加连击，于是战技命中被算了两次。
 * 最严重的是终结技「连斩」：每打一下扣 1 连击，命中又被钩子加回来，只有打空才真的减少。
 */
public class Extra_AudBuffsA_ComboMoveCount implements Scenario {

	@Override
	public String describe() {
		return "角斗士战技命中只加 1 次连击；7 连击的连斩终结技正好打 7 下";
	}

	@Override
	public void run( H h ) {
		Arena.start( h );
		h.stuckMs = 45000;
		int cell = Arena.c( 11, 10 );
		Rat rat = new Rat();
		rat.HP = rat.HT = 300;
		rat.state = rat.PASSIVE;
		h.spawn( rat, cell );

		h.world( "gladiator with 3 combo", () -> {
			Dungeon.hero.subClass = HeroSubClasses.GLADIATOR;
			Combo c = Buff.affect( Dungeon.hero, Combo.class );
			c.hit();
			c.hit();
			c.hit();
			Reflect.set( c, "comboTime", 500f );
		} );
		Combo combo = h.get( () -> Dungeon.hero.buff( Combo.class ) );
		Hero hero = h.get( () -> Dungeon.hero );
		h.require( h.get( combo::getComboCount ) == 3, "开始时 3 连击" );

		h.input( "combo: crush", () -> combo.useMove( Combo.ComboMove.CRUSH ) );
		h.selectCell( cell );
		int afterCrush = h.get( combo::getComboCount );
		h.check( afterCrush == 4, "必中的「震击」命中 1 次：连击数 3 → 4（实际 " + afterCrush + "）" );

		h.world( "set combo to 7 with 1 move available", () -> {
			Reflect.set( combo, "count", 7 );
			combo.couldUseTime = 1;
			Reflect.set( combo, "comboTime", 500f );
		} );
		h.input( "combo: finish", () -> combo.useMove( Combo.ComboMove.FINISH ) );
		h.closeWindows();
		int from = Probe.hitCount();
		h.input( "combo: fury finisher", () -> combo.useMove( Combo.ComboMove.FURY ) );
		h.selectCell( cell );

		int attacks = Probe.count( hero, rat, from );
		h.note( "after fury finisher: " + Arena.describe( h, rat ) );
		h.check( attacks == 7, "7 连击的「连斩」终结技发起 7 次攻击（实际 " + attacks + " 次；" + Arena.describe( h, rat ) + "）" );
		h.check( h.get( () -> Dungeon.hero.buff( Combo.class ) == null ), "终结技放完后连击重置" );
	}
}
