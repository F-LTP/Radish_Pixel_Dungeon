package com.shatteredpixel.shatteredpixeldungeon.actors.hero.talents.moonlight;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClasses;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;

/**
 * 砥砺锋芒天赋
 * 以一件已鉴定(+1)或任意(+2)武器/护甲为代价鉴定同类型物品
 */
public class SharpeningEdgeTalent {

    public static final String AC_SHARPENING_EDGE = "SHARPENING_EDGE";

    /**
     * 检查是否可以使用砥砺锋芒
     * @param hero 英雄
     * @param sacrificeItem 作为代价的物品
     * @return 是否可以使用
     */
    public static boolean canUse(Hero hero, Item sacrificeItem) {
        // 只对月华英雄生效
        if (hero.heroClass != HeroClasses.MOONLIGHT) return false;

        // 检查天赋点数
        int points = hero.pointsInTalent(Talent.SHARPENING_EDGE);
        if (points <= 0) return false;

        // +1时需要已鉴定的物品，+2时任意物品都可以
        if (points == 1 && !sacrificeItem.isIdentified()) return false;

        return true;
    }

    private static boolean isValidTarget(Hero hero, Item sacrificeItem, Item targetItem) {
        if (targetItem == null || targetItem == sacrificeItem
                || !hero.belongings.contains(targetItem) || targetItem.isIdentified()) {
            return false;
        }

        return sacrificeItem instanceof KindOfWeapon
                ? targetItem instanceof KindOfWeapon
                : sacrificeItem instanceof Armor && targetItem instanceof Armor;
    }

    private static boolean hasValidTarget(Hero hero, Item sacrificeItem) {
        for (Item item : hero.belongings) {
            if (isValidTarget(hero, sacrificeItem, item)) return true;
        }
        return false;
    }

    /**
     * 执行砥砺锋芒
     * @param hero 英雄
     * @param sacrificeItem 代价物品
     * @param targetItem 目标物品
     */
    public static void execute(Hero hero, Item sacrificeItem, Item targetItem) {
        // 消耗代价物品（先卸下再丢弃）
        if (sacrificeItem instanceof EquipableItem && sacrificeItem.isEquipped(hero)) {
            ((EquipableItem)sacrificeItem).doUnequip(hero, false, true);
        }
        sacrificeItem.detach(hero.belongings.backpack);

        // 鉴定目标物品
        if (targetItem instanceof Weapon) {
            ((Weapon) targetItem).completeIdentificationProgress();
        } else if (targetItem instanceof Armor) {
            ((Armor) targetItem).completeIdentificationProgress();
        }

        // Oblivion shards block passive identification, but can identify items at full progress.
        if (!ShardOfOblivion.passiveIDDisabled()) {
            targetItem.identify();
        }

        GLog.p(Messages.get(SharpeningEdgeTalent.class, "success", targetItem.name()));
        hero.spendAndNext(1f);
        hero.sprite.operate(hero.pos);
    }

    /**
     * 显示选择目标物品的窗口
     * @param hero 英雄
     * @param sacrificeItem 代价物品
     */
    public static void showTargetSelectionWindow(Hero hero, Item sacrificeItem) {
        if (!hasValidTarget(hero, sacrificeItem)) {
            GLog.w(Messages.get(SharpeningEdgeTalent.class, "no_target"));
            return;
        }

        GameScene.selectItem(new WndBag.ItemSelector() {
            @Override
            public String textPrompt() {
                return Messages.get(SharpeningEdgeTalent.class, "prompt");
            }

            @Override
            public Class<? extends Bag> preferredBag() {
                return Belongings.Backpack.class;
            }

            @Override
            public boolean itemSelectable(Item item) {
                return isValidTarget(hero, sacrificeItem, item);
            }

            @Override
            public void onSelect(Item item) {
                if (item == null) return;

                if (!hero.belongings.contains(sacrificeItem)
                        || !canUse(hero, sacrificeItem)
                        || !isValidTarget(hero, sacrificeItem, item)) {
                    GLog.w(Messages.get(SharpeningEdgeTalent.class, "no_target"));
                    return;
                }

                execute(hero, sacrificeItem, item);
            }
        });
    }
}
