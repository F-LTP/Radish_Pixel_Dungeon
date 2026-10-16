package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.watabou.noosa.NinePatch;

/** Owns the visual state of a button across the normal and themed skins. */
public class ButtonSkin {

    private final NinePatch chrome;
    private final RoundedFrame themedFrame;
    private int lineColor = UITheme.DICE_LINE;
    private float alpha = 1.0f;
    private boolean themedFrameVisible = true;

    public ButtonSkin(Chrome.Type type) {
        chrome = Chrome.get(type);
        themedFrame = UITheme.roundedFrame(UITheme.DICE_FILL, UITheme.DICE_LINE);
        themedFrame.visible = false;
    }

    /** 控制骰子法师主题边框是否显示（用于包裹在卡片内的按钮隐藏自身外框）。 */
    public void setThemedFrameVisible(boolean visible) {
        themedFrameVisible = visible;
    }

    public NinePatch chrome() {
        return chrome;
    }

    public RoundedFrame themedFrame() {
        return themedFrame;
    }

    public void lineColor(int color) {
        lineColor = color;
        themedFrame.setLineColor(color);
    }

    public void layout(float x, float y, float width, float height) {
        chrome.x = x;
        chrome.y = y;
        chrome.size(width, height);

        boolean themed = UITheme.isDiceMage();
        chrome.visible = !themed;
        themedFrame.visible = themed && themedFrameVisible;
        if (themed) {
            themedFrame.setRect(x, y, width, height);
        }
    }

    public void onPointerDown() {
        chrome.brightness(1.2f);
        if (themedFrame.visible) {
            themedFrame.setLineColor(UITheme.DICE_PRESSED_LINE);
        }
    }

    public void onPointerUp() {
        if (UITheme.isDiceMage()) {
            chrome.hardlight(UITheme.DICE_FILL);
        } else {
            chrome.resetColor();
        }
        if (themedFrame.visible) {
            themedFrame.setLineColor(lineColor);
        }
    }

    public void alpha(float value) {
        alpha = value;
        chrome.alpha(value);
        themedFrame.alpha(value);
    }

    public float alpha() {
        return alpha;
    }

    public float marginHor() {
        return chrome.marginHor();
    }

    public float marginVer() {
        return chrome.marginVer();
    }

    public float marginLeft() {
        return chrome.marginLeft();
    }

    public float marginRight() {
        return chrome.marginRight();
    }

    public float marginTop() {
        return chrome.marginTop();
    }

    public float marginBottom() {
        return chrome.marginBottom();
    }
}
