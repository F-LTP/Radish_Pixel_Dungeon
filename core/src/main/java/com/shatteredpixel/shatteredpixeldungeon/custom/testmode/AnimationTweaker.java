package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.input.KeyEvent;
import com.watabou.noosa.*;
import com.watabou.noosa.ui.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏内动画调整器（test mode）。
 * 这个代码是纯ai写的。出bug直接弃用就是了。
 *
 * <p>用于为角色制作/调试皮肤动画：加载任意贴图路径的 sprite，默认按 12x16
 * 帧格划分，自由挑选若干帧组成动画序列并实时预览播放；可为每个贴图帧
 * 指定一张 affix（贴附特效帧）——贴图可大于 12x16，整图叠在角色帧上方渲染，
 * 并以像素偏移贴附到角色帧的指定位置。</p>
 *
 * <p>支持<b>多个命名动画</b>：每个动画各自持有 帧序列 + fps + 循环 + 逐帧 affix。
 * 可新增/重命名/删除/切换当前编辑的动画，并可把全部动画<b>输出为 Java 代码</b>
 * （粘贴进精灵类即可）或<b>从粘贴的 Java 代码加载</b>回本工具。</p>
 *
 * <p>操作：编辑窗口内 上一帧/下一帧 手动步进（暂停时逐帧绑定 affix）；
 * 方向键微调当前帧 affix 偏移（Shift=×10，Ctrl=×0.1）；Enter 打印偏移，
 * Backspace 清零；[ / ] 缩小/放大步长。</p>
 */
public class AnimationTweaker extends TestItem {

	// ==================== 共享状态 ====================

	/** 当前贴图路径（相对 assets，如 sprites/RadishSnDSprite/gambler.png）。 */
	public static String sheetPath = null;
	public static SmartTexture texture;
	public static TextureFilm film;
	public static int frameW = 12;
	public static int frameH = 16;
	public static int cols;
	public static int rows;
	public static int totalFrames;

	/** 所有命名动画（顺序即显示顺序）。 */
	public static final List<AnimDef> anims = new ArrayList<>();

	/** 当前正在编辑/预览的动画下标。 */
	public static int currentAnimIndex = 0;

	/** 偏移调整步长（帧内像素）。 */
	public static float step = 1f;

	/** 预览是否在播放。 */
	public static boolean playing = true;

	/** 预览当前显示的贴图帧下标（-1 表示无）。 */
	public static int curSheetFrame = -1;

	public static class Affix {
		public String path;
		public float offX, offY;
		public Affix( String path, float offX, float offY ) {
			this.path = path;
			this.offX = offX;
			this.offY = offY;
		}
	}

	/** 一个命名动画：帧序列 + fps + 循环 + 逐帧 affix。 */
	public static class AnimDef {
		public String name = "anim";
		public int fps = 12;
		public boolean looping = true;
		public final List<Integer> frames = new ArrayList<>();
		public final Map<Integer, Affix> affixes = new LinkedHashMap<>();

		AnimDef() {
		}

		AnimDef( String name, int fps, boolean looping ) {
			this.name = name;
			this.fps = fps;
			this.looping = looping;
		}

		AnimDef copy() {
			AnimDef c = new AnimDef( name, fps, looping );
			c.frames.addAll( frames );
			for (Map.Entry<Integer, Affix> e : affixes.entrySet()) {
				Affix a = e.getValue();
				c.affixes.put( e.getKey(), new Affix( a.path, a.offX, a.offY ) );
			}
			return c;
		}
	}

	/** 当前动画（为空列表时返回 null）。 */
	public static AnimDef currentAnim() {
		if (anims.isEmpty()) return null;
		if (currentAnimIndex < 0) currentAnimIndex = 0;
		if (currentAnimIndex >= anims.size()) currentAnimIndex = anims.size() - 1;
		return anims.get( currentAnimIndex );
	}

	// ==================== 物品行为 ====================

	private static final String AC_EDIT  = "edit";
	private static final String AC_RESET = "reset";

	private static AnimTweakWindow current;

