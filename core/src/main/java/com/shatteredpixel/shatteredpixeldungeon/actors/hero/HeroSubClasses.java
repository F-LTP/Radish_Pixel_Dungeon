package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.Assassin;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.Battlemage;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.Battlepreist;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.Berserker;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.DeadKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.DiceMage;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.Freerunner;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.Gladiator;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.JutteChampion;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.LittleKnight;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.RedCardinal;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.Sniper;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.Warden;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.Warlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;

import java.util.Arrays;
import java.util.Collection;

/**
 * 子职业管理器 - 集中持有所有子职业单例常量、全量子职业集合与反序列化查找。
 *
 * <p>本类只是各 {@link HeroSubClass} 定义类的<b>持有者/管理器</b>，并非其父类型，
 * 因此可在静态初始化中安全地 {@code new} 各定义类，不存在"超类实例化子类"的类加载循环。</p>
 */
public final class HeroSubClasses {

	private HeroSubClasses(){ /* 工具类，禁止实例化 */ }

	public static final HeroSubClass NONE = new HeroSubClass("NONE", HeroIcon.NONE){
		@Override public Talent[] subclassT3() { return new Talent[0]; }
		@Override public Talent[] subclassT4() { return new Talent[0]; }
	};

	public static final HeroSubClass BERSERKER     = new Berserker();
	public static final HeroSubClass GLADIATOR     = new Gladiator();
	public static final HeroSubClass BATTLEMAGE    = new Battlemage();
	public static final HeroSubClass WARLOCK       = new Warlock();
	public static final HeroSubClass ASSASSIN      = new Assassin();
	public static final HeroSubClass FREERUNNER    = new Freerunner();
	public static final HeroSubClass SNIPER        = new Sniper();
	public static final HeroSubClass WARDEN        = new Warden();
	public static final HeroSubClass BATTLEPREIST  = new Battlepreist();
	public static final HeroSubClass REDCARDINAL   = new RedCardinal();
	public static final HeroSubClass DEAD_KNIGHT   = new DeadKnight();
	public static final HeroSubClass LITTLE_KNIGHT = new LittleKnight();
	public static final HeroSubClass DICE_MAGE     = new DiceMage();
	public static final HeroSubClass JUTTE_CHAMPION= new JutteChampion();
	public static final HeroSubClass CHAMPION      = new HeroSubClass("CHAMPION", HeroIcon.CHAMPION){
		@Override public Talent[] subclassT3() { return new Talent[0]; }
		@Override public Talent[] subclassT4() { return new Talent[0]; }
	};
	public static final HeroSubClass MONK          = new HeroSubClass("MONK", HeroIcon.MONK){
		@Override public Talent[] subclassT3() { return new Talent[0]; }
		@Override public Talent[] subclassT4() { return new Talent[0]; }
	};

	//所有子职业集合
	public static final Collection<HeroSubClass> ALL = Arrays.asList(
			NONE,
			BERSERKER, GLADIATOR,
			BATTLEMAGE, WARLOCK,
			ASSASSIN, FREERUNNER,
			SNIPER, WARDEN,
			BATTLEPREIST, REDCARDINAL, DEAD_KNIGHT,
			LITTLE_KNIGHT, DICE_MAGE, JUTTE_CHAMPION,
			CHAMPION, MONK
	);

	//存档反序列化
	public static HeroSubClass fromSaveName(String name){
		for (HeroSubClass cls : ALL){
			if (cls.name().equals(name)) return cls;
		}
		return NONE;
	}
}
