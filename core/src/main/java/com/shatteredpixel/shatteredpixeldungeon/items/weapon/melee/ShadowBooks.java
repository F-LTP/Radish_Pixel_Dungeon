package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;

public class ShadowBooks extends MeleeWeapon {

    public int chance;

    {
        image = ItemSpriteSheet.SHADOW_BOOK;
        tier = 4;
    }

    @Override
    public boolean doEquip( Hero hero ) {
        // 装备可能失败（手里是脱不下来的诅咒武器、村正狂热），确认装上了再挂 buff
        if (super.doEquip(hero)) {
            Buff.affect(hero, ShadowProject.class);
            return true;
        }
        return false;
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        // 同理，卸不下来（诅咒）时 buff 要留着
        if (super.doUnequip(hero, collect, single)) {
            Buff.detach(hero, ShadowProject.class);
            return true;
        }
        return false;
    }

    public static class ShadowProject extends Buff {

        {
            type = buffType.POSITIVE;
        }

        @Override
        public boolean act() {
            // 主手不一定是幻影之书：旧存档里可能留着装备失败时挂上的 buff，直接强转会崩溃
            ShadowBooks w2 = target instanceof Hero
                    ? com.shatteredpixel.shatteredpixeldungeon.items.Fusion.fusedItem(
                        ((Hero) target).belongings.weapon, ShadowBooks.class) : null;
            if (w2 != null) {
                w2.chance = w2.mirrorCasts();
            } else {
                detach();
                return true;
            }
            spend(TICK);
            return true;
        }
    }

    // 每读一张卷轴施放几次镜像，与 Scroll 里实际使用的公式一致
    private int mirrorCasts() {
        return (int) (1 + 0.2*level());
    }

    @Override
    public int STRReq(int lvl) {
        return (7 + tier * 2) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        chance = bundle.getInt("chance");
    }

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put("chance", chance);
    }

    @Override
    public int min(int lvl) {
        return 2 + lvl * 2;
    }
    @Override
    public int max(int lvl) {
        return 20 + lvl * 2;
    }

    @Override
    public String desc() {

        String desc;

        if(isIdentified()){
            // 数字现算：从没装备过的书 chance 字段还是 0；每次施放实际生成 2 个镜像
            desc = Messages.get(this, "desc", 2 * mirrorCasts());
        } else {
            desc = Messages.get(this, "normal_desc",1);
        }

        return desc;
    }

}