	{
		image = ItemSpriteSheet.WAND_MAGIC_MISSILE;
		defaultAction = AC_EDIT;
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_EDIT );
		actions.add( AC_RESET );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (action.equals( AC_EDIT )) {
			if (current != null && current.exists && current.alive && current.parent != null) {
				Game.scene().bringToFront( current );
			} else {
				current = new AnimTweakWindow();
				Game.scene().addToFront( current );
			}
		}
		if (action.equals( AC_RESET )) {
			resetAll();
			GLog.i( "动画调整器：状态已重置" );
		}
	}

	public static void resetAll() {
		sheetPath = null;
		texture = null;
		film = null;
		frameW = 12;
		frameH = 16;
		cols = rows = totalFrames = 0;
		anims.clear();
		currentAnimIndex = 0;
		step = 1f;
		playing = true;
		curSheetFrame = -1;
	}

	// ==================== 贴图加载 ====================

	/** 加载贴图并按 (w x h) 划分帧格。成功返回 true。 */
	public static boolean loadSheet( String path, int w, int h ) {
		if (path == null || path.trim().isEmpty()) {
			GLog.n( "动画调整器：贴图路径为空" );
			return false;
		}
		if (!Gdx.files.internal( path.trim() ).exists()) {
			GLog.n( "动画调整器：文件不存在 -> " + path.trim() );
			return false;
		}
		try {
			SmartTexture tx = TextureCache.get( path.trim() );
			if (tx == null) {
				GLog.n( "动画调整器：贴图加载失败 -> " + path.trim() );
				return false;
			}
			sheetPath = path.trim();
			texture = tx;
			frameW = Math.max( 1, w );
			frameH = Math.max( 1, h );
			cols = Math.max( 1, texture.width / frameW );
			rows = Math.max( 1, texture.height / frameH );
			totalFrames = cols * rows;
			film = new TextureFilm( texture, frameW, frameH );
			GLog.i( "动画调整器：已加载 " + sheetPath + " (" + texture.width + "x" + texture.height
					+ ")，帧格 " + frameW + "x" + frameH + "，共 " + totalFrames + " 帧（" + cols + "x" + rows + "）" );
			return true;
		} catch (Exception e) {
			Game.reportException( e );
			GLog.n( "动画调整器：加载贴图异常 -> " + path.trim() );
			return false;
		}
	}

	// ==================== 代码生成与解析 ====================

	private static String listCode( List<Integer> list ) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) sb.append( ", " );
			sb.append( list.get( i ) );
		}
		return sb.toString();
	}

	/**
	 * 生成可直接粘贴进精灵类的 Java 代码（覆盖全部命名动画）。
	 * 该格式同时可被 {@link #parseCode(String)} 解析回本工具。
	 */
	public static String generateCode() {
		StringBuilder sb = new StringBuilder();
		sb.append( "// =============================================\n" );
		sb.append( "// 动画调整器输出（帧格 " ).append( frameW ).append( 'x' ).append( frameH ).append( "）\n" );
		sb.append( "// 贴图: " ).append( sheetPath == null ? "(未加载)" : sheetPath ).append( '\n' );
		sb.append( "// 动画数: " ).append( anims.size() ).append( '\n' );
		sb.append( "// =============================================\n\n" );

		sb.append( "// 1) 帧划分（放入 updateArmor()/构造函数）:\n" );
		sb.append( "TextureFilm film = new TextureFilm( texture, " ).append( frameW ).append( ", " ).append( frameH ).append( " );\n\n" );

		sb.append( "// 2) 动画定义:\n" );
		for (AnimDef a : anims) {
			if (a.frames.isEmpty()) continue;
			sb.append( "// ---- 动画: " ).append( a.name )
					.append( " (fps=" ).append( a.fps )
					.append( ", loop=" ).append( a.looping ).append( " ) ----\n" );
			sb.append( "Animation " ).append( a.name ).append( " = new Animation( " )
					.append( a.fps ).append( ", " ).append( a.looping ).append( " );\n" );
			sb.append( a.name ).append( ".frames( film, " ).append( listCode( a.frames ) ).append( " );\n" );
			if (!a.affixes.isEmpty()) {
				for (Map.Entry<Integer, Affix> e : a.affixes.entrySet()) {
					Affix fx = e.getValue();
					sb.append( "FRAME_AFFIX.put( " ).append( e.getKey() )
							.append( ", new Affix( \"" ).append( fx.path )
							.append( "\", " ).append( fx.offX ).append( "f, " ).append( fx.offY ).append( "f ) );" )
							.append( " // -> " ).append( a.name ).append( '\n' );
				}
			}
			sb.append( '\n' );
		}

		sb.append( "// 3) 逐帧 affix（贴附特效帧）——贴图可大于 12x16，整图叠在角色上方渲染;\n" );
		sb.append( "//    偏移为贴图像素, 相对角色帧左上角:\n" );
		sb.append( "Map<Integer, Affix> FRAME_AFFIX = new HashMap<>();\n" );
		for (AnimDef a : anims) {
			for (Map.Entry<Integer, Affix> e : a.affixes.entrySet()) {
				Affix fx = e.getValue();
				sb.append( "FRAME_AFFIX.put( " ).append( e.getKey() )
						.append( ", new Affix( \"" ).append( fx.path )
						.append( "\", " ).append( fx.offX ).append( "f, " ).append( fx.offY ).append( "f ) );\n" );
			}
		}

		return sb.toString();
	}

	/**
	 * 从 {@link #generateCode()} 输出的 Java 代码解析回多动画结构。
	 * 解析规则（容忍注释/空白）：
	 * <ul>
	 *   <li>{@code // ---- 动画: NAME (fps=N, loop=BOOL) ----} 或
	 *       {@code Animation NAME = new Animation( FPS, LOOP );} 开始/界定一个动画；</li>
	 *   <li>{@code NAME.frames( film, a, b, c );} 追加帧；</li>
	 *   <li>{@code FRAME_AFFIX.put( IDX, new Affix( "PATH", X, Y ) );} 绑定到当前动画。</li>
	 * </ul>
	 * 成功返回 true，并清空重建 {@link #anims}。
	 */
	public static boolean parseCode( String code ) {
		if (code == null) return false;
		List<AnimDef> parsed = new ArrayList<>();
		AnimDef cur = null;

		String[] lines = code.split( "\r?\n" );
		for (String raw : lines) {
			String line = raw.trim();
			if (line.isEmpty() || line.startsWith( "//" )) {
				// 动画头注释： // ---- 动画: name (fps=5, loop=false) ----
				if (line.startsWith( "// ---- 动画:" )) {
					AnimDef a = new AnimDef();
					String rest = line.substring( "// ---- 动画:".length() );
					int lp = rest.indexOf( '(' );
					String name = (lp >= 0 ? rest.substring( 0, lp ) : rest).trim();
					a.name = sanitizeName( name );
					if (lp >= 0) {
						String meta = rest.substring( lp + 1 );
						a.fps = parseIntAfter( meta, "fps=", a.fps );
						a.looping = parseBoolAfter( meta, "loop=", a.looping );
					}
					parsed.add( a );
					cur = a;
				}
				continue;
			}

			// Animation NAME = new Animation( FPS, LOOP );
			if (line.startsWith( "Animation " ) && line.contains( "= new Animation(" )) {
				AnimDef a = new AnimDef();
				int eq = line.indexOf( '=' );
				String name = line.substring( "Animation ".length(), eq ).trim();
				a.name = sanitizeName( name );
				int lp = line.indexOf( '(' );
				int rp = line.indexOf( ')', lp );
				String args = lp >= 0 && rp > lp ? line.substring( lp + 1, rp ) : "";
				String[] parts = args.split( "," );
				if (parts.length >= 1) a.fps = parseInt( parts[0].trim(), a.fps );
				if (parts.length >= 2) a.looping = parseBool( parts[1].trim(), a.looping );
				parsed.add( a );
				cur = a;
				continue;
			}

			// NAME.frames( film, a, b, c );
			if (line.contains( ".frames( film," )) {
				if (cur == null) { cur = new AnimDef(); parsed.add( cur ); }
				int lp = line.indexOf( "( film," ) + "( film,".length();
				int rp = line.indexOf( ')', lp );
				String seq = lp >= 0 && rp > lp ? line.substring( lp, rp ) : "";
				for (String part : seq.split( "," )) {
					part = part.trim();
					if (part.isEmpty()) continue;
					try {
						int idx = Integer.parseInt( part );
						if (idx >= 0) cur.frames.add( idx );
					} catch (NumberFormatException ignored) {
					}
				}
				continue;
			}

			// FRAME_AFFIX.put( IDX, new Affix( "PATH", X, Y ) );
			if (line.startsWith( "FRAME_AFFIX.put(" ) && line.contains( "new Affix(" )) {
				if (cur == null) { cur = new AnimDef(); parsed.add( cur ); }
				int lp = line.indexOf( '(' ) + 1;
				int ca = line.indexOf( "new Affix(" );
				String keyPart = line.substring( lp, ca ).trim();
				int key;
				try {
					key = Integer.parseInt( keyPart );
				} catch (NumberFormatException e) {
					continue;
				}
				int ap = line.indexOf( "new Affix(" ) + "new Affix(".length();
				int arp = line.indexOf( ')', ap );
				String args = line.substring( ap, arp );
				// 解析 "PATH", X, Y
				String[] pieces = splitAffixArgs( args );
				if (pieces.length >= 3) {
					String path = pieces[0].trim().replace( "\"", "" );
					float ox = parseFloat( pieces[1], 0f );
					float oy = parseFloat( pieces[2], 0f );
					cur.affixes.put( key, new Affix( path, ox, oy ) );
				}
			}
		}

		if (parsed.isEmpty()) return false;
		anims.clear();
		anims.addAll( parsed );
		if (currentAnimIndex >= anims.size()) currentAnimIndex = anims.size() - 1;
		if (currentAnimIndex < 0) currentAnimIndex = 0;
		return true;
	}

	private static String sanitizeName( String name ) {
		if (name == null || name.trim().isEmpty()) return "anim";
		StringBuilder sb = new StringBuilder();
		for (char c : name.trim().toCharArray()) {
			if (Character.isJavaIdentifierPart( c )) sb.append( c );
			else sb.append( '_' );
		}
		String s = sb.toString();
		if (s.isEmpty() || !Character.isJavaIdentifierStart( s.charAt( 0 ) )) s = "a" + s;
		return s;
	}

	private static int parseInt( String s, int def ) {
		try {
			return (int) Float.parseFloat( s.trim().replace( "f", "" ) );
		} catch (Exception e) {
			return def;
		}
	}

	private static boolean parseBool( String s, boolean def ) {
		String t = s.trim().toLowerCase();
		if (t.equals( "true" ) || t.equals( "1" ) || t.equals( "on" )) return true;
		if (t.equals( "false" ) || t.equals( "0" ) || t.equals( "off" )) return false;
		return def;
	}

	/** 在 text 中找 prefix 之后到下一个逗号/右括号/空白之间的值并解析为 int。 */
	private static int parseIntAfter( String text, String prefix, int def ) {
		int p = text.indexOf( prefix );
		if (p < 0) return def;
		p += prefix.length();
		int end = text.length();
		for (int i = p; i < text.length(); i++) {
			char c = text.charAt( i );
			if (c == ',' || c == ')' || c == '}') { end = i; break; }
		}
		return parseInt( text.substring( p, end ), def );
	}

	/** 在 text 中找 prefix 之后到下一个逗号/右括号/空白之间的值并解析为 bool。 */
	private static boolean parseBoolAfter( String text, String prefix, boolean def ) {
		int p = text.indexOf( prefix );
		if (p < 0) return def;
		p += prefix.length();
		int end = text.length();
		for (int i = p; i < text.length(); i++) {
			char c = text.charAt( i );
			if (c == ',' || c == ')' || c == '}') { end = i; break; }
		}
		return parseBool( text.substring( p, end ), def );
	}

	private static float parseFloat( String s, float def ) {
		try {
			return Float.parseFloat( s.trim().replace( "f", "" ) );
		} catch (Exception e) {
			return def;
		}
	}

	private static String[] splitAffixArgs( String args ) {
		// 形如: "path", 1.0f, -2.0f
		List<String> out = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		boolean inStr = false;
		for (char c : args.toCharArray()) {
			if (c == '"') {
				inStr = !inStr;
				sb.append( c );
			} else if (c == ',' && !inStr) {
				out.add( sb.toString() );
				sb.setLength( 0 );
			} else {
				sb.append( c );
			}
		}
		if (sb.length() > 0 || !out.isEmpty()) out.add( sb.toString() );
		return out.toArray( new String[0] );
	}

	// ==================== 编辑器窗口 ====================

	private static class AnimTweakWindow extends Window {

		private static final int W = 162;

		private PreviewClip clip;
		private Image affixImage;
		private float previewScale = 3f;
		private final ColorBlock[] frameOutline = new ColorBlock[4];
		private float previewTopY;

		private RenderedTextBlock infoSheet;
		private RenderedTextBlock infoAnim;
		private RenderedTextBlock infoSeq;
		private RenderedTextBlock infoFrame;
		private RenderedTextBlock infoFx;
		private RenderedTextBlock infoOffset;
		private RedButton btnPlay;
		private RedButton btnLoop;
		private RedButton btnFps;
		private RedButton btnStep;
		private RedButton btnPrevAnim;
		private RedButton btnNextAnim;

		AnimTweakWindow() {
			super();

			float y = 4;
			float margin = 2;

			RenderedTextBlock title = PixelScene.renderTextBlock( "动画调整器", 9 );
			title.hardlight( Window.TITLE_COLOR );
			title.setPos( (W - title.width()) / 2f, y );
			add( title );
			y = title.bottom() + margin;

			RedButton b;

			// 贴图
			infoSheet = PixelScene.renderTextBlock( sheetPath == null ? "贴图: (未加载)" : "贴图: " + sheetPath, 6 );
			infoSheet.maxWidth( W - 4 );
			infoSheet.setPos( margin, y );
			add( infoSheet );
			y = infoSheet.bottom() + margin;

			b = new RedButton( "设置贴图路径" ) { @Override protected void onClick() { pickSheetPath(); } };
			b.setRect( margin, y, W - 2 * margin, 14 );
			add( b );
			y = b.bottom() + margin;

			b = new RedButton( "帧格 " + frameW + "x" + frameH ) { @Override protected void onClick() { pickFrameSize(); } };
			b.setRect( margin, y, (W - 3 * margin) / 2f, 14 );
			add( b );

			b = new RedButton( "重新加载" ) { @Override protected void onClick() { reloadSheet(); } };
			b.setRect( margin + (W - 3 * margin) / 2f + margin, y, (W - 3 * margin) / 2f, 14 );
			add( b );
			y = b.bottom() + margin;

			// 预览（affix 整图叠在角色上方渲染，角色帧格画一圈白框便于对齐）
			previewTopY = y;
			rebuildPreview();
			float previewH = frameH * previewScale + 8;
			y += previewH + 4;

			// ---- 动画管理：上一切换 / 名称 / 下一切换 / 新增 / 重命名 / 删除 ----
			float bw = (W - 4 * margin) / 3f;
			btnPrevAnim = new RedButton( "<" ) { @Override protected void onClick() { switchAnim( -1 ); } };
			btnPrevAnim.setRect( margin, y, bw, 14 );
			add( btnPrevAnim );

			btnNextAnim = new RedButton( ">" ) { @Override protected void onClick() { switchAnim( 1 ); } };
			btnNextAnim.setRect( margin + bw + margin, y, bw, 14 );
			add( btnNextAnim );

			infoAnim = PixelScene.renderTextBlock( animLabel(), 6 );
			infoAnim.maxWidth( W - 4 );
			infoAnim.setPos( margin + 2 * (bw + margin), y );
			infoAnim.setSize( W - 4 - 2 * (bw + margin), 14 );
			add( infoAnim );
			y += 14 + margin;

			b = new RedButton( "新增动画" ) { @Override protected void onClick() { addAnim(); } };
			b.setRect( margin, y, bw, 14 );
			add( b );

			b = new RedButton( "重命名" ) { @Override protected void onClick() { renameAnim(); } };
			b.setRect( margin + bw + margin, y, bw, 14 );
			add( b );

			b = new RedButton( "删除动画" ) { @Override protected void onClick() { removeAnim(); } };
			b.setRect( margin + 2 * (bw + margin), y, bw, 14 );
			add( b );
			y = b.bottom() + margin;

			// 序列
			infoSeq = PixelScene.renderTextBlock( seqLabel(), 6 );
			infoSeq.maxWidth( W - 4 );
			infoSeq.setPos( margin, y );
			add( infoSeq );
			y = infoSeq.bottom() + margin;

			b = new RedButton( "设置帧序列" ) { @Override protected void onClick() { pickSequence(); } };
			b.setRect( margin, y, W - 2 * margin, 14 );
			add( b );
			y = b.bottom() + margin;

			// 播放控制：上一帧 / 播放暂停 / 下一帧（手动步进用于逐帧绑定 affix）
			b = new RedButton( "上一帧" ) { @Override protected void onClick() { stepFrame( -1 ); } };
			b.setRect( margin, y, bw, 14 );
			add( b );

			btnPlay = new RedButton( playing ? "暂停" : "播放" ) { @Override protected void onClick() { togglePlay(); } };
			btnPlay.setRect( margin + bw + margin, y, bw, 14 );
			add( btnPlay );

			b = new RedButton( "下一帧" ) { @Override protected void onClick() { stepFrame( 1 ); } };
			b.setRect( margin + 2 * (bw + margin), y, bw, 14 );
			add( b );
			y = b.bottom() + margin;

			// 循环 / fps / 步长
			btnLoop = new RedButton( "循环:开" ) { @Override protected void onClick() { toggleLoop(); } };
			btnLoop.setRect( margin, y, bw, 14 );
			add( btnLoop );

			btnFps = new RedButton( "fps 12" ) { @Override protected void onClick() { pickFps(); } };
			btnFps.setRect( margin + bw + margin, y, bw, 14 );
			add( btnFps );

			btnStep = new RedButton( "步长 1" ) { @Override protected void onClick() { cycleStep(); } };
			btnStep.setRect( margin + 2 * (bw + margin), y, bw, 14 );
			add( btnStep );
			y = btnStep.bottom() + margin;

			// 当前帧与 affix
			infoFrame = PixelScene.renderTextBlock( "当前帧: " + (curSheetFrame < 0 ? "-" : curSheetFrame), 6 );
			infoFrame.setPos( margin, y );
			add( infoFrame );
			y = infoFrame.bottom() + margin;

			Affix cur = curAffix();
			infoFx = PixelScene.renderTextBlock( "affix: " + (cur == null ? "(无)" : cur.path), 6 );
			infoFx.maxWidth( W - 4 );
			infoFx.setPos( margin, y );
			add( infoFx );
			y = infoFx.bottom() + margin;

			b = new RedButton( "设置 affix 图" ) { @Override protected void onClick() { pickAffixPath(); } };
			b.setRect( margin, y, (W - 3 * margin) / 2f, 14 );
			add( b );

			b = new RedButton( "清除 affix" ) { @Override protected void onClick() { clearAffix(); } };
			b.setRect( margin + (W - 3 * margin) / 2f + margin, y, (W - 3 * margin) / 2f, 14 );
			add( b );
			y = b.bottom() + margin;

			infoOffset = PixelScene.renderTextBlock( offsetText(), 6 );
			infoOffset.maxWidth( W - 4 );
			infoOffset.setPos( margin, y );
			add( infoOffset );
			y = infoOffset.bottom() + margin;

			// 底部按钮
			b = new RedButton( "帧网格" ) { @Override protected void onClick() { openGrid(); } };
			b.setRect( margin, y, (W - 4 * margin) / 3f, 14 );
			add( b );

			b = new RedButton( "输出代码" ) { @Override protected void onClick() { printCode(); } };
			b.setRect( margin + (W - 4 * margin) / 3f + margin, y, (W - 4 * margin) / 3f, 14 );
			add( b );

			b = new RedButton( "重置" ) { @Override protected void onClick() { resetAll(); rebuildPreview(); } };
			b.setRect( margin + 2 * ((W - 4 * margin) / 3f + margin), y, (W - 4 * margin) / 3f, 14 );
			add( b );
			y = b.bottom() + margin;

			// 保存 / 加载（Java 代码）
			b = new RedButton( "保存代码" ) { @Override protected void onClick() { saveCode(); } };
			b.setRect( margin, y, (W - 3 * margin) / 2f, 14 );
			add( b );

			b = new RedButton( "加载代码" ) { @Override protected void onClick() { loadCode(); } };
			b.setRect( margin + (W - 3 * margin) / 2f + margin, y, (W - 3 * margin) / 2f, 14 );
			add( b );
			y = b.bottom() + margin;

			b = new RedButton( "反射动画播放器" ) { @Override protected void onClick() { pickSpriteClass(); } };
			b.setRect( margin, y, W - 2 * margin, 14 );
			add( b );
			y = b.bottom() + margin;

			resize( W, (int) y );
		}

		private void pickSpriteClass() {
			Game.scene().addToFront( new WndTextInput(
					"输入 Sprite 类名", "输入要播放的游戏精灵类名，如 RatSprite（自动补全 sprites 包）或完整类名 com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite",
					"RatSprite", 100, false, "确定", "取消" ) {
				@Override public void onSelect( boolean positive, String text ) {
					if (positive && text != null && !text.trim().isEmpty()) {
						String name = text.trim();
						if (!name.contains( "." )) {
							name = "com.shatteredpixel.shatteredpixeldungeon.sprites." + name;
						}
						pickAnimationField( name );
					}
				}
			} );
		}

		private void pickAnimationField( final String className ) {
			try {
				Class<?> type = Class.forName( className );
				List<String> fields = new ArrayList<>();
				for (Class<?> c = type; c != null; c = c.getSuperclass()) for (Field f : c.getDeclaredFields())
					if (MovieClip.Animation.class.isAssignableFrom( f.getType() )) fields.add( f.getName() );
				if (fields.isEmpty()) { GLog.n( "动画播放器：未找到动画字段" ); return; }
				final String[] options = fields.toArray( new String[0] );
				Game.scene().addToFront( new WndOptions( "选择动画", className.substring( className.lastIndexOf('.') + 1 ), options ) {
					@Override public void onSelect( int index ) { if (index >= 0) Game.scene().addToFront( new ReflectedSpritePlayer( className, options[index] ) ); }
				} );
			} catch (Exception e) { GLog.n( "动画播放器：无法读取 Sprite -> " + e.getMessage() ); }
		}

		// ---------- 动画管理 ----------

		private String animLabel() {
			AnimDef a = currentAnim();
			if (a == null) return "动画: (无)";
			return "动画 " + (currentAnimIndex + 1) + "/" + anims.size() + ": " + a.name;
		}

		private String seqLabel() {
			AnimDef a = currentAnim();
			if (a == null || a.frames.isEmpty()) return "序列: (空)";
			return "序列: " + listCode( a.frames );
		}

		private void switchAnim( int delta ) {
			if (anims.isEmpty()) return;
			currentAnimIndex = (currentAnimIndex + delta) % anims.size();
			if (currentAnimIndex < 0) currentAnimIndex += anims.size();
			curSheetFrame = -1;
			rebuildPreview();
		}

		private void addAnim() {
			Game.scene().addToFront( new WndTextInput(
					"新增动画", "动画名称（英文字母/数字/下划线），如 attack、run",
					"anim" + (anims.size() + 1), 24, false, "新增", "取消" ) {
				@Override public void onSelect( boolean positive, String text ) {
					if (positive && text != null && !text.trim().isEmpty()) {
						String name = sanitizeName( text );
						anims.add( new AnimDef( name, 12, true ) );
						currentAnimIndex = anims.size() - 1;
						curSheetFrame = -1;
						rebuildPreview();
					}
				}
			} );
		}

		private void renameAnim() {
			AnimDef a = currentAnim();
			if (a == null) { GLog.i( "动画调整器：当前无动画" ); return; }
			Game.scene().addToFront( new WndTextInput(
					"重命名动画", "新名称", a.name, 24, false, "应用", "取消" ) {
				@Override public void onSelect( boolean positive, String text ) {
					if (positive && text != null && !text.trim().isEmpty()) {
						a.name = sanitizeName( text );
					}
				}
			} );
		}

		private void removeAnim() {
			AnimDef a = currentAnim();
			if (a == null) { GLog.i( "动画调整器：当前无动画" ); return; }
			anims.remove( currentAnimIndex );
			if (currentAnimIndex >= anims.size()) currentAnimIndex = anims.size() - 1;
			if (currentAnimIndex < 0) currentAnimIndex = 0;
			curSheetFrame = -1;
			rebuildPreview();
			GLog.i( "动画调整器：已删除动画 " + a.name );
		}

		// ---------- 预览 ----------

		private void rebuildPreview() {
			if (clip != null) {
				clip.remove();
				clip.destroy();
			}
			clip = new PreviewClip();
			if (texture != null) clip.texture( texture );
			clip.scale.set( previewScale );
			float pw = frameW * previewScale;
			float ph = frameH * previewScale;
			clip.x = (W - pw - 8) / 2f + 4;
			clip.y = previewTopY + 4;
			buildFrameOutline( clip.x, clip.y, pw, ph );
			AnimDef a = currentAnim();
			if (film != null && a != null && !a.frames.isEmpty()) {
				MovieClip.Animation anim = new MovieClip.Animation( a.fps, a.looping );
				Object[] idx = new Object[a.frames.size()];
				for (int i = 0; i < a.frames.size(); i++) idx[i] = a.frames.get( i );
				anim.frames( film, idx );
				clip.play( anim, true );
			} else {
				clip.visible = false;
			}
			clip.paused = !playing;
			add( clip );
		}

		private void buildFrameOutline( float x, float y, float fw, float fh ) {
			for (int i = 0; i < 4; i++) {
				if (frameOutline[i] != null) {
					frameOutline[i].remove();
					frameOutline[i].destroy();
				}
			}
			frameOutline[0] = new ColorBlock( fw + 2, 1, 0xFFFFFF );
			frameOutline[1] = new ColorBlock( fw + 2, 1, 0xFFFFFF );
			frameOutline[2] = new ColorBlock( 1, fh + 2, 0xFFFFFF );
			frameOutline[3] = new ColorBlock( 1, fh + 2, 0xFFFFFF );
			for (int i = 0; i < 4; i++) {
				frameOutline[i].am = 0.5f;
				add( frameOutline[i] );
			}
			frameOutline[0].x = x - 1; frameOutline[0].y = y - 1;
			frameOutline[1].x = x - 1; frameOutline[1].y = y + fh;
			frameOutline[2].x = x - 1; frameOutline[2].y = y - 1;
			frameOutline[3].x = x + fw; frameOutline[3].y = y - 1;
		}

		private void stepFrame( int delta ) {
			if (clip == null) return;
			playing = false;
			clip.paused = true;
			clip.stepFrame( delta );
			updatePreview();
		}

		private void updatePreview() {
			// 帧变化时更新 affix 图层
			int f = clip.curFrameIndex();
			AnimDef a = currentAnim();
			int sheetIdx = (a != null && f >= 0 && f < a.frames.size()) ? a.frames.get( f ) : -1;
			if (sheetIdx != curSheetFrame) {
				curSheetFrame = sheetIdx;
				updateAffix();
			}
		}

		private void updateAffix() {
			Affix fx = curAffix();
			if (fx == null) {
				if (affixImage != null) affixImage.visible = false;
				return;
			}
			if (affixImage == null) {
				affixImage = new Image();
				add( affixImage );
			}
			if (!fx.path.isEmpty() && Gdx.files.internal( fx.path ).exists()) {
				affixImage.texture( fx.path );
				affixImage.scale.set( previewScale );
				affixImage.x = clip.x + fx.offX * previewScale;
				affixImage.y = clip.y + fx.offY * previewScale;
				affixImage.visible = true;
			} else {
				affixImage.visible = false;
				GLog.n( "动画调整器：affix 图不存在 -> " + fx.path );
			}
		}

		private Affix curAffix() {
			AnimDef a = currentAnim();
			if (a == null || curSheetFrame < 0) return null;
			return a.affixes.get( curSheetFrame );
		}

		private void adjustOffset( float dx, float dy ) {
			Affix fx = curAffix();
			if (fx == null) {
				GLog.i( "动画调整器：当前帧(" + curSheetFrame + ")无 affix，请先设置 affix 图" );
				return;
			}
			float s = step;
			if (Gdx.input.isKeyPressed( Input.Keys.SHIFT_LEFT ) || Gdx.input.isKeyPressed( Input.Keys.SHIFT_RIGHT )) s = step * 10;
			if (Gdx.input.isKeyPressed( Input.Keys.CONTROL_LEFT ) || Gdx.input.isKeyPressed( Input.Keys.CONTROL_RIGHT )) s = step * 0.1f;
			fx.offX += dx * s;
			fx.offY += dy * s;
			updateAffix();
		}

		// ---------- 交互 ----------

		private void pickSheetPath() {
			Game.scene().addToFront( new WndTextInput(
					"贴图路径", "相对 assets 的路径，如 sprites/RadishSnDSprite/gambler.png",
					sheetPath == null ? "sprites/RadishSnDSprite/gambler.png" : sheetPath,
					100, false, "加载", "取消" ) {
				@Override public void onSelect( boolean positive, String text ) {
					if (positive) {
						loadSheet( text, frameW, frameH );
						rebuildPreview();
					}
				}
			} );
		}

		private void pickFrameSize() {
			Game.scene().addToFront( new WndTextInput(
					"帧格尺寸", "格式: 宽x高（默认 12x16）",
					frameW + "x" + frameH, 20, false, "应用", "取消" ) {
				@Override public void onSelect( boolean positive, String text ) {
					if (positive) {
						try {
							String[] parts = text.trim().split( "[xX×* ]+" );
							int w = Integer.parseInt( parts[0] );
							int h = Integer.parseInt( parts[1] );
							if (sheetPath != null) loadSheet( sheetPath, w, h );
							else { frameW = w; frameH = h; }
							rebuildPreview();
						} catch (Exception e) {
							GLog.n( "动画调整器：帧格格式错误，应为 宽x高" );
						}
					}
				}
			} );
		}

		private void reloadSheet() {
			if (sheetPath != null) loadSheet( sheetPath, frameW, frameH );
			rebuildPreview();
		}

		private void pickSequence() {
			AnimDef a = currentAnim();
			if (a == null) { GLog.i( "动画调整器：请先新增动画" ); return; }
			Game.scene().addToFront( new WndTextInput(
					"帧序列", "贴图帧下标，逗号或空格分隔，如 0,0,0,1,0,0,1,1",
					a.frames.isEmpty() ? "" : listCode( a.frames ), 200, false, "应用", "取消" ) {
				@Override public void onSelect( boolean positive, String text ) {
					if (positive) {
						applySequence( text );
					}
				}
			} );
		}

		private void applySequence( String text ) {
			AnimDef a = currentAnim();
			if (a == null) return;
			a.frames.clear();
			for (String part : text.trim().split( "[,\\s]+" )) {
				if (part.isEmpty()) continue;
				try {
					int idx = Integer.parseInt( part );
					if (idx >= 0 && idx < totalFrames) a.frames.add( idx );
					else GLog.w( "动画调整器：越界帧下标 " + idx + "（忽略）" );
				} catch (NumberFormatException e) {
					GLog.w( "动画调整器：无法解析帧 " + part );
				}
			}
			curSheetFrame = -1;
			rebuildPreview();
		}

		private void togglePlay() {
			playing = !playing;
			if (clip != null) clip.paused = !playing;
		}

		private void toggleLoop() {
			AnimDef a = currentAnim();
			if (a == null) { GLog.i( "动画调整器：请先新增动画" ); return; }
			a.looping = !a.looping;
			rebuildPreview();
		}

		private void pickFps() {
			AnimDef a = currentAnim();
			if (a == null) { GLog.i( "动画调整器：请先新增动画" ); return; }
			Game.scene().addToFront( new WndTextInput(
					"动画速度", "每秒帧数（fps），如 12", String.valueOf( a.fps ), 4, false, "应用", "取消" ) {
				@Override public void onSelect( boolean positive, String text ) {
					if (positive) {
						try {
							int v = Integer.parseInt( text.trim() );
							if (v > 0 && v <= 120) {
								a.fps = v;
								rebuildPreview();
							} else {
								GLog.n( "动画调整器：fps 需在 1-120 之间" );
							}
						} catch (NumberFormatException e) {
							GLog.n( "动画调整器：fps 格式错误" );
						}
					}
				}
			} );
		}

		private void pickAffixPath() {
			if (curSheetFrame < 0) {
				GLog.i( "动画调整器：请先让预览显示目标帧（播放或步进到该帧）" );
				return;
			}
			AnimDef a = currentAnim();
			if (a == null) return;
			Affix cur = curAffix();
			Game.scene().addToFront( new WndTextInput(
					"设置 affix 图", "相对 assets 的路径，如 sprites/affixes/slash.png",
					cur == null ? "" : cur.path, 100, false, "设置", "取消" ) {
				@Override public void onSelect( boolean positive, String text ) {
					if (positive) {
						if (text.trim().isEmpty()) {
							GLog.n( "动画调整器：affix 路径为空" );
							return;
						}
						if (!Gdx.files.internal( text.trim() ).exists()) {
							GLog.n( "动画调整器：affix 文件不存在 -> " + text.trim() );
						}
						Affix fx = a.affixes.get( curSheetFrame );
						if (fx == null) {
							fx = new Affix( text.trim(), 0f, 0f );
							a.affixes.put( curSheetFrame, fx );
						} else {
							fx.path = text.trim();
						}
						updateAffix();
					}
				}
			} );
		}

		private void clearAffix() {
			AnimDef a = currentAnim();
			if (a != null && curSheetFrame >= 0 && a.affixes.remove( curSheetFrame ) != null) {
				updateAffix();
				GLog.i( "动画调整器：已清除帧 " + curSheetFrame + " 的 affix" );
			}
		}

		private void cycleStep() {
			if (step < 1) step = 1;
			else if (step < 10) step = 10;
			else step = 0.1f;
		}

		private String offsetText() {
			Affix fx = curAffix();
			return fx == null
					? "affix 偏移: (0, 0)  [方向键微调]"
					: "affix 偏移: (" + fx.offX + ", " + fx.offY + ")  [方向键微调]";
		}

		private void openGrid() {
			Game.scene().addToFront( new FrameGridWindow() );
		}

		private void printCode() {
			String code = generateCode();
			GLog.i( "======== 动画调整器 Java 代码 ========" );
			for (String line : code.split( "\n" )) {
				GLog.i( line );
			}
			GLog.i( "======== 代码结束 ========" );
		}

		private void saveCode() {
			String code = generateCode();
			Game.scene().addToFront( new WndTextInput(
					"保存动画代码", "复制下面的 Java 代码，粘贴进精灵类即可（也用于\"加载代码\"回读）",
					code, 4000, true, "复制", "关闭" ) {
				@Override public void onSelect( boolean positive, String text ) {
					// 多行文本框，直接展示；positive 不额外处理
				}
			} );
		}

		private void loadCode() {
			Game.scene().addToFront( new WndTextInput(
					"加载动画代码", "粘贴由\"保存代码\"生成的 Java 代码，将重建全部动画",
					"", 4000, true, "加载", "取消" ) {
				@Override public void onSelect( boolean positive, String text ) {
					if (positive && text != null && !text.trim().isEmpty()) {
						if (parseCode( text )) {
							GLog.i( "动画调整器：已加载 " + anims.size() + " 个动画" );
						} else {
							GLog.n( "动画调整器：未能解析出任何动画" );
						}
						curSheetFrame = -1;
						rebuildPreview();
					}
				}
			} );
		}

		// ---------- 每帧刷新 ----------

		private int lastSeqHash = -1;
		private String lastAnimName = "";

		@Override
		public synchronized void update() {
			super.update();
			if (!exists) return;
			AnimDef a = currentAnim();
			int hash = (a == null) ? -1 : a.frames.hashCode();
			String name = (a == null) ? "" : a.name;
			if (hash != lastSeqHash || !name.equals( lastAnimName )) {
				lastSeqHash = hash;
				lastAnimName = name;
				rebuildPreview();
				curSheetFrame = -1;
			}
			updatePreview();

			infoSheet.text( sheetPath == null ? "贴图: (未加载)" : "贴图: " + sheetPath );
			infoAnim.text( animLabel() );
			infoSeq.text( seqLabel() );
			infoFrame.text( "当前帧: " + (curSheetFrame < 0 ? "-" : curSheetFrame) );
			Affix cur = curAffix();
			infoFx.text( "affix: " + (cur == null ? "(无)" : cur.path) );
			infoOffset.text( offsetText() );
			btnPlay.text( playing ? "暂停" : "播放" );
			if (a != null) {
				btnLoop.text( a.looping ? "循环:开" : "循环:关" );
				btnFps.text( "fps " + a.fps );
			}
			btnStep.text( "步长 " + step );
		}

		// ---------- 按键 ----------

		@Override
		public boolean onSignal( KeyEvent event ) {
			if (event.pressed) {
				int code = event.code;
				if (code == Input.Keys.DPAD_LEFT)  adjustOffset( -1, 0 );
				else if (code == Input.Keys.DPAD_RIGHT) adjustOffset( 1, 0 );
				else if (code == Input.Keys.DPAD_UP)    adjustOffset( 0, -1 );
				else if (code == Input.Keys.DPAD_DOWN)  adjustOffset( 0, 1 );
				else if (code == Input.Keys.ENTER) {
					Affix fx = curAffix();
					if (fx != null) GLog.i( "动画调整器：帧 " + curSheetFrame + " 偏移 = (" + fx.offX + ", " + fx.offY + ")" );
				} else if (code == Input.Keys.BACKSPACE) {
					Affix fx = curAffix();
					if (fx != null) { fx.offX = 0; fx.offY = 0; updateAffix(); }
				} else if (code == Input.Keys.LEFT_BRACKET) {
					step = Math.max( 0.1f, step - 0.9f );
				} else if (code == Input.Keys.RIGHT_BRACKET) {
					step = Math.min( 100f, step + 9f );
				}
			}
			return super.onSignal( event );
		}

		@Override
		public void destroy() {
			super.destroy();
			if (current == this) current = null;
		}
	}

	// ==================== 预览组件 ====================

	private static class PreviewClip extends MovieClip {
		PreviewClip() {
			super();
		}

		PreviewClip( Object tx ) {
			super( tx );
		}

		int curFrameIndex() {
			return curFrame;
		}

		/** 手动步进动画帧（暂停态逐帧定位用），delta 可为 ±1 等。 */
		void stepFrame( int delta ) {
			if (curAnim == null || curAnim.frames == null) return;
			int n = curAnim.frames.length;
			if (n == 0) return;
			int next = (curFrame + delta) % n;
			if (next < 0) next += n;
			curFrame = next;
			frameTimer = 0;
			frame( curAnim.frames[curFrame] );
		}
	}

	// ==================== 反射动画播放器 ====================

	private static class ReflectedSpritePlayer extends Window {

		private static final int W = 120;

		ReflectedSpritePlayer( String className, String animationName ) {
			super();
			float y = 4;
			RenderedTextBlock title = PixelScene.renderTextBlock( animationName, 8 );
			title.hardlight( Window.TITLE_COLOR );
			title.setPos( (W - title.width()) / 2f, y );
			add( title );
			y = title.bottom() + 4;

			try {
				// HeroSprite 子类构造时会 link(hero) 把 hero.sprite 覆盖为预览精灵，
				// 若不还原，预览窗口销毁时 destroy() 会把 hero.sprite 置空，导致后续 ready() NPE。
				CharSprite realHeroSprite = (Dungeon.hero != null && Dungeon.hero.sprite instanceof CharSprite)
						? Dungeon.hero.sprite : null;

				Class<?> type = Class.forName( className );
				Constructor<?> constructor = type.getDeclaredConstructor();
				constructor.setAccessible( true );
				Object instance = constructor.newInstance();
				if (!(instance instanceof CharSprite)) {
					throw new IllegalArgumentException( "该类不是 CharSprite" );
				}
				if (realHeroSprite != null) {
					Dungeon.hero.sprite = realHeroSprite;
				}
				Field field = findField( type, animationName );
				field.setAccessible( true );
				Object value = field.get( instance );
				if (!(value instanceof MovieClip.Animation)) {
					throw new IllegalArgumentException( "字段不是 MovieClip.Animation" );
				}

				CharSprite sprite = (CharSprite) instance;
				// 预览用精灵没有绑定 ch，动画播完的 onComplete 会去调 ch.onAttackComplete() 等导致 NPE，
				// 因此用一个空 listener 替换掉 CharSprite 构造时注册的 this。
				sprite.listener = anim -> { };
				// 用 force 播放：CharSprite.play(anim) 在 curAnim == die 时会拒绝切换
				// （无 ch 时 updateArmor 会调用 die()，导致所有动画都播成 die），force 版直接覆盖。
				sprite.play( (MovieClip.Animation) value, true );
				float scale = Math.min( 4f, Math.min( 80f / Math.max( 1f, sprite.width ),
						80f / Math.max( 1f, sprite.height ) ) );
				sprite.scale.set( scale );
				sprite.x = (W - sprite.width * scale) / 2f;
				sprite.y = y + (80f - sprite.height * scale) / 2f;
				add( sprite );
				y += 84;

				RenderedTextBlock name = PixelScene.renderTextBlock( type.getSimpleName() + "." + animationName, 6 );
				name.maxWidth( W - 4 );
				name.setPos( (W - name.width()) / 2f, y );
				add( name );
				y = name.bottom() + 4;
			} catch (Exception e) {
				Game.reportException( e );
				GLog.n( "动画播放器：加载失败 -> " + className + "." + animationName + "：" + e.getMessage() );
				RenderedTextBlock error = PixelScene.renderTextBlock( "加载失败，请查看日志", 6 );
				error.setPos( (W - error.width()) / 2f, y );
				add( error );
				y = error.bottom() + 4;
			}
			resize( W, (int) y );
		}

		private static Field findField( Class<?> type, String name ) throws NoSuchFieldException {
			for (Class<?> c = type; c != null; c = c.getSuperclass()) {
				try {
					return c.getDeclaredField( name );
				} catch (NoSuchFieldException ignored) {
				}
			}
			throw new NoSuchFieldException( name );
		}
	}

	// ==================== 帧网格窗口 ====================

	/**
	 * 子功能：按 12x16 帧格渲染整张贴图的独立窗口。
	 * 左键点击帧会将其追加到当前动画序列，右键点击会移除该帧的一次出现；
	 * 这样可以直接编辑包含重复帧的序列（例如 idle 的 00010011）。
	 */
	private static class FrameGridWindow extends Window {

		private static final int W = 140;
		private static final int TILE_W = 24;
		private static final int TILE_H = 34;

		private final List<GridTile> tiles = new ArrayList<>();
		private RenderedTextBlock infoSel;
		private final int page;
		private static final int PAGE_SIZE = 25;

		FrameGridWindow() {
			this( 0 );
		}

		FrameGridWindow( int page ) {
			super();
			this.page = Math.max( 0, page );

			float pos = 2;
			RenderedTextBlock title = PixelScene.renderTextBlock( "帧网格（点击勾选动画帧）", 7 );
			title.hardlight( Window.TITLE_COLOR );
			title.setPos( (W - title.width()) / 2f, pos );
			add( title );
			pos = title.bottom() + 2;

			infoSel = PixelScene.renderTextBlock( selText(), 6 );
			infoSel.maxWidth( W - 4 );
			infoSel.setPos( 2, pos );
			add( infoSel );
			pos = infoSel.bottom() + 2;

			if (film == null) {
				RenderedTextBlock msg = PixelScene.renderTextBlock( "未加载贴图，请先在编辑窗口设置贴图路径", 6 );
				msg.maxWidth( W - 4 );
				msg.setPos( 2, pos );
				add( msg );
				pos = msg.bottom() + 2;
			} else {
				int perRow = Math.max( 1, (W - 4) / TILE_W );
				int col = 0, row = 0;
				int first = this.page * PAGE_SIZE;
				int last = Math.min( totalFrames, first + PAGE_SIZE );
				for (int i = first; i < last; i++) {
					GridTile t = new GridTile( i );
					t.setPos( 2 + col * TILE_W, pos + row * TILE_H );
					add( t );
					tiles.add( t );
					if (++col >= perRow) { col = 0; row++; }
				}
				pos += row * TILE_H + TILE_H;
			}
			pos += 2;

			float bw = (W - 3 * 2) / 2f;
			RedButton b = new RedButton( "全选" ) { @Override protected void onClick() {
				AnimDef a = currentAnim();
				if (a == null) { GLog.i( "动画调整器：请先新增动画" ); return; }
				for (int i = 0; i < totalFrames; i++) {
					if (!a.frames.contains( i )) a.frames.add( i );
				}
				refreshTiles();
			} };
			b.setRect( 2, pos, bw, 14 );
			add( b );

			b = new RedButton( "清空" ) { @Override protected void onClick() {
				AnimDef a = currentAnim();
				if (a != null) a.frames.clear();
				refreshTiles();
			} };
			b.setRect( 2 + bw + 2, pos, bw, 14 );
			add( b );
			pos = b.bottom() + 2;
			if (totalFrames > PAGE_SIZE) {
				int pages = (totalFrames + PAGE_SIZE - 1) / PAGE_SIZE;
				RedButton prev = new RedButton( "上一页" ) { @Override protected void onClick() {
					if (page > 0) reopenPage( page - 1 );
				} };
				prev.setRect( 2, pos, bw, 14 );
				add( prev );
				RedButton next = new RedButton( "下一页" ) { @Override protected void onClick() {
					if (page + 1 < pages) reopenPage( page + 1 );
				} };
				next.setRect( 2 + bw + 2, pos, bw, 14 );
				add( next );
				pos = next.bottom() + 2;
			}

			resize( W, (int) pos );
		}

		private void reopenPage( int nextPage ) {
			destroy();
			Game.scene().addToFront( new FrameGridWindow( nextPage ) );
		}

		String selText() {
			AnimDef a = currentAnim();
			int n = (a == null) ? 0 : a.frames.size();
			return "当前动画: " + (a == null ? "(无)" : a.name) + "  已选 " + n + " / " + totalFrames + " 帧";
		}

		void refreshTiles() {
			for (GridTile t : tiles) t.refresh();
			if (infoSel != null) infoSel.text( selText() );
		}

		private class GridTile extends Component {

			final int index;
			final Image img;
			final RenderedTextBlock num;
			final ColorBlock bg;
			final ColorBlock tickH, tickV;
			final PointerArea area;

			GridTile( int index ) {
				this.index = index;

				bg = new ColorBlock( TILE_W - 2, TILE_H - 12, 0x222222 );
				addToBack( bg );

				img = new Image( texture );
				img.frame( film.get( index ) );
				// Keep the whole frame inside the tile even when a custom frame size is used.
				float scale = Math.min( (TILE_W - 2f) / Math.max( 1, frameW ),
						(TILE_H - 12f) / Math.max( 1, frameH ) );
				img.scale.set( Math.max( 0.1f, scale ) );
				add( img );

				num = PixelScene.renderTextBlock( String.valueOf( index ), 6 );
				add( num );

				// 右上角勾选标记（白色 ⌐ 角）
				tickH = new ColorBlock( 4, 1, 0xFFFFFF );
				tickV = new ColorBlock( 1, 5, 0xFFFFFF );
				add( tickH );
				add( tickV );

				area = new PointerArea( 0, 0, TILE_W, TILE_H ) {
					@Override protected void onClick( com.watabou.input.PointerEvent event ) {
						if (event.button == com.watabou.input.PointerEvent.RIGHT) {
							removeFrame();
						} else if (event.button == com.watabou.input.PointerEvent.LEFT) {
							addFrame();
						}
					}
				};
				// 关键：把本 PointerArea 提到指针事件队列最前。
				// 否则 ScrollPane 的 PointerController 会先消费掉 DOWN 事件，
				// 导致永远无法点击到帧格。
				area.givePointerPriority();
				add( area );

				setSize( TILE_W, TILE_H );
				refresh();
			}

			// 关键：本引擎 Group/Component 不会自动把子节点相对自身偏移，
			// 必须在这里把所有子节点摆到 (x,y) + 局部偏移 的绝对坐标，否则所有帧会重叠挤在一起。
			@Override
			protected void layout() {
				// Component children are scene-absolute; GridTile itself is laid out
				// in content coordinates, so explicitly include its grid origin.
				bg.setPos( left() + 1, top() + 1 );
				img.setPos( left() + 1, top() + 1 );
				num.setPos( left() + 1, top() + TILE_H - 10 );
				area.x = left(); area.y = top(); area.width = TILE_W; area.height = TILE_H;
				tickH.x = left() + TILE_W - 6; tickH.y = top() + 2;
				tickV.x = left() + TILE_W - 2; tickV.y = top() + 2;
			}

			void addFrame() {
				AnimDef a = currentAnim();
				if (a == null) { GLog.i( "动画调整器：请先新增动画" ); return; }
				a.frames.add( index );
				GLog.i( "动画调整器：追加帧 " + index + "（序列长度 " + a.frames.size() + "）" );
				refreshTiles();
			}

			void removeFrame() {
				AnimDef a = currentAnim();
				if (a == null) { GLog.i( "动画调整器：请先新增动画" ); return; }
				if (a.frames.remove( (Integer) index )) {
					GLog.i( "动画调整器：移除帧 " + index + "（序列长度 " + a.frames.size() + "）" );
				}
				refreshTiles();
			}

			void refresh() {
				AnimDef a = currentAnim();
				boolean inSeq = a != null && a.frames.contains( index );
				bg.color( inSeq ? 0x33FF66 : 0x222222 );
				tickH.visible = inSeq;
				tickV.visible = inSeq;
			}
		}

		@Override
		public void destroy() {
			super.destroy();
		}
	}
}
