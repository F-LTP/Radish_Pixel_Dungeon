package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class RectorSkills extends Item {

    public static class CORRECT extends RectorSkills {
        {
            image = ItemSpriteSheet.CORRECT;
        }

        @Override
        public String name(){
            String name = "";
            if(Dungeon.hero.subClass == HeroSubClasses.BATTLEPREIST){
                name += Messages.get(this, "name_plus");
            }  else {
                name += Messages.get(this, "name");
            }
            return name;
        }

        @Override
        public String desc() {
            String desc = "";
            if(Dungeon.hero.subClass == HeroSubClasses.BATTLEPREIST){
                desc += Messages.get(this, "desc_plus", Dungeon.depth+10);
            } else {
                desc += Messages.get(this, "desc",12 + Dungeon.depth);
            }
            return desc;
        }
    }

    public static class LIGHTIMUEE extends RectorSkills {
        {
            image = ItemSpriteSheet.LIGHTIMUEE;
        }


        @Override
        public String name(){
            String name = "";
            if(Dungeon.hero.subClass == HeroSubClasses.BATTLEPREIST){
                name += Messages.get(this, "name_plus");
            }  else {
                name += Messages.get(this, "name");
            }
            return name;
        }

        @Override
        public String desc() {
            String desc = "";
            int level = Dungeon.depth/5*8;

            if(Dungeon.hero.subClass == HeroSubClasses.BATTLEPREIST){
                desc += Messages.get(this, "desc_plus",Dungeon.depth/5 *12 ,  Dungeon.depth+10,Dungeon.depth/5 * 4);
            }  else {
                desc += Messages.get(this, "desc",level, Dungeon.depth+10);
            }

            return desc;
        }
    }

    public static class CLEAN extends RectorSkills {
        {
            image = ItemSpriteSheet.CLEAN;
        }
    }

    public static class PRAYERS extends RectorSkills {
        {
            image = ItemSpriteSheet.PRAYERS;
        }
    }

    public static class BLESS extends RectorSkills {
        {
            image = ItemSpriteSheet.BLESS;
        }
    }

    //RED
    public static class HOLYFIRE extends RectorSkills {
        {
            image = ItemSpriteSheet.HOLYFIRE;
        }
    }

    public static class HOLYLAND extends RectorSkills {
        {
            image = ItemSpriteSheet.HOLYLAND;
        }
        public String desc(){
            return Messages.get(this, "desc", radius(),Dungeon.depth/5 == 0 ? 1 : Dungeon.depth/5);
        }

        private String radius() {
            String string;
            switch (Dungeon.hero.pointsInTalent(Talent.SKY_TOWER)) {
                case 1:
                   string = Messages.get(this, "radius1");
                   break;
                case 2:
                    string = Messages.get(this, "radius2");
                    break;
                case 3:
                    string = Messages.get(this, "radius3");
                    break;
                default:
                    string = Messages.get(this, "radius0");
                    break;
            }
            return string;
        }
    }

    //DEAD
    public static class APOWER extends RectorSkills {
        {
            image = ItemSpriteSheet.APOWER;
        }
    }

    public static class BACKMESSAGE extends RectorSkills {
        {
            image = ItemSpriteSheet.BACKMESSAGE;
        }
    }

    public static class DEADMODE extends RectorSkills {
        {
            image = ItemSpriteSheet.DEADMODE;
        }
    }

    //MODE SWITCH
    public static class DEADMODE_X extends RectorSkills {
        {
            image = ItemSpriteSheet.DEADMODE;
        }
    }

    public static class NORMALMODE_X extends RectorSkills {
        {
            image = ItemSpriteSheet.BLESS;
        }
    }

}
