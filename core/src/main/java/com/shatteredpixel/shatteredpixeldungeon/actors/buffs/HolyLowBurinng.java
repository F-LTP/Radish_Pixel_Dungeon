package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.HalomethaneFire;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.food.ChargrilledMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.food.FrozenCarpaccio;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMeat;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfArcana;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class HolyLowBurinng extends Buff implements Hero.Doom {

    private static final float DURATION = 8f;

    private float left;

    //for tracking burning of hero items
    private int burnIncrement = 0;

    private static final String LEFT	= "left";
    private static final String BURN	= "burnIncrement";

    {
        type = buffType.NEGATIVE;
        announced = true;
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( LEFT, left );
        bundle.put( BURN, burnIncrement );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle(bundle);
        left = bundle.getFloat( LEFT );
        burnIncrement = bundle.getInt( BURN );
    }

    @Override
    public boolean attachTo(Char target) {
        Buff.detach( target, Chill.class);

        return super.attachTo(target);
    }

    @Override
    public boolean act() {

        if (target.isAlive() && !target.isImmune(getClass())) {

            int damage = Char.combatRoll( 1, 3 + Dungeon.scalingDepth()/4 );
            Buff.detach( target, Chill.class);

            DamageInfo dmgInfo = new DamageInfo(damage, DamageType.BURNING_STATUS, null, null, this);

            if (target instanceof Hero
                    && target.buff(TimekeepersHourglass.timeStasis.class) == null
                    && target.buff(TimeStasis.class) == null) {

                Hero hero = (Hero)target;

                hero.damage( dmgInfo );
                burnIncrement++;

                //at 4+ turns, there is a (turns-3)/3 chance an item burns
                if (Random.Int(3) < (burnIncrement - 3)){
                    burnIncrement = 0;

                    ArrayList<Item> burnable = new ArrayList<>();
                    //does not reach inside of containers
                    if (!hero.belongings.lostInventory()) {
                        for (Item i : hero.belongings.backpack.items) {
                            if (!i.unique && (i instanceof Scroll || i instanceof MysteryMeat || i instanceof FrozenCarpaccio)) {
                                burnable.add(i);
                            }
                        }
                    }

                    if (!burnable.isEmpty()){
                        Item toBurn = Random.element(burnable).detach(hero.belongings.backpack);
                        GLog.w( Messages.capitalize(Messages.get(this, "burnsup", toBurn.title())) );
                        if (toBurn instanceof MysteryMeat || toBurn instanceof FrozenCarpaccio){
                            ChargrilledMeat steak = new ChargrilledMeat();
                            if (!steak.collect( hero.belongings.backpack )) {
                                Dungeon.level.drop( steak, hero.pos ).sprite.drop();
                            }
                        }
                        Heap.burnFX( hero.pos );
                    }
                }

            } else {
                target.damage( dmgInfo );
            }

            if (target instanceof Thief && ((Thief) target).item != null) {

                Item item = ((Thief) target).item;

                if (!item.unique && item instanceof Scroll) {
                    target.sprite.emitter().burst( ElmoParticle.FACTORY, 6 );
                    ((Thief)target).item = null;
                } else if (item instanceof MysteryMeat) {
                    target.sprite.emitter().burst( ElmoParticle.FACTORY, 6 );
                    ((Thief)target).item = new ChargrilledMeat();
                }

            }

        } else {

            detach();
        }

        if (Dungeon.level.flamable[target.pos] && Blob.volumeAt(target.pos, HalomethaneFire.class) == 0 && Dungeon.hero.pointsInTalent(Talent.FIRE_GLASS) > 1) {
            GameScene.add( Blob.seed( target.pos, 4, HalomethaneFire.class ) );
        }

        spend( TICK );
        left -= TICK;

        if (left <= 0 ) {
            detach();
        }

        return true;
    }

    public void reignite( Char ch ) {
        reignite( ch, DURATION );
    }

    public void reignite( Char ch, float duration ) {
        if (ch.isImmune(Burning.class)){
            //TODO this only works for the hero, not others who can have brimstone+arcana effect
            // e.g. prismatic image, shadow clone
            if (ch instanceof Hero
                    && ((Hero) ch).belongings.armor() != null
                    && ((Hero) ch).belongings.armor().hasGlyph(Brimstone.class, ch)){
                //generate avg of 1 shield per turn per 50% boost, to a max of 4x boost
                float shieldChance = 2*(RingOfArcana.enchantPowerMultiplier(ch)*ch.talentProc() - 1f);
                int shieldCap = Math.round(shieldChance*4f);
                int shieldGain = (int)shieldChance;
                if (Random.Float() < shieldChance%1) shieldGain++;
                if (shieldCap > 0 && shieldGain > 0){
                    Barrier barrier = Buff.affect(ch, Barrier.class);
                    if (barrier.shielding() < shieldCap){
                        barrier.incShield(1);
                    }
                }
            }
        }
        left = duration;
    }

    @Override
    public String icon() {
        return BuffIndicator.FIRE;
    }

    @Override
    public String heroMessage() {
        return null;
    }

    @Override
    public float iconFadePercent() {
        return Math.max(0, (DURATION - left) / DURATION);
    }

    @Override
    public String iconTextDisplay() {
        return Integer.toString((int)left);
    }

    @Override
    public void fx(boolean on) {
        if (on) target.sprite.add(CharSprite.State.ELMOBURNING);
        else target.sprite.remove(CharSprite.State.ELMOBURNING);
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(0x22EE66);

    }

    @Override
    public String desc() {
        return Messages.get(this, "desc", dispTurns(left));
    }

    @Override
    public void onDeath() {

        Badges.validateDeathFromFire();

        Dungeon.fail( this );
        GLog.n( Messages.get(this, "ondeath") );
    }
}