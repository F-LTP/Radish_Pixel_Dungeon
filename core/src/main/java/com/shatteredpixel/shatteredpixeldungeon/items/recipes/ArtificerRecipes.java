package com.shatteredpixel.shatteredpixeldungeon.items.recipes;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionPack;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingChain;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

/**
 * 奇械师的炼金配方。
 *
 * <p>把最多 3 件投掷武器 / 药剂打包成 1 件物品：
 * 投掷武器 → 投掷武器链，药剂 → 投掷药水包。
 * 这两件物品都可以整包投出，一次性使用其中的全部内容物。</p>
 */
public final class ArtificerRecipes {

	private ArtificerRecipes() {
	}

	private static boolean isArtificer() {
		return Dungeon.hero != null
				&& Dungeon.hero.subClass == HeroSubClasses.ARTIFICER;
	}

	private static Item copyOf(Item src) {
		Item copy = Reflection.newInstance(src.getClass());
		if (copy == null) return null;
		copy.quantity(1);
		copy.identify();
		return copy;
	}

	/** 投掷武器 → 投掷武器链 */
	public static class MissileCombine extends Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (!isArtificer() || ingredients.isEmpty() || ingredients.size() > 3) return false;
			if (ingredients.size() == 1 && ingredients.get(0).quantity() < 2) return false;
			for (Item i : ingredients) {
				if (!(i instanceof MissileWeapon) || !i.isIdentified()) return false;
			}
			return true;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return ingredients.size() == 1
					? Math.min(3, ingredients.get(0).quantity())
					: ingredients.size();
		}

		private ThrowingChain packOf(ArrayList<Item> ingredients) {
			ThrowingChain chain = new ThrowingChain();
			int remaining = 3;
			for (Item i : ingredients) {
				int n = Math.min(remaining, i.quantity());
				for (int k = 0; k < n; k++) {
					Item copy = copyOf(i);
					if (copy instanceof MissileWeapon) chain.add((MissileWeapon) copy);
				}
				remaining -= n;
				if (remaining <= 0) break;
			}
			return chain;
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			return packOf(ingredients);
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			ThrowingChain out = packOf(ingredients);
			consume(ingredients);
			return out;
		}
	}

	/** 药剂 → 投掷药水包 */
	public static class PotionCombine extends Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (!isArtificer() || ingredients.isEmpty() || ingredients.size() > 3) return false;
			if (ingredients.size() == 1 && ingredients.get(0).quantity() < 2) return false;
			for (Item i : ingredients) {
				if (!(i instanceof Potion) || !i.isIdentified()) return false;
			}
			return true;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return ingredients.size() == 1
					? Math.min(3, ingredients.get(0).quantity())
					: ingredients.size();
		}

		private PotionPack packOf(ArrayList<Item> ingredients) {
			PotionPack pack = new PotionPack();
			int remaining = 3;
			for (Item i : ingredients) {
				int n = Math.min(remaining, i.quantity());
				for (int k = 0; k < n; k++) {
					Item copy = copyOf(i);
					if (copy instanceof Potion) pack.add((Potion) copy);
				}
				remaining -= n;
				if (remaining <= 0) break;
			}
			return pack;
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			return packOf(ingredients);
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			PotionPack out = packOf(ingredients);
			consume(ingredients);
			return out;
		}
	}

	/** 扣除投入的材料（单堆时最多扣 3 个，多堆时每堆扣 1 个）。 */
	private static void consume(ArrayList<Item> ingredients) {
		if (ingredients.size() == 1) {
			Item only = ingredients.get(0);
			only.quantity(Math.max(0, only.quantity() - Math.min(3, only.quantity())));
		} else {
			for (Item i : ingredients) i.quantity(i.quantity() - 1);
		}
	}
}
