package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

/** Equipment and consumables carried by a mob in the Real Intelligence challenge. */
public class MobEquipment {

	private static final String WEAPON = "real_intelligence_weapon";
	private static final String ARMOR = "real_intelligence_armor";
	private static final String ARTIFACT = "real_intelligence_artifact";
	private static final String MISC = "real_intelligence_misc";
	private static final String RING = "real_intelligence_ring";
	private static final String POTION = "real_intelligence_potion";
	private static final String SCROLL = "real_intelligence_scroll";

	private final Mob owner;

	public KindOfWeapon weapon;
	public Armor armor;
	public Artifact artifact;
	public KindofMisc misc;
	public Ring ring;
	public Potion potion;
	public Scroll scroll;

	public MobEquipment(Mob owner) {
		this.owner = owner;
	}

	public void generate() {
		if (owner.attackingWeapon() == null && Random.Int(3) == 0) {
			weapon = Generator.randomWeapon(true);
			weapon.activate(owner);
		}
		if (owner.armor() == null && Random.Int(3) == 0) {
			armor = Generator.randomArmor();
			armor.activate(owner);
		}
		if (Random.Int(4) == 0) misc = activate((Ring) Generator.randomUsingDefaults(Generator.Category.RING));
		if (Random.Int(4) == 0) ring = activate((Ring) Generator.randomUsingDefaults(Generator.Category.RING));
		if (Random.Int(5) == 0) potion = (Potion) Generator.randomUsingDefaults(Generator.Category.POTION);
		if (Random.Int(5) == 0) scroll = (Scroll) Generator.randomUsingDefaults(Generator.Category.SCROLL);
	}

	private <T extends KindofMisc> T activate(T item) {
		if (item != null) item.activate(owner);
		return item;
	}

	public void storeInBundle(Bundle bundle) {
		bundle.put(WEAPON, weapon);
		bundle.put(ARMOR, armor);
		bundle.put(ARTIFACT, artifact);
		bundle.put(MISC, misc);
		bundle.put(RING, ring);
		bundle.put(POTION, potion);
		bundle.put(SCROLL, scroll);
	}

	public void restoreFromBundle(Bundle bundle) {
		weapon = (KindOfWeapon) bundle.get(WEAPON);
		if (weapon != null) weapon.activate(owner);
		armor = (Armor) bundle.get(ARMOR);
		if (armor != null) armor.activate(owner);
		artifact = (Artifact) bundle.get(ARTIFACT);
		misc = activate((KindofMisc) bundle.get(MISC));
		ring = activate((Ring) bundle.get(RING));
		potion = (Potion) bundle.get(POTION);
		scroll = (Scroll) bundle.get(SCROLL);
	}

	public void dropAll() {
		drop(weapon);
		drop(armor);
		drop(artifact);
		drop(misc);
		drop(ring);
		drop(potion);
		drop(scroll);
		weapon = null;
		armor = null;
		artifact = null;
		misc = null;
		ring = null;
		potion = null;
		scroll = null;
	}

	public ArrayList<Item> carriedItems() {
		ArrayList<Item> items = new ArrayList<>();
		if (weapon != null) items.add(weapon);
		if (armor != null) items.add(armor);
		if (artifact != null) items.add(artifact);
		if (misc != null) items.add(misc);
		if (ring != null) items.add(ring);
		if (potion != null) items.add(potion);
		if (scroll != null) items.add(scroll);
		return items;
	}

	private void drop(Item item) {
		if (item != null && Dungeon.level != null) {
			Dungeon.level.drop(item, owner.pos).sprite.drop();
		}
	}
}
