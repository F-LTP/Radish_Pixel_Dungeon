package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionHero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class Spanner extends MeleeWeapon {

    private static final String AC_REPAIR = "repair";

    {
        image = ItemSpriteSheet.REPAIRV2;
        tier = 2;
        defaultAction = AC_REPAIR;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_REPAIR);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if(action.equals(AC_REPAIR) && !cursed && isEquipped(hero)){
            GameScene.selectCell(porter);
        }

    }

    @Override
    public int min(int lvl) {
        return 4 + lvl;
    }
    @Override
    public int max(int lvl) {
        return 12 + lvl * 3;
    }

    protected static CellSelector.Listener porter = new CellSelector.Listener() {
        @Override
        public String prompt() {
            return Messages.get(this,"select_repair");
        }
        @Override
        public void onSelect(Integer target) {

            float dealy = 2f;

            if (target != null ) {

                if (target == curUser.pos) {
                    GLog.i(Messages.get(this, "select_cannot"));
                    return;
                }

                if(!curItem.isEquipped(hero)){
                    GLog.n(Messages.get(this,"youmus"));
                }

                try {
                    QuickSlotButton.target(Actor.findChar(target));
                    Trap t = Dungeon.level.traps.get(target).reveal();

                    Spanner ks =(Spanner) hero.belongings.weapon;

                    int reach = 1;

                    if(Dungeon.hero.buff(ChampionHero.Projecting.class) != null){
                        reach = 1314;
                    } else if(ks.hasEnchant(Projecting.class,curUser)){
                        reach = 2;
                    }

                    if(target == t.pos &&Dungeon.level.distance( t.pos, curUser.pos ) <= reach){
                        boolean isRepair = !t.active && t.onlyRepair;
                        if(isRepair){
                            t.active = true;
                            t.onlyRepair = false;
                            curUser.spend( dealy );
                            curUser.busy();
                            Level.set(t.pos, Terrain.TRAP);
                            Dungeon.level.setTrap(t,t.pos);
                            GameScene.updateMap(target);
                            GLog.p(Messages.get(this, "sr"));
                            curUser.sprite.operate( curUser.pos );
                        } else if(t.active) {
                            GLog.w(Messages.get(this, "active"));
                        } else {
                            GLog.n(Messages.get(this, "you"));
                        }
                    } else {
                        GLog.i(Messages.get(this, "srf"));
                    }
                } catch (Exception e){
                    GLog.w(Messages.get(this, "select_notrap"));
                }

            }
        }
    };
}
