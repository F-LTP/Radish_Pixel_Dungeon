package com.shatteredpixel.shatteredpixeldungeon.items.toys;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.TieredToyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.watabou.utils.Bundle;

public abstract class TieredToy extends ItemArmorAttachable {

	private final int tier;
	private final String id;

	protected TieredToy(int tier, String id, String image) {
		this.tier = tier;
		this.id = id;
		sndImageName = image;
	}

	public int tier() {
		return tier;
	}

	public String id() {
		return id;
	}

	@Override
	public String name() {
		return Messages.get(TieredToy.class, id + "_name");
	}

	@Override
	public String desc() {
		return Messages.get(TieredToy.class, id + "_desc");
	}

	@Override
	public void applyEffect(Hero hero) {
		Buff.affect(hero, TieredToyBuff.class);
		if (this instanceof Antivenom) {
			Buff.detach(hero, com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison.class);
			Buff.detach(hero, TieredToyEffects.TieredToyPoison.class);
		}
		hero.updateHT(false);
	}

	@Override
	public void removeEffect(Hero hero) {
		if (hero != null && !TieredToyEffects.hasAnyTieredToy(hero)) {
			Buff.detach(hero, TieredToyBuff.class);
		}
		if (hero != null) hero.updateHT(false);
	}

	public static class Buckler extends TieredToy { public Buckler(){super(2,"buckler","buckler");} }
	public static class TwinDaggers extends TieredToy { public TwinDaggers(){super(2,"twin_daggers","twin-daggers");} }
	public static class BlessedWater extends TieredToy { public BlessedWater(){super(2,"blessed_water","blessed-water");} }
	public static class Spinach extends TieredToy { public Spinach(){super(2,"spinach","spinach");} }
	public static class Terrarium extends TieredToy { public Terrarium(){super(2,"terrarium","terrarium");} }

	public static class Harpoon extends TieredToy { public Harpoon(){super(3,"harpoon","harpoon");} }
	public static class EnchantedShield extends TieredToy { public EnchantedShield(){super(3,"enchanted_shield","enchanted-shield");} }
	public static class IronPendant extends TieredToy { public IronPendant(){super(3,"iron_pendant","iron-pendant");} }
	public static class Shortsword extends TieredToy { public Shortsword(){super(3,"shortsword","shortsword");} }
	public static class BloodChalice extends TieredToy { public BloodChalice(){super(3,"blood_chalice","blood-chalice");} }

	public static class CrackedPlate extends TieredToy {
		private static final String LOST_MAX_HEALTH = "lost_max_health";
		private int lostMaxHealth;
		private transient boolean active;

		public CrackedPlate(){ super(4,"cracked_plate","cracked-plate"); }

		@Override
		public void applyEffect(Hero hero) {
			Buff.affect(hero, TieredToyBuff.class);
			if (!active) {
				int oldMaxHealth = hero.HT;
				active = true;
				hero.updateHT(false);
				if (lostMaxHealth == 0) lostMaxHealth = Math.max(0, oldMaxHealth - hero.HT);
			}
		}

		@Override
		public void removeEffect(Hero hero) {
			if (hero != null && active) {
				active = false;
				hero.updateHT(false);
				lostMaxHealth = 0;
			}
			if (hero != null && !TieredToyEffects.hasAnyTieredToy(hero)) Buff.detach(hero, TieredToyBuff.class);
		}

		public boolean active() { return active; }
		public int lostMaxHealth() { return lostMaxHealth; }

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(LOST_MAX_HEALTH, lostMaxHealth);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			lostMaxHealth = bundle.getInt(LOST_MAX_HEALTH);
			active = false;
		}
	}
	public static class LifeBolt extends TieredToy { public LifeBolt(){super(4,"life_bolt","life-bolt");} }
	public static class SplittingArrows extends TieredToy { public SplittingArrows(){super(4,"splitting_arrows","splitting-arrows");} }
	public static class HissingRing extends TieredToy { public HissingRing(){super(4,"hissing_ring","hissing-ring");} }
	public static class Antivenom extends TieredToy { public Antivenom(){super(4,"antivenom","antivenom");} }

	public static class Ambrosia extends TieredToy { public Ambrosia(){super(5,"ambrosia","ambrosia");} }
	public static class Nunchaku extends TieredToy { public Nunchaku(){super(5,"nunchaku","nunchaku");} }
	public static class Sponge extends TieredToy { public Sponge(){super(5,"sponge","sponge");} }
	public static class MiniCrossbow extends TieredToy { public MiniCrossbow(){super(5,"mini_crossbow","mini-crossbow");} }
	public static class Doomblade extends TieredToy { public Doomblade(){super(5,"doomblade","doom-blade");} }
	public static class BagOfHolding extends TieredToy { public BagOfHolding(){super(5,"bag_of_holding","bag-of-holding");} }

	public static class BlindingBolt extends TieredToy { public BlindingBolt(){super(6,"blinding_bolt","blinding-bolt");} }
	public static class Hourglass extends TieredToy { public Hourglass(){super(6,"hourglass","hourglass");} }
	public static class GhostShield extends TieredToy { public GhostShield(){super(6,"ghost_shield","ghost-shield");} }
	public static class ToothNecklace extends TieredToy { public ToothNecklace(){super(6,"tooth_necklace","tooth-necklace");} }
	public static class Determination extends TieredToy { public Determination(){super(6,"determination","determination");} }
}
