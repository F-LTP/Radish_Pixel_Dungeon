package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchools;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.ItemArmorAttachable;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.OptionSlider;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class SnDFunctions extends TestItem {

	private static final String AC_SPAWN = "SPAWN";
	private static final String AC_INFINITE = "INFINITE_MP";
	private static final String AC_LEARN = "LEARN_SPELL";
	private static final String AC_RECIPE = "RECIPE";

	private static final String TIER = "tier";
	private static final String SELECTED = "selected";

	private int tier = 1;
	private int selected;

	{
		image = ItemSpriteSheet.ARMOR_HOLDER;
		sndImageName = "golden-d6";
		defaultAction = AC_SPAWN;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_SPAWN);
		actions.add(AC_INFINITE);
		actions.add(AC_LEARN);
		actions.add(AC_RECIPE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_SPAWN)) {
			GameScene.show(new SettingsWindow());
		} else if (action.equals(AC_INFINITE)) {
			toggleInfinite(hero);
		} else if (action.equals(AC_LEARN)) {
			if (hero.buff(MagicPoint.class) == null) {
				GLog.w(Messages.get(this, "not_dice_mage"));
				return;
			}
			GameScene.show(new LearnSpellWindow());
		} else if (action.equals(AC_RECIPE)) {
			if (hero.buff(MagicPoint.class) == null) {
				GLog.w(Messages.get(this, "not_dice_mage"));
				return;
			}
			GameScene.show(new RecipeWindow());
		}
	}

	private void toggleInfinite(Hero hero) {
		MagicPoint mp = hero.buff(MagicPoint.class);
		if (mp == null) {
			GLog.w(Messages.get(this, "not_dice_mage"));
			return;
		}
		mp.setInfiniteMana(!mp.infiniteMana());
		GLog.i(Messages.get(this, mp.infiniteMana() ? "infinite_on" : "infinite_off"));
	}

	@SuppressWarnings("unchecked")
	private Class<? extends ItemArmorAttachable>[] classesForTier(int tier) {
		Class<? extends ItemArmorAttachable>[] toys = Armor.toyClassesForTier(tier);
		if (tier != 1) return toys;

		Class<? extends ItemArmorAttachable>[] result = new Class[toys.length + 1];
		System.arraycopy(toys, 0, result, 0, toys.length);
		result[toys.length] = BrokenSeal.class;
		return result;
	}

	private ItemArmorAttachable selectedItem() {
		Class<? extends ItemArmorAttachable>[] classes = classesForTier(tier);
		selected = Math.max(0, Math.min(selected, classes.length - 1));
		return Reflection.newInstance(classes[selected]);
	}

	private void createItem() {
		ItemArmorAttachable item = selectedItem();
		if (item == null) {
			GLog.w(Messages.get(this, "create_failed"));
			return;
		}

		item.identify();
		if (item.collect()) {
			GLog.i(Messages.get(this, "collect_success", item.name()));
		} else {
			GLog.w(Messages.get(this, "no_space"));
		}
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TIER, tier);
		bundle.put(SELECTED, selected);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		tier = Math.max(1, Math.min(bundle.getInt(TIER), Armor.toyTierCount()));
		selected = Math.max(0, bundle.getInt(SELECTED));
	}

	private class SettingsWindow extends Window {

		private static final int WIDTH = 120;
		private static final int BUTTON_SIZE = 17;
		private static final int GAP = 2;
		private static final int COLUMNS = 7;

		private final ArrayList<IconButton> buttons = new ArrayList<>();
		private final OptionSlider tierSlider;
		private final RenderedTextBlock selectedText;
		private final RedButton createButton;

		private Class<? extends ItemArmorAttachable>[] classes;
		private int rows;

		SettingsWindow() {
			tierSlider = new OptionSlider(Messages.get(this, "tier"), "1",
					String.valueOf(Armor.toyTierCount()), 1, Armor.toyTierCount()) {
				@Override
				protected void onChange() {
					tier = getSelectedValue();
					selected = 0;
					rebuildButtons();
					updateSelectedText();
				}
			};
			tierSlider.setSelectedValue(tier);
			add(tierSlider);
			tierSlider.setRect(0, GAP, WIDTH, 24);

			selectedText = PixelScene.renderTextBlock("", 6);
			selectedText.maxWidth(WIDTH);
			add(selectedText);

			createButton = new RedButton(Messages.get(this, "create")) {
				@Override
				protected void onClick() {
					createItem();
				}
			};
			add(createButton);

			rebuildButtons();
			updateSelectedText();
		}

		private void rebuildButtons() {
			for (IconButton button : buttons) {
				button.destroy();
			}
			buttons.clear();

			classes = classesForTier(tier);
			selected = Math.max(0, Math.min(selected, classes.length - 1));
			rows = (classes.length + COLUMNS - 1) / COLUMNS;
			float top = tierSlider.bottom() + GAP;

			for (int i = 0; i < classes.length; i++) {
				final int index = i;
				ItemArmorAttachable item = Reflection.newInstance(classes[i]);

				IconButton button = new IconButton() {
					@Override
					protected void onClick() {
						buttons.get(selected).icon().resetColor();
						selected = index;
						buttons.get(selected).icon().hardlight(0xffbf00);
						updateSelectedText();
						super.onClick();
					}
				};
				Image icon = item == null
						? new ItemSprite(ItemSpriteSheet.SOMETHING)
						: new ItemSprite(item);
				icon.scale.set(1f);
				button.icon(icon);

				int row = i / COLUMNS;
				int rowStart = row * COLUMNS;
				int rowLength = Math.min(COLUMNS, classes.length - rowStart);
				float left = (WIDTH - rowLength * BUTTON_SIZE) / 2f;
				button.setRect(left + (i - rowStart) * BUTTON_SIZE,
						top + row * (BUTTON_SIZE + GAP), BUTTON_SIZE, BUTTON_SIZE);
				add(button);
				buttons.add(button);
			}

			buttons.get(selected).icon().hardlight(0xffbf00);
			layout();
		}

		private void updateSelectedText() {
			ItemArmorAttachable item = selectedItem();
			selectedText.text(Messages.get(this, "selected", item == null ? "?" : item.name()));
			layout();
		}

		private void layout() {
			tierSlider.setRect(0, GAP, WIDTH, 24);
			float gridBottom = tierSlider.bottom() + GAP
					+ rows * BUTTON_SIZE + Math.max(0, rows - 1) * GAP;
			selectedText.setPos(0, gridBottom + GAP);
			createButton.setRect(0, selectedText.bottom() + GAP, WIDTH, 16);
			resize(WIDTH, (int) createButton.bottom() + GAP);
		}
	}

	/** 学习骰子法师列表中任意一个法术，无视天赋限制。 */
	private class LearnSpellWindow extends Window {

		private static final int WIDTH = 140;
		private static final int HEIGHT = 160;
		private static final int ITEM_HEIGHT = 18;
		private static final int GAP = 1;
		// 为右侧滚动条留出空间
		private static final int CONTENT_W = WIDTH - 6;

		LearnSpellWindow() {
			chrome.hardlight(0x222222);
			resize(WIDTH, HEIGHT);

			ScrollPane scrollingList = new ScrollPane(new Component());
			add(scrollingList);

			Component content = scrollingList.content();
			int y = 0;

			RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
			title.maxWidth(CONTENT_W);
			title.setPos(0, 2);
			title.hardlight(0xFFFFFF);
			content.add(title);
			y = (int) title.bottom() + GAP;

			RenderedTextBlock hint = PixelScene.renderTextBlock(Messages.get(this, "hint"), 6);
			hint.maxWidth(CONTENT_W);
			hint.setPos(0, y);
			hint.hardlight(0xCCCCCC);
			content.add(hint);
			y = (int) hint.bottom() + GAP * 2;

			MagicPoint mp = Dungeon.hero.buff(MagicPoint.class);
			for (Class<? extends DiceMageSpell> c : DiceMageSchools.allSpells()) {
				DiceMageSpell spell = Reflection.newInstance(c);
				String label = spell != null ? spell.name() : c.getSimpleName();
				boolean already = mp != null && mp.isLearned(c);
				final Class<? extends DiceMageSpell> spellClass = c;
				final String spellLabel = label;

				RedButton btn = new RedButton((already ? "[O] " : "") + label) {
					@Override
					protected void onClick() {
						if (Dungeon.hero == null || Dungeon.hero.buff(MagicPoint.class) == null) {
							GLog.w(Messages.get(SnDFunctions.this, "not_dice_mage"));
							return;
						}
						Dungeon.hero.buff(MagicPoint.class).learnSpell(spellClass);
						GLog.i(Messages.get(SnDFunctions.this, "learned", spellLabel));
						hide();
					}
				};
				if (already) {
					btn.enable(false);
					btn.textColor(0x88CC88);
				}
				btn.leftJustify = true;
				btn.setRect(0, y, CONTENT_W, ITEM_HEIGHT);
				content.add(btn);
				y += ITEM_HEIGHT + GAP;
			}

			content.setRect(0, 0, CONTENT_W, y + 1);
			scrollingList.setRect(0, 0, WIDTH, HEIGHT);
		}
	}

	/** 查看本局魔力药水的炼金配方。 */
	private class RecipeWindow extends Window {

		private static final int WIDTH = 160;
		private static final int GAP = 3;

		RecipeWindow() {
			chrome.hardlight(0x222222);

			RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
			title.maxWidth(WIDTH);
			title.setPos(0, 2);
			title.hardlight(0xFFFFFF);
			add(title);
			float y = title.bottom() + GAP;

			MagicPoint mp = Dungeon.hero.buff(MagicPoint.class);
			if (mp == null) {
				RenderedTextBlock warn = PixelScene.renderTextBlock(Messages.get(SnDFunctions.this, "not_dice_mage"), 6);
				warn.maxWidth(WIDTH);
				warn.setPos(0, y);
				warn.hardlight(0xFF6666);
				add(warn);
				y = warn.bottom() + GAP;
				resize(WIDTH, (int) y);
				return;
			}

			y = addLine(y, Messages.get(this, "potion"), itemName(mp.correctPotion()));
			y = addLine(y, Messages.get(this, "seed"), itemName(mp.correctSeed()));
			y = addLine(y, Messages.get(this, "scroll"), itemName(mp.correctScroll()));
			resize(WIDTH, (int) y);
		}

		private float addLine(float y, String label, String value) {
			RenderedTextBlock line = PixelScene.renderTextBlock(label + ": " + value, 6);
			line.maxWidth(WIDTH);
			line.setPos(0, y);
			line.hardlight(0xCCCCCC);
			add(line);
			return line.bottom() + GAP;
		}

		private String itemName(Class<?> cls) {
			if (cls == null) return "?";
			Object inst = Reflection.newInstance(cls);
			if (inst instanceof com.shatteredpixel.shatteredpixeldungeon.items.Item) {
				return ((com.shatteredpixel.shatteredpixeldungeon.items.Item) inst).name();
			}
			return cls.getSimpleName();
		}
	}
}
