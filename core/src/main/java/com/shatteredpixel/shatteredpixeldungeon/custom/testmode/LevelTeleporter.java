package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.depth;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Awareness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.shatteredpixel.shatteredpixeldungeon.custom.messages.M;
import com.shatteredpixel.shatteredpixeldungeon.custom.utils.Constants;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branches;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.noosa.ui.Component;

import java.util.ArrayList;

public class LevelTeleporter extends TestItem {
    {
        image = ItemSpriteSheet.AMULET;
        defaultAction = AC_DESCEND;
        changeDefAct = true;
    }

    @Override
    public String actionName(String action, Hero hero) {
        if (action.equals(AC_BRANCH_ASCEND)){
            return Messages.upperCase(Messages.get(this, "ac_branch_ascend",depth));
        } else if (action.equals(AC_BRANCH_DESCEND)){
            return Messages.upperCase(Messages.get(this, "ac_branch_descend",depth));
        } else {
            return super.actionName(action, hero);
        }
    }

    private static final String AC_ASCEND = "ascend";

    private static final String AC_BRANCH_ASCEND = "branch_ascend";
    private static final String AC_DESCEND = "descend";

    private static final String AC_BRANCH_DESCEND = "branch_descend";

    private static final String AC_VIEW = "view";
    private static final String AC_TP = "teleport";

