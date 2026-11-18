package rpdtest.scenarios;

import java.util.ArrayList;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.challenge.AlchemyApprenticeChallenge;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfIntuition;

import rpdtest.H;
import rpdtest.Scenario;

public class Extra_AlchemyApprentice implements Scenario {

    @Override
    public String describe() {
        return "炼金学徒：配方加价、卷轴拆解不鉴定且产出两种符石";
    }

    @Override
    public void run(H h) {
        Arena.start(h);
        h.world("brew with alchemy apprentice", () -> {
            Scroll.initLabels();
            Scroll scroll = new ScrollOfIdentify();
            ArrayList<Item> ingredients = new ArrayList<>();
            ingredients.add(scroll);
            Scroll.ScrollToStone recipe = new Scroll.ScrollToStone();
            h.require(!scroll.isKnown(), "卷轴尚未鉴定");
            h.check(recipe.energyCost(ingredients) == 0, "普通拆解配方不耗能量");
            Dungeon.challenges |= Challenges.ALCHEMY_APPRENTICE;
            h.check(recipe.energyCost(ingredients) == 2, "挑战开启后免费配方也加价 2 点");
            h.check(AlchemyApprenticeChallenge.recipeCost(5) == 7, "原本耗能的配方加价 2 点");
            h.check(AlchemyApprenticeChallenge.blocksEnergizeIdentify(scroll), "挑战禁止转化卷轴时鉴定");
            Item output = recipe.brew(ingredients);
            h.require(output instanceof Recipe.MultiOutput, "拆解获得多个产出");
            ArrayList<Item> stones = ((Recipe.MultiOutput) output).items;
            h.check(stones.size() == 2 && stones.get(0).quantity() == 1 && stones.get(1).quantity() == 1,
                    "产出两颗各一件的符石");
            h.check(stones.get(0).getClass() != stones.get(1).getClass(), "随机符石与原符石种类不同");
            h.check(stones.stream().anyMatch(item -> item instanceof StoneOfIntuition), "保留一颗原本对应的符石");
            h.check(!scroll.isKnown(), "拆解没有鉴定卷轴");
            h.check(scroll.quantity() == 0, "配方消耗一张卷轴");
        });
    }
}
