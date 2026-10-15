package com.shatteredpixel.shatteredpixeldungeon.events;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;

/**
 * 命中判定事件。
 * <p>
 * 在 {@code Char.hit(...)} 中、基础命中 / 闪避数值（含戒指等修正）算出之后、随机投掷
 * 之前发布。携带攻守双方、是否法术攻击、双方武器，以及可变的命中 / 闪避数值。
 * <p>
 * 订阅者可修改 {@link #getAccuracy()} / {@link #getEvasion()} 参与数值计算；也可以通过
 * {@link #forceHit()} / {@link #forceMiss()} 覆盖本次判定结果。
 * <p>
 * <b>覆盖语义为「先写者获胜」</b>：数值越小越先执行的约定依然成立，但只要已有订阅者设置了
 * 结果，后续订阅者都应先检查 {@link #getResult()} 再决定是否覆写。<b>这与
 * {@link GameEvent#cancel()} 的语义不同</b>，请勿混用。
 * <p>
 * 优先级约定（数值越大越先执行）：攻击方强制必中（+1000）→ 防御方强制必闪（+800 区间）
 * → 武器强制必中（+500）。
 */
public class HitCheckEvent extends GameEvent {

    public enum Result {
        /** 未覆写，走常规随机比较。 */
        NONE,
        /** 本次必定命中。 */
        FORCE_HIT,
        /** 本次必定闪避。 */
        FORCE_MISS
    }

    private final Char attacker;
    private final Char defender;
    private final boolean magic;

    private final Item attackerWeapon;
    private final Item defenderWeapon;

    private float accuracy;
    private float evasion;

    private Result result = Result.NONE;

    public HitCheckEvent(Char attacker, Char defender, boolean magic,
                         Item attackerWeapon, Item defenderWeapon,
                         float accuracy, float evasion) {
        this.attacker = attacker;
        this.defender = defender;
        this.magic = magic;
        this.attackerWeapon = attackerWeapon;
        this.defenderWeapon = defenderWeapon;
        this.accuracy = accuracy;
        this.evasion = evasion;
    }

    /** 攻击方。 */
    public Char getAttacker() { return attacker; }

    /** 防御方。 */
    public Char getDefender() { return defender; }

    /** 本次是否为法术攻击。 */
    public boolean isMagic() { return magic; }

    /** 攻击者武器（若有），否则为 null。 */
    public Item getAttackerWeapon() { return attackerWeapon; }

    /** 防御者武器（若有），否则为 null。 */
    public Item getDefenderWeapon() { return defenderWeapon; }

    /** 攻击方命中数值（可变）。 */
    public float getAccuracy() { return accuracy; }

    public void setAccuracy(float accuracy) { this.accuracy = accuracy; }

    /** 防御方闪避数值（可变）。 */
    public float getEvasion() { return evasion; }

    public void setEvasion(float evasion) { this.evasion = evasion; }

    /** 当前覆盖结果，默认 {@link Result#NONE}。 */
    public Result getResult() { return result; }

    public void setResult(Result result) { this.result = result; }

    /** 覆写为必定命中（先写者获胜，已覆写则不生效）。 */
    public void forceHit() {
        if (result == Result.NONE) result = Result.FORCE_HIT;
    }

    /** 覆写为必定闪避（先写者获胜，已覆写则不生效）。 */
    public void forceMiss() {
        if (result == Result.NONE) result = Result.FORCE_MISS;
    }
}
