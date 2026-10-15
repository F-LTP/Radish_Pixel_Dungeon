/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Radish Pixel Dungeon
 * Copyright (C) 2024-2026 Radish Pixel Dungeon Team
 */

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.damage.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.HeadCleaver;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 死因 → 死亡动画 的选择器。Char.die() 据此决定播放哪种死亡动画。
 * shader 死亡动画（按伤害类型）与触发条件解耦：
 * 通用映射见 {@link #shaderDeathFor(Char)}，任何职业/来源只需通过
 * {@link #registerShaderDeathRule(Predicate)} 声明自己的适用条件即可复用。
 */
public class DeathAnimations {

  public interface Rule {
    /** @return 该死因应播放的死亡动画；不适用则返回 null 交给下一条规则 */
    DeathAnimation select(Char ch, Object src);
  }

  private static final List<Rule> RULES = new ArrayList<>();

  public static void register(Rule rule) {
    RULES.add(rule);
  }

  /** @return 匹配到的死亡动画；没有任何规则适用时返回 null（由 Char.die 回退 sprite.die()） */
  public static DeathAnimation select(Char ch, Object src) {
    for (Rule rule : RULES) {
      DeathAnimation anim = rule.select(ch, src);
      if (anim != null) {
        return anim;
      }
    }
    return null;
  }

  /**
   * 注册一条"按伤害类型的 shader 死亡动画"规则。
   * 条件成立且受害者最近伤害类型有对应 shader 时生效，否则交给后续规则。
   */
  public static void registerShaderDeathRule(Predicate<Char> condition) {
    register((ch, src) -> {
      if (!condition.test(ch)) {
        return null;
      }
      return shaderDeathFor(ch);
    });
  }

  /** @return 按受害者最近伤害类型选择的 shader 死亡动画；该类型无对应 shader 时返回 null */
  public static DeathAnimation shaderDeathFor(Char ch) {
    ShaderEffect.ShaderType type = DiceMageShaderFor(ch.lastDamageType);
    return type == null ? null : ShaderEffect.deathAnimation(type, 0.5f);
  }

  /**
   * 斩首者：被斩首打击杀死（die(src) 的 src 是 HeadCleaver.proc 传入的 headCleaverTracker 标记）。
   * 上半段朝远离攻击者的方向抛出，附带特殊音效。
   */
  private static final Rule HEADCLEAVER_CLEAVE = (ch, src) -> {
    if (!(src instanceof HeadCleaver.headCleaverTracker) || ch == Dungeon.hero) {
      return null;
    }
    return (sprite, onFinished) -> {
      Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG, 1f, 0.75f);
      HalvedCorpse.animation(cleaveAngle(ch), 120f).play(sprite, onFinished);
    };
  };

  static {
    // 斩首者：被斩首打击杀死 → 腰斩动画 + 特殊音效（先于通用 shader 规则）
    register(HEADCLEAVER_CLEAVE);

    // 骰子法师：敌方怪物死亡时按伤害类型播放 shader 死亡动画
    registerShaderDeathRule(DeathAnimations::diceMageEnemyKill);

    // 兜底：默认死亡动画——贴图序列帧（MobSprite 会在动画结束后自行淡出）
    register((ch, src) -> (sprite, onFinished) -> sprite.die());
  }

  /** 上半段抛出方向：背对攻击者斜向上（攻击者在左 → 右上 45°，在右 → 左上 135°）。 */
  private static float cleaveAngle(Char ch) {
    Char attacker = ch.lastAttacker;
    if (attacker != null && attacker.sprite != null && ch.sprite != null) {
      return attacker.sprite.x < ch.sprite.x ? 45f : 135f;
    }
    return Random.Int(2) == 0 ? 45f : 135f;
  }

  private static boolean diceMageEnemyKill(Char ch) {
    if (!(ch instanceof Mob) || ch.alignment != Char.Alignment.ENEMY) {
      return false;
    }
    Hero hero = Dungeon.hero;
    return hero != null && hero.subClass == HeroSubClasses.DICE_MAGE;
  }

  private static ShaderEffect.ShaderType DiceMageShaderFor(DamageType type) {
    if (type == null) {
      return null;
    }
    switch (type) {
      case FIRE:
      case BURNING_STATUS:
        return ShaderEffect.ShaderType.BURN;
      case FROST:
      case CHILL:
        return ShaderEffect.ShaderType.ALPHA;
      case LIGHTNING:
        return ShaderEffect.ShaderType.SINGULARITY;
      case TOXIC:
      case CORROSIVE:
      case POISON:
      case OOZE:
        return ShaderEffect.ShaderType.ACID;
      case MAGICAL:
      case CORRUPTION:
        return ShaderEffect.ShaderType.NOISE;
      case PHYSICAL:
      case PHYSICAL_NO_ARMOR:
      case BLEEDING:
      case FALL:
      case CHASM:
      case PICK:
        return ShaderEffect.ShaderType.CUT;
      default:
        return null;
    }
  }
}
