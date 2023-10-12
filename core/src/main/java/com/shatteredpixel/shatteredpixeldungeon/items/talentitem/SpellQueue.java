package com.shatteredpixel.shatteredpixeldungeon.items.talentitem;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.ArcaneResin;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

import java.util.ArrayList;
//TODO ZAP,MAKE &BIND, HOW TO DEAL WITH THROW&DROP&TRANSFORM image change, buff?
public class SpellQueue extends Item {

    private static final String AC_MAKE = "MAKE";
    private static final String AC_ZAP = "ZAP";
    private static final String AC_REMAKE = "REMAKE";

    private static Wand firstWand;
    private static Wand secondWand;
    private static Wand thirdWand;
    private static ArrayList<Wand> wandQueue=new ArrayList<>();
    static {
        wandQueue.add(firstWand);
        wandQueue.add(secondWand);
        wandQueue.add(thirdWand);
    }

    {
        image = ItemSpriteSheet.SPELL_QUEUE_OFF;

        defaultAction = AC_ZAP;
        usesTargeting=true;
        stackable = false;
        unique= true;
    }
    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = new ArrayList<>();
        if (hero.hasTalent(Talent.SPELL_QUEUE)) {
            actions.add(AC_ZAP);
            if (secondWand==null || (hero.pointsInTalent(Talent.SPELL_QUEUE)>1 && thirdWand==null)){
                actions.add(AC_MAKE);
            }else actions.add(AC_REMAKE);
        }
        return actions;
    }
    private  void updateImage(boolean on){
        if (on){
            image=ItemSpriteSheet.SPELL_QUEUE_ON;
        }else
            image=ItemSpriteSheet.SPELL_QUEUE_OFF;
        updateQuickslot();
    }
    private  boolean checkCharge(){
        if (Dungeon.hero.pointsInTalent(Talent.SPELL_QUEUE)>1)
            return checkCharge(firstWand,secondWand,thirdWand);
        else
            return checkCharge(firstWand,secondWand);
    }
    private boolean checkCharge(Wand a,Wand b){
        if (a==null || b==null) return false;
        if (!Dungeon.hero.belongings.backpack.contains(a) ||!Dungeon.hero.belongings.backpack.contains(b)) return false;
        if (a.curCharges<=1) return false;
        a.curCharges-=2;
        boolean res=b.curCharges>1;
        a.curCharges+=2;
        return res;
    }
    private boolean checkCharge(Wand a,Wand b ,Wand c){
        if (a==null || b==null || c==null) return false;
        if (!Dungeon.hero.belongings.backpack.contains(a) ||!Dungeon.hero.belongings.backpack.contains(b) ||!Dungeon.hero.belongings.backpack.contains(c)) return false;
        if (a.curCharges<=1) return false;
        a.curCharges-=2;
        boolean res1=b.curCharges>1;
        b.curCharges-=2;
        boolean res2=c.curCharges>1;
        a.curCharges+=2;
        b.curCharges+=2;
        return res1 && res2;
    }
    public void updateImage(){
        if (Dungeon.hero.pointsInTalent(Talent.SPELL_QUEUE)>1){
            if (thirdWand!=null) {
                updateImage(checkCharge(firstWand,secondWand,thirdWand));
            }
            else
                image=ItemSpriteSheet.SPELL_QUEUE_OFF;
        }else if (secondWand!=null){
            updateImage(checkCharge(firstWand,secondWand));
        }else {
            image=ItemSpriteSheet.SPELL_QUEUE_OFF;
        }
        updateQuickslot();
}
    @Override
    public void execute(Hero hero, String action) {

        if (action.equals(AC_MAKE)) {
            GameScene.selectItem(wandSelector);
        }
        else if (action.equals(AC_ZAP)){
            if (hero.buff(MagicImmune.class) != null){
                GLog.w( Messages.get(this, "no_magic") );
            }else if (checkCharge()) {
                curUser = hero;
                curItem = this;
                GameScene.selectCell(zapper);
            }else
                GLog.w(Messages.get(this,"not_ready"));
        }else if (action.equals(AC_REMAKE)){
            GameScene.show(new WndOptions(Messages.get(this,"title"),Messages.get(this,"inform"),Messages.get(Chasm.class, "yes"),
                    Messages.get(Chasm.class, "no")){
                private float elapsed = 0f;

                @Override
                public synchronized void update() {
                    super.update();
                    elapsed += Game.elapsed;
                }

                @Override
                public void hide() {
                    if (elapsed > 0.2f){
                        super.hide();
                    }
                }
                @Override
                protected void onSelect(int index){
                    ArcaneResin ar = null;
                if (index==0 && elapsed > 0.2f){
                    for (ArcaneResin i : Dungeon.hero.belongings.getAllItems(ArcaneResin.class)){
                        if (ar == null ) {
                            ar = i;
                        }
                    }
                    if (ar!=null) {
                        ar.detach(Dungeon.hero.belongings.backpack);
                        firstWand=null;
                        secondWand=null;
                        thirdWand=null;
                        SpellQueue.this.updateImage();
                    }else {
                        GLog.w(Messages.get(SpellQueue.class,"lack"));
                    }
                    }
                }
            });
        }
    }
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("wand_queue",wandQueue);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        int i=0;
        for (Bundlable wand : bundle.getCollection("wand_queue" )) {
            switch (i){
                case 0: default:
                firstWand=(Wand)wand;
                i++;
                break;
                case 1: secondWand=(Wand)wand;
                i++;
                break;
                case 2: thirdWand=(Wand)wand;
                break;
            }
        }
    }
    @Override
    public String desc(){
        String a,b,c;
        a=firstWand==null?"--":firstWand.name();
        b=secondWand==null?"--":secondWand.name();
        c=thirdWand==null?"--":thirdWand.name();
        if (Dungeon.hero.pointsInTalent(Talent.SPELL_QUEUE)>1)
            return Messages.get(this, "desc2",a,b,c);
        else
            return Messages.get(this, "desc1",a,b);
    }
    protected WndBag.ItemSelector wandSelector = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return Messages.get(SpellQueue.class, "prompt");
        }

        @Override
        public Class<?extends Bag> preferredBag(){
            return MagicalHolster.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            return item instanceof Wand && ((Wand) item).maxCharges>1;
        }

        @Override
        public void onSelect( Item item ) {
            if (itemSelectable(item) && Dungeon.hero.hasTalent(Talent.SPELL_QUEUE)) {
                if (item.isIdentified()) {
                    Hero hero = Dungeon.hero;
                    hero.sprite.operate(hero.pos);
                    hero.busy();
                    hero.spend(Actor.TICK);
                    if (firstWand == null) {
                        firstWand = (Wand) item;
                    } else if (secondWand == null) {
                        secondWand = (Wand) item;
                    } else {
                        thirdWand = (Wand) item;
                    }
                    updateImage();
                }else {
                    GLog.w(Messages.get(SpellQueue.class,"not_id"));
                }
            }
        }
    };

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }
    protected CellSelector.Listener zapper = new  CellSelector.Listener() {

        private void doZap(Integer target, Wand wandToZap){

            if (target != null) {

                //FIXME this safety check shouldn't be necessary
                //it would be better to eliminate the curItem static variable.
                final Wand curWand=wandToZap;

                final Ballistica shot = new Ballistica( curUser.pos, target, curWand.collisionProperties(target));
                int cell = shot.collisionPos;

                if (target == curUser.pos || cell == curUser.pos) {
                    GLog.i( Messages.get(Wand.class, "self_target") );
                    return;
                }

                curUser.sprite.zap(cell);

                //attempts to target the cell aimed at if something is there, otherwise targets the collision pos.
                if (Actor.findChar(target) != null)
                    QuickSlotButton.target(Actor.findChar(target));
                else
                    QuickSlotButton.target(Actor.findChar(cell));
                curUser.busy();
                if (curWand.cursed){
                    CursedWand.cursedZap(curWand,
                            curUser,
                            new Ballistica(curUser.pos, target, Ballistica.MAGIC_BOLT),
                            new Callback() {
                                @Override
                                public void call() {
                                    curWand.spellUsed();
                                }
                            });
                } else {
                    curWand.fx(shot, new Callback() {
                        public void call() {
                            curWand.onZap(shot);
                            curWand.spellUsed();
                        }
                    });
                }
            }
        }
        @Override
        public void onSelect( Integer target ) {

            if (target != null) {
                doZap(target, firstWand);
                doZap(target,secondWand);
                if (thirdWand!=null)
                    doZap(target,thirdWand);
                curUser.spendAndNext(1f);
            }
                /* {

                //FIXME this safety check shouldn't be necessary
                //it would be better to eliminate the curItem static variable.
                final Wand curWand;
                if (curItem instanceof SpellQueue) {
                    curWand = SpellQueue.this.firstWand;
                } else {
                    return;
                }

                final Ballistica shot = new Ballistica( curUser.pos, target, curWand.collisionProperties(target));
                int cell = shot.collisionPos;

                if (target == curUser.pos || cell == curUser.pos) {
                    GLog.i( Messages.get(Wand.class, "self_target") );
                    return;
                }

                curUser.sprite.zap(cell);

                //attempts to target the cell aimed at if something is there, otherwise targets the collision pos.
                if (Actor.findChar(target) != null)
                    QuickSlotButton.target(Actor.findChar(target));
                else
                    QuickSlotButton.target(Actor.findChar(cell));
                curUser.busy();
                    if (curWand.cursed){
                        CursedWand.cursedZap(curWand,
                                curUser,
                                new Ballistica(curUser.pos, target, Ballistica.MAGIC_BOLT),
                                new Callback() {
                                    @Override
                                    public void call() {
                                        curWand.spellUsed();
                                    }
                                });
                    } else {
                        curWand.fx(shot, new Callback() {
                            public void call() {
                                curWand.onZap(shot);
                                curWand.spellUsed();
                            }
                        });
                    }
            }*/
        }

        @Override
        public String prompt() {
            return Messages.get(Wand.class, "prompt");
        }
    };

}
