package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchool;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchools;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.spells.BurstSpell;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.SNDItems;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

import java.util.ArrayList;

/** 骰子法师施法界面：只显示玩家当前拥有的法术。 */
public class WndDiceMageSpells extends Window {

    private static final int WIDTH = 150;
    private static final int MARGIN = 2;
    private static final int BUTTON_HEIGHT = 20;

    public WndDiceMageSpells() {
        float pos = MARGIN;
        chrome.hardlight(DiceMageUI.DARK);

        // 窄屏适配：窗口宽度不能超出 UI 相机可用宽度
        int w = Math.min(WIDTH, PixelScene.uiCamera.width - chrome.marginHor());

        RenderedTextBlock title = PixelScene.renderTextBlock("[DICE] " + Messages.get(this, "title"), 9);
        title.hardlight(DiceMageUI.GOLD);
        title.maxWidth(w - MARGIN * 2);
        title.setPos(MARGIN, pos);
        add(title);
        pos = title.bottom() + MARGIN;

        RenderedTextBlock message = PixelScene.renderTextBlock(Messages.get(this, "message"), 6);
        message.hardlight(DiceMageUI.CREAM);
        message.maxWidth(w - MARGIN * 2);
        message.setPos(MARGIN, pos);
        add(message);
        pos = message.bottom() + MARGIN * 2;

        ArrayList<DiceMageSpell> spells = ownedSpells();

        if (spells.isEmpty()) {
            RenderedTextBlock empty = PixelScene.renderTextBlock(Messages.get(this, "empty"), 6);
            empty.hardlight(DiceMageUI.GREY_LINE);
            empty.maxWidth(w - MARGIN * 2);
            empty.setPos(MARGIN, pos);
            add(empty);
            pos = empty.bottom() + MARGIN;
        }

        for (DiceMageSpell spell : spells) {
            SpellButton btn = new SpellButton(spell) {
                @Override
                protected void onClick() {
                    if (spell.isPassive()) return;
                    hide();
                    spell.cast();
                }
            };
            btn.textColor(DiceMageUI.SKY_BLUE);
            btn.lineColor(spellColor(spell));
            btn.enable(spell.canCast());
            btn.setRect(0, pos, w - BUTTON_HEIGHT - MARGIN, BUTTON_HEIGHT);
            add(btn);

            RedButton info = new RedButton("?") {
                @Override
                protected void onClick() {
                    hide();
                    GameScene.show(new WndMessage(spell.desc()));
                }
            };
            info.textColor(DiceMageUI.CREAM);
            info.setRect(w - BUTTON_HEIGHT, pos, BUTTON_HEIGHT, BUTTON_HEIGHT);
            add(info);

            pos += BUTTON_HEIGHT + MARGIN;
        }

        resize(w, (int) (pos - MARGIN));
    }

    /** 玩家当前拥有的法术：迸发固定拥有 + 每个有天赋点的学派的当前等级法术 + 已"学会"的法术。 */
    private ArrayList<DiceMageSpell> ownedSpells() {
        ArrayList<DiceMageSpell> list = new ArrayList<>();
        ArrayList<Class<? extends DiceMageSpell>> seen = new ArrayList<>();
        list.add(new BurstSpell());
        seen.add(BurstSpell.class);
        for (DiceMageSchool school : DiceMageSchool.values()) {
            int points = Dungeon.hero.pointsInTalent(school.talent);
            if (points <= 0) continue;
            DiceMageSpell spell = DiceMageSchools.spellForLevel(school, points);
            if (spell != null && !seen.contains(spell.getClass())) {
                list.add(spell);
                seen.add(spell.getClass());
            }
        }
        MagicPoint mp = Dungeon.hero.buff(MagicPoint.class);
        if (mp != null) {
            for (Class<? extends DiceMageSpell> c : mp.learnedSpells()) {
                if (seen.contains(c)) continue;
                try {
                    DiceMageSpell spell = c.newInstance();
                    if (spell != null) {
                        list.add(spell);
                        seen.add(c);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return list;
    }

    private int spellColor(DiceMageSpell spell) {
        Talent s = spell.school();
        if (s == Talent.SCHOOL_FIRE) return DiceMageUI.ORANGE;
        if (s == Talent.SCHOOL_BLADES) return DiceMageUI.RED;
        if (s == Talent.SCHOOL_CONJURATION) return DiceMageUI.PURPLE;
        if (s == Talent.SCHOOL_MANA) return DiceMageUI.BLUE;
        if (s == Talent.SCHOOL_BLOOD) return DiceMageUI.RED;
        if (s == Talent.SCHOOL_NATURE) return DiceMageUI.GREEN;
        if (s == Talent.SCHOOL_MEDICAL) return DiceMageUI.GREEN;
        if (s == Talent.SCHOOL_PHYSICAL) return DiceMageUI.CREAM;
        if (s == Talent.SCHOOL_EMERGENCY) return DiceMageUI.GOLD;
        if (s == Talent.SCHOOL_SPECIAL) return DiceMageUI.PURPLE;
        return DiceMageUI.CREAM;
    }

    private static class SpellButton extends RedButton {

        private static final int COST_MARGIN = 3;
        private static final int MAX_COST_WIDTH = 56;
        private final ArrayList<Image> costIcons = new ArrayList<>();

        SpellButton(DiceMageSpell spell) {
            super(spell.isPassive() ? "[" + spell.name() + "]" : spell.name());
            leftJustify = true;
            Image snd = SNDItems.get(spell.sndImageName());
            if (snd != null) {
                snd.scale.set(2f);
                icon(snd);
            } else {
                icon(new TalentIcon(spell.iconTalent()));
            }

            if (!spell.isPassive()) {
                for (int i = 0; i < spell.mpCost(); i++) {
                    Image point = new Image(Assets.Interfaces.MAGIC_POINT);
                    costIcons.add(point);
                    add(point);
                }
            }
        }

        @Override
        protected void layout() {
            super.layout();
            if (costIcons.isEmpty()) return;

            float pointWidth = costIcons.get(0).width();
            float spacing = pointWidth;
            if (costIcons.size() > 1) {
                spacing = Math.min(pointWidth,
                        (MAX_COST_WIDTH - pointWidth) / (costIcons.size() - 1));
            }
            float costWidth = pointWidth + spacing * (costIcons.size() - 1);
            float p = x + width - COST_MARGIN - costWidth;
            for (Image point : costIcons) {
                point.x = p;
                point.y = y + (height - point.height()) / 2f;
                PixelScene.align(point);
                p += spacing;
            }
        }

        @Override
        public void enable(boolean value) {
            super.enable(value);
            for (Image point : costIcons) point.alpha(value ? 1f : 0.3f);
        }
    }
}
