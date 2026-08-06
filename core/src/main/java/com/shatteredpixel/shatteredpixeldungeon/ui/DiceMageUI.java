package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

public class DiceMageUI {

    public static final int DARK       = 0x120F17;
    public static final int BLACK      = 0x09070B;
    public static final int PANEL      = 0x211A20;
    public static final int PANEL_ALT  = 0x2A2022;
    public static final int CREAM      = 0xF1E5B5;
    public static final int GOLD       = 0xB59E09;
    public static final int ORANGE     = 0xC45E16;
    public static final int RED        = 0xAD1F1F;
    public static final int BLUE       = 0x217B91;
    public static final int PURPLE     = 0x6A4484;
    public static final int GREEN      = 0x388044;
    public static final int GREY_LINE  = 0x51464D;
    private static final String HEALTH_PIP = "interfaces/health_pip.png";

    // 血量格子尺寸
    private static final int PIP_W = 3;
    private static final int PIP_H = 3;
    private static final int PIP_GAP = 1;
    private static final int PIPS_PER_ROW = 10;
    private static final int PIP_PER_HP = 10; // 每格代表10点血

    public static boolean active() {
        return Dungeon.hero != null && Dungeon.hero.subClass == HeroSubClass.DICE_MAGE;
    }

    public static int itemLineColor(Item item, boolean equipped) {
        if (item == null) {
            return GREY_LINE;
        } else if (item.cursed && item.cursedKnown) {
            return RED;
        } else if (!item.isIdentified()) {
            if ((item instanceof EquipableItem || item instanceof Wand) && item.cursedKnown) {
                return BLUE;
            } else {
                return PURPLE;
            }
        } else if (equipped) {
            return GOLD;
        } else {
            return GREY_LINE;
        }
    }

    public static int optionLineColor(int index) {
        switch (index % 4) {
            case 0: return BLUE;
            case 1: return PURPLE;
            case 2: return GOLD;
            default: return RED;
        }
    }

    // 计算需要的格子数
    public static int pipCount(int max) {
        return (max + PIP_PER_HP - 1) / PIP_PER_HP;
    }

    public static int pipWidth(int count) {
        int columns = Math.min(Math.max(count, 0), PIPS_PER_ROW);
        if (columns == 0) return 0;
        return columns * PIP_W + (columns - 1) * PIP_GAP;
    }

    public static int pipHeight(int count) {
        if (count <= 0) return 0;
        int rows = (count + PIPS_PER_ROW - 1) / PIPS_PER_ROW;
        return rows * PIP_H + (rows - 1) * PIP_GAP;
    }

    public static class HealthPips extends Component {

        private Image[] pips;
        private int lastHp = -1, lastShield = -1, lastMax = -1;
        private int columns = PIPS_PER_ROW;

        public void maxWidth(float maxWidth) {
            int nextColumns = Math.max(1,
                    (int) ((maxWidth + PIP_GAP) / (PIP_W + PIP_GAP)));
            if (columns != nextColumns) {
                columns = nextColumns;
                layout();
            }
        }

        public void level(int hp, int shield, int max) {
            if (hp == lastHp && shield == lastShield && max == lastMax && pips != null) {
                return; // 无变化跳过重绘
            }
            lastHp = hp; lastShield = shield; lastMax = max;
            int filled = pipCount(hp);
            int shldPips = pipCount(shield);
            int count = Math.max(pipCount(max), filled + shldPips);
            if (pips == null || pips.length != count) {
                if (pips != null) {
                    for (Image p : pips) remove(p);
                }
                pips = new Image[count];
                for (int i = 0; i < count; i++) {
                    pips[i] = new Image(HEALTH_PIP);
                    pips[i].hardlight(BLACK);
                    add(pips[i]);
                }
            }

            for (int i = 0; i < count; i++) {
                if (i < filled) {
                    pips[i].hardlight(RED);
                } else if (i < filled + shldPips) {
                    pips[i].hardlight(BLUE);
                } else {
                    pips[i].hardlight(GREY_LINE);
                }
            }
            layout();
        }

