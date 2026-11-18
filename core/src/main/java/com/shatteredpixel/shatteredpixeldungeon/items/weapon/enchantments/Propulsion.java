package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackHitEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.AttackMissEvent;
import com.shatteredpixel.shatteredpixeldungeon.events.SubscribeEvent;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

/**
 * 推进附魔：不贴脸的攻击（近战加距离、法术、远程物理）命中或未命中后，
 * 询问玩家是否突进到目标身边。玩家可以拒绝，不会自动位移。
 */
public class Propulsion extends Weapon.Enchantment {

    //推进会把远程角色拽到敌人面前，对灵能弓是负收益，所以不给远程
    @Override
    public boolean canEnchant( Weapon weapon ){
        return !isRangedWeapon( weapon );
    }

    private static final ItemSprite.Glowing AZURE = new ItemSprite.Glowing( 0x1E90FF );

    private static Char pendingTarget = null;
    private static boolean prompting = false;

    @Override
    public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
        return damage;
    }

    @SubscribeEvent(event = AttackHitEvent.class, priority = 0)
    public static void onAttackHit( AttackHitEvent event ) {
        onAttack( event.getAttacker(), event.getDefender() );
    }

    @SubscribeEvent(event = AttackMissEvent.class, priority = 0)
    public static void onAttackMiss( AttackMissEvent event ) {
        onAttack( event.getAttacker(), event.getDefender() );
    }

    /** 统一入口：不管是近战远距、法术还是远程，只要不是贴脸就排队询问。 */
    private static void onAttack( Char attacker, Char defender ) {
        if (attacker == null || defender == null || attacker == defender) {
            return;
        }

        Hero hero;
        Char target;
        if (attacker instanceof Hero) {
            hero = (Hero) attacker;
            target = defender;
        } else if (defender instanceof Hero) {
            // 英雄被不贴脸的攻击命中/未命中，同样可以反推过去
            hero = (Hero) defender;
            target = attacker;
        } else {
            return;
        }

        //看主手栏位里的武器，不能用 belongings.weapon()：投掷、射击结算期间它返回的是飞出去的那一件，
        //于是投掷武器和灵能弓的攻击永远不会触发，与说明里的「远程物理均可」不符
        KindOfWeapon held = hero.belongings.weapon;
        if (held != null && hero.buff( LostInventory.class ) != null && !held.keptThoughLostInvent) {
            held = null;
        }
        if (!(held instanceof Weapon) || !(((Weapon) held).enchantment instanceof Propulsion)) {
            return;
        }
        if (!target.isAlive()) {
            return;
        }
        if (Dungeon.level != null && Dungeon.level.distance( hero.pos, target.pos ) >= 2) {
            queue( hero, target );
        }
    }

    /** 英雄就绪后补上待处理的询问。 */
    public static void onHeroReady( Hero hero ) {
        tryPrompt( hero );
    }

    private static void queue( Hero hero, Char target ) {
        if (hero == null || target == null || target == hero || !target.isAlive()) {
            return;
        }
        pendingTarget = target;
        tryPrompt( hero );
    }

    private static void tryPrompt( Hero hero ) {
        if (pendingTarget == null || prompting) {
            return;
        }
        if (hero == null || !hero.ready || hero.curAction != null
                || !hero.isAlive() || Dungeon.level == null) {
            return;
        }

        final Char target = pendingTarget;
        pendingTarget = null;
        if (!onLevel( target )) {
            return;
        }
        prompting = true;
        Game.runOnRenderThread( new Callback() {
            @Override
            public void call() {
                prompting = false;
                if (Dungeon.hero == null || !Dungeon.hero.ready
                        || !Dungeon.hero.isAlive() || !onLevel( target )) {
                    return;
                }
                GameScene.show( new WndOptions(
                        Messages.get( Propulsion.class, "prompt_title" ),
                        Messages.get( Propulsion.class, "prompt_text" ),
                        Messages.get( Propulsion.class, "prompt_yes" ),
                        Messages.get( Propulsion.class, "prompt_no" ) ) {
                    @Override
                    protected void onSelect( int index ) {
                        if (index == 0) {
                            dashAdjacent( Dungeon.hero, target );
                        }
                    }
                } );
            }
        } );
    }

    /**
     * 目标是否还活着并且还在当前这一层。
     * 待询问的目标记在静态字段里，要等英雄下一次就绪才弹窗；英雄在那之前换了层、读了档或重开了一局
     * （被远程攻击打死、边挨打边下楼）时，字段里留着的是上一层的怪物。不拦下的话会在新的一层弹出询问，
     * 选「突进」就把英雄放到那只怪物旧坐标旁边的格子上。
     */
    private static boolean onLevel( Char target ) {
        return target != null && target.isAlive() && Actor.chars().contains( target );
    }

    /** 传送到目标相邻的空格。 */
    private static void dashAdjacent( Hero hero, Char target ) {
        if (hero == null || !hero.isAlive() || !onLevel( target )
                || Dungeon.level == null || hero.rooted) {
            return;
        }

        int w = Dungeon.level.width();
        int tx = target.pos % w, ty = target.pos / w;
        for (int offset : PathFinder.NEIGHBOURS8) {
            int cell = target.pos + offset;
            if (cell < 0 || cell >= Dungeon.level.length()) {
                continue;
            }
            if (Math.abs( cell % w - tx ) > 1 || Math.abs( cell / w - ty ) > 1) {
                continue;
            }
            if (!Dungeon.level.passable[cell] || Dungeon.level.solid[cell]) {
                continue;
            }
            if (Actor.findChar( cell ) != null) {
                continue;
            }

            //走角色自己的瞬移流程，而不是直接改坐标再踩格子：离开门格要关门、漂浮时不该踩到陷阱和草、
            //落点的蛛网要生效，英雄移动事件也要照常发出
            hero.sprite.interruptMotion();
            hero.move( cell, false );
            hero.sprite.place( hero.pos );
            hero.sprite.visible = true;
            Dungeon.observe();
            return;
        }
    }

    @Override
    public ItemSprite.Glowing glowing() {
        return AZURE;
    }
}
