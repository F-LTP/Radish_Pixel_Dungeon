package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.Druid;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.shatteredpixel.shatteredpixeldungeon.Assets;

/**
 * 德鲁伊技能面板（点击侧边栏自然能量图标打开）。
 * 排布与牧师（Rector）的 {@code WndBless} 一致：两列图标格，点格子后弹确认。
 */
public class WndDruidSkills extends Window {

	private static final int WIDTH = 100;
	private static final int BTN_SIZE = 32;
	private static final int BTN_GAP = 6;
	private static final int GAP = 6;

	private static final String[] KEYS = {
			"boar", "wolves", "bear", "oak_sage", "vines",
			"sunflower", "enchant_arrow", "firestorm", "thundercloud", "cataclysm"
	};
	private static final int[] COSTS = { 12, 16, 20, 15, 10, 30, 5, 10, 16, 30 };

	public WndDruidSkills( Druid.DruidEnergy energy ) {

		IconTitle titlebar = new IconTitle();
		titlebar.setRect( 0, -3, WIDTH, 0 );
		titlebar.icon( new HeroIcon( HeroIcon.DRUID_SKILL ) );
		titlebar.label( Messages.get( this, "title" ) );
		add( titlebar );

		RenderedTextBlock message = PixelScene.renderTextBlock(
				Messages.get( this, "energy", Math.round( Druid.energy( Dungeon.hero ) ), (int) Druid.maxEnergy( Dungeon.hero ) ), 6 );
		message.maxWidth( WIDTH );
		message.setPos( 0, titlebar.bottom() + GAP );
		add( message );

		float top = message.top() + message.height() + BTN_GAP;

		for (int i = 0; i < KEYS.length; i++) {
			float x = (i % 2 == 0) ? (WIDTH / 2f - BTN_SIZE) : (WIDTH / 2f);
			float y = top + (i / 2) * (BTN_SIZE + BTN_GAP);
			SkillButton b = new SkillButton( i );
			b.setRect( x, y, BTN_SIZE, BTN_SIZE );
			add( b );
		}

		resize( WIDTH, (int) (top + ((KEYS.length + 1) / 2) * (BTN_SIZE + BTN_GAP)) );
	}

	private class SkillButton extends com.shatteredpixel.shatteredpixeldungeon.ui.Button {

		private final int index;

		protected NinePatch bg;
		protected Image icon;

		SkillButton( int index ) {
			this.index = index;

			bg = Chrome.get( Chrome.Type.WINDOW );
			add( bg );

			// 占位图标（待替换为正式技能图标）
			icon = new HeroIcon( HeroIcon.DRUID_SKILL );
			add( icon );
		}

		@Override
		protected void layout() {
			super.layout();

			bg.x = x;
			bg.y = y;
			bg.size( width, height );

			icon.x = x + (width - icon.width()) / 2f;
			icon.y = y + (height - icon.height()) / 2f;
		}

		@Override
		protected void onClick() {
			GameScene.show( new WndOptions(
					Messages.get( WndDruidSkills.this, KEYS[index] ),
					Messages.get( WndDruidSkills.this, "cost", COSTS[index] ),
					Messages.get( WndDruidSkills.this, "cast" ),
					Messages.get( WndDruidSkills.this, "cancel" ) ) {
				@Override
				protected void onSelect( int i ) {
					if (i == 0) {
						hide();
						WndDruidSkills.this.hide();
						runSkill( index );
					}
				}
			} );
		}
	}

	private void runSkill( int i ) {
		Hero hero = Dungeon.hero;
		if (hero == null) {
			return;
		}
		boolean ok;
		switch (i) {
			case 0: ok = Druid.summonBoar( hero ); break;
			case 1: ok = Druid.summonWolves( hero ); break;
			case 2: ok = Druid.summonBear( hero ); break;
			case 3: ok = Druid.summonOakSage( hero ); break;
			case 4: ok = Druid.summonVines( hero ); break;
			case 5: ok = Druid.summonSunflower( hero ); break;
			case 6: ok = Druid.castEnchantArrow( hero ); break;
			case 7: Druid.promptTargeted( hero, 0 ); return;
			case 8: Druid.promptTargeted( hero, 1 ); return;
			case 9: Druid.promptTargeted( hero, 2 ); return;
			default: return;
		}
		if (ok) {
			Sample.INSTANCE.play( Assets.Sounds.READ );
			hero.sprite.operate( hero.pos );
			hero.spendAndNext( 1f );
		} else {
			GLog.w( Messages.get( this, "no_energy" ) );
		}
	}
}
