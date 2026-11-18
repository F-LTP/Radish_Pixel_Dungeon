/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2023 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.SeedFinder;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Group;
import com.watabou.noosa.ui.Component;

import java.util.Arrays;

public class SeedFindScene extends PixelScene {

    //正在进行（或最近一次）的搜索。搜索在后台线程里最长跑 20 秒，离开界面时要把它停掉
    private SeedFinder finder;

    //搜索开始前「自定义种子」设置的值，null 表示没有需要还原的。
    //搜索是借这个设置逐个试种子的：没找到（超时）或中途离开时不还原的话，最后试的那个种子会留在设置里，
    //玩家下一局就在不知情的情况下成了自定义种子局（拿不到徽章）。找到了则照旧把找到的种子留下
    private String seedBeforeSearch = null;

    @Override
    public void create() {
        super.create();

        final float colWidth = 120;
        final float fullWidth = colWidth * (landscape() ? 2 : 1);

        int w = Camera.main.width;
        int h = Camera.main.height;

        Archs archs = new Archs();
        archs.setSize(w, h);
        add(archs);

        //darkens the arches
        add(new ColorBlock(w, h, 0x88000000));

        ScrollPane list = new ScrollPane(new Component());
        add(list);

        Component content = list.content();
        content.clear();

        ShatteredPixelDungeon.scene().addToFront(new WndTextInput(Messages.get(this, "title"), Messages.get(this, "body"), ""/*Messages.get(this, "initial_value")*/, 1000, true, Messages.get(this, "find"), Messages.get(HeroSelectScene.class, "custom_seed_clear")) {
            @Override
            public void onSelect(boolean positive, String text) {
                int floor = SPDSettings.getKeySeedDepth();
                /*boolean floorOption = false;
                String up_to_floor;
                if (Messages.lang() == Languages.KOREAN) {
                    up_to_floor = "층까지";
                } else {
                    up_to_floor = "floor end";
                }
                String strFloor;
                if (Messages.lang() == Languages.KOREAN) {
                    strFloor = "층";
                } else {
                    strFloor = "floor";
                }
                if (text.contains(up_to_floor)) {
                    floorOption = true;
                    String fl = text.split(strFloor)[0].trim();
                    try {
                        floor = Integer.parseInt(fl);
                    } catch (
                            NumberFormatException e) {
                    }
                }*/
                //这里原来写的是 text != ""，比较的是引用：输入框交回来的空串不是同一个对象，
                //什么都没填也会开始搜索，而空串能匹配任何物品名
                if (positive && !text.trim().isEmpty()) {
                    String[] itemList = Arrays.copyOfRange(text.split("\n"), 0, text.split("\n").length);

                    Component content = list.content();
                    content.clear();

                    // 先显示搜索中提示
                    CreditsBlock txt = new CreditsBlock(true, Window.TITLE_COLOR, Messages.get(SeedFinder.class, "searching"));
                    txt.setRect((Camera.main.width - colWidth)/2f, 12, colWidth, 0);
                    content.add(txt);

                    content.setSize( fullWidth, txt.bottom()+10 );

                    list.setRect( 0, 0, w, h );
                    list.scrollTo(0, 0);

                    // 异步搜索
                    finder = new SeedFinder();
                    seedBeforeSearch = SPDSettings.customSeed();
                    final String notFound = Messages.get(SeedFinder.class, "not_found");
                    final String timeout = Messages.get(SeedFinder.class, "timeout");
                    final ScrollPane finalList = list;
                    final Component finalContent = content;
                    final float finalColWidth = colWidth;
                    final float finalFullWidth = fullWidth;
                    
                    finder.findSeedAsync(itemList, floor+1, result -> {
                        // 回到主线程更新UI
                        Gdx.app.postRunnable(() -> {
                            //结果回来时玩家可能已经离开了这个界面：界面销毁后再往里面加东西会空指针
                            if (ShatteredPixelDungeon.scene() != SeedFindScene.this) return;

                            if (result.equals(notFound) || result.equals(timeout)) {
                                //超时只是打断搜索线程，这里再明确叫停一次，确保它不会在还原之后接着试下一个种子
                                finder.cancelSearch();
                                restoreSeed();
                            } else {
                                //找到了：找到的种子留在设置里，不还原
                                seedBeforeSearch = null;
                            }

                            Component c = finalList.content();
                            c.clear();

                            CreditsBlock resultTxt = new CreditsBlock(true, Window.TITLE_COLOR, result);
                            resultTxt.setRect((Camera.main.width - finalColWidth)/2f, 12, finalColWidth, 0);
                            c.add(resultTxt);

                            c.setSize(finalFullWidth, resultTxt.bottom()+10);

                            finalList.setRect( 0, 0, w, h );
                            finalList.scrollTo(0, 0);
                        });
                    });

                } else {
                    SPDSettings.customSeed("");
                    ShatteredPixelDungeon.switchNoFade(TitleScene.class);
                }
            }
        });

        ExitButton btnExit = new ExitButton();
        btnExit.setPos(Camera.main.width - btnExit.width(), 0);
        add(btnExit);

        //fadeIn();
    }

    @Override
    protected void onBackPressed() {
        ShatteredPixelDungeon.switchScene(TitleScene.class);
    }

    @Override
    public void destroy() {
        //搜索还在跑的话停掉它（它每试一个种子都要重建一遍 Dungeon 的静态状态，不能让它跑到别的界面里去），
        //并把它借用的「自定义种子」设置还原
        if (finder != null) {
            finder.cancelSearch();
        }
        restoreSeed();
        super.destroy();
    }

    private void restoreSeed() {
        if (seedBeforeSearch != null) {
            SPDSettings.customSeed(seedBeforeSearch);
            seedBeforeSearch = null;
        }
    }

    private void addLine(float y, Group content) {
        ColorBlock line = new ColorBlock(Camera.main.width, 1, 0xFF333333);
        line.y = y;
        content.add(line);
    }


    public static class CreditsBlock extends Component {

        boolean large;

        RenderedTextBlock body;

        public CreditsBlock(boolean large, int highlight, String body) {
            super();

            this.large = large;

            this.body = PixelScene.renderTextBlock(body, 6);
            if (highlight != -1)
                this.body.setHightlighting(true, highlight);
            if (large)
                this.body.align(RenderedTextBlock.CENTER_ALIGN);
            add(this.body);
        }

        @Override
        protected void layout() {
            super.layout();

            float topY = top();

            if (large){
                body.maxWidth((int)width());
                body.setPos( x + (width() - body.width())/2f, topY);
            } else {
                topY += 1;
                body.maxWidth((int)width());
                body.setPos( x, topY);
            }

            topY += body.height();

            height = Math.max(height, topY - top());
        }
    }
}