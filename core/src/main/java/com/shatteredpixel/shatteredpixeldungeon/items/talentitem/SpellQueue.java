package com.shatteredpixel.shatteredpixeldungeon.items.talentitem;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.ArcaneResin;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.CursedWand;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.tweeners.Delayer;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;
//TODO ZAP,MAKE &BIND, HOW TO DEAL WITH THROW&DROP&TRANSFORM image change, buff?
public class SpellQueue extends Item {

    private static final String AC_MAKE = "MAKE";
    private static final String AC_ZAP = "ZAP";
    private static final String AC_REMAKE = "REMAKE";

    private Wand firstWand;
    private Wand secondWand;
    private Wand thirdWand;
    private ArrayList<Wand> wandQueue=new ArrayList<>();

    {
        image = ItemSpriteSheet.SPELL_QUEUE_OFF;

        defaultAction = AC_ZAP;
        usesTargeting=true;
        stackable = false;
        //unique= true;
        wandQueue.add(firstWand);
        wandQueue.add(secondWand);
        wandQueue.add(thirdWand);
    }
    @Override
    public ArrayList<String> actions(Hero hero) {
        syncWand(hero);
        ArrayList<String> actions = new ArrayList<>();
        if (hero.hasTalent(Talent.SPELL_QUEUE)) {
            actions.add(AC_ZAP);
            defaultAction = AC_ZAP;
            if (secondWand==null || (hero.pointsInTalent(Talent.SPELL_QUEUE)>1 && thirdWand==null)){
                actions.add(AC_MAKE);
            }else actions.add(AC_REMAKE);
        }else {
            defaultAction="NOT_EXIST";
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
        if (!Dungeon.hero.hasTalent(Talent.SPELL_QUEUE)) return false;
        if (firstWand==null || secondWand==null
        || (!Dungeon.hero.belongings.backpack.contains(firstWand) && checkMageWand(Dungeon.hero)%2==0)||(!Dungeon.hero.belongings.backpack.contains(secondWand) && checkMageWand(Dungeon.hero)/2%2==0)
        || (thirdWand!=null && !Dungeon.hero.belongings.backpack.contains(thirdWand) && checkMageWand(Dungeon.hero)/4%2==0 )) return false;
        if (firstWand.curCharges<=1) return false;
        firstWand.curCharges-=2;
        boolean res1=secondWand.curCharges>1;
        if (thirdWand==null){
            firstWand.curCharges+=2;
            return res1;
        }else {
            secondWand.curCharges-=2;
            boolean res2=thirdWand.curCharges>1;
            firstWand.curCharges+=2;
            secondWand.curCharges+=2;
            return res1 && res2;
        }
    }
    public void updateImage(){
        syncWand(Dungeon.hero);
        updateImage(checkCharge());
}
    public int checkMageWand(Hero hero){
        MagesStaff Staff=hero.belongings.getItem(MagesStaff.class);
        if (Staff!=null){
            Wand mageWand =Staff.wand;
            if (mageWand!=null){
                return mageWand.spellSelected;
            }
        }
        return 0;
    }
    private void syncWand(Hero hero){
        ArrayList<Wand> wands = hero.belongings.getAllItems(Wand.class);
        for (Wand w:wands){
            int s=w.spellSelected;
            if (s%2!=0) firstWand=w;
            if (s/2%2!=0) secondWand=w;
            if (s/4%2!=0) thirdWand=w;
        }
        MagesStaff Staff=hero.belongings.getItem(MagesStaff.class);
        if (Staff!=null) {
            Wand mageWand = Staff.wand;
            if (mageWand != null) {
                int s = mageWand.spellSelected;
                if (s % 2 != 0) firstWand = mageWand;
                if (s / 2 % 2 != 0) secondWand = mageWand;
                if (s / 4 % 2 != 0) thirdWand = mageWand;
            }
        }
    }
    @Override
    public void execute(Hero hero, String action) {
        syncWand(hero);
        if (action.equals(AC_MAKE)) {
            GameScene.selectItem(wandSelector);
        }
        else if (action.equals(AC_ZAP)){
            curUser = hero;
            //curItem = this;
            if (curUser.buff(MagicImmune.class) != null){
                GLog.w( Messages.get(SpellQueue.class, "no_magic") );
                usesTargeting = false;
            }else if (!checkCharge()) {
                GLog.w(Messages.get(SpellQueue.class,"not_ready"));
                usesTargeting = false;
            }else {
                usesTargeting=true;
                GameScene.selectCell(zapper);
            }
        }
        else if (action.equals(AC_REMAKE)){
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

                        ArrayList<Wand> wands = hero.belongings.getAllItems(Wand.class);
                        for (Wand w:wands){
                            w.spellSelected=0;
                        }
                        MagesStaff Staff=hero.belongings.getItem(MagesStaff.class);
                        if (Staff!=null)
                        {
                            Wand mageWand =Staff.wand;
                            mageWand.spellSelected=0;
                        }
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
        String a="--",b="--",c="--";
        if (firstWand!=null){
            a=firstWand.title();
        }
        if (secondWand!=null){
            b=secondWand.title();
        }
        if (thirdWand!=null){
            c=thirdWand.title();
        }
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
            if (item instanceof  Wand ){
                if (((Wand) item).maxCharges>1){
                    return true;
                }
                return false;
            }
            return  item instanceof MagesStaff;
        }

        @Override
        public void onSelect( Item item ) {
            if (itemSelectable(item) && Dungeon.hero.hasTalent(Talent.SPELL_QUEUE)) {
                if (item.isIdentified()) {
                    Hero hero = Dungeon.hero;
                    hero.sprite.operate(hero.pos);
                    hero.busy();
                    hero.spend(Actor.TICK);
                    if (item instanceof Wand){
                    if (firstWand == null) {
                        ((Wand)item).spellSelected+=1;
                        firstWand = (Wand) item;
                    } else if (secondWand == null) {
                        ((Wand)item).spellSelected+=2;
                        secondWand = (Wand) item;
                    } else {
                        ((Wand)item).spellSelected+=4;
                        thirdWand = (Wand) item;
                    }
                    }else if (item instanceof MagesStaff){
                        if (firstWand == null) {
                            ((MagesStaff)item).wand.spellSelected+=1;
                            firstWand = ((MagesStaff)item).wand;
                        } else if (secondWand == null) {
                            ((MagesStaff)item).wand.spellSelected+=2;
                            secondWand = ((MagesStaff)item).wand;
                        } else {
                            ((MagesStaff)item).wand.spellSelected+=4;
                            thirdWand = ((MagesStaff)item).wand;
                        }
                    }
                    updateImage();
                }else {
                    GLog.w(Messages.get(SpellQueue.class,"not_id"));
                }
            }
        }
    };
    public static class tmpTracker extends FlavourBuff{

    }
    public static class imageListner extends Buff{
        {
            actPriority=-99;
        }
        @Override
        public boolean act(){
            SpellQueue sq=((Hero)target).belongings.getItem(SpellQueue.class);
            if (sq!=null) {
                sq.updateImage();
            }
            spend(TICK);
            if (!((Hero)target).hasTalent(Talent.SPELL_QUEUE)) detach();
            return true;
        }
    }
    Actor spellActor = null;
    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }
    protected CellSelector.Listener zapper = new  CellSelector.Listener() {
        private void afterZap( Wand cur, ArrayList<Wand> wands, Hero hero, int target){
            cur.curCharges-=2f;
            cur.partialCharge+=0.5f;
            if (hero.pointsInTalent(Talent.SPELL_QUEUE)>2){
                cur.partialCharge+=0.25f;
            }
            if (cur.partialCharge>=1f)
            {
                cur.partialCharge-=1f;
                cur.curCharges+=1f;
            }
            if (spellActor != null){
                spellActor.next();
                spellActor = null;
            }

            Char ch = Actor.findChar(target);
            if (!wands.isEmpty() && hero.isAlive()) {
                Actor.add(new Actor() {
                    {
                        actPriority = VFX_PRIO-1;
                    }

                    @Override
                    protected boolean act() {
                        spellActor = this;
                        zapWand(wands, hero, ch == null ? target : ch.pos);
                        Actor.remove(this);
                        return false;
                    }
                });
                hero.next();
            } else {
                if (hero.buff(tmpTracker.class) != null) {
                    hero.buff(tmpTracker.class).detach();
                }
                Item.updateQuickslot();
                Invisibility.dispel();
                    hero.spendAndNext(Actor.TICK);
            }
        }
        private void zapWand(ArrayList<Wand> wands, Hero hero, int cell){
            Wand cur = wands.remove(0);
            Ballistica aim = new Ballistica(hero.pos, cell, cur.collisionProperties(cell));

            hero.sprite.zap(cell);

            float startTime = Game.timeTotal;
            if (!cur.cursed) {
                cur.fx(aim, new Callback() {
                    @Override
                    public void call() {
                        cur.onZap(aim);
                        if (Game.timeTotal - startTime < 0.33f){
                            hero.sprite.parent.add(new Delayer(0.33f - (Game.timeTotal - startTime)) {
                                @Override
                                protected void onComplete() {
                                    afterZap(cur, wands, hero, cell);
                                }
                            });
                        } else {
                            afterZap(cur, wands, hero, cell);
                        }
                    }
                });
            } else {
                CursedWand.cursedZap(cur,
                        hero,
                        new Ballistica(hero.pos, cell, Ballistica.MAGIC_BOLT),
                        new Callback() {
                            @Override
                            public void call() {
                                if (Game.timeTotal - startTime < 0.33f){
                                    hero.sprite.parent.add(new Delayer(0.33f - (Game.timeTotal - startTime)) {
                                        @Override
                                        protected void onComplete() {
                                            afterZap(cur, wands, hero, cell);
                                        }
                                    });
                                } else {
                                    afterZap(cur, wands, hero, cell);
                                }
                            }
                        });
            }
        }
        private void doZap(Integer target){
            if (target == Dungeon.hero.pos){
                GLog.w(Messages.get(Wand.class, "self_target"));
                return;
            }
            ArrayList<Wand> wands =new ArrayList<>();
            wands.add(firstWand);
            wands.add(secondWand);
            if (thirdWand!=null) {
                wands.add(thirdWand);
            }
            float chargeUsePerShot = 2;

            Dungeon.hero.busy();
            Buff.affect(Dungeon.hero,tmpTracker.class,0f);
            zapWand(wands,Dungeon.hero,target);
            updateImage();
        }
        @Override
        public void onSelect( Integer target ) {

            if (target != null) {
                doZap(target);
            }
        }
        @Override
        public String prompt() {
            return Messages.get(Wand.class, "prompt");
        }
    };

}
