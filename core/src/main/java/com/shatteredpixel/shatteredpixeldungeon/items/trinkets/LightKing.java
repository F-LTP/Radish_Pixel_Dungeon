package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class LightKing extends Trinket {

    {
        image = ItemSpriteSheet.LIGHT_KING;
    }

    @Override
    protected int upgradeEnergyCost() {
        return 6+2*level();
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return new ItemSprite.Glowing(0x00CCCC, 1f);
    }

    @Override
    public String statsDesc() {
        if (isIdentified()){
            return Messages.get(this, "stats_desc", LastHP(buffedLvl()),damagePlus(buffedLvl()),checkStats());
        } else {
            return Messages.get(this, "stats_desc", LastHP(0), damagePlus(0),checkStats());
        }
    }

    public String checkStats(){
        if(Dungeon.hero != null){
            if(checkHpThreshold(Dungeon.hero.HP,Dungeon.hero.HT)){
                return Messages.get(this, "stats_desc_yes");
            } else {
                return Messages.get(this, "stats_desc_no");
            }
        }
        return Messages.get(this, "stats_desc_no");
    }

    public int damagePlus( int level ){
        int hpBarChance = 0;
        switch (level){
            case 0:
                hpBarChance = 25;
                break;
            case 1:
                hpBarChance = 33;
                break;
            case 2:
                hpBarChance = 41;
                break;
            case 3:
                hpBarChance = 50;
                break;
        }
        return hpBarChance;
    }

    public int LastHP( int level ){
        int hpBarChance = 0;
        switch (level){
            case 0:
                hpBarChance = 90;
                break;
            case 1:
                hpBarChance = 85;
                break;
            case 2:
                hpBarChance = 80;
                break;
            case 3:
                hpBarChance = 75;
                break;
        }
        return hpBarChance;
    }

    public boolean checkHpThreshold(int currentHP, int maxHP){
        if (maxHP <= 0) return false;
        float hpPercentage = (float)currentHP / maxHP;

        switch(level()){
            case 0:
                return hpPercentage >= 0.90f;
            case 1:
                return hpPercentage >= 0.85f;
            case 2:
                return hpPercentage >= 0.80f;
            case 3:
                return hpPercentage >= 0.75f;
            default:
                return false;
        }
    }
}

