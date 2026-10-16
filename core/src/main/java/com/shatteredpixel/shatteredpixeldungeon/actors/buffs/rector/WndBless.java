package com.shatteredpixel.shatteredpixeldungeon.actors.buffs.rector;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemSlot;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSettings;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

public class WndBless extends Window {
    private static final int WIDTH		= 100;
    private static final int BTN_SIZE	= 32;
    private static final int BTN_GAP	= 6;
    private static final int GAP		= 6;

    public static Item S1;
    public static Item S2;
    public static Item S3;
    public static Item S4;

    public static Item S5;
    public static Item S6;

    public WndBless(Belief belief) {

        S1 = new RectorSkills.CORRECT();
        S2 = hero.subClass == HeroSubClasses.DEAD_KNIGHT ? new RectorSkills.APOWER() : new RectorSkills.LIGHTIMUEE();
        S3 = hero.subClass == HeroSubClasses.DEAD_KNIGHT ? new RectorSkills.BACKMESSAGE() : new RectorSkills.CLEAN();
        S4 = hero.subClass == HeroSubClasses.DEAD_KNIGHT ? new RectorSkills.DEADMODE() : hero.subClass == HeroSubClasses.BATTLEPREIST ? new RectorSkills.BLESS() : new RectorSkills.PRAYERS();

        if(hero.subClass == HeroSubClasses.REDCARDINAL){
            S5 = new RectorSkills.HOLYFIRE();
            S6 = new RectorSkills.HOLYLAND();
        } else if(hero.subClass == HeroSubClasses.DEAD_KNIGHT){
            S5 = new RectorSkills.DEADMODE_X();
            S6 = new RectorSkills.NORMALMODE_X();
        }

        IconTitle titlebar = new IconTitle();
        titlebar.setRect(0, -3, WIDTH, 0);
        titlebar.icon(new BuffIcon(BuffIndicator.BLESS, true));
        titlebar.label(Messages.get(belief,"action_name"));
        add( titlebar );
        RenderedTextBlock message = PixelScene.renderTextBlock( (Messages.get(this,"select_skills")), 6 );
        message.maxWidth(WIDTH);
        message.setPos(0, titlebar.bottom() + GAP);
        add( message );

        RewardButton skills1 = new RewardButton( S1 );
        skills1.setRect( (WIDTH) / 2f - BTN_SIZE, message.top() + message.height() + BTN_GAP, BTN_SIZE,
                BTN_SIZE );
        add( skills1 );

        RewardButton skills2 = new RewardButton( S2 );
        skills2.setRect( skills1.right(), skills1.top(), BTN_SIZE, BTN_SIZE );
        add(skills2);

        RewardButton skills3 = new RewardButton( S3 );
        skills3.setRect( skills1.left(), skills1.bottom(), BTN_SIZE, BTN_SIZE );
        add(skills3);

        RewardButton skills4 = new RewardButton( S4 );
        skills4.setRect( skills3.right(), skills2.bottom(), BTN_SIZE, BTN_SIZE );
        add(skills4);

        if(hero.subClass == HeroSubClasses.REDCARDINAL) {
            RewardButton skills5 = new RewardButton(S5);
            skills5.setRect(skills1.left(), skills3.bottom(), BTN_SIZE, BTN_SIZE);
            add(skills5);
            
            RewardButton skills6 = new RewardButton(S6);
            skills6.setRect(skills5.right(), skills3.bottom(), BTN_SIZE, BTN_SIZE);
            add(skills6);
            resize(WIDTH, (int) skills5.bottom());
        } else if(hero.subClass == HeroSubClasses.DEAD_KNIGHT){
            StyledButton skills5 = new StyledButton(Chrome.Type.WINDOW,Messages.get(this, hero.rectorDeadKngithDeadMode ? "mode_d" : "mode_s")){
                @Override
                protected void onClick() {
                    add(new WndOptions(new Image(new ItemSprite(ItemSpriteSheet.DEADMODE)),
                            Messages.get(this, "mode"),
                            Messages.get(this, "mode_desc"),
                            Messages.get(this, "okay"),
                            Messages.get(this, "cancel")) {
                        @Override
                        protected void onSelect(int index) {
                            if (index == 0) {
                                if(!hero.rectorDeadKngithDeadMode){
                                    hero.rectorDeadKngithDeadMode = true;
                                }
                               WndBless.this.hide();
                               GLog.n(Messages.get(this,"dead"));
                            } else {
                                if(hero.rectorDeadKngithDeadMode){
                                    hero.rectorDeadKngithDeadMode = false;
                                }
                                WndBless.this.hide();
                                GLog.p(Messages.get(this,"normal"));
                            }
                        }
                    });
                }
            };
            skills5.setRect(skills1.left(), skills3.bottom()+5, 64, 20);
            add(skills5);
            if(hero.rectorDeadKngithDeadMode){
                skills5.textColor(0xff0000);
            } else {
                skills5.textColor(Window.TITLE_COLOR);
            }
            resize(WIDTH, (int) skills5.bottom());
        } else {
            resize(WIDTH, (int) skills3.bottom());
        }


    }

