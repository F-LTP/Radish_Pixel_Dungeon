package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import java.util.HashMap;
import java.util.Map;

/** Agent rogue skin using the animation-tweaker frame layout. */
public class AgentSprite extends HeroSprite {
	private static final int W = 12, H = 16;
	private Animation slash1, slash2, thrub;
	private static final Map<Integer, Affix> FRAME_AFFIX = new HashMap<>();
	private Map<Animation, int[]> affixSeqs;
	private Image affixImage;
	private int lastFrame = -1;
	private Animation lastAnim;
	static { FRAME_AFFIX.put(81, new Affix("sprites/RadishSnDSprite/agent_kill_red_fx.png", 0, 0)); }
	private Map<Animation, int[]> affixSeqs() { if (affixSeqs == null) affixSeqs = new HashMap<>(); return affixSeqs; }
	private static class Affix { final String path; final float x, y; Affix(String p,float x,float y){path=p;this.x=x;this.y=y;} }
	public AgentSprite() { super(); }
	@Override public void updateArmor() {
		texture(Assets.Sprites.AGENT); TextureFilm f = new TextureFilm(texture, W, H);
		idle = new Animation(5,true); idle.frames(f,0,0,0,0,0,0,1,1);
		die = idle.clone();
		run = new Animation(12,true); run.frames(f,2,3,4,5,6,7);
		attack = new Animation(8,true); attack.frames(f,19,20,21); zap=attack.clone();
		operate = new Animation(8,true); operate.frames(f,23,22);
		read = new Animation(5,false); read.frames(f,24,25,26);
		slash1 = new Animation(8,true); slash1.frames(f,38,39,40,41,42,43); affixSeqs().put(slash1,new int[]{38,39,40,41,42,43});
		slash2 = new Animation(12,true); slash2.frames(f,44,45,46,47,48,49,50,51); affixSeqs().put(slash2,new int[]{44,45,46,47,48,49,50,51});
		thrub = new Animation(8,true); thrub.frames(f,76,77,78,79,81,82,83,84,85); affixSeqs().put(thrub,new int[]{76,77,78,79,81,82,83,84,85});
		fly=idle.clone(); if(ch!=null&&ch.isAlive()) idle(); else die();
	}
	public void slash1(){play(slash1);} public void slash2(){play(slash2);} public void thrub(){play(thrub);}
	@Override public void update(){ super.update(); int[] seq=curAnim==null?null:affixSeqs().get(curAnim); if(seq==null){if(affixImage!=null)affixImage.visible=false;lastFrame=-1;lastAnim=null;return;} if(curAnim!=lastAnim||curFrame!=lastFrame){lastAnim=curAnim;lastFrame=curFrame; Affix a=FRAME_AFFIX.get(seq[curFrame]); if(a==null){if(affixImage!=null)affixImage.visible=false;} else {if(affixImage==null)affixImage=new Image(); affixImage.texture(a.path); affixImage.flipHorizontal=flipHorizontal; affixImage.x=flipHorizontal?x+W-a.x-affixImage.width():x+a.x; affixImage.y=y+a.y; affixImage.visible=true;}} }
	@Override public void draw(){super.draw();if(affixImage!=null&&affixImage.visible&&visible){affixImage.camera=camera();affixImage.draw();}}
	@Override public void destroy(){super.destroy();if(affixImage!=null){affixImage.destroy();affixImage=null;}}
}
