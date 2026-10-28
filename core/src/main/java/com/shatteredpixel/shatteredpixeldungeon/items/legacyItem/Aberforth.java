package com.shatteredpixel.shatteredpixeldungeon.items.legacyItem;

import static com.shatteredpixel.shatteredpixeldungeon.actors.Actor.TICK;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Freezing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vulnerable;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.legacyItem.utils.LegacyItemWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;


// 传承·魔剑阿波菲斯
// DoggingDog on 20250415
public class Aberforth extends LegacyItemWeapon {

    public static final String AC_ADSORB		= "ADSORB";

    {
        image = ItemSpriteSheet.ABERFORTH;
        hitSound = Assets.Sounds.HIT_STAB;
        ACC = 1f;
        defaultAction = AC_ADSORB;
    }

    private float aberforthDebuff = 6 * TICK;

    Weapon adsorbedWeapon = new WornShortsword();

    private static final String ADSORBED_WEAPON	= "adsorbed_weapon";
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put( ADSORBED_WEAPON, adsorbedWeapon );
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        adsorbedWeapon = (Weapon) bundle.get( ADSORBED_WEAPON );
    }


    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_ADSORB);
        return actions;
    }


    @Override
    public void execute(Hero hero, String action) {

        super.execute(hero, action);

        if (action.equals(AC_ADSORB)) {

            GameScene.selectItem(itemSelector);
            curUser = hero;
            curItem = this;

        }
    }


//    @Override
//    public int damageRoll(Char owner) {
//        int dmg = super.damageRoll(owner);
//        return dmg;
//    }


    @Override
    public int STRReq(int lvl) {
        return adsorbedWeapon.STRReq(adsorbedWeapon.buffedLvl());
    }


    @Override
    public int min(){
        return Aberforth.this.min(adsorbedWeapon.buffedLvl());
    }

    @Override
    public int min(int lvl) {
        return adsorbedWeapon.min(adsorbedWeapon.buffedLvl());
    }

    @Override
    public int max(){
        return Aberforth.this.max(adsorbedWeapon.buffedLvl());
    }

    @Override
    public int max(int lvl){
        return adsorbedWeapon.max(adsorbedWeapon.buffedLvl());
    }


    @Override
    public int proc(Char attacker, Char defender, int damage) {
        switch (Random.Int(0,13)){
            case 0:
                Buff.affect(defender, Burning.class).reignite(defender, aberforthDebuff);break;
            case 1:
                Buff.affect(defender, Chill.class, aberforthDebuff);Freezing.affect( defender.pos );break;
            case 2:
                Buff.prolong(defender, Paralysis.class,aberforthDebuff);break;
            case 3:
                Buff.affect( defender, Poison.class ).set(aberforthDebuff);break;
            case 4:
                Buff.affect(defender, Vertigo.class, aberforthDebuff);break;
            case 5:
                Buff.prolong(defender, Blindness.class, aberforthDebuff);
                Buff.prolong(defender, Cripple.class, aberforthDebuff);break;
            case 6:
                Buff.affect(defender, Corrosion.class).set(aberforthDebuff, (int)aberforthDebuff);break;
            case 7:
                Buff.prolong(defender, Daze.class, aberforthDebuff);break;
            case 8:
                Buff.affect(defender, Ooze.class).set(aberforthDebuff);break;
            case 9:
                Buff.prolong(defender, Roots.class, aberforthDebuff);break;
            case 10:
                Buff.prolong(defender, Terror.class, aberforthDebuff);break;
            case 11:
                Buff.prolong(defender, Vulnerable.class, aberforthDebuff);break;
            case 12:
                Buff.prolong(defender, Weakness.class, aberforthDebuff);break;
        }

        return super.proc(attacker,defender,damage);
    }



    protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

            @Override
            public String textPrompt() {
                return Messages.get(Aberforth.class, "prompt");
            }

            @Override
            public boolean itemSelectable(Item item) {
                return item instanceof MeleeWeapon;
            }

            @SuppressWarnings("SuspiciousIndentation")
            @Override
            public void onSelect( Item item ) {
                //todo
                if(!(item instanceof MeleeWeapon)){
                    GLog.i( Messages.get(this, "isnoweapon") );
                    return;
                }

                if(Dungeon.hero != null)
                    item.detach(Dungeon.hero.belongings.backpack);
                    GLog.i( Messages.get(Aberforth.class, "absorb_weapon") );
                    adsorbedWeapon = (Weapon) item;
                    Dungeon.hero.HT-=8;
                    Dungeon.hero.HT = Math.max(Dungeon.hero.HT,8);
                    Dungeon.hero.HP = Math.min(Dungeon.hero.HT,Dungeon.hero.HP);
                    aberforthDebuff += TICK;
                    curUser.spend( Actor.TICK );
                    curUser.busy();
                    curUser.sprite.operate( curUser.pos );
                    Sample.INSTANCE.play( Assets.Sounds.MASTERY );
            }
        };

    @Override
    public String desc() {
        return Messages.get(this, "desc",adsorbedWeapon.name());
    }

}
