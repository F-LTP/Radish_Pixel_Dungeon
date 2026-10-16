package com.shatteredpixel.shatteredpixeldungeon.levels.traps;

import static com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.effects.SnDBGM;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishBoss.GnollKing;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RadishBoss.GnollShamanKing;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.levels.CavesLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BossHealthBar;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class GnollKingBossLevel extends Level {

    private static final int W = WALL;
    private static final int E = Terrain.EMPTY;
    private static final int R = Terrain.EXIT;
    private static final int H = Terrain.HIGH_GRASS;
    private static final int P = Terrain.PEDESTAL;
    private static final int X = Terrain.ENTRANCE;
    private static final int D = WALL;

    private static final int WIDTH = 19;
    private static final int HEIGHT = 19;
    private static final int GATE_POS = 180;
    private static final int EXIT_POS = 28;
    private static final int GNOLL_KING_POS = 236;
    private static final int RETURN_ENTRANCE_POS = 237;
    private static final int SHAMAN_KING_POS = 238;

    @Override
    public void playLevelMusic() {
        if (SnDBGM.playLevelMusic()) return;
        if (locked){
            if (BossHealthBar.isBleeding()){
                Music.INSTANCE.play(Assets.Music.CAVES_BOSS_FINALE, true);
            } else {
                Music.INSTANCE.play(Assets.Music.CAVES_BOSS, true);
            }
            //if wall isn't broken
        } else {
            Music.INSTANCE.playTracks(CavesLevel.CAVES_TRACK_LIST, CavesLevel.CAVES_TRACK_CHANCES, false);
        }
    }

    @Override
    public void occupyCell( Char ch ) {
        super.occupyCell(ch);
        if (ch == Dungeon.hero && !locked
                && distance(ch.pos, GATE_POS) >= 3
                && map[GATE_POS] == Terrain.ENTRANCE) {
            seal();
        }
    }

    @Override
    public void seal() {
        if (locked) return;

        LevelTransition exitTransition = getTransitionByDirection(LevelTransition.Direction.DOWN);
        if (exitTransition == null) {
            exitTransition = new LevelTransition(this, EXIT_POS, LevelTransition.Type.REGULAR_EXIT);
            transitions.add(exitTransition);
        }

        super.seal();
        int exit = exitTransition.cell();
        if (exit < 0 || exit >= length()) {
            exit = EXIT_POS;
        }
        set( exit, Terrain.EMPTY );
        GameScene.updateMap( exit );
        Dungeon.observe();

        CellEmitter.get( exit ).start( Speck.factory( Speck.ROCK ), 0.07f, 10 );
        PixelScene.shake( 3, 0.7f );
        Sample.INSTANCE.play( Assets.Sounds.ROCKS );

        GnollKing gk = new GnollKing();
        gk.pos = GNOLL_KING_POS;
        BossHealthBar.assignBoss(gk);
        gk.state = gk.WANDERING;
        GameScene.add(gk);

        CellEmitter.get( GNOLL_KING_POS ).start( Speck.factory( Speck.BUBBLE ), 0.07f, 10 );
        PixelScene.shake( 3, 0.7f );
        Sample.INSTANCE.play( Assets.Sounds.TELEPORT );

        GnollShamanKing gsk = new GnollShamanKing();
        gsk.pos = SHAMAN_KING_POS;
        BossHealthBar.assignBoss(gsk);
        gsk.state = gsk.WANDERING;
        GameScene.add(gsk);

        CellEmitter.get( SHAMAN_KING_POS ).start( Speck.factory( Speck.BUBBLE ), 0.07f, 10 );
        PixelScene.shake( 3, 0.7f );
        Sample.INSTANCE.play( Assets.Sounds.TELEPORT );

        Game.runOnRenderThread(new Callback() {
            @Override
            public void call() {
                if (!SnDBGM.playLevelMusic()) {
                    Music.INSTANCE.play(Assets.Music.CAVES_BOSS, true);
                }
            }
        });
    }

    @Override
    public void unseal() {
        super.unseal();
        transitions.clear();

        LevelTransition exit = new LevelTransition(this, EXIT_POS, LevelTransition.Type.REGULAR_EXIT);
        transitions.add(exit);
        set(EXIT_POS, Terrain.EXIT);
        GameScene.updateMap(EXIT_POS);

        set(GATE_POS, Terrain.EMPTY);
        GameScene.updateMap(GATE_POS);

        int entrance = RETURN_ENTRANCE_POS;
        LevelTransition enter = new LevelTransition(this, entrance, LevelTransition.Type.REGULAR_ENTRANCE);
        transitions.add(enter);

        set(entrance, Terrain.ENTRANCE);
        GameScene.updateMap(entrance);
        Dungeon.observe();

    }

    private static final int[] code_map = {
        D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,
        D,W,W,W,W,W,W,W,W,R,W,W,W,W,W,W,W,W,D,
        D,W,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,W,D,
        D,W,E,E,E,E,W,W,W,E,W,W,W,E,E,E,E,W,D,
        D,W,E,E,E,E,E,E,W,W,W,E,E,E,E,E,E,W,D,
        D,W,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,W,D,
        D,W,E,E,W,W,E,E,E,E,E,E,E,W,W,E,E,W,D,
        D,W,E,W,W,E,E,E,E,E,E,E,E,E,W,W,E,W,D,
        D,W,E,W,E,E,E,E,E,E,E,E,E,E,E,W,E,W,D,
        D,W,E,E,E,E,E,E,E,X,E,E,E,E,E,E,E,W,D,
        D,W,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,W,D,
        D,W,E,E,E,E,E,E,E,E,E,E,E,E,E,E,E,W,D,
        D,W,E,H,H,H,H,E,P,E,P,E,H,H,H,H,E,W,D,
        D,W,E,H,E,E,H,E,E,E,E,E,H,E,E,H,E,W,D,
        D,W,E,H,H,H,H,E,H,H,H,E,H,H,H,H,E,W,D,
        D,W,E,E,E,E,E,E,H,E,H,E,E,E,E,E,E,W,D,
        D,W,E,E,E,E,E,E,H,H,H,E,E,E,E,E,E,W,D,
        D,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,W,D,
        D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,D,
    };


    {
        color1 = 5459774;
        color2 = 12179041;
    }

    protected boolean build() {
        setSize(WIDTH, HEIGHT);
        map = code_map.clone();

        LevelTransition ecne = new LevelTransition(this, GATE_POS, LevelTransition.Type.REGULAR_ENTRANCE);
        transitions.add(ecne);

        LevelTransition exit = new LevelTransition(this, EXIT_POS, LevelTransition.Type.REGULAR_EXIT);
        transitions.add(exit);
        map[EXIT_POS] = Terrain.LOCKED_EXIT;

        return true;
    }


    protected void createItems() {
    }

    @Override
    protected void createMobs() {

    }

    public Actor respawner() {
        return null;
    }

    public String tilesTex() {
        return Assets.Environment.TILES_MOSS;
    }

    public String waterTex() {
        return Assets.Environment.WATER_MOSS;
    }

}
