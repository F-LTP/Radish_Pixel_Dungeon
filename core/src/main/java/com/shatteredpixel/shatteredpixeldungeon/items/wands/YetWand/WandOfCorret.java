package com.shatteredpixel.shatteredpixeldungeon.items.wands.YetWand;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.HalomethaneFire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector.Belief;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM100;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mimic;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.NPC;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.stats.DM100H;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageInfo;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PurpleParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.DamageWand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class WandOfCorret extends DamageWand {

    {
        image = ItemSpriteSheet.WAND_DISINTEGRATION;
        usesTargeting = true;
        collisionProperties = Ballistica.WONT_STOP;
    }

    @Override
    public void wandUsed() {
        super.wandUsed();
        if(hero.subClass == HeroSubClasses.REDCARDINAL){
            float timeToZap;
            timeToZap = -hero.cooldown();
            curUser.spendAndNext(timeToZap);
        }
    }


    public int min(int lvl){
        return hero.hasTalent(Talent.NOHOPE_LANG) && Dungeon.hero.HP < Dungeon.hero.HT/4 ? (int) (1.5f * 12 + Dungeon.depth) : 12 + Dungeon.depth;
    }

    public int max(int lvl){
        return hero.hasTalent(Talent.NOHOPE_LANG) && Dungeon.hero.HP < Dungeon.hero.HT/4 ? (int) (1.5f * 12 + Dungeon.depth) : 12 + Dungeon.depth;
    }

    @Override
    public int targetingPos(Hero user, int dst) {
        return dst;
    }

    @Override
    public void onZap(Ballistica beam) {

        int maxDistance = Math.min(distance(), beam.dist);

        ArrayList<Char> chars = new ArrayList<>();

        for (int c : beam.subPath(1, maxDistance)) {
            Char ch;
            if ((ch = Actor.findChar(c)) != null) {
                // 检查角色是否是被动状态且未被发现
                if (ch instanceof Mob && ((Mob) ch).state == ((Mob) ch).PASSIVE
                        && !(Dungeon.level.mapped[c] || Dungeon.level.visited[c])) {
                    // 避免伤害未被发现的被动角色
                } else {
                    chars.add(ch);
                }
            }

            CellEmitter.center(c).burst(PurpleParticle.BURST, Random.IntRange(1, 2));
        }

        int fixedDamage = 12 + Dungeon.depth;
        int fixedDamagePlus = 0;

        boolean damageDealt = false;
        for (Char ch : chars) {
            if (!damageDealt) {
                wandProc(ch, chargesPerCast());
                if (ch.properties().contains(Char.Property.DEMONIC) || ch.properties().contains(Char.Property.UNDEAD)) {
                    fixedDamage = (int) (fixedDamage * 1.25f);
                }
                float x = ch.sprite.center().x;
                float y = ch.sprite.center().y;
                ch.sprite.parent.add(new Lightning(ch.sprite.center(), new PointF( x, y-300f),null));
                ch.sprite.parent.add(new Lightning(new PointF(x-5f, y), new PointF( x-5f, y-300f),null));
                ch.sprite.parent.add(new Lightning(new PointF(x+5f, y), new PointF( x+5f, y-300f),null));
                Sample.INSTANCE.play( Assets.Sounds.LIGHTNING, 1.5f);
                ch.damage(new DamageInfo(fixedDamage + fixedDamagePlus, DamageType.LIGHTNING, curUser, this, this));
                ch.sprite.centerEmitter().burst( SparkParticle.FACTORY, 32 );
                ch.sprite.flash();
                damageDealt = true;
                if (Dungeon.hero.pointsInTalent(Talent.FIRE_GLASS) > 0 ){
                    GameScene.add(Blob.seed(ch.pos, 2, HalomethaneFire.class));
                }
            }
        }

        Belief creaditSkills = Dungeon.hero.buff(Belief.class);
        if (hero.pointsInTalent(Talent.ACT_GODPROGRESS) >= 1 && creaditSkills != null && hero.buff(Talent.NoBeliefUsedCooldown.class) == null && creaditSkills.credibility<5) {
            float cooldown;
            switch (hero.pointsInTalent(Talent.ACT_GODPROGRESS)){
                default:
                case 1:
                    cooldown = 500f;
                    break;
                case 2:
                    cooldown = 425f;
                    break;
                case 3:
                    cooldown = 350f;
                    break;
            }
            Buff.affect(hero, Talent.NoBeliefUsedCooldown.class, cooldown);
        } else if(creaditSkills!= null && !(hero.subClass == HeroSubClasses.BATTLEPREIST)) {
            creaditSkills.DownBelief(5);
        }

        if (hero.hasTalent(Talent.DEVOTIONAL)){
            Buff.affect(hero, Barrier.class).setShield(4 * hero.pointsInTalent(Talent.DEVOTIONAL));
            hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(4 * hero.pointsInTalent(Talent.DEVOTIONAL)), FloatingText.SHIELDING );
        }
    }

    @Override
    public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
        //no direct effect, see magesStaff.reachfactor
    }

    private int distance() {
        return buffedLvl()*2 + 6;
    }

    @Override
    public void fx(Ballistica beam, Callback callback) {
        int cell = beam.path.get(Math.min(beam.dist, distance()));
        curUser.sprite.parent.add(new Lightning(hero.sprite.center(), cell, callback));
    }

    @Override
    public void staffFx(MagesStaff.StaffParticle particle) {
        particle.color(0x220022);
        particle.am = 0.6f;
        particle.setLifespan(1f);
        particle.acc.set(10, -10);
        particle.setSize( 0.5f, 3f);
        particle.shuffleXY(1f);
    }

}
