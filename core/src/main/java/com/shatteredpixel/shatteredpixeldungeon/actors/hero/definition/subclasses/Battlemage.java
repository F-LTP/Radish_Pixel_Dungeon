package com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.Game;

public class Battlemage extends HeroSubClass {

	public Battlemage(){
		super("BATTLEMAGE", HeroIcon.BATTLEMAGE);
	}

	@Override public Talent[] subclassT3() {
		return new Talent[]{ Talent.EMPOWERED_STRIKE, Talent.MYSTICAL_CHARGE, Talent.WAR_THROW };
	}
	@Override public Talent[] subclassT4() {
		return new Talent[]{ Talent.WAND_DODGE, Talent.MAGIC_WORKMAN };
	}

	//Include the staff effect description in the battlemage's desc if possible
	@Override public String desc() {
		String desc = Messages.get(HeroSubClass.class, name() + "_desc");
		if (Game.scene() instanceof GameScene){
			MagesStaff staff = Dungeon.hero.belongings.getItem(MagesStaff.class);
			if (staff != null && staff.wandClass() != null){
				desc += "\n\n" + Messages.get(staff.wandClass(), "bmage_desc");
				desc = desc.replaceAll("_", "");
			}
		}
		return desc;
	}
}
