package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

/** Pixel-art rounded rectangle with a separately tintable fill and border. */
public class RoundedFrame extends Component {

    private ColorBlock fill;
    private ColorBlock fillInset;
    private ColorBlock top;
    private ColorBlock bottom;
    private ColorBlock left;
    private ColorBlock right;
    private ColorBlock topLeft;
    private ColorBlock topRight;
    private ColorBlock bottomLeft;
    private ColorBlock bottomRight;

    public RoundedFrame(int fillColor, int lineColor) {
        super();
        setFillColor(fillColor);
        setLineColor(lineColor);
    }

    @Override
    protected void createChildren() {
        fill = new ColorBlock(1, 1, 0xFFFFFFFF);
        add(fill);
        fillInset = new ColorBlock(1, 1, 0xFFFFFFFF);
        add(fillInset);
        top = new ColorBlock(1, 1, 0xFFFFFFFF);
        add(top);
        bottom = new ColorBlock(1, 1, 0xFFFFFFFF);
        add(bottom);
        left = new ColorBlock(1, 1, 0xFFFFFFFF);
        add(left);
        right = new ColorBlock(1, 1, 0xFFFFFFFF);
        add(right);
        topLeft = new ColorBlock(1, 1, 0xFFFFFFFF);
        add(topLeft);
        topRight = new ColorBlock(1, 1, 0xFFFFFFFF);
        add(topRight);
        bottomLeft = new ColorBlock(1, 1, 0xFFFFFFFF);
        add(bottomLeft);
        bottomRight = new ColorBlock(1, 1, 0xFFFFFFFF);
        add(bottomRight);
    }

    @Override
    protected void layout() {
        // Two-pixel corners match the game's pixel-art scale.
        fill.x = x + 1;
        fill.y = y + 2;
        fill.size(Math.max(0, width - 2), Math.max(0, height - 4));

        fillInset.x = x + 2;
        fillInset.y = y + 1;
        fillInset.size(Math.max(0, width - 4), Math.max(0, height - 2));

        top.x = x + 2;
        top.y = y;
        top.size(Math.max(0, width - 4), 1);
        bottom.x = x + 2;
        bottom.y = y + height - 1;
        bottom.size(Math.max(0, width - 4), 1);

        left.x = x;
        left.y = y + 2;
        left.size(1, Math.max(0, height - 4));
        right.x = x + width - 1;
        right.y = y + 2;
        right.size(1, Math.max(0, height - 4));

        topLeft.x = x + 1;
        topLeft.y = y + 1;
        topRight.x = x + width - 2;
        topRight.y = y + 1;
        bottomLeft.x = x + 1;
        bottomLeft.y = y + height - 2;
        bottomRight.x = x + width - 2;
        bottomRight.y = y + height - 2;
    }

    public void setLineColor(int color) {
        top.hardlight(color);
        bottom.hardlight(color);
        left.hardlight(color);
        right.hardlight(color);
        topLeft.hardlight(color);
        topRight.hardlight(color);
        bottomLeft.hardlight(color);
        bottomRight.hardlight(color);
    }

    public void setFillColor(int color) {
        fill.hardlight(color);
        fillInset.hardlight(color);
    }

    public void alpha(float value) {
        fill.alpha(value);
        fillInset.alpha(value);
        top.alpha(value);
        bottom.alpha(value);
        left.alpha(value);
        right.alpha(value);
        topLeft.alpha(value);
        topRight.alpha(value);
        bottomLeft.alpha(value);
        bottomRight.alpha(value);
    }
}
