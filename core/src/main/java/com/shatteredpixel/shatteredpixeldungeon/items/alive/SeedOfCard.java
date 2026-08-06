package com.shatteredpixel.shatteredpixeldungeon.items.alive;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.TestItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Rotberry;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Objects;

public class SeedOfCard extends TestItem {
    {
        image = ItemSpriteSheet.SEED_CARD;
        defaultAction = AC_SPAWN;
    }

    private int cateSelected;
    private int item_quantity;
    private int selected;
    private static final String AC_SPAWN = "spawn";

    private static ArrayList<Class<? extends Plant.Seed>> seedList = new ArrayList<>();

    public SeedOfCard(){
        this.cateSelected = 0;
        this.item_quantity = 1;
        this.selected = 0;

        buildList();
    }

    private void buildList(){
        if(seedList.isEmpty()) {
            for (int i = 0; i < Generator.Category.SEED.classes.length; i++) {
                seedList.add((Class<? extends Plant.Seed>) Generator.Category.SEED.classes[i]);
            }
        }
        seedList.remove(Rotberry.Seed.class);
    }

    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_SPAWN);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if (action.equals(AC_SPAWN)) {
            GameScene.show(new SeedOfCard.SettingsWindow());
        }
    }

    private void createItem(){
        boolean collect;
        Item item = Reflection.newInstance(idToItem(selected));
        if(Challenges.isItemBlocked(item)) return;
        if (item != null) {
            if(item.stackable){
                collect = item.quantity(item_quantity).collect();
            }
            else collect = item.collect();
            item.identify();
            if(collect){
                GLog.i(Messages.get(hero, "you_now_have", item.name()));
                Sample.INSTANCE.play( Assets.Sounds.ITEM );
                GameScene.pickUp( item, hero.pos );
                detach( hero.belongings.backpack );
            }else{
                item.doDrop(curUser);
                detach( hero.belongings.backpack );
            }
        }

    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("item_quantity", item_quantity);
        bundle.put("selected", selected);
        bundle.put("cate_selected", cateSelected);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        item_quantity = bundle.getInt("item_quantity");
        selected = bundle.getInt("selected");
        cateSelected = bundle.getInt("cate_selected");
    }

    private Class<? extends Item> idToItem(int id){
        return seedList.get(id);
    }

    private int maxIndex(int cate){
        return seedList.size() - 1;
    }


    private class SettingsWindow extends Window {
        private RedButton RedButton_create;
        private ArrayList<IconButton> buttonList = new ArrayList<>();
        private static final int WIDTH = 170;
        private static final int BTN_SIZE = 16;
        private static final int GAP = 2;

        public SettingsWindow() {
            createImage();
            Class<? extends Item> item = idToItem(selected);
            RedButton_create = new RedButton(Messages.get(this, "item_quantity",Messages.get(item, "name"))) {
                @Override
                protected void onClick() {
                    createItem();
                    hide();
                }
            };
            add(RedButton_create);

            WindowLayout();
        }

        private void WindowLayout() {;
            RedButton_create.setRect(0, buttonList.get(buttonList.size() - 1).bottom() + GAP + 2 * GAP, WIDTH, 16);
            resize(WIDTH, (int) RedButton_create.bottom());
        }

        private void createImage() {
            float left;

            int length = maxIndex(cateSelected)+1;
            int maxImageCount = 6;
            for (int i = 0; i < length; ++i) {
                final int j = i;
                IconButton btn = new IconButton() {
                    @Override
                    protected void onClick() {
                        selected = Math.min(j, maxIndex(cateSelected));
                        super.onClick();
                    }
                };
                Image im = new ItemSprite(Objects.requireNonNull(Reflection.newInstance(seedList.get(i))).image);
                im.scale.set(1.0f);
                btn.icon(im);

                left = (WIDTH - BTN_SIZE * maxImageCount) / 2f;
                int line = i / maxImageCount;
                btn.setRect(left + (i - maxImageCount * line) * BTN_SIZE,  GAP * line + BTN_SIZE * line, BTN_SIZE, BTN_SIZE);

                add(btn);
                buttonList.add(btn);
            }
        }

        @Override
        public void update() {
            super.update();
            Class<? extends Item> item = idToItem(selected);
            RedButton_create.text(Messages.get(this, "item_quantity",Messages.get(item, "name")));
        }
    }
}


