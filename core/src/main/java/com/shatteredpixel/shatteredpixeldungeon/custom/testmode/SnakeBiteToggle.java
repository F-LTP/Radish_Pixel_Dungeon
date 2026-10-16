package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.challenge.SnakeBiteChallengeManager;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class SnakeBiteToggle extends TestItem {
    
    {
        image = ItemSpriteSheet.SNAKE_BITED_YENDOR;
        defaultAction = AC_TOGGLE_ITEM_SPRITE;
        unique = true;
        bones = false;
    }
    
    private static final String AC_TOGGLE_ITEM_SPRITE = "toggle_item_sprite";
    private static final String AC_TOGGLE_ITEM_TEXT = "toggle_item_text";
    private static final String AC_TOGGLE_MOB_SPRITE = "toggle_mob_sprite";
    private static final String AC_TOGGLE_MOB_TEXT = "toggle_mob_text";
    private static final String AC_INFO = "info";
    
    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_TOGGLE_ITEM_SPRITE);
        actions.add(AC_TOGGLE_ITEM_TEXT);
        actions.add(AC_TOGGLE_MOB_SPRITE);
        actions.add(AC_TOGGLE_MOB_TEXT);
        actions.add(AC_INFO);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        
        if (action.equals(AC_TOGGLE_ITEM_SPRITE)) {
            SnakeBiteChallengeManager.setItemSpriteEnabled(!SnakeBiteChallengeManager.isItemSpriteEnabled());
        } else if (action.equals(AC_TOGGLE_ITEM_TEXT)) {
            SnakeBiteChallengeManager.setItemTextEnabled(!SnakeBiteChallengeManager.isItemTextEnabled());
        } else if (action.equals(AC_TOGGLE_MOB_SPRITE)) {
            SnakeBiteChallengeManager.setMobSpriteEnabled(!SnakeBiteChallengeManager.isMobSpriteEnabled());
            GLog.w("注意：重新进入该存档后怪物贴图修改才能够生效。");
        } else if (action.equals(AC_TOGGLE_MOB_TEXT)) {
            SnakeBiteChallengeManager.setMobTextEnabled(!SnakeBiteChallengeManager.isMobTextEnabled());
        } else if (action.equals(AC_INFO)) {
        }
        showStatus(action);
    }

    private void showStatus(String name) {
        String status = "";
        switch (name){
            case AC_TOGGLE_ITEM_SPRITE:
                status = String.format("物品贴图: %s", SnakeBiteChallengeManager.isItemSpriteEnabled() ? "蛇咬" : "蛇不咬");
                break;
            case AC_TOGGLE_ITEM_TEXT:
                status = String.format("物品文本: %s", SnakeBiteChallengeManager.isItemTextEnabled() ? "蛇咬" : "蛇不咬");
                break;
            case AC_TOGGLE_MOB_SPRITE:
                status = String.format("怪物贴图: %s", SnakeBiteChallengeManager.isMobSpriteEnabled() ? "蛇咬" : "蛇不咬");
                break;
            case AC_TOGGLE_MOB_TEXT:
                status = String.format("怪物文本: %s", SnakeBiteChallengeManager.isMobTextEnabled() ? "蛇咬" : "蛇不咬");
                break;
            default:
                status = String.format("物品贴图: %s | 物品文本: %s | 怪物贴图: %s | 怪物文本: %s",
                        SnakeBiteChallengeManager.isItemSpriteEnabled() ? "ON" : "OFF",
                        SnakeBiteChallengeManager.isItemTextEnabled() ? "ON" : "OFF",
                        SnakeBiteChallengeManager.isMobSpriteEnabled() ? "ON" : "OFF",
                        SnakeBiteChallengeManager.isMobTextEnabled() ? "ON" : "OFF"
                );
        }
        GLog.p(status);
    }
    
    // State storage methods for save/load (not used for logic, just persistence)
    private static final String ITEM_SPRITE_ENABLED = "item_sprite_enabled";
    private static final String ITEM_TEXT_ENABLED = "item_text_enabled";
    private static final String MOB_SPRITE_ENABLED = "mob_sprite_enabled";
    private static final String MOB_TEXT_ENABLED = "mob_text_enabled";
    
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(ITEM_SPRITE_ENABLED, SnakeBiteChallengeManager.isItemSpriteEnabled());
        bundle.put(ITEM_TEXT_ENABLED, SnakeBiteChallengeManager.isItemTextEnabled());
        bundle.put(MOB_SPRITE_ENABLED, SnakeBiteChallengeManager.isMobSpriteEnabled());
        bundle.put(MOB_TEXT_ENABLED, SnakeBiteChallengeManager.isMobTextEnabled());
    }
    
    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        SnakeBiteChallengeManager.setItemSpriteEnabled(bundle.getBoolean(ITEM_SPRITE_ENABLED));
        SnakeBiteChallengeManager.setItemTextEnabled(bundle.getBoolean(ITEM_TEXT_ENABLED));
        SnakeBiteChallengeManager.setMobSpriteEnabled(bundle.getBoolean(MOB_SPRITE_ENABLED));
        SnakeBiteChallengeManager.setMobTextEnabled(bundle.getBoolean(MOB_TEXT_ENABLED));
    }
}
