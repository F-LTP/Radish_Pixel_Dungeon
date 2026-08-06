package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Pickaxe;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MoonLightSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.TheCatistSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class MoonLight extends NPC{
    int hastalk=0;
    {
        spriteClass = MoonLightSprite.class;

        properties.add(Property.IMMOVABLE);
    }

    public static boolean heroIsMoonLight() {
        return Dungeon.hero != null && Dungeon.hero.heroClass == HeroClass.MOONLIGHT;
    }

    private String getMessagePrefix() {
        return heroIsMoonLight() ? "the_catist." : "";
    }

    @Override
    public String name() {
        return Messages.get(this, getMessagePrefix() + "name");
    }

    @Override
    public String description() {
        return Messages.get(this, getMessagePrefix() + "desc");
    }

    @Override
    public void storeInBundle(Bundle bundle){
        super.storeInBundle(bundle);
        bundle.put("ml_hastalk",hastalk);
    }
    @Override
    public void restoreFromBundle(Bundle bundle){
        super.restoreFromBundle(bundle);
        if (bundle.contains("ml_hastalk"))
            hastalk=bundle.getInt("ml_hastalk");
    }
    @Override
    public int defenseSkill( Char enemy ) {
        return INFINITE_EVASION;
    }

    @Override
    public void damage( int dmg, Object src ) {
    }

    @Override
    public boolean add( Buff buff ) {
        return false;
    }

    @Override
    public boolean reset() {
        return true;
    }

    // 月华去当英雄的时候 猫权主义者的猫来顶班
    @Override
    public CharSprite sprite() {
        if (heroIsMoonLight()) {
            return new TheCatistSprite();
        }
        return new MoonLightSprite();
    }
    @Override
    public boolean interact(Char c){
        sprite.turnTo(pos,c.pos);

        if (!(c instanceof Hero)){
            return true;
        }

        String prefix = getMessagePrefix();

        if (hastalk<2) {
            GLog.i(Messages.get(this, prefix + "msg" + hastalk));
            hastalk++;
        } else if (hastalk==2) {
            GLog.p(Messages.get(this, prefix + "msg" + hastalk));
            hastalk++;
            Game.runOnRenderThread(new Callback() {
                @Override
                public void call() {
                    GameScene.show(new WndWeaponTransform(null,MoonLight.this));
                }
            });
        } else if(hastalk == 3){
            Game.runOnRenderThread(new Callback() {
                @Override
                public void call() {
                    GameScene.show(new WndWeaponTransform(null,MoonLight.this));
                }
            });
        }


        return true;
    }

    public static class WndWeaponTransform extends Window {

        // 窗口尺寸常量
        private static final int WIDTH = 120;
        private static final int BTN_SIZE = 32;
        private static final float GAP = 2;

        private ItemButton btnPressed;
        private ItemButton btnWeaponInput;
        private RedButton btnTransform;

        // 父窗口引用
        private Window wndParent;

        public WndWeaponTransform(Window wndParent, MoonLight get) {
            super();
            this.wndParent = wndParent;

            String prefix = get.getMessagePrefix();

            IconTitle titlebar = new IconTitle();
            // 根据职业选择不同的贴图
            if (heroIsMoonLight()) {
                titlebar.icon(new Image(new TheCatistSprite()));
            } else {
                titlebar.icon(new Image(new MoonLightSprite()));
            }
            titlebar.label(Messages.titleCase(Messages.get(this, prefix + "weapon_transform")));
            titlebar.setRect(0, 0, WIDTH, 0);
            add(titlebar);

            RenderedTextBlock message = PixelScene.renderTextBlock(
                    Messages.get(this, prefix + "transform_message"), 6);
            message.maxWidth(WIDTH);
            message.setPos(0, titlebar.bottom() + GAP);
            add(message);

            btnWeaponInput = new ItemButton() {
                @Override
                protected void onClick() {
                    btnPressed = btnWeaponInput;
                    GameScene.selectItem(weaponSelector);
                }
            };
            btnWeaponInput.setRect(
                    (float) (WIDTH - BTN_SIZE) / 2,
                    message.top() + message.height(),
                    BTN_SIZE,
                    BTN_SIZE
            );
            add(btnWeaponInput);

            btnTransform = new RedButton(Messages.get(this, prefix + "transform")) {
                @Override
                protected void onClick() {
                    if (btnWeaponInput.item() instanceof Weapon) {
                        handleWeaponTransformation((Weapon) btnWeaponInput.item(), get);
                        hide();
                        if (wndParent != null) {
                            wndParent.hide();
                        }
                        get.hastalk++;
                        get.yell(Messages.get(get, get.getMessagePrefix() + "loop"));
                        get.die(true);
                    }
                }
            };
            btnTransform.enable(false);
            btnTransform.setRect(0, btnWeaponInput.bottom() + GAP, WIDTH, 20);
            add(btnTransform);

            resize(WIDTH, (int) btnTransform.bottom());
        }

        /**
         * 处理武器转换逻辑：移除原武器，生成随机2阶武器
         */
        private void handleWeaponTransformation(Weapon oldWeapon, MoonLight npc) {
            oldWeapon.detach(hero.belongings.backpack);
            GLog.i(Messages.get(this, npc.getMessagePrefix() + "weapon_removed", oldWeapon.name()));
            Weapon newWeapon = generateRandomTier2Weapon(oldWeapon);
            newWeapon.doPickUp(hero);
            GLog.p(Messages.get(this, npc.getMessagePrefix() + "new_weapon_gained", newWeapon.name()));
        }

        /**
         * 生成随机的2阶武器
         */
        private Weapon generateRandomTier2Weapon(Weapon oldweapon) {
            Weapon newWeapon = oldweapon;
            if(newWeapon instanceof MeleeWeapon){
                newWeapon = (MeleeWeapon)Generator.randomUsingDefaults(Generator.Category.WEP_T2);
            } else if(newWeapon instanceof MissileWeapon){
                newWeapon = (MissileWeapon)Generator.randomUsingDefaults(Generator.Category.MIS_T2);
            }
            newWeapon.level = 0;
            newWeapon.identify(false);
            ScrollOfRemoveCurse.uncurse( hero, newWeapon );
            return newWeapon;
        }

        /**
         * 武器选择器：仅允许选择任意武器
         */
        protected WndBag.ItemSelector weaponSelector = new WndBag.ItemSelector() {
            @Override
            public String textPrompt() {
                return Messages.get(this, "select_weapon");
            }

            @Override
            public boolean itemSelectable(Item item) {
                return item instanceof Weapon && item != hero.belongings.weapon() && !(item instanceof Pickaxe);
            }

            @Override
            public void onSelect(Item item) {
                if (item != null && btnPressed.parent != null) {
                    btnWeaponInput.item(item);
                    btnTransform.enable(true);
                }
            }
        };
    }

}