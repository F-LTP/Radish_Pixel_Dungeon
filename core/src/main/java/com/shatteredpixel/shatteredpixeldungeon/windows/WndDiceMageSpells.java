package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.BlazeSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.CrushSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.CutSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.HealSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.LiquorSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.MiasmaSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.OperateSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.SootheSpell;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndDiceMageSpells extends Window {

    private static final int WIDTH = 150;
    private static final int MARGIN = 2;
    private static final int BUTTON_HEIGHT = 20;

    private final DiceMageSpell[] spells = new DiceMageSpell[]{
            new CutSpell(),
            new HealSpell(),
            new SootheSpell(),
            new LiquorSpell(),
            new OperateSpell(),
            new MiasmaSpell(),
            new CrushSpell(),
            new BlazeSpell()
    };

    public WndDiceMageSpells() {
        float pos = MARGIN;
        chrome.hardlight(DiceMageUI.DARK);

        RenderedTextBlock title = PixelScene.renderTextBlock("[DICE] " + Messages.get(this, "title"), 9);
        title.hardlight(DiceMageUI.GOLD);
        title.maxWidth(WIDTH - MARGIN * 2);
        title.setPos(MARGIN, pos);
        add(title);
        pos = title.bottom() + MARGIN;

        RenderedTextBlock message = PixelScene.renderTextBlock(Messages.get(this, "message"), 6);
        message.hardlight(DiceMageUI.CREAM);
        message.maxWidth(WIDTH - MARGIN * 2);
        message.setPos(MARGIN, pos);
        add(message);
        pos = message.bottom() + MARGIN * 2;

        for (DiceMageSpell spell : spells) {
            RedButton btn = new RedButton(diceLabel(spell)) {
                @Override
                protected void onClick() {
                    hide();
                    spell.cast();
                }
            };
            btn.leftJustify = true;
            btn.textColor(spellColor(spell));
            btn.enable(spell.canCast());
            btn.setRect(0, pos, WIDTH - BUTTON_HEIGHT - MARGIN, BUTTON_HEIGHT);
            add(btn);

            RedButton info = new RedButton("?") {
                @Override
                protected void onClick() {
                    hide();
                    GameScene.show(new WndMessage(spell.desc()));
                }
            };
            info.textColor(DiceMageUI.CREAM);
            info.setRect(WIDTH - BUTTON_HEIGHT, pos, BUTTON_HEIGHT, BUTTON_HEIGHT);
            add(info);

            pos += BUTTON_HEIGHT + MARGIN;
        }

        resize(WIDTH, (int) (pos - MARGIN));
    }

    private String diceLabel(DiceMageSpell spell) {
        // TODO 像SND一样显示带图标的mp消耗
        return " " + spell.name() + "  MP " + spell.mpCost();
    }

    private int spellColor(DiceMageSpell spell) {
        if (spell instanceof CutSpell || spell instanceof CrushSpell) {
            return DiceMageUI.RED;
        } else if (spell instanceof BlazeSpell) {
            return DiceMageUI.ORANGE;
        } else if (spell instanceof HealSpell || spell instanceof SootheSpell) {
            return DiceMageUI.GREEN;
        } else if (spell instanceof LiquorSpell) {
            return DiceMageUI.BLUE;
        } else if (spell instanceof OperateSpell) {
            return DiceMageUI.PURPLE;
        } else if (spell instanceof MiasmaSpell) {
            return DiceMageUI.GREEN;
        } else {
            return DiceMageUI.CREAM;
        }
    }
}