        @Override
        protected void layout() {
            if (pips == null) return;
            for (int i = 0; i < pips.length; i++) {
                int column = i % columns;
                int row = i / columns;
                pips[i].x = x + column * (PIP_W + PIP_GAP);
                pips[i].y = y + row * (PIP_H + PIP_GAP);
            }
            int usedColumns = Math.min(pips.length, columns);
            int rows = (pips.length + columns - 1) / columns;
            width = usedColumns == 0 ? 0 : usedColumns * PIP_W + (usedColumns - 1) * PIP_GAP;
            height = rows == 0 ? 0 : rows * PIP_H + (rows - 1) * PIP_GAP;
        }

        public void alpha(float value) {
            if (pips == null) return;
            for (Image pip : pips) {
                pip.alpha(value);
            }
        }
        public void level(com.shatteredpixel.shatteredpixeldungeon.actors.Char c) {
            level(c.HP, c.shielding(), c.HT);
        }
    }

    public static class Frame extends Component {

        private final int fillColor;
        private final int lineColor;
        private ColorBlock fill;
        private ColorBlock top;
        private ColorBlock bottom;
        private ColorBlock left;
        private ColorBlock right;

        public Frame(int fillColor, int lineColor) {
            super();
            this.fillColor = fillColor;
            this.lineColor = lineColor;
            fill.hardlight(fillColor);
            top.hardlight(lineColor);
            bottom.hardlight(lineColor);
            left.hardlight(lineColor);
            right.hardlight(lineColor);
        }

        @Override
        protected void createChildren() {
            fill = new ColorBlock(1, 1, 0xFFFFFFFF);
            add(fill);
            top = new ColorBlock(1, 1, 0xFFFFFFFF);
            add(top);
            bottom = new ColorBlock(1, 1, 0xFFFFFFFF);
            add(bottom);
            left = new ColorBlock(1, 1, 0xFFFFFFFF);
            add(left);
            right = new ColorBlock(1, 1, 0xFFFFFFFF);
            add(right);
        }

        @Override
        protected void layout() {
            fill.x = x;
            fill.y = y;
            fill.size(width, height);

            top.x = x;
            top.y = y;
            top.size(width, 1);

            bottom.x = x;
            bottom.y = y + height - 1;
            bottom.size(width, 1);

            left.x = x;
            left.y = y;
            left.size(1, height);

            right.x = x + width - 1;
            right.y = y;
            right.size(1, height);
        }
        public void alpha(float value) {
            fill.alpha(value);
            top.alpha(value);
            bottom.alpha(value);
            left.alpha(value);
            right.alpha(value);
        }
    }

    // Slice&Dice 风格按钮：黑色底 + 1px 边框 + 居中文字
    public static class DiceButton extends Button {

        private Frame bg;
        private RenderedTextBlock text;

        public DiceButton(String label) {
            super();
            text.text(label);
        }

        public void text(String value) {
            text.text(value);
            layout();
        }

        public void textColor(int value) {
            text.hardlight(value);
        }

        @Override
        protected void createChildren() {
            bg = new Frame(BLACK, GREY_LINE);
            add(bg);

            text = PixelScene.renderTextBlock(8);
            text.hardlight(CREAM);
            add(text);

            super.createChildren();
        }

        @Override
        protected void layout() {
            bg.setRect(x, y, width, height);

            text.setPos(
                x + (width - text.width()) / 2f,
                y + (height - text.height()) / 2f
            );
            PixelScene.align(text);

            super.layout();
        }

        @Override
        protected void onPointerDown() {
            bg.top.hardlight(GOLD);
            bg.bottom.hardlight(GOLD);
            bg.left.hardlight(GOLD);
            bg.right.hardlight(GOLD);
        }

        @Override
        protected void onPointerUp() {
            bg.top.hardlight(GREY_LINE);
            bg.bottom.hardlight(GREY_LINE);
            bg.left.hardlight(GREY_LINE);
            bg.right.hardlight(GREY_LINE);
        }
    }
}
