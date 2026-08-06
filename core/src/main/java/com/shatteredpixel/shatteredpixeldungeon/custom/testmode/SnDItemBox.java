package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
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
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class SnDItemBox extends TestItem {

	private static final String AC_SPAWN = "SPAWN";
	private static final String TIER = "tier";
	private static final String SELECTED = "selected";

	private int tier = 1;
	private int selected;

	{
		image = ItemSpriteSheet.ARMOR_HOLDER;
		defaultAction = AC_SPAWN;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_SPAWN);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_SPAWN)) {
			GameScene.show(new SettingsWindow());
		}
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
}

