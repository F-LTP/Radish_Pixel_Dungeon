/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.input.GameAction;
import com.watabou.input.KeyBindings;
import com.watabou.input.KeyEvent;
import com.watabou.input.PointerEvent;
import com.watabou.input.ScrollEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.ScrollArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.GameMath;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;
import com.watabou.utils.Signal;

public class ScrollPane extends Component {

	protected static final int THUMB_COLOR		= 0xFF7b8073;
	protected static final float THUMB_ALPHA	= 0.5f;

	protected PointerController controller;
	protected Signal.Listener<KeyEvent> keyListener;
	protected Component content;
	protected ColorBlock thumb;

	/** 滚动条在无操作后自动隐藏。 */
	private float thumbTimer = 0f;
	private static final float THUMB_HOLD = 1.2f;

	private float keyScroll = 0;

	public ScrollPane( Component content ) {
		super();

		this.content = content;
		addToBack( content );

		width = content.width();
		height = content.height();

		content.camera = new Camera( 0, 0, 1, 1, PixelScene.defaultZoom );
		Camera.add( content.camera );

		KeyEvent.addKeyListener(keyListener = new Signal.Listener<KeyEvent>() {
			@Override
			public boolean onSignal(KeyEvent keyEvent) {
				GameAction action = KeyBindings.getActionForKey(keyEvent);
				if (action == SPDAction.ZOOM_IN){
					if (keyEvent.pressed){
						keyScroll += 1;
					} else {
						keyScroll -= 1;
					}
					keyScroll = GameMath.gate(-1f, keyScroll, +1f);
					return true;
				} else if (action == SPDAction.ZOOM_OUT){
					if (keyEvent.pressed){
						keyScroll -= 1;
					} else {
						keyScroll += 1;
					}
					keyScroll = GameMath.gate(-1f, keyScroll, +1f);
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public void destroy() {
		super.destroy();
		Camera.remove( content.camera );
		KeyEvent.removeKeyListener(keyListener);
	}

	public void scrollTo( float x, float y ) {
		Camera c = content.camera;
		c.scroll.set( x, y );
		clampScroll();
	}

	/** 相对滚动：滚轮、拖动、缩略条都用这一条路径，保证边界与缩略条位置永远一致。 */
	public void scrollBy( float dx, float dy ) {
		Camera c = content.camera;
		c.scroll.x += dx;
		c.scroll.y += dy;
		clampScroll();
	}

	public void setScrollY( float y ) {
		content.camera.scroll.y = y;
		clampScroll();
	}

	private void clampScroll() {
		Camera c = content.camera;
		float maxX = Math.max( 0, content.width()  - width );
		float maxY = Math.max( 0, content.height() - height );
		c.scroll.x = GameMath.gate( 0, c.scroll.x, maxX );
		c.scroll.y = GameMath.gate( 0, c.scroll.y, maxY );
		updateThumb();
	}

	/**
	 * 缩略条与 ScrollPane 画在同一个相机里，子节点并不相对父节点定位，
	 * 所以它的坐标要从面板自己的左上角 (x, y) 算起（原版也是这样）；少了这一步，
	 * 面板不在窗口原点时缩略条就画到面板外面去了。
	 */
	protected void updateThumb() {
		if (thumb == null) return;
		// 只有内容比视口高、且刚刚滚动过（thumbTimer 还在）时才露出来；
		// 最小高度 12 可能比按比例算出的高度大，位置要夹在视口内，否则滚到底时缩略条会被推出面板下沿。
		if (thumbTimer > 0 && height < content.height()) {
			thumb.visible = true;
			thumb.scale.set( 3, Math.max( 12, height * height / content.height() ) );
			thumb.x = x + width - thumb.width() - 1;
			thumb.y = y + Math.min( height - thumb.height(),
					height * content.camera.scroll.y / content.height() );
		} else {
			thumb.visible = false;
		}
	}

	@Override
	public synchronized void update() {
		super.update();

		// 窗口刚创建时 camera() 还是 null，layout() 会直接返回，导致视口尺寸/相机没被摆好，
		// 之后又没人再排一次 —— 表现就是“没有滚动条、也滑不动”。
		// 这里每帧核对一次：视口尺寸或内容相机位置跟窗口对不上就重排。
		if (content != null && content.camera != null && camera() != null) {
			Point p = camera().cameraToScreen( x, y );
			if (content.camera.width != (int)width || content.camera.height != (int)height
					|| content.camera.x != p.x || content.camera.y != p.y) {
				layout();
			}
		}

		if (keyScroll != 0){
			scrollTo(content.camera.scroll.x, content.camera.scroll.y + (keyScroll * 150 * Game.elapsed));
		}
		if (thumbTimer > 0){
			thumbTimer -= Game.elapsed;
			if (thumbTimer <= 0){
				thumbTimer = 0;
				thumb.am = 0;
				// 淡出后要真的隐藏：按下判定只看 visible，只把透明度归零的话，
				// 看不见的缩略条还会把落在它身上的点击吃掉（onClick 收不到）
				thumb.visible = false;
			} else {
				thumb.am = THUMB_ALPHA * Math.min(1f, thumbTimer / 0.3f);
			}
		}
	}

	/** 显示滚动条，并重置自动隐藏计时。 */
	protected void showThumb(){
		thumbTimer = THUMB_HOLD;
		if (thumb != null){
			// 亮出来之前先按当前滚动量摆好：计时归零期间没人更新它的位置和大小
			updateThumb();
			thumb.am = THUMB_ALPHA;
		}
	}

	@Override
	protected void createChildren() {
		controller = new PointerController();
		add( controller );

		thumb = new ColorBlock( 1, 1, THUMB_COLOR );
		thumb.am = 0;
		add( thumb );
	}

	@Override
	protected void layout() {

		// 窗口尚未挂到场景时 camera() 为 null，调用 cameraToScreen 会 NPE
		if (camera() == null) {
			return;
		}

		content.setPos( 0, 0 );
		// 控制器是 ScrollPane 自己的子节点，但命中判定用的是父级相机坐标
		// （Visual.overlapsScreenPoint → screenToCamera → overlapsPoint）。
		// 所以控制器矩形必须落在视口在父级相机里的位置上，不能用 (0,0)：
		// 背包视口的 y=TITLE_HEIGHT，用 0 会让内容下半截收不到按下事件，表现就是“滑不动”。
		controller.x = x;
		controller.y = y;
		controller.width = width;
		controller.height = height;

		Point p = camera().cameraToScreen( x, y );
		Camera cs = content.camera;
		cs.x = p.x;
		cs.y = p.y;
		cs.resize( (int)width, (int)height );

		thumb.visible = height < content.height();
		if (thumb.visible) {
			updateThumb();
			if (thumbTimer <= 0) thumb.am = 0;
		}
	}

	public Component content() {
		return content;
	}

	public void onClick( float x, float y ) {
	}

	public class PointerController extends ScrollArea {

		private float dragThreshold;

		public PointerController() {
			super( 0, 0, 0, 0 );
			dragThreshold = PixelScene.defaultZoom * 8;
			// 不阻挡点击：内容里的按钮/物品格必须能收到 DOWN/UP
			blockLevel = NEVER_BLOCK;
		}

		@Override
		protected void onScroll(ScrollEvent event) {
			// 滚轮直接按当前滚动量相对移动。之前是拿 lastPos（只有拖动时才更新）当参照，
			// 桌面端纯滚轮时 lastPos 还是 (0,0)，一滚就整页跳走。
			if (event.amount != 0) {
				scrollBy( 0, event.amount * Math.max( 1, height ) * 0.5f );
				showThumb();
			}
		}

		protected void onPointerDown( PointerEvent event ) {
			// 按住缩略条 = 直接拖滚动条（桌面端和手机都适用）
			if (thumb != null && thumb.visible) {
				PointF p = cameraPoint( event.current );
				if (p.x >= thumb.x - 2 && p.x <= thumb.x + thumb.width() + 2
						&& p.y >= thumb.y - 2 && p.y <= thumb.y + thumb.height() + 2) {
					thumbDragging = true;
					thumbStartY = p.y;
					thumbScrollStart = content.camera.scroll.y;
					showThumb();
					return;
				}
			}
			lastPos.set( event.current );
		}

		@Override
		protected void onPointerUp( PointerEvent event ) {
			if (thumbDragging) {
				thumbDragging = false;
				showThumb();
				return;
			}

			if (dragging) {

				dragging = false;
				showThumb();

			} else {

				PointF p = content.camera.screenToCamera( (int) event.current.x, (int) event.current.y );
				ScrollPane.this.onClick( p.x, p.y );

			}
		}

		private boolean dragging = false;
		private boolean thumbDragging = false;
		private float thumbStartY = 0;
		private float thumbScrollStart = 0;
		private PointF lastPos = new PointF();

		// 面板所在相机里的点：与缩略条的坐标是同一套，可以直接比较
		private PointF cameraPoint( PointF screen ){
			return camera().screenToCamera( (int)screen.x, (int)screen.y );
		}

		@Override
		protected void onDrag( PointerEvent event ) {

			if (thumbDragging) {
				float track = Math.max( 1, height );
				float thumbH = thumb.height();
				float maxScroll = Math.max( 1, content.height() - height );
				float maxThumbY = Math.max( 1, track - thumbH );
				float dy = cameraPoint( event.current ).y - thumbStartY;
				setScrollY( thumbScrollStart + dy * maxScroll / maxThumbY );
				showThumb();
				return;
			}

			if (dragging) {

				scroll(event.current);
				showThumb();

			} else if (PointF.distance( event.current, event.start ) > dragThreshold) {

				dragging = true;
				lastPos.set( event.current );
				thumb.am = 1;
				showThumb();

			}
		}

		private void scroll( PointF current ){

			PointF diff = PointF.diff( lastPos, current ).invScale( content.camera.zoom );
			//拖动过程中必须每帧更新参照点，否则每次都拿"按下那一瞬间"的位置去算位移，
			//手指一动位移就成倍累积，内容会直接闪到另一头
			lastPos.set( current );
			scrollBy( diff.x, diff.y );
		}
		
	}
}
