package com.shatteredpixel.shatteredpixeldungeon.custom.testmode;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class TalentSetter extends TestItem {

    private static final String AC_MAXIMIZE = "MAXIMIZE";
    private static final String AC_CLEAR = "CLEAR";

    {
        image = ItemSpriteSheet.EXOTIC_GOLDEN;
        unique = true;
        bones = false;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_MAXIMIZE);
        actions.add(AC_CLEAR);
        return actions;
    }

    @Override
    public void execute(final Hero hero, String action) {
        super.execute(hero, action);

        if (action.equals(AC_MAXIMIZE) || action.equals(AC_CLEAR)) {
            final boolean maximize = action.equals(AC_MAXIMIZE);

            GameScene.show(new WndOptions(
                    Messages.titleCase(trueName()),
                    Messages.get(this, maximize ? "maximize_select" : "clear_select"),
                    Messages.titleCase(Messages.get(TalentsPane.class, "tier", 1)),
                    Messages.titleCase(Messages.get(TalentsPane.class, "tier", 2)),
                    Messages.titleCase(Messages.get(TalentsPane.class, "tier", 3)),
                    Messages.titleCase(Messages.get(TalentsPane.class, "tier", 4))
            ) {
                @Override
                protected boolean enabled(int index) {
                    // 第1层、第2层始终可用（如果存在天赋）
                    if (index <= 1) {
                        return hero.talents.size() > index && !hero.talents.get(index).isEmpty();
                    }
                    // 第3层需要先转职
                    if (index == 2) {
                        return hero.subClass != HeroSubClass.NONE 
                                && hero.talents.size() > index && !hero.talents.get(index).isEmpty();
                    }
                    // 第4层需要护甲技能或恶魔天赋
                    if (index == 3) {
                        return (hero.armorAbility != null || hero.powerOfImp)
                                && hero.talents.size() > index && !hero.talents.get(index).isEmpty();
                    }
                    return false;
                }

                @Override
                protected void onSelect(int index) {
                    if (index != -1) {
                        applyTalentChange(hero, index, maximize);
                    }
                }
            });
        }
    }

    private void applyTalentChange(Hero hero, int tierIndex, boolean maximize) {
        if (hero.talents.size() <= tierIndex) {
            GLog.w(Messages.get(this, "no_talents"));
            return;
        }

        LinkedHashMap<Talent, Integer> tierTalents = hero.talents.get(tierIndex);
        if (tierTalents.isEmpty()) {
            GLog.w(Messages.get(this, "no_talents"));
            return;
        }

        int changedCount = 0;
        for (Talent talent : tierTalents.keySet()) {
            int currentPoints = tierTalents.get(talent);
            int targetPoints = maximize ? talent.maxPoints() : 0;

            if (currentPoints != targetPoints) {
                tierTalents.put(talent, targetPoints);

                // 如果是点满，触发天赋升级回调
                if (maximize && targetPoints > 0) {
                    for (int i = currentPoints; i < targetPoints; i++) {
                        Talent.onTalentUpgraded(hero, talent);
                    }
                }

                changedCount++;
            }
        }

        if (changedCount > 0) {
            if (maximize) {
                GLog.p(Messages.get(this, "maximized", tierIndex + 1, changedCount));
            } else {
                GLog.i(Messages.get(this, "cleared", tierIndex + 1, changedCount));
            }
        } else {
            GLog.i(Messages.get(this, maximize ? "already_max" : "already_empty", tierIndex + 1));
        }
    }

    @Override
    public String desc() {
        return Messages.get(this, "desc");
    }

    @Override
    public String name() {
        return Messages.get(this, "name");
    }
}