package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionHero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Projecting;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class Crowbar extends MeleeWeapon{

    private static final String AC_BOMB = "bomb";

    {
        image = ItemSpriteSheet.RAPIER;
        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 1f;

        tier = 3;
    }
    //3 tiles that is next to the door is called"after the door"

    @Override
    public String defaultAction() {
        if(isEquipped(hero)){
            return defaultAction = AC_BOMB;
        }
        return AC_THROW;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if(isEquipped(hero))actions.add(AC_BOMB);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);
        if(action.equals(AC_BOMB) && !cursed){
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

            if (target != null ) {

                if (target == curUser.pos) {
                    GLog.i(Messages.get(this, "select_cannot"));
                    return;
                }

                try {
                    QuickSlotButton.target(Actor.findChar(target));
                    Crowbar ks =(Crowbar) hero.belongings.weapon;

                    int reach = 1;

                    if(Dungeon.hero.buff(ChampionHero.Projecting.class) != null){
                        reach = 1314;
                    } else if(ks.hasEnchant(Projecting.class,curUser)){
                        reach = 2;
                    }

                    if(Dungeon.level.distance(target, curUser.pos ) <= reach){
                        if (Dungeon.level.map[target] == Terrain.CRYSTAL_DOOR
                                || Dungeon.level.map[target] == Terrain.DOOR
                                    || Dungeon.level.map[target] == Terrain.LOCKED_DOOR) {
                            Level.set(target, Terrain.EMBERS);
                            //TODO Must Work But Not Now
//                            for (int i = 0; i < PathFinder.NEIGHBOURST3.length; i++) {
//                                GameScene.add( Blob.seed( target - i, 4, Fire.class ) );
//                            }
                            GameScene.updateMap(target);
                            GLog.p(Messages.get(this, "bomb"));
                            curUser.sprite.operate( curUser.pos );
                        } else {
                            GLog.w(Messages.get(this, "not_tiled"));
                        }
                    } else {
                        GLog.i(Messages.get(this, "no_rch"));
                    }
                } catch (Exception e){
                    GLog.w(Messages.get(this, "select_nodoor"));
                }

            }
        }
    };

}
