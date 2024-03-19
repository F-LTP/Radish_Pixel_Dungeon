package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionHero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class EliteBadge extends Artifact{
    public static final String AC_ELITE       = "ELITE";

    public static final String AC_ELITE_REMOVE       = "ELITE_REMOVE";

    {
        image = ItemSpriteSheet.ARTIFACT_ELTIE1;

        levelCap = 10;
        exp = 0;

        charge = 5;
        partialCharge=0;
        chargeCap=20+level()*2;
        defaultAction = AC_ELITE;
    }

    @Override
    public String defaultAction() {
        for (Buff EliteBuffActive : hero.buffs(ChampionHero.class)) {
            if (EliteBuffActive != null && !cursed && isEquipped(hero)) {
                return defaultAction = AC_ELITE_REMOVE;
            }
        }
        return AC_ELITE;
    }

    int randthree[]= {0,0};
    public float growing_mul=1.19f;
    private void updateImage(){
        switch (level()){
            case 1:
                image = ItemSpriteSheet.ARTIFACT_ELTIE2;
                break;
            case 2:
            case 3:
                image = ItemSpriteSheet.ARTIFACT_ELTIE3;
                break;
            case 4:
            case 5:
                image = ItemSpriteSheet.ARTIFACT_ELTIE4;
                break;
            case 6:
            case 7:
                image = ItemSpriteSheet.ARTIFACT_ELTIE5;
                break;
            case 8:
            case 9:
                image = ItemSpriteSheet.ARTIFACT_ELTIE6;
                break;
            case 10:
                image = ItemSpriteSheet.ARTIFACT_ELTIE7;
                break;
            default:
                image = ItemSpriteSheet.ARTIFACT_ELTIE1;
                break;
        }
        updateQuickslot();
    }
    private static final String RT = "randomthree";
    private static final String GM = "growingmul";
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put( RT, randthree );
        bundle.put( GM,growing_mul);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        if (bundle.getIntArray(RT)!=null)
            randthree = bundle.getIntArray(RT);
        else {
            randthree[0]=0;
            randthree[1]=0;
        }
        if (bundle.contains(GM)){
            growing_mul= bundle.getFloat(GM);
        }
        else growing_mul=1.19f;
    }
    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions( hero );
        if (isEquipped(hero) && charge >= 5+level()/5 && !cursed && hero.buff(MagicImmune.class) == null) {
            actions.add(AC_ELITE);
        } else {
            for (Buff EliteBuffActive : hero.buffs(ChampionHero.class)) {
                if (EliteBuffActive != null && !cursed && isEquipped(hero)) {
                    actions.add(AC_ELITE_REMOVE);
                }
            }
        }
        return actions;
    }

    @Override
    public Item upgrade() {
        super.upgrade();
        chargeCap = 20+level()*2;
        updateImage();
        return this;
    }
    @Override
    public void level(int value) {
        super.level(value);
        chargeCap = 20+level()*2;
        updateImage();
    }
    @Override
    public void execute(Hero hero, String action) {

        super.execute(hero, action);

        if (hero.buff(MagicImmune.class) != null) return;

        if (action.equals(AC_ELITE)){

            curUser = hero;

            if (!isEquipped( hero )) {
                GLog.i( Messages.get(Artifact.class, "need_to_equip") );

            } else if (charge < 5+level()/5) {
                GLog.i( Messages.get(this, "low_charge") );

            } else if (cursed) {
                GLog.w( Messages.get(this, "cursed") );

            } else {
                EliteBuff(hero);
            }
        } else if (action.equals(AC_ELITE_REMOVE)){
            for (Buff EliteBuffActive : hero.buffs(ChampionHero.class)){
                if(EliteBuffActive != null){
                    GameScene.show(new WndOptions(new ItemSprite(image),
                            Messages.get(EliteBadge.class, "title"),
                            Messages.get(EliteBadge.class, "clear_desc"),
                            Messages.get(EliteBadge.class, "ok"),
                            Messages.get(EliteBadge.class, "no")) {
                        @Override
                        protected void onSelect(int index) {
                            if (index == 0) {
                                EliteBuffActive.detach();
                                GLog.w(Messages.get(EliteBadge.class, "clear_buff"));
                            } else {
                                hide();
                            }
                        }
                    });
                    return;
                }
            }
        }
    }

    private void EliteBuff(Hero hero) {
        boolean canelite=true;
        for (Buff b:hero.buffs()){
            if (b instanceof ChampionHero){
                canelite=false;
                break;
            }
        }
        if (canelite) {
            if (randthree[0]==0 &&randthree[1]==0) {
                randthree[0] = Random.Int(6);
                while ((randthree[1] = Random.Int(6)) == randthree[0]) ;
            }
            GameScene.show(new WndElite(ChampionHero.elitenames[randthree[0]],ChampionHero.elitenames[randthree[1]]));
        }
        else
            GLog.i(Messages.get(this,"already_elite"));
    }

    @Override
    public void charge(Hero target, float amount) {
        if (cursed || target.buff(MagicImmune.class) != null) return;

        partialCharge+=0.5f;
        while (partialCharge>=1f){
            partialCharge-=1f;
            charge++;
        }
        charge=Math.min(charge,chargeCap);
        updateQuickslot();
    }
    @Override
    protected ArtifactBuff passiveBuff() {
        return new EliteBadge.badgeRecharge();
    }

    @Override
    public String desc() {
        String desc = super.desc();

        if (isEquipped( hero )){
            desc += "\n\n";
            if (cursed)
                desc += Messages.get(this, "desc_cursed");
            else
                desc += Messages.get(this, "desc_equipped");
        }
        return desc;
    }
    public void giveElite(int i){
        ChampionHero.getElite(hero,randthree[i],10f+level()*2f, this);
        Sample.INSTANCE.play( Assets.Sounds.BURNING );
        Talent.onArtifactUsed( hero );
        ActionIndicator.updateIcon();
    }
    public void record_growing(float m){
        growing_mul=m;
    }
    public class badgeRecharge extends ArtifactBuff{
        public void gainExp( int amount ) {
            if (cursed || target.buff(MagicImmune.class) != null ) return;

            exp += amount;
            if (exp >= 100+level()*40 && level() < levelCap){
                exp -= 100+level()*40;
                GLog.p( Messages.get(this, "levelup") );
                upgrade();
            }

        }
        public void gainCharge(float amount) {
            if (cursed || target.buff(MagicImmune.class) != null) return;
            float chargegain=amount* RingOfEnergy.artifactChargeMultiplier(target);
            if (charge < chargeCap) {
                partialCharge += chargegain;

                while (partialCharge >= 1f) {
                    charge++;
                    partialCharge -= 1f;

                    updateQuickslot();
                }
                charge = Math.min(charge,chargeCap);
            } else {
                partialCharge = 0;
            }
        }
        @Override
        public void detach(){
            for (Buff b:target.buffs()){
                if (b instanceof ChampionHero){
                    b.detach();
                }
            }
            super.detach();
        }
    }
    public class WndElite extends WndOptions {
            //used in PixelScene.restoreWindows
            public WndElite(){
                this(ChampionHero.elitenames[randthree[0]],
                        ChampionHero.elitenames[randthree[1]]);
            }
            public WndElite(String s1,String s2){

                super(new ItemSprite(new EliteBadge()),
                        Messages.titleCase(new EliteBadge().name()),
                        Messages.get(EliteBadge.class, "text"),
                        s1,
                        s2,
                        Messages.get(EliteBadge.class, "cancel"));
            }

            @Override
            protected void onSelect(int index) {
                charge-=(5+level()/5);
                updateQuickslot();
                if (index < 2) {
                    if (Dungeon.hero.belongings.getItem(EliteBadge.class)!=null){
                        EliteBadge eb=Dungeon.hero.belongings.getItem(EliteBadge.class);
                        if (eb.isEquipped(Dungeon.hero)){
                            eb.giveElite(index);
                        }
                    }
                }
                randthree[0]=0;
                randthree[1]=0;
            }

            @Override
            protected boolean hasInfo(int index) {
                return index < 2;
            }

            @Override
            protected void onInfo( int index ) {
                GameScene.show(new WndTitledMessage(
                        Icons.get(Icons.INFO),
                        Messages.titleCase(ChampionHero.elitenames[randthree[index]]),
                        ChampionHero.elitedescs[randthree[index]]));
            }

        @Override
        public void onBackPressed() {
            //do nothing, reader has to cancel
        }
    }
}
