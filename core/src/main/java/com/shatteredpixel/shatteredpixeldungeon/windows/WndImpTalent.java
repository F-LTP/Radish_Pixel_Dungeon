package com.shatteredpixel.shatteredpixeldungeon.windows;

import static com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.detach;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.custom.testmode.generator.TestTalentOFTerminalBook;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.TengusMask;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.DwarfToken;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;

public class WndImpTalent extends Window {

    private static final int WIDTH		= 130;
    private static final float GAP		= 2;

    public WndImpTalent(final TestTalentOFTerminalBook tome ) {

        super();

        IconTitle titlebar = new IconTitle();
        titlebar.icon( new ItemSprite( tome.image(), null ) );
        titlebar.label( tome.name() );
        titlebar.setRect( 0, 0, WIDTH, 0 );
        add( titlebar );

        RenderedTextBlock message = PixelScene.renderTextBlock( 6 );
        message.text( Messages.get(this, "message"), WIDTH );
        message.setPos( titlebar.left(), titlebar.bottom() + GAP );
        add( message );

        float pos = message.bottom() + 3*GAP;
        TestTalentOFTerminalBook tokens = Dungeon.hero.belongings.getItem( TestTalentOFTerminalBook.class );
        RedButton btnCls = new RedButton( Dungeon.hero.subClass.shortDesc(), 6 ) {
            @Override
            protected void onClick() {
                Hero hero = Dungeon.hero;
                hero.powerOfImp = true;
                Buff.affect(hero, WndImp.powerGainTracker.class);
                hero.spend(Actor.TICK);
                hero.busy();
                Talent.initT4Talents(hero);

                hero.sprite.operate(hero.pos);
                Sample.INSTANCE.playDelayed(Assets.Sounds.LEVELUP, 0.3f, 0.7f, 1.2f);
                Sample.INSTANCE.playDelayed(Assets.Sounds.LEVELUP, 0.6f, 0.7f, 1.2f);

                Emitter e = hero.sprite.centerEmitter();
                e.pos(e.x - 2, e.y - 6, 4, 4);
                e.start(Speck.factory(Speck.MASK), 0.05f, 20);
                GLog.p(Messages.get(TestTalentOFTerminalBook.class,"power_mode_on",hero.name()));
                hero.sprite.operate( hero.pos );
                tokens.detach( hero.belongings.backpack );
                WndImpTalent.this.hide();
            }
        };

        btnCls.leftJustify = true;
        btnCls.multiline = true;
        btnCls.setSize(WIDTH-20, btnCls.reqHeight()+2);
        btnCls.setRect( 0, pos, WIDTH-20, btnCls.reqHeight()+2);
        add( btnCls );

        IconButton clsInfo = new IconButton(Icons.get(Icons.INFO)){
            @Override
            protected void onClick() {
                GameScene.show(new WndInfoImpSuperTalent(Dungeon.hero.heroClass,Dungeon.hero.subClass));
            }
        };
        clsInfo.setRect(WIDTH-20, btnCls.top() + (btnCls.height()-20)/2, 20, 20);
        add(clsInfo);

        pos = btnCls.bottom() + GAP;

        RedButton btnCancel = new RedButton( Messages.get(this, "cancel") ) {
            @Override
            protected void onClick() {
                hide();
            }
        };
        btnCancel.setRect( 0, pos, WIDTH, 18 );
        add( btnCancel );

        resize( WIDTH, (int)btnCancel.bottom() );
    }
}

