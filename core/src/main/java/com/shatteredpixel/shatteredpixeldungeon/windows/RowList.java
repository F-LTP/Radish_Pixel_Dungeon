package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

/**
 * 「一行一个按钮」的列表，拾取选择窗口（WndHeapSelect）和格子内容窗口（WndCellContents）共用。
 *
 * 一格上有多少件东西就有多少行，玩家把背包倒在一格上时行数没有上限。
 * 放得下时各行就是窗口里的普通按钮；一屏放不下时整列放进滚动面板——
 * 否则窗口会比屏幕还高，标题和排在两头的行画到屏幕外面，点不到。
 */
final class RowList {

	static final int ROW_HEIGHT = 18;
	static final int GAP = 2;

	private RowList() {}

	/** 列表里的一行。 */
	static class Row extends RedButton {

		//这一行是否放在滚动面板里。是的话关掉按钮自己的点击热区，由面板统一分发点击（见 clickAt）：
		//按钮自己接事件的话，按在按钮上的那一下会被按钮截走，列表就拖不动了
		private boolean inScrollPane = false;

		Row( String label, int size ) {
			super( label, size );
		}

		@Override
		protected void layout() {
			super.layout();
			if (inScrollPane) {
				hotArea.width = hotArea.height = 0;
			}
		}

		//滚动面板把「点了一下」（没有拖动）的位置报过来，落在这一行上就算点了这一行
		private boolean clickAt( float x, float y ) {
			if (!inside( x, y )) return false;
			if (active) {
				//按钮自己处理按下时会响这一声，这里热区关掉了，补上
				Sample.INSTANCE.play( Assets.Sounds.CLICK );
				onClick();
			}
			return true;
		}
	}

	/** 窗口内容区最多能有多高：整个窗口（含边框）不超过屏幕高度的九成。 */
	static int maxContentHeight( NinePatch chrome ) {
		return (int)(PixelScene.uiCamera.height * 0.9f) - chrome.marginVer();
	}

	/**
	 * 把说明文字（可以为 null）和各行从 top 开始往下排进窗口，返回排完之后内容区的高度，窗口应当 resize 到这个高度。
	 * 总高度超过 maxHeight 时，top 以下的部分放进滚动面板，返回值不超过 maxHeight。
	 */
	static int place( Window wnd, RenderedTextBlock text, final ArrayList<Row> rows, float top, int width, int maxHeight ) {

		float needed = top;
		if (text != null) needed += text.height() + GAP;
		needed += rows.size() * (ROW_HEIGHT + GAP);
		if (text != null || !rows.isEmpty()) needed -= GAP;

		if (needed <= maxHeight) {

			float pos = top;
			if (text != null) {
				text.setPos( GAP, pos );
				wnd.add( text );
				pos = text.bottom() + GAP;
			}
			for (Row row : rows) {
				row.setRect( 0, pos, width - GAP, ROW_HEIGHT );
				wnd.add( row );
				pos = row.bottom() + GAP;
			}
			return (int) needed;

		} else {

			ScrollPane pane = new ScrollPane( new Component() ) {
				@Override
				public void onClick( float x, float y ) {
					for (Row row : rows) {
						if (row.clickAt( x, y )) return;
					}
				}
			};
			wnd.add( pane );

			//面板里的坐标从 0 起算
			Component content = pane.content();
			float pos = 0;
			if (text != null) {
				text.setPos( GAP, pos );
				content.add( text );
				pos = text.bottom() + GAP;
			}
			for (Row row : rows) {
				row.inScrollPane = true;
				row.setRect( 0, pos, width - GAP, ROW_HEIGHT );
				content.add( row );
				pos = row.bottom() + GAP;
			}
			content.setSize( width, pos - GAP );

			//面板的可视高度取整：内容相机的高度是整数，带小数的话最后一行滚到底时会被裁掉不到一个像素的一条边
			int paneHeight = (int)(maxHeight - top);
			pane.setRect( 0, top, width, paneHeight );
			pane.scrollTo( 0, 0 );
			return (int)Math.ceil( top + paneHeight );
		}
	}
}
