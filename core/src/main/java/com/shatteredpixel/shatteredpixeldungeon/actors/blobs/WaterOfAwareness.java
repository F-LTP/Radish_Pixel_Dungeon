/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Awareness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.HolySpringUsedBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Identification;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes.Landmark;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;

public class WaterOfAwareness extends WellWater {

    @Override
    protected boolean affectHero(Hero hero) {
        if (hero.heroClass == HeroClass.MOONLIGHT) {
            int points = hero.pointsInTalent(Talent.HOLY_SPRING);
            if (points > 0) {
                HolySpringUsedBuff usedBuff = hero.buff(HolySpringUsedBuff.class);
                if (usedBuff == null) {
                    usedBuff = Buff.affect(hero, HolySpringUsedBuff.class);
                }

				if (usedBuff.canTransformAwareness()) {
					int wellPos = hero.pos;
					GameScene.show(new WndOptions(
							Messages.get(WaterOfAwareness.class, "holy_spring_title"),
							Messages.get(WaterOfAwareness.class, "holy_spring_desc"),
							Messages.get(WaterOfAwareness.class, "holy_spring_normal"),
							Messages.get(WaterOfAwareness.class, "holy_spring_transform")
					) {
						@Override
						protected void onSelect(int index) {
							if (index == 0) {
								consume(wellPos);
								normalEffect(hero);
							} else if (index == 1) {
								consume(wellPos);
								transformEffect(hero, points);
							}
						}
					});
					return false; // 暂时不消耗泉水，等待玩家选择
				}
            }
        }
		return normalEffect(hero);
    }

    private boolean normalEffect(Hero hero) {
        Sample.INSTANCE.play(Assets.Sounds.DRINK);
        emitter.parent.add(new Identification(hero.sprite.center()));

        hero.belongings.observe();

        for (int i = 0; i < Dungeon.level.length(); i++) {

            int terr = Dungeon.level.map[i];
            if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {

                Dungeon.level.discover(i);

                if (Dungeon.level.heroFOV[i]) {
                    GameScene.discoverTile(i, terr);
                }
            }
        }

        Buff.affect(hero, Awareness.class, Awareness.DURATION);
        Dungeon.observe();

        Dungeon.hero.interrupt();

        GLog.p(Messages.get(this, "procced"));

        return true;
    }

    private boolean transformEffect(Hero hero, int points) {
        // 标记已转化
        HolySpringUsedBuff usedBuff = hero.buff(HolySpringUsedBuff.class);
        if (usedBuff != null) {
            usedBuff.markAwarenessUsed();
        }

        Sample.INSTANCE.play(Assets.Sounds.DRINK);
        emitter.parent.add(new Identification(hero.sprite.center()));

        // 掉落升级卷轴
        Dungeon.level.drop(new ScrollOfUpgrade(), hero.pos).sprite.drop();

        // +2 时额外掉落驱邪卷轴
        if (points >= 2) {
            Dungeon.level.drop(new ScrollOfRemoveCurse(), hero.pos).sprite.drop();
            Dungeon.level.drop(new ScrollOfRemoveCurse(), hero.pos).sprite.drop();
        }

        GLog.newLine();
        GLog.p(Messages.get(WaterOfAwareness.class, "holy_spring_transformed"));

        QuickSlotButton.refresh();
        return true;
    }

    @Override
    protected Item affectItem(Item item, int pos) {
        if (item.isIdentified()) {
            return null;
        } else {
            item.identify();
            Badges.validateItemLevelAquired(item);

            Sample.INSTANCE.play(Assets.Sounds.DRINK);
            emitter.parent.add(new Identification(DungeonTilemap.tileCenterToWorld(pos)));

            return item;
        }
    }

    @Override
    protected Landmark record() {
        return Landmark.WELL_OF_AWARENESS;
    }

    @Override
    public void use(BlobEmitter emitter) {
        super.use(emitter);
        emitter.pour(Speck.factory(Speck.QUESTION), 0.3f);
    }

    @Override
    public String tileDesc() {
        return Messages.get(this, "desc");
    }
}
