package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import java.util.ArrayList;

/**
 * 骰子法师施法时魔力不足、但背包法杖充能可覆盖消耗时弹出的确认窗口。
 * 列出将被消耗充能的法杖图标，确认后消耗充能并继续施法。
 */
public class WndDiceMageWandDrain extends Window {

    private static final int WIDTH_P = 120;
    private static final int WIDTH_L = 144;

    private static final int MARGIN = 2;
    private static final int BUTTON_HEIGHT = 18;

    private final MagicPoint mp;
    private final int cost;
    private final DiceMageSpell spell;
    private final Hero hero;

    public WndDiceMageWandDrain(MagicPoint mp, int cost, DiceMageSpell spell) {
        super();
        this.mp = mp;
        this.cost = cost;
        this.spell = spell;
        this.hero = Dungeon.hero;

        int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
        chrome.hardlight(DiceMageUI.DARK);

        RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
        title.hardlight(DiceMageUI.GOLD);
        title.maxWidth(width - MARGIN * 2);
        title.setPos(MARGIN, MARGIN);
        add(title);
        float pos = title.bottom() + 2 * MARGIN;

        RenderedTextBlock message = PixelScene.renderTextBlock(Messages.get(this, "message", cost), 6);
        message.hardlight(DiceMageUI.CREAM);
        message.maxWidth(width - MARGIN * 2);
        message.setPos(MARGIN, pos);
        add(message);
        pos = message.bottom() + 2 * MARGIN;

        // 列出将被消耗充能的法杖图标
        ArrayList<Wand> wands = mp.allWands();
        float iconX = MARGIN;
        float iconSize = 16f;
        for (Wand w : wands) {
            ItemSprite icon = new ItemSprite(w);
            icon.scale.set(iconSize / icon.width());
            icon.x = iconX;
            icon.y = pos;
            PixelScene.align(icon);
            add(icon);
            iconX += iconSize + MARGIN;
        }
        pos += iconSize + 2 * MARGIN;

        RedButton confirm = new RedButton(Messages.get(this, "confirm")) {
            @Override
            protected void onClick() {
                mp.drainWandsFor(cost);
                hide();
                spell.continueCast(hero);
            }
        };
        confirm.setRect(0, pos, width, BUTTON_HEIGHT);
        add(confirm);
        pos += BUTTON_HEIGHT + MARGIN;

        RedButton cancel = new RedButton(Messages.get(this, "cancel")) {
            @Override
            protected void onClick() {
                hide();
            }
        };
        cancel.setRect(0, pos, width, BUTTON_HEIGHT);
        add(cancel);
        pos += BUTTON_HEIGHT + MARGIN;

        resize(width, (int) (pos - MARGIN));
    }
}