    private class RewardWindow extends WndInfoItem {
        boolean isDead = hero.subClass == HeroSubClasses.DEAD_KNIGHT;
        public RewardWindow( Item item ) {
            super(item);
            Belief creaditSkills = hero.buff(Belief.class);
            StyledButton btnConfirm = new StyledButton(Chrome.Type.RED_BUTTON,Messages.get(this, "ac_ask")){
                @Override
                protected void onClick() {
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
                    //惩戒
                    if(item == S1) {
                        if (hero.pointsInTalent(Talent.ACT_GODPROGRESS) >= 1  && creaditSkills != null && hero.buff(Talent.NoBeliefUsedCooldown.class) == null
                                && creaditSkills.credibility<5) {
                            creaditSkills.useSkills(Belief.SkillList.valueOf("CORRECT"));
                            WndBless.this.hide();
                        } else if(creaditSkills != null && creaditSkills.credibility>=5){
                            creaditSkills.useSkills(Belief.SkillList.valueOf("CORRECT"));
                            WndBless.this.hide();
                        } else if(hero.buff(Talent.NoBeliefUsedCooldown.class) != null){
                            GLog.n(Messages.get(WndBless.class,"not_talent"));
                        } else {
                            GLog.n(Messages.get(WndBless.class,"not_enough_credibility"));
                        }
                    //净化
                    } else if(item == S2){
                        if (hero.pointsInTalent(Talent.ACT_GODPROGRESS) >= 1 &&
                                creaditSkills != null && hero.buff(Talent.NoBeliefUsedCooldown.class) == null && creaditSkills.credibility< (isDead ? 4 : 12) ) {
                            Buff.affect(hero, Talent.NoBeliefUsedCooldown.class, cooldown);
                            if(isDead) {
                                creaditSkills.useSkills(Belief.SkillList.valueOf("DEADKILL"));
                                WndBless.this.hide();
                            } else {
                                creaditSkills.useSkills(Belief.SkillList.valueOf("LIGHTIMUEE"));
                                WndBless.this.hide();
                            }
                            if(hero.subClass != HeroSubClasses.REDCARDINAL){
                                hero.spend(1f);
                            }
                        } else if(creaditSkills != null && creaditSkills.credibility>=(isDead ? 4 : 12)){
                            if(isDead) {
                                creaditSkills.useSkills(Belief.SkillList.valueOf("DEADKILL"));
                                WndBless.this.hide();
                            } else {
                                creaditSkills.useSkills(Belief.SkillList.valueOf("LIGHTIMUEE"));
                                WndBless.this.hide();
                                creaditSkills.DownBelief(12);
                            }
                            if(hero.subClass != HeroSubClasses.REDCARDINAL){
                                hero.spend(1f);
                            }
                        } else if(hero.buff(Talent.NoBeliefUsedCooldown.class) != null){
                            GLog.n(Messages.get(WndBless.class,"not_talent"));
                        } else {
                            GLog.n(Messages.get(WndBless.class,"not_enough_credibility"));
                        }
                    } else if(item == S3){
                        if (hero.pointsInTalent(Talent.ACT_GODPROGRESS) >= 1 && creaditSkills != null
                                && hero.buff(Talent.NoBeliefUsedCooldown.class) == null  && creaditSkills.credibility<(isDead ? 12 : 15)) {

                            Buff.affect(hero, Talent.NoBeliefUsedCooldown.class,  cooldown);
                            if(isDead){
                                creaditSkills.useSkills(Belief.SkillList.valueOf("BACK"));
                                WndBless.this.hide();
                            } else {
                                creaditSkills.useSkills(Belief.SkillList.valueOf("CLEAN"));
                                WndBless.this.hide();
                            }
                            if(hero.subClass != HeroSubClasses.REDCARDINAL){
                                hero.spend(1f);
                            }
                        } else if (creaditSkills != null && creaditSkills.credibility>=(isDead ? 12 : 15)){
                            if(isDead){
                                creaditSkills.useSkills(Belief.SkillList.valueOf("BACK"));
                                WndBless.this.hide();
                                creaditSkills.DownBelief(12);
                            } else {
                                creaditSkills.useSkills(Belief.SkillList.valueOf("CLEAN"));
                                WndBless.this.hide();
                                creaditSkills.DownBelief(15);
                            }

                            if(hero.subClass != HeroSubClasses.REDCARDINAL){
                                hero.spend(1f);
                            }
                        } else if(hero.buff(Talent.NoBeliefUsedCooldown.class) != null){
                            GLog.n(Messages.get(WndBless.class,"not_talent"));
                        } else {
                            GLog.n(Messages.get(WndBless.class,"not_enough_credibility"));
                        }
                    //祷告
                    } else if(item == S4){
                        if (hero.pointsInTalent(Talent.ACT_GODPROGRESS) >= 1 && creaditSkills != null
                                && hero.buff(Talent.NoBeliefUsedCooldown.class) == null  && creaditSkills.credibility<(isDead ? 20 : (hero.subClass == HeroSubClasses.BATTLEPREIST ? 15 : 20))) {
                            Buff.affect(hero, Talent.NoBeliefUsedCooldown.class,  cooldown);

                            if(isDead){
                                creaditSkills.useSkills(Belief.SkillList.valueOf("ENDDEAD"));
                            } else {
                                creaditSkills.useSkills(hero.subClass == HeroSubClasses.BATTLEPREIST ? Belief.SkillList.valueOf("BATTLE") : Belief.SkillList.valueOf("PRAYERS"));
                                creaditSkills.DownBelief(hero.subClass == HeroSubClasses.BATTLEPREIST ? 15 : 20);
                            }

                            WndBless.this.hide();
                            if(hero.subClass != HeroSubClasses.REDCARDINAL){
                                hero.spend(1f);
                            }
                        } else if(creaditSkills != null && creaditSkills.credibility>=(isDead ? 20 : (hero.subClass == HeroSubClasses.BATTLEPREIST ? 15 : 20))) {
                            if(isDead){
                                creaditSkills.useSkills(Belief.SkillList.valueOf("ENDDEAD"));
                                creaditSkills.DownBelief(20);
                            } else {
                                creaditSkills.useSkills(hero.subClass == HeroSubClasses.BATTLEPREIST ? Belief.SkillList.valueOf("BATTLE") : Belief.SkillList.valueOf("PRAYERS"));
                                creaditSkills.DownBelief(hero.subClass == HeroSubClasses.BATTLEPREIST ? 15 : 20);
                            }
                            WndBless.this.hide();
                            if(hero.subClass != HeroSubClasses.REDCARDINAL){
                                hero.spend(1f);
                            }
                        } else if(hero.buff(Talent.NoBeliefUsedCooldown.class) != null){
                            GLog.n(Messages.get(WndBless.class,"not_talent"));
                        } else {
                            GLog.n(Messages.get(WndBless.class,"not_enough_credibility"));
                        }
                    }  else if(item == S5) {
                        if (hero.pointsInTalent(Talent.ACT_GODPROGRESS) >= 1 && creaditSkills != null && hero.buff(Talent.NoBeliefUsedCooldown.class) == null && creaditSkills.credibility<10) {
                            Buff.affect(hero, Talent.NoBeliefUsedCooldown.class,  cooldown);
                            creaditSkills.useSkills(Belief.SkillList.valueOf("HOLYFIRE"));
                            WndBless.this.hide();
                        } else if(creaditSkills != null && creaditSkills.credibility>=10) {
                            creaditSkills.useSkills(Belief.SkillList.valueOf("HOLYFIRE"));
                            WndBless.this.hide();
                        } else if(hero.buff(Talent.NoBeliefUsedCooldown.class) != null){
                            GLog.n(Messages.get(WndBless.class,"not_talent"));
                        } else {
                            GLog.n(Messages.get(WndBless.class,"not_enough_credibility"));
                        }
                    } else if(item == S6) {
                        if (hero.pointsInTalent(Talent.ACT_GODPROGRESS) >= 1 && creaditSkills != null && hero.buff(Talent.NoBeliefUsedCooldown.class) == null && creaditSkills.credibility<8) {
                            Buff.affect(hero, Talent.NoBeliefUsedCooldown.class,  cooldown);
                            creaditSkills.useSkills(Belief.SkillList.valueOf("HOLYLAND"));
                            WndBless.this.hide();
                        } else if(creaditSkills != null && creaditSkills.credibility>=8) {
                            creaditSkills.useSkills(Belief.SkillList.valueOf("HOLYLAND"));
                            WndBless.this.hide();
                        } else if(hero.buff(Talent.NoBeliefUsedCooldown.class) != null){
                            GLog.n(Messages.get(WndBless.class,"not_talent"));
                        } else {
                            GLog.n(Messages.get(WndBless.class,"not_enough_credibility"));
                        }

                    }
                {


                }
                    hide();
                    RewardWindow.this.hide();
                }
            };
            btnConfirm.setRect(0, height+2, width, 16);
            add(btnConfirm);

            resize(width, (int)btnConfirm.bottom());
        }
    }

    public class RewardButton extends Component {

        protected NinePatch bg;
        protected ItemSlot slot;

        public RewardButton( Item item ){
            bg = Chrome.get( Chrome.Type.WINDOW);
            add( bg );

            slot = new ItemSlot( item ){
                @Override
                protected void onPointerDown() {
                    bg.brightness( 1.2f );
                    Sample.INSTANCE.play( Assets.Sounds.CLICK );
                }
                @Override
                protected void onPointerUp() {
                    bg.resetColor();
                }
                @Override
                protected void onClick() {
                    ShatteredPixelDungeon.scene().addToFront(new RewardWindow(item));
                }
            };
            add(slot);
        }

        @Override
        protected void layout() {
            super.layout();

            bg.x = x;
            bg.y = y;
            bg.size( width, height );

            slot.setRect( x + 2, y + 2, width - 4, height - 4 );

        }
    }
}
