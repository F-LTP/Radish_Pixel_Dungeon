package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchool;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSchools;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.dicemage.DiceMageSpell;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.DiceMageUI;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.RoundedFrame;
import com.shatteredpixel.shatteredpixeldungeon.ui.SNDItems;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.UITheme;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.Random;

import java.util.ArrayList;

/** 骰子法师升级时：从2个候选学派二选一，或随机提升某学派，或跳过。 */
public class WndDiceMageTalentChoice extends Window {

    private static final int WIDTH = 170;
    private static final int PAD = 4;
    private static final int CARD_HEIGHT = 55;
    private static final int BUTTON_HEIGHT = 18;

    // 当前所有已打开的升级窗口（用于选择后刷新其他窗口，以及防止旧窗口堆叠显示陈旧状态）
    private static final ArrayList<WndDiceMageTalentChoice> open = new ArrayList<>();

    private SchoolChoice firstCard = null;
    private SchoolChoice secondCard = null;

    /** 在渲染线程上安全地创建并显示升级窗口（内部涉及文本/UI 组件构建）。 */
    public static void show() {
        Game.runOnRenderThread(() -> {
            // 同一时刻只保留一个升级窗口：先关闭已打开的窗口，避免堆叠出显示陈旧"下一个法术"的窗口
            for (int i = open.size() - 1; i >= 0; i--) {
                open.get(i).hide();
            }
            if (canShow()) {
                GameScene.show(new WndDiceMageTalentChoice());
            }
        });
    }

    public WndDiceMageTalentChoice() {
        chrome.hardlight(DiceMageUI.DARK);

        // 窄屏适配：窗口宽度不能超出 UI 相机可用宽度
        int w = Math.min(WIDTH, PixelScene.uiCamera.width - chrome.marginHor());

        DiceMageSchool first = weightedPick();
        DiceMageSchool second = weightedPick();
        if (second == first) {
            second = weightedPick();
        }

        float cardsBottom = PAD;
        if (first != null) {
            firstCard = new SchoolChoice(first);
            firstCard.setRect(PAD, PAD, w - PAD * 2, CARD_HEIGHT);
            add(firstCard);
            cardsBottom = firstCard.bottom();
        }
        if (second != null) {
            secondCard = new SchoolChoice(second);
            secondCard.setRect(PAD, cardsBottom + PAD, w - PAD * 2, CARD_HEIGHT);
            add(secondCard);
            cardsBottom = secondCard.bottom();
        }

        DiceMageUI.DiceButton random = new DiceMageUI.DiceButton("随机提升某学派") {
            @Override
            protected void onClick() {
                choose(randomAvailableSchool());
            }
        };
        random.textColor(DiceMageUI.CREAM);
        random.setRect(22, cardsBottom + PAD, w - 44, BUTTON_HEIGHT);
        add(random);

        DiceMageUI.DiceButton skip = new DiceMageUI.DiceButton("跳过") {
            @Override
            protected void onClick() {
                skip();
            }
        };
        skip.textColor(DiceMageUI.GREY_LINE);
        skip.setRect(22, random.bottom() + PAD, w - 44, BUTTON_HEIGHT);
        add(skip);

        resize(w, (int) (skip.bottom() + PAD));

        open.add(this);
    }

    @Override
    public void hide() {
        open.remove(this);
        super.hide();
    }

    private static void refreshOpenWindows() {
        for (WndDiceMageTalentChoice w : new ArrayList<>(open)) {
            w.refreshCards();
        }
    }

    private void refreshCards() {
        if (firstCard != null) firstCard.refresh();
        if (secondCard != null) secondCard.refresh();
    }

    public static boolean canShow() {
        Hero hero = Dungeon.hero;
        if (hero == null || hero.subClass != HeroSubClasses.DICE_MAGE) {
            return false;
        }
        return hasAnyAvailableSchool();
    }

    private static boolean hasAnyAvailableSchool() {
        for (DiceMageSchool s : DiceMageSchool.values()) {
            if (DiceMageSchools.canInvest(s)) return true;
        }
        return false;
    }

    private static DiceMageSchool weightedPick() {
        ArrayList<DiceMageSchool> pool = new ArrayList<>();
        for (DiceMageSchool s : DiceMageSchool.values()) {
            if (DiceMageSchools.canInvest(s)) pool.add(s);
        }
        if (pool.isEmpty()) return null;

        float total = 0f;
        for (DiceMageSchool s : pool) total += s.weight;
        float roll = Random.Float(total);
        float acc = 0f;
        for (DiceMageSchool s : pool) {
            acc += s.weight;
            if (roll <= acc) return s;
        }
        return pool.get(pool.size() - 1);
    }

