//package com.shatteredpixel.shatteredpixeldungeon.windows;
//
//import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
//import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Berserk;
//import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
//import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
//import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
//import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
//import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
//import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
//import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
//import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
//import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
//import com.watabou.noosa.Image;
//
//public class WndBerserk extends Window {
//
//    private static final int WIDTH_P = 120;
//    private static final int WIDTH_L = 160;
//
//    private static final int MARGIN  = 2;
//
//    public WndBerserk(Berserk berserk){
//        super();
//
//        int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
//
//        float pos = MARGIN;
//        RenderedTextBlock title = PixelScene.renderTextBlock(Messages.titleCase(Messages.get(this, "title")), 9);
//        title.hardlight(TITLE_COLOR);
//        title.setPos((width-title.width())/2, pos);
//        title.maxWidth(width - MARGIN * 2);
//        add(title);
//
//        pos = title.bottom() + 3*MARGIN;
//
//        Image icon1=new TalentIcon(Talent.REVENGE_ROAR);
//        Image icon2=new BuffIcon(BuffIndicator.BERSERK,true);
//
//        int p=Math.max(1,Dungeon.hero.pointsInTalent(Talent.REVENGE_ROAR));
//        RedButton roarBtn = new RedButton(Messages.get(Berserk.class,"roar"+p), 6){
//            @Override
//            protected void onClick() {
//                super.onClick();
//                hide();
//                berserk.roar();
//            }
//        };
//        roarBtn.icon(icon1);
//        roarBtn.leftJustify = true;
//        roarBtn.multiline = true;
//        roarBtn.setSize(width,roarBtn.reqHeight());
//        roarBtn.setRect(0,pos,width,roarBtn.reqHeight());
//        roarBtn.enable(berserk.canRoar());
//        add(roarBtn);
//        pos = roarBtn.bottom() + MARGIN;
//
//        RedButton berserkBtn = new RedButton(Messages.get(Berserk.class,"toberserk"), 6){
//            @Override
//            protected void onClick() {
//                super.onClick();
//                hide();
//                berserk.toStartBerserking();
//            }
//        };
//        berserkBtn.icon(icon2);
//        berserkBtn.leftJustify = true;
//        berserkBtn.multiline = true;
//        berserkBtn.setSize(width,berserkBtn.reqHeight());
//        berserkBtn.setRect(0,pos,width,berserkBtn.reqHeight());
//        berserkBtn.enable(berserk.canActivateBerserk());
//        add(berserkBtn);
//        pos = berserkBtn.bottom() + MARGIN;
//
//        resize(width, (int)pos);
//
//    }
//
//
//}
