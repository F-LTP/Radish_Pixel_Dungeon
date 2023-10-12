package com.shatteredpixel.shatteredpixeldungeon.custom.dict;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.custom.ch.ChallengeItem;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextInput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class NewDictBook extends ChallengeItem {

    {
        image = ItemSpriteSheet.GUIDE_PAGE;
        defaultAction = AC_READ;
        unique = true;
    }

    private static final String AC_READ = "read";

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> action = super.actions(hero);
        action.add(AC_READ);
        return action;
    }

    @Override
    public void execute(Hero hero, String action) {
        if(action.equals(AC_READ)){
            GameScene.show(new NewDictBook.WndNewDict());
        }else {
            super.execute(hero, action);
        }
    }

    public static class WndNewDict extends Window {

        protected TextInput textBox;
        public ArrayList<StyledButton> button = new ArrayList<>();
        public String lastSearch;

        public float GAP = 2f;
        public int WIDTH = 120;

        public WndNewDict() {

            lastSearch = "";

            int textSize = (int) PixelScene.uiCamera.zoom * 9;
            textBox = new TextInput(Chrome.get(Chrome.Type.TOAST_WHITE),false,textSize)
//            {
//                @Override
//                public void enterPressed() {
//                    //triggers positive action on enter pressed, only with non-multiline though.
//                    onSelect(true, getText());
//                    hide();
//                }
//            };
            ;
            textBox.setText("输入关键词搜索");
            lastSearch = textBox.getText();
            textBox.setMaxLength(10);
            add(textBox);
            textBox.setRect(GAP,GAP,WIDTH - 2 * GAP,16);

            for (int i = 0;i < 8;i++) {
                StyledButton styledButton = new StyledButton(Chrome.Type.RED_BUTTON,"",9);
                styledButton.setRect(GAP + i % 2 * 59,16 + 2 * GAP + (int)(i / 2) * 30,57,28);
                button.add(styledButton);
                add(styledButton);
            }

            resize(WIDTH,140);
            textBox.setRect(GAP,textBox.top(),WIDTH - 2 * GAP,16);
        }

        @Override
        public void update() {

            if (!(textBox.getText() == null || textBox.getText().equals(lastSearch))) {
                lastSearch = textBox.getText();
                Wishing.checkSimilarity(textBox.getText());
//                GLog.i(lastSearch);
                if (Wishing.hashMap.size() > 0 && Wishing.hashMap.size() <= 8) {
                    ArrayList<Float> arrayList = new ArrayList<>();
                    arrayList.addAll(Wishing.hashMap.keySet());
                    Collections.sort(arrayList);

                    int i = Wishing.hashMap.size() - 1;
                    for (Float f : arrayList) {
                        button.get(i).text(M.L((Wishing.hashMap.get(f)),"name"));
                        i--;
                    }
                }
            }
        }

        @Override
        public void offset(int xOffset, int yOffset) {
            super.offset(xOffset, yOffset);
            if (textBox != null){
                textBox.setRect(textBox.left(), textBox.top(), textBox.width(), textBox.height());
            }
        }

//        public void onSelect(boolean positive, String text){
//
//        }
    }
}