    private static final String AC_RESET = "reset";

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_ASCEND );
        actions.add(AC_DESCEND);

        actions.add(AC_VIEW);
        actions.add(AC_TP);
        actions.add(AC_RESET );

        actions.add(AC_BRANCH_ASCEND);
        actions.add(AC_BRANCH_DESCEND);
        return actions;
    }

    @Override
    protected boolean allowChange(String action){
        return !action.equals(AC_VIEW) && super.allowChange(action);
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute( hero, action );
        if(action.equals(AC_DESCEND)) {
            if(Dungeon.hero.buff(LockedFloor.class) != null || depth>= Constants.MAX_DEPTH) {
                GLog.w(Messages.get(this,"cannot_send"));
                return;
            }

            TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
            if (timeFreeze != null) timeFreeze.disarmPresses();
            Swiftthistle.TimeBubble timeBubble = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
            if (timeBubble != null) timeBubble.disarmPresses();
			InterlevelScene.mode = InterlevelScene.Mode.RETURN;
			InterlevelScene.returnDepth = depth + 1;
			InterlevelScene.returnBranchId = Dungeon.branchId;
			InterlevelScene.returnPos = -1;
            Game.switchScene( InterlevelScene.class );

        } else if(action.equals(AC_ASCEND)) {
            if (Dungeon.hero.buff(LockedFloor.class) != null || depth <= 0) {
                GLog.w(Messages.get(this, "cannot_send"));
                return;
            }

            TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
            if (timeFreeze != null) timeFreeze.disarmPresses();
            Swiftthistle.TimeBubble timeBubble = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
            if (timeBubble != null) timeBubble.disarmPresses();
            InterlevelScene.mode = InterlevelScene.Mode.RETURN;
            InterlevelScene.returnDepth = depth - 1;
            InterlevelScene.returnPos = -1;
            InterlevelScene.returnBranchId = Dungeon.branchId;
            Game.switchScene(InterlevelScene.class);

        } else if (action.equals(AC_BRANCH_DESCEND)){
            // 从主线进入支线分支
            if (!Dungeon.branchId.equals(Branches.MAIN)) {
                GLog.w(Messages.get(this, "already_in_branch"));
                return;
            }

            // 根据当前层决定可以进入哪个分支
            String targetBranch = null;
            int targetDepth = 1;
            
            if (depth >= 11 && depth <= 14) {
                // 黑smith区域：进入采矿分支
                targetBranch = Branches.MINING;
                targetDepth = 1;
            } else if (depth >= 2 && depth <= 4) {
                // Sewer区域：进入苔藓分支
                targetBranch = Branches.MOSS;
                targetDepth = 1;
            } else {
                GLog.w(Messages.get(this, "no_branch_here"));
                return;
            }

            TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
            if (timeFreeze != null) timeFreeze.disarmPresses();
            Swiftthistle.TimeBubble timeBubble = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
            if (timeBubble != null) timeBubble.disarmPresses();
            
			InterlevelScene.returnDepth = targetDepth;
			InterlevelScene.returnBranchId = targetBranch;
			InterlevelScene.returnPos = -1;
			InterlevelScene.mode = InterlevelScene.Mode.RETURN;
            Game.switchScene(InterlevelScene.class);

        } else if (action.equals(AC_BRANCH_ASCEND)){
            // 从支线返回主线
            if (Dungeon.branchId.equals(Branches.MAIN)) {
                GLog.w(Messages.get(this, "not_in_branch"));
                return;
            }

            // 根据当前分支决定返回主线哪一层
            int returnDepth;
            if (Dungeon.branchId.equals(Branches.MOSS)) {
                returnDepth = 2;  // 苔藓分支返回主线第2层
            } else if (Dungeon.branchId.equals(Branches.MINING)) {
                returnDepth = Dungeon.depth;  // 采矿分支返回主线对应层
            } else {
                returnDepth = 1;
            }

            TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
            if (timeFreeze != null) timeFreeze.disarmPresses();
            Swiftthistle.TimeBubble timeBubble = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
            if (timeBubble != null) timeBubble.disarmPresses();
            
			InterlevelScene.returnDepth = returnDepth;
			InterlevelScene.returnBranchId = Branches.MAIN;
			InterlevelScene.returnPos = -1;
            InterlevelScene.mode = InterlevelScene.Mode.RETURN;
            Game.switchScene(InterlevelScene.class);

        } else if(action.equals(AC_VIEW)){
            Buff.affect( hero, Awareness.class, Awareness.DURATION );
            Buff.affect( hero, MindVision.class, MindVision.DURATION );
            Dungeon.observe();
            ScrollOfMagicMapping som = new ScrollOfMagicMapping();
            som.doRead();
        } else if(action.equals(AC_TP)){
            empoweredRead();
        }else if (action.equals(AC_RESET)) {
            switch (depth){
                case 2:
                case 3:
                case 4:
                    for (Mob m: Dungeon.level.mobs){
                        if (m instanceof Ghost) {
                            Ghost.Quest.reset();
                        }
                    }
                    break;
                case 7:
                case 8:
                case 9:
                    for (Mob m: Dungeon.level.mobs){
                        if (m instanceof Wandmaker) {
                            Wandmaker.Quest.reset();
                        }
                    }
                    break;
                case 12:
                case 13:
                case 14:
                    for (Mob m: Dungeon.level.mobs){
                        if (m instanceof Blacksmith) {
                            Blacksmith.Quest.reset();
                        }
                    }
                    break;
                case 17:
                case 18:
                case 19:
                    for (Mob m: Dungeon.level.mobs){
                        if (m instanceof Imp) {
                            Imp.Quest.reset();
                        }
                    }
                    break;
            }
            if(Dungeon.level.locked)
                Dungeon.level.unseal();

            Statistics.gnoll_boss = 0;

            InterlevelScene.mode = InterlevelScene.Mode.RESET;

            Game.switchScene(InterlevelScene.class);
        }
    }

    public static class WndSelectLevel extends Window{
        private static final int WIDTH = 120;
        private static final int GAP = 2;
        private static final int BTN_SIZE = 16;
        private static final int PANE_MAX_HEIGHT = 96;

        private int selectedLevel = 0;
        private ArrayList<DepthButton> btns = new ArrayList<>();
        private StyledButton icb;

        public WndSelectLevel(){
            super();
            resize(WIDTH, 0);
            RenderedTextBlock ttl = PixelScene.renderTextBlock(8);
            ttl.text(M.L(LevelTeleporter.class, "interlevel_teleport_title"));
            add(ttl);
            ttl.setPos(WIDTH/2f-ttl.width()/2f, GAP);
            PixelScene.align(ttl);
            ScrollPane sp = new ScrollPane(new Component()){
                @Override
                public void onClick(float x, float y) {
                    super.onClick(x, y);
                    for(DepthButton db: btns){
                        if(db.click(x, y)){
                            break;
                        }
                    }
                }
            };
            add(sp);
            Component content = sp.content();
            float xpos = (WIDTH - 5*BTN_SIZE - GAP*8)/2f;
            float ypos = 0;
            float each = GAP*2 + BTN_SIZE;
            for(int i=0; i< Constants.MAX_DEPTH; ++i){
                int column = i % 5;
                int row = i / 5;
                final int j = i;
                DepthButton db = new DepthButton(j){
                    @Override
                    protected void onClick() {
                        super.onClick();
                        setSelectedLevel(j);
                    }
                };
                db.enable(true);
                db.setRect(xpos + column * each, ypos + row * each, BTN_SIZE, BTN_SIZE);
                PixelScene.align(db);
                content.add(db);
                btns.add(db);
            }

            content.setSize(WIDTH, btns.get(btns.size() - 1).bottom());
            sp.setRect(0, ttl.bottom() + GAP * 2, WIDTH, Math.min(btns.get(btns.size()-1).bottom(), PANE_MAX_HEIGHT));

            icb = new StyledButton(Chrome.Type.RED_BUTTON, M.L(LevelTeleporter.class, "interlevel_teleport_go", selectedLevel)){
                @Override
                protected void onClick() {
                    super.onClick();
                    TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
                    if (timeFreeze != null) timeFreeze.disarmPresses();
                    Swiftthistle.TimeBubble timeBubble = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
                    if (timeBubble != null) timeBubble.disarmPresses();
                    InterlevelScene.curTransition = Dungeon.level.getTransition(Dungeon.hero.pos);
                    InterlevelScene.mode = InterlevelScene.Mode.RETURN;
                    InterlevelScene.returnDepth = selectedLevel;
                    InterlevelScene.returnPos = -1;
                    InterlevelScene.returnBranchId = Dungeon.branchId;
                    Game.switchScene( InterlevelScene.class );
                }
            };
            add(icb);
            icb.icon(Icons.get(Icons.DEPTH));
            icb.setRect(0, sp.bottom() + GAP * 2, WIDTH, BTN_SIZE);
            setSelectedLevel(0);

            sp.scrollTo(0, 0);

            resize(WIDTH, (int) (icb.bottom()));

            sp.setPos(0, ttl.bottom() + GAP * 2);
        }

        private void setSelectedLevel(int lvl){
            this.selectedLevel = lvl;
            icb.text(M.L(LevelTeleporter.class, "interlevel_teleport_go", selectedLevel));
            icb.enable(selectedLevel <= Constants.MAX_DEPTH);
        }
    }

    public static class DepthButton extends StyledButton{
        private int depth;
        public DepthButton(int depth){
            super(Chrome.Type.GEM, String.valueOf(depth), 8);
            this.depth = depth;
        }

        @Override
        protected void layout() {
            super.layout();
            hotArea.width = 0;
            hotArea.height = 0;
        }

        public int getDepth() {
            return depth;
        }

        public boolean click(float x, float y){
            if(active && x > left() && x < right() && y > top() && y < bottom()){
                onClick();
                return true;
            }
            return false;
        }

    }

    public void empoweredRead() {

        GameScene.selectCell(new CellSelector.Listener() {
            @Override
            public void onSelect(Integer target) {
                if (target != null) {
                    //time isn't spent
                    ((HeroSprite)curUser.sprite).read();
                    teleportToLocation(curUser, target);

                }
            }

            @Override
            public String prompt() {
                return Messages.get(ScrollOfTeleportation.class, "prompt");
            }
        });
    }

    public static void teleportToLocation(Hero hero, int pos){
        if (Dungeon.level.avoid[pos] || !Dungeon.level.passable[pos]
                || Actor.findChar(pos) != null){
            GLog.w( Messages.get(ScrollOfTeleportation.class, "cant_reach") );
            return;
        }

        appear( hero, pos );
        Dungeon.level.occupyCell(hero );
        Dungeon.observe();
        GameScene.updateFog();

    }

    public static void appear(Char ch, int pos ) {

        ch.sprite.interruptMotion();

        if (Dungeon.level.heroFOV[pos] || Dungeon.level.heroFOV[ch.pos]){
            Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
        }

        ch.move( pos );
        if (ch.pos == pos) ch.sprite.place( pos );

        if (ch.invisible == 0) {
            ch.sprite.alpha( 0 );
            ch.sprite.parent.add( new AlphaTweener( ch.sprite, 1, 0.4f ) );
        }

        if (Dungeon.level.heroFOV[pos] || ch == Dungeon.hero ) {
            ch.sprite.emitter().start(Speck.factory(Speck.LIGHT), 0.2f, 3);
        }
    }
}