    private static DiceMageSchool randomAvailableSchool() {
        ArrayList<DiceMageSchool> pool = new ArrayList<>();
        for (DiceMageSchool s : DiceMageSchool.values()) {
            if (DiceMageSchools.canInvest(s)) pool.add(s);
        }
        if (pool.isEmpty()) return null;
        return pool.get(Random.Int(pool.size()));
    }

    private void choose(DiceMageSchool school) {
        if (school == null) return;
        Hero hero = Dungeon.hero;
        if (school == DiceMageSchool.SPECIAL) {
            DiceMageSchools.ensureSpecialRolled(hero.buff(MagicPoint.class));
        }
        // 有可用3阶天赋点时消耗它，否则免费升级（记账补偿）
        boolean freeUpgrade = hero.talentPointsAvailable(3) < 1;
        hero.upgradeTalent(school.talent);
        if (freeUpgrade) {
            MagicPoint mp = hero.buff(MagicPoint.class);
            if (mp != null) mp.addFreeSchoolUpgrade();
        }
        hide();
        // 若仍有其他已打开的升级窗口（历史堆叠），刷新其候选法术显示
        refreshOpenWindows();
        // 免费升级一次即可；仅当仍有可用3阶点时才继续弹窗（如灵感药水加成）
        if (!freeUpgrade && hero.talentPointsAvailable(3) > 0) {
            show();
        }
    }

    private void skip() {
        // 跳过不消耗任何点数，留待下次升级再选
        hide();
    }

    @Override
    public void onBackPressed() {
        // 必须通过其中一个选项
    }

    private class SchoolChoice extends Button {

        private final DiceMageSchool school;
        private DiceMageSpell spell;
        private RoundedFrame frame;
        private Image icon;
        private RenderedTextBlock name;
        private RenderedTextBlock desc;
        private ColorBlock divider;

        SchoolChoice(DiceMageSchool school) {
            this.school = school;
            if (school == DiceMageSchool.SPECIAL) {
                DiceMageSchools.ensureSpecialRolled(Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicPoint.class));
            }
            name = PixelScene.renderTextBlock("", 6);
            name.hardlight(DiceMageUI.SKY_BLUE);
            desc = PixelScene.renderTextBlock("", 6);
            desc.hardlight(Window.WHITE);
            refresh();
            add(name);
            add(desc);
        }

        /** 依据当前学派投入点数刷新"即将解锁"的法术（图标、名称、描述）。 */
        void refresh() {
            int nextLevel = Math.min(3, Dungeon.hero.pointsInTalent(school.talent) + 1);
            DiceMageSpell newSpell = DiceMageSchools.spellForLevel(school, nextLevel);
            if (icon != null) {
                remove(icon);
                icon.destroy();
            }
            Image snd = null;
            if (newSpell != null && newSpell.sndImageName() != null) {
                snd = SNDItems.get(newSpell.sndImageName());
            }
            icon = snd != null ? snd : new TalentIcon(school.talent);
            if (snd != null) {
                // 与 TalentIcon(16px×0.65) 显示尺寸一致
                float s = 0.65f * 16f / snd.width();
                icon.scale.set(s);
            } else {
                icon.scale.set(0.65f);
            }
            add(icon);
            spell = newSpell;
            name.text(spell != null ? spell.name() : school.talent.title());
            desc.text(spell != null ? spell.desc() : school.talent.desc());
            layout();
        }

        @Override
        protected void createChildren() {
            frame = UITheme.roundedFrame(DiceMageUI.PANEL, DiceMageUI.GREY_LINE);
            add(frame);
            divider = new ColorBlock(1, 1, 0xFF000000 | DiceMageUI.GREY_LINE);
            add(divider);
            super.createChildren();
        }

        @Override
        protected void layout() {
            super.layout();
            frame.setRect(x, y, width, height);
            float headerBottom = y + 16;
            divider.x = x + 2;
            divider.y = headerBottom;
            divider.size(width - 4, 1);
            icon.x = x + 4;
            icon.y = y + 2;
            name.setPos(x + 18, y + 5);
            desc.maxWidth((int) width - 8);
            desc.setPos(x + 4, headerBottom + 4);
        }

        @Override
        protected void onPointerDown() {
            frame.setLineColor(DiceMageUI.SKY_BLUE);
        }

        @Override
        protected void onPointerUp() {
            frame.setLineColor(DiceMageUI.GREY_LINE);
        }

        @Override
        protected void onClick() {
            choose(school);
        }
    }
}
