package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class WndInfoImpSuperTalent extends WndTitledMessage {

    public WndInfoImpSuperTalent(HeroClass cls, HeroSubClass subCls){
        super( new HeroIcon(subCls), Messages.titleCase(subCls.title()), subCls.super_desc());

        ArrayList<LinkedHashMap<Talent, Integer>> talentList = new ArrayList<>();
        Talent.initClassTalents(cls, talentList);
        Talent.initSubclassTalents(subCls, talentList);
        Talent.initT4Talents(cls,subCls,talentList);

        TalentsPane.TalentTierPane talentPane = new TalentsPane.TalentTierPane(talentList.get(3), 4, TalentButton.Mode.INFO);
        talentPane.title.text( Messages.titleCase(Messages.get(WndHeroInfo.class, "talents")));
        talentPane.setRect(0, height + 5, width, talentPane.height());
        add(talentPane);
        resize(width, (int) talentPane.bottom());

    }

}
