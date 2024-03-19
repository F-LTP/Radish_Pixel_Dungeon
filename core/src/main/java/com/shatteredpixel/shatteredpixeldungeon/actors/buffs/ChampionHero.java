/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.EliteBadge;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.curses.Wayward;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.HashSet;
import java.util.function.ToDoubleBiFunction;

public abstract class ChampionHero extends Buff {

    {
        type = buffType.POSITIVE;
    }

    protected int color;
    protected float left;
    private static final String LEFT	= "left";
    @Override
    public boolean act() {
        spend(TICK);
        left -= TICK;
        if (left <= 0){
            detach();
        }
        return true;
    }
    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( LEFT, left );

    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        left = bundle.getFloat( LEFT );
    }

    public void set( float duration ) {
        this.left = duration;
    }


    @Override
    public int icon() {
        return BuffIndicator.CORRUPT;
    }

    @Override
    public void tintIcon(Image icon) {
        icon.hardlight(color);
    }

    @Override
    public void fx(boolean on) {
        if (on) target.sprite.aura( color );
        else target.sprite.clearAura();
    }

    public void onAttackProc(Char enemy ){

    }

    @Override
    public String desc() {
        return Messages.get(this, "desc",dispTurns(left));
    }
    public boolean canAttackWithExtraReach( Char enemy ){
        return false;
    }

    public float meleeDamageFactor(){
        return 1f;
    }

    public float damageTakenFactor(){
        return 1f;
    }

    public float evasionAndAccuracyFactor(){
        return 1f;
    }

    public float speedFactor(){return 1f;}
    public static String elitenames[]={Messages.get(ChampionHero.Blazing.class, "name"),
            Messages.get(ChampionHero.Projecting.class, "name"),
            Messages.get(ChampionHero.AntiMagic.class, "name"),
            Messages.get(ChampionHero.Giant.class, "name"),
            Messages.get(ChampionHero.Blessed.class, "name"),
            Messages.get(ChampionHero.Growing.class, "name")
    };
    public static String elitedescs[]={Messages.get(ChampionHero.Blazing.class, "descforshow"),
            Messages.get(ChampionHero.Projecting.class, "descforshow"),
            Messages.get(ChampionHero.AntiMagic.class, "descforshow"),
            Messages.get(ChampionHero.Giant.class, "descforshow"),
            Messages.get(ChampionHero.Blessed.class, "descforshow"),
            Messages.get(ChampionHero.Growing.class, "descforshow")
    };
    public static void rollForElite(Hero hero,float dur){
        getElite(hero, Random.Int(6),dur);
    }
    public static void getElite(Hero hero, int el, float dur, Item s){
        if (el!=5){
            getElite(hero,el,dur);
        }else {
            Buff.affect(hero,Growing.class).set(dur);
            Growing g=hero.buff(Growing.class);
            g.control(s);
        }
    }
    public static void getElite(Hero hero,int el,float dur){
        Class<?extends ChampionHero> buffCls;
        switch (el){
            case 0: default:    buffCls = Blazing.class;      break;
            case 1:             buffCls = Projecting.class;   break;
            case 2:             buffCls = AntiMagic.class;    break;
            case 3:             buffCls = Giant.class;        break;
            case 4:             buffCls = Blessed.class;      break;
            case 5:             buffCls = Growing.class;      break;
        }
        Buff.affect(hero, buffCls).set(dur);
    }

    public static class Blazing extends ChampionHero {

        {
            color = 0xFF8800;
        }

        @Override
        public void onAttackProc(Char enemy) {
            Buff.affect(enemy, Burning.class).reignite(enemy);
        }

        @Override
        public float meleeDamageFactor() {
            return 1.25f;
        }

        {
            immunities.add(Burning.class);
        }
    }

    public static class Projecting extends ChampionHero {

        {
            color = 0x8800FF;
        }

        @Override
        public float meleeDamageFactor() {
            return 1.25f;
        }

        @Override
        public boolean canAttackWithExtraReach( Char enemy ) {
            boolean[] passable = BArray.not(Dungeon.level.solid, null);

            PathFinder.buildDistanceMap(enemy.pos, passable, 12);

            return PathFinder.distance[target.pos] <= 12 && target.fieldOfView[enemy.pos];



        }
    }
        //TODO
    public static class AntiMagic extends ChampionHero {

        {
            color = 0x00FF00;
        }
            public static final HashSet<Class> MY_ANTI_MAGIC=new HashSet<>();
        static {
            MY_ANTI_MAGIC.addAll(com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic.RESISTS);
            MY_ANTI_MAGIC.add(Slow.class);
            MY_ANTI_MAGIC.add(Drowsy.class);
            MY_ANTI_MAGIC.add(Wayward.WaywardBuff.class);
        }
            @Override
            public boolean attachTo(Char target) {
            for (Buff b:target.buffs()){
                if (MY_ANTI_MAGIC.contains(b)){
                    b.detach();
                }
            }
                return super.attachTo(target);
            }
        @Override
        public float damageTakenFactor() {
            return 0.75f;
        }

        {
            immunities.addAll(MY_ANTI_MAGIC);
        }

    }
    //TODO
    //Also makes target large, see Char.properties()
    public static class Giant extends ChampionHero {

        {
            color = 0x0088FF;
        }

        @Override
        public float damageTakenFactor() {
            return 0.25f;
        }
        @Override
        public float speedFactor(){
            if (Dungeon.level.openSpace[target.pos]) return 1f;
            return 0.5f;
        }
        @Override
        public boolean canAttackWithExtraReach(Char enemy) {
            boolean[] passable = BArray.not(Dungeon.level.solid, null);
            for (Char ch : Actor.chars()) {
                //our own tile is always passable
                passable[ch.pos] = ch == target;
            }

            PathFinder.buildDistanceMap(enemy.pos, passable, 2);

            return PathFinder.distance[target.pos] <= 2;
        }
    }

    public static class Blessed extends ChampionHero {

        {
            color = 0xFFFF00;
        }

        @Override
        public float evasionAndAccuracyFactor() {
            return 3f;
        }
    }

    public static class Growing extends ChampionHero {

        {
            color = 0xFF0000;
        }

        public float multiplier = 1.19f;
        Item source;
        @SuppressWarnings("SuspiciousIndentation")
        public void control(Item s){
            source=s;
            if (s instanceof EliteBadge)
            multiplier=((EliteBadge) s).growing_mul;
        }
        private int count=0;
        @Override
        public boolean act() {
            count++;
            if (count>=3) {
                multiplier += 0.01f;
                count-=3;
            }
            return super.act();
        }
        void updateSource(){
            if (source!=null){
                if (source instanceof EliteBadge){
                    ((EliteBadge) source).record_growing(multiplier);
                }
            }
        }
        @Override
        public void detach(){
            super.detach();
            updateSource();
        }
        @Override
        public float meleeDamageFactor() {
            return multiplier;
        }

        @Override
        public float damageTakenFactor() {
            return 1f/multiplier;
        }

        @Override
        public float evasionAndAccuracyFactor() {
            return multiplier;
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc", (int)(100*(multiplier-1)), (int)(100*(1 - 1f/multiplier)),dispTurns(left));
        }

        private static final String MULTIPLIER = "hero_multiplier";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            bundle.put(MULTIPLIER, multiplier);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            multiplier = bundle.getFloat(MULTIPLIER);
        }
    }

}
