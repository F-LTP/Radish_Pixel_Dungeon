package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.definition.subclasses.Druid;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.DeviceCompat;

import java.util.ArrayList;

/**
 * 德鲁伊法术书。
 *
 * <p>布局与破碎的 {@code WndClericSpells} 一致：120 宽，法术按阶分行，
 * 每行居中排列 20×20 图标；右上角按钮在「施放 / 说明」之间切换；
 * 点图标直接施放，长按或右键查看说明。</p>
 */
public class WndDruidSkills extends Window {

	protected static final int WIDTH = 120;
	public static int BTN_SIZE = 20;

	private static final String[] KEYS = {
			"boar", "wolves", "bear", "oak_sage", "vines",
			"sunflower", "enchant_arrow", "firestorm", "thundercloud", "cataclysm"
	};
	private static final int[] COSTS = { 12, 16, 20, 15, 10, 30, 5, 10, 16, 30 };
	private static final int[][] TIERS = {
			{ 0, 1, 2 },
			{ 3, 4, 5 },
			{ 6, 7, 8, 9 }
	};

	// COSTS 是基础消耗，实际扣多少还要看天赋：动物伙伴让三种动物加价，元素掌握让四个法术减价。
	// 能否施放、悬停提示都必须用这个值，否则会出现「按钮是亮的却放不出」或「够能量却被拒绝」。
	private static int realCost( int index ) {
		Hero hero = Dungeon.hero;
		if (index <= 2) {
			return (int) Druid.animalCost( hero, COSTS[index] );
		} else if (index >= 6) {
			return (int) Druid.spellCost( hero, COSTS[index] );
		}
		return COSTS[index];
	}

	// 说明文字里写的是基础消耗；天赋改变了消耗时在末尾补一行当前的实际数值
	private String descOf( int index ) {
		String desc = Messages.get( this, KEYS[index] + ".desc" );
		int cost = realCost( index );
		if (cost != COSTS[index]) {
			desc += "\n\n" + Messages.get( this, "cost_talent", cost );
		}
		return desc;
	}

	private final Druid.DruidEnergy energy;

	public WndDruidSkills( Druid.DruidEnergy energy, boolean info ) {
		this.energy = energy;

		IconTitle title;
		if (!info) {
			title = new IconTitle( new HeroIcon( HeroIcon.DRUID_SKILL ),
					Messages.titleCase( Messages.get( this, "cast_title" ) ) );
		} else {
			title = new IconTitle( Icons.INFO.get(),
					Messages.titleCase( Messages.get( this, "info_title" ) ) );
		}
		title.setRect( 0, 0, WIDTH, 0 );
		add( title );

		IconButton btnInfo = new IconButton( info ? new HeroIcon( HeroIcon.DRUID_SKILL ) : Icons.INFO.get() ) {
			@Override
			protected void onClick() {
				GameScene.show( new WndDruidSkills( energy, !info ) );
				hide();
			}
		};
		btnInfo.setRect( WIDTH - 16, 0, 16, 16 );
		add( btnInfo );

		RenderedTextBlock msg;
		if (info) {
			msg = PixelScene.renderTextBlock( Messages.get( this, "info_desc" ), 6 );
		} else {
			// 向下取整，和快捷栏按钮上的数字一致；四舍五入会出现「显示 12 却放不出 12 点的法术」
			String energyLine = Messages.get( this, "energy",
					(int) Druid.energy( Dungeon.hero ), (int) Druid.maxEnergy( Dungeon.hero ) );
			String hint = DeviceCompat.isDesktop()
					? Messages.get( this, "cast_desc_desktop" )
					: Messages.get( this, "cast_desc_mobile" );
			msg = PixelScene.renderTextBlock( energyLine + "\n" + hint, 6 );
		}
		msg.maxWidth( WIDTH );
		msg.setPos( 0, title.bottom() + 4 );
		add( msg );

		int top = (int) msg.bottom() + 4;

		for (int t = 0; t < TIERS.length; t++) {
			if (t > 0) {
				top += BTN_SIZE + 2;
				ColorBlock sep = new ColorBlock( WIDTH, 1, 0xFF000000 );
				sep.y = top;
				add( sep );
				top += 3;
			}

			ArrayList<IconButton> btns = new ArrayList<>();
			for (int idx : TIERS[t]) {
				IconButton b = new SpellButton( idx, info );
				add( b );
				btns.add( b );
			}

			int left = 2 + (WIDTH - btns.size() * (BTN_SIZE + 4)) / 2;
			for (IconButton b : btns) {
				b.setRect( left, top, BTN_SIZE, BTN_SIZE );
				left += (int) b.width() + 4;
			}
		}

		resize( WIDTH, top + BTN_SIZE );

		if (SPDSettings.interfaceSize() != 2) {
			offset( 0, (int) (GameScene.uiCamera.height / 2 - 30 - height / 2) );
		}
	}

	public class SpellButton extends IconButton {

		private final int index;
		private final boolean info;
		private NinePatch bg;

		public SpellButton( int index, boolean info ) {
			super( new HeroIcon( HeroIcon.DRUID_SKILL ) );
			this.index = index;
			this.info = info;

			if (!canCast()) {
				icon.alpha( 0.3f );
			}

			bg = Chrome.get( Chrome.Type.TOAST );
			addToBack( bg );
		}

		private boolean canCast() {
			return Druid.energy( Dungeon.hero ) >= realCost( index );
		}

		@Override
		protected void onPointerUp() {
			super.onPointerUp();
			icon.alpha( canCast() ? 1f : 0.3f );
		}

		@Override
		protected void layout() {
			super.layout();
			if (bg != null) {
				bg.size( width, height );
				bg.x = x;
				bg.y = y;
			}
		}

		@Override
		protected void onClick() {
			if (info) {
				GameScene.show( new WndTitledMessage( new HeroIcon( HeroIcon.DRUID_SKILL ),
						Messages.titleCase( Messages.get( WndDruidSkills.this, KEYS[index] ) ),
						descOf( index ) ) );
				return;
			}
			hide();
			if (!canCast()) {
				GLog.w( Messages.get( WndDruidSkills.this, "no_energy" ) );
				return;
			}
			runSkill( index );
		}

		@Override
		protected boolean onLongClick() {
			GameScene.show( new WndTitledMessage( new HeroIcon( HeroIcon.DRUID_SKILL ),
					Messages.titleCase( Messages.get( WndDruidSkills.this, KEYS[index] ) ),
					descOf( index ) ) );
			return true;
		}

		@Override
		protected String hoverText() {
			return "_" + Messages.titleCase( Messages.get( WndDruidSkills.this, KEYS[index] ) ) + "_\n"
					+ Messages.get( WndDruidSkills.this, "cost", realCost( index ) );
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
		// 没放出来时不在这里提示：原因不止能量不足（还可能是周围没有空位），由 Druid 里的各个入口自己说明
		if (ok) {
			Sample.INSTANCE.play( Assets.Sounds.READ );
			hero.sprite.operate( hero.pos );
			hero.spendAndNext( 1f );
		}
	}
}
