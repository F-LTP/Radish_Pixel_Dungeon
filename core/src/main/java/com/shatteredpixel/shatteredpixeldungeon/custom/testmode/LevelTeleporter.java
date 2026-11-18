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
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.SewerBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.PrisonBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.WardenBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CavesBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.CityBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.HallsBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.GnollKingBossLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.branches.Branch;
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
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextNumberInput;
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

    private static final String AC_LEVEL_TP = "level_teleport";

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_ASCEND );
        actions.add(AC_DESCEND);

        actions.add(AC_VIEW);
        actions.add(AC_TP);
        actions.add(AC_LEVEL_TP);
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
            if(Dungeon.hero.buff(LockedFloor.class) != null || depth >= maxDepthOf(Dungeon.branchId)) {
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
            // 每个分支的楼层都从 1 开始编号，第 1 层上面没有楼层
            if (Dungeon.hero.buff(LockedFloor.class) != null || depth <= 1) {
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
                // 矿洞回主线的楼梯通向铁匠所在的那一层。还没有任何一层生成过铁匠时这一层是未知的，
                // 矿洞建不出来（加载时抛异常，直接闪退），所以先拦下
                if (Blacksmith.Quest.entranceDepth() < 1
                        && !Dungeon.levelHasBeenGenerated(targetDepth, targetBranch)) {
                    GLog.w(Messages.get(this, "no_branch_here"));
                    return;
                }
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
                // 采矿分支返回主线对应层。矿洞在自己的分支里永远是第 1 层，不能拿当前层数当主线层数；
                // 它回主线的楼梯通向铁匠所在的那一层，这里也回那一层
                returnDepth = Blacksmith.Quest.entranceDepth();
            } else {
                returnDepth = 1;
            }

            if (returnDepth < 1 || returnDepth > maxDepthOf(Branches.MAIN)) {
                GLog.w(Messages.get(this, "cannot_send"));
                return;
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
        } else if (action.equals(AC_LEVEL_TP)) {
            // 打开「楼层传送」面板：网格选层 / 手动输入层数 / Boss 直达
            Game.runOnRenderThread(() -> GameScene.show(new WndSelectLevel()));
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

    /**
     * 某个分支最深到第几层。
     * 支线只有一两层，不能拿主线的总层数去比：跳到不存在的楼层要等切到加载界面才报错，那时已经是闪退。
     */
    private static int maxDepthOf(String branch) {
        Branch b = Branches.get(branch);
        return b == null ? 0 : b.maxDepth;
    }

    /** 「跳层」的公共逻辑：网格选层 / 手动输入层数 / Boss 直达共用 */
    private static void jumpTo(int targetDepth, String branch, Class<? extends Level> forcedLevel) {
        if (targetDepth < 1 || targetDepth > maxDepthOf(branch)) {
            GLog.w(Messages.get(LevelTeleporter.class, "cannot_send"));
            return;
        }

        TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class);
        if (timeFreeze != null) timeFreeze.disarmPresses();
        Swiftthistle.TimeBubble timeBubble = Dungeon.hero.buff(Swiftthistle.TimeBubble.class);
        if (timeBubble != null) timeBubble.disarmPresses();

        if (forcedLevel != null) {
            // 同一层可能有多个 Boss 变体（第10层：天狗 / 典狱长）。
            // 先让这一层变成「未生成」，再由 Dungeon.newLevel() 按指定关卡类重建。
            Dungeon.generatedLevels.remove(Dungeon.levelKey(branch, targetDepth));
            Dungeon.forcedLevelClass = forcedLevel;
        }

        InterlevelScene.mode = InterlevelScene.Mode.RETURN;
        InterlevelScene.returnDepth = targetDepth;
        InterlevelScene.returnPos = -1;
        InterlevelScene.returnBranchId = branch;
        Game.switchScene(InterlevelScene.class);
    }

    /** 「Boss 直达」的一个条目：目标层数 + 关卡类 + 名字文本键 */
    private static class BossEntry {
        final int depth;
        final Class<? extends Level> levelClass;
        final String nameKey;

        BossEntry(int depth, Class<? extends Level> levelClass, String nameKey) {
            this.depth = depth;
            this.levelClass = levelClass;
            this.nameKey = nameKey;
        }
    }

    private static final BossEntry[] BOSSES = {
            new BossEntry(5,  SewerBossLevel.class,     "boss_sewer"),
            new BossEntry(10, PrisonBossLevel.class,    "boss_tengu"),
            new BossEntry(10, WardenBossLevel.class,    "boss_warden"),
            new BossEntry(15, CavesBossLevel.class,     "boss_dm300"),
            new BossEntry(15, GnollKingBossLevel.class, "boss_gnollking"),
            new BossEntry(20, CityBossLevel.class,      "boss_dwarfking"),
            new BossEntry(25, HallsBossLevel.class,     "boss_yog"),
    };

    public static class WndSelectLevel extends Window{
        private static final int WIDTH = 140;
        private static final int GAP = 2;
        private static final int BTN_SIZE = 16;
        private static final int PANE_MAX_HEIGHT = 96;

        private int selectedLevel = 1;
        // 面板只在当前分支里跳，可选的楼层数跟着当前分支走（支线只有一两层）
        private final int maxDepth = Math.max(1, maxDepthOf(Dungeon.branchId));
        private ArrayList<DepthButton> btns = new ArrayList<>();
        private StyledButton icb;
        private StyledButton inputBtn;
        private StyledButton bossBtn;

        public WndSelectLevel(){
            super();
            resize(WIDTH, 0);
            RenderedTextBlock ttl = PixelScene.renderTextBlock(8);
            ttl.text(M.L(LevelTeleporter.class, "interlevel_teleport_title"));
            add(ttl);
            ttl.setPos(WIDTH/2f-ttl.width()/2f, GAP);
            PixelScene.align(ttl);

            // 手动输入层数
            inputBtn = new StyledButton(Chrome.Type.RED_BUTTON, M.L(LevelTeleporter.class, "interlevel_teleport_input")){
                @Override
                protected void onClick() {
                    super.onClick();
                    showNumberInput();
                }
            };
            inputBtn.icon(Icons.get(Icons.DEPTH));
            add(inputBtn);
            inputBtn.setRect(0, ttl.bottom() + GAP * 2, WIDTH, BTN_SIZE);

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
            for(int i=0; i< maxDepth; ++i){
                int column = i % 5;
                int row = i / 5;
                final int j = i + 1; // 楼层从1开始，不再出现「0层」
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
            sp.setRect(0, inputBtn.bottom() + GAP * 2, WIDTH, Math.min(btns.get(btns.size()-1).bottom(), PANE_MAX_HEIGHT));

            icb = new StyledButton(Chrome.Type.RED_BUTTON, M.L(LevelTeleporter.class, "interlevel_teleport_go", selectedLevel)){
                @Override
                protected void onClick() {
                    super.onClick();
                    jumpTo(selectedLevel, Dungeon.branchId, null);
                }
            };
            add(icb);
            icb.icon(Icons.get(Icons.DEPTH));
            icb.setRect(0, sp.bottom() + GAP * 2, WIDTH, BTN_SIZE);

            // Boss 直达
            bossBtn = new StyledButton(Chrome.Type.GEM, M.L(LevelTeleporter.class, "interlevel_teleport_boss")){
                @Override
                protected void onClick() {
                    super.onClick();
                    GameScene.show(new WndSelectBoss());
                }
            };
            add(bossBtn);
            bossBtn.setRect(0, icb.bottom() + GAP * 2, WIDTH, BTN_SIZE);

            setSelectedLevel(Math.max(1, Math.min(maxDepth, depth)));

            sp.scrollTo(0, 0);

            resize(WIDTH, (int) (bossBtn.bottom()));

            sp.setPos(0, inputBtn.bottom() + GAP * 2);
        }

        /** 手动输入层数：确认后直接跳到该层（当前分支） */
        private void showNumberInput() {
            GameScene.show(new WndTextNumberInput(
                    M.L(LevelTeleporter.class, "interlevel_teleport_input_title"),
                    M.L(LevelTeleporter.class, "interlevel_teleport_input_body", 1, maxDepth),
                    String.valueOf(selectedLevel),
                    2,
                    false,
                    M.L(LevelTeleporter.class, "interlevel_teleport_confirm"),
                    M.L(LevelTeleporter.class, "interlevel_teleport_cancel"),
                    false){
                @Override
                public void onSelect(boolean positive, String text) {
                    if (!positive || text == null) return;
                    int target;
                    try {
                        target = Integer.parseInt(text.trim());
                    } catch (NumberFormatException e) {
                        GLog.w(M.L(LevelTeleporter.class, "interlevel_teleport_invalid", maxDepth));
                        return;
                    }
                    if (target < 1 || target > maxDepth) {
                        GLog.w(M.L(LevelTeleporter.class, "interlevel_teleport_invalid", maxDepth));
                        return;
                    }
                    jumpTo(target, Dungeon.branchId, null);
                }
            });
        }

        private void setSelectedLevel(int lvl){
            this.selectedLevel = lvl;
            icb.text(M.L(LevelTeleporter.class, "interlevel_teleport_go", selectedLevel));
            icb.enable(selectedLevel <= maxDepth);
        }
    }

    /** Boss 直达：每个 Boss 一个按钮，点击后按「对应层 + 指定关卡类」重新生成该层 */
    public static class WndSelectBoss extends Window {
        private static final int WIDTH = 140;
        private static final int GAP = 2;
        private static final int BTN_HEIGHT = 16;
        private static final int PANE_MAX_HEIGHT = 160;

        public WndSelectBoss() {
            super();
            resize(WIDTH, 0);

            RenderedTextBlock ttl = PixelScene.renderTextBlock(8);
            ttl.text(M.L(LevelTeleporter.class, "interlevel_teleport_boss_title"));
            add(ttl);
            ttl.setPos(WIDTH/2f-ttl.width()/2f, GAP);
            PixelScene.align(ttl);

            ScrollPane sp = new ScrollPane(new Component());
            add(sp);
            Component content = sp.content();

            float ypos = 0;
            for (final BossEntry be : BOSSES) {
                StyledButton btn = new StyledButton(Chrome.Type.RED_BUTTON,
                        M.L(LevelTeleporter.class, "interlevel_teleport_boss_entry",
                                be.depth, M.L(LevelTeleporter.class, be.nameKey))) {
                    @Override
                    protected void onClick() {
                        super.onClick();
                        jumpTo(be.depth, Branches.MAIN, be.levelClass);
                    }
                };
                btn.setRect(0, ypos, WIDTH, BTN_HEIGHT);
                PixelScene.align(btn);
                content.add(btn);
                ypos += BTN_HEIGHT + GAP * 2;
            }

            content.setSize(WIDTH, ypos);
            sp.setRect(0, ttl.bottom() + GAP * 2, WIDTH, Math.min(ypos, PANE_MAX_HEIGHT));
            sp.scrollTo(0, 0);
            resize(WIDTH, (int) (sp.bottom()));
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
