package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Image;
import com.watabou.utils.RectF;
import java.util.HashMap;
import java.util.Map;

/**
 * Slice&Dice 物品图标加载器
 * 从 snd/atlas_image.png 贴图集中加载物品图标
 * 
 * 完整物品名称列表见下方 ITEMS 映射
 */
public class SNDItems {
    
    public static final String ATLAS = "snd/atlas_image.png";
    public static final int DEFAULT_SIZE = 14;
    
    // 物品名称 -> 坐标映射 (名称不含 item/ 前缀)
    private static final Map<String, int[]> ITEMS = new HashMap<>();
    
    static {
        ITEMS.put("abacus", new int[]{1008, 765, 14, 14});
        ITEMS.put("ace-of-spades", new int[]{1008, 749, 14, 14});
        ITEMS.put("aegis", new int[]{1008, 733, 14, 14});
        ITEMS.put("affliction", new int[]{666, 719, 14, 14});
        ITEMS.put("alembic", new int[]{666, 703, 14, 14});
        ITEMS.put("ambrosia", new int[]{683, 739, 14, 14});
        ITEMS.put("amnesia", new int[]{693, 759, 14, 14});
        ITEMS.put("anchor", new int[]{699, 743, 14, 14});
        ITEMS.put("angel-feather", new int[]{162, 301, 14, 14});
        ITEMS.put("antivenom", new int[]{162, 285, 14, 14});
        ITEMS.put("antlers", new int[]{162, 269, 14, 14});
        ITEMS.put("anvil", new int[]{162, 253, 14, 14});
        ITEMS.put("apple", new int[]{162, 237, 14, 14});
        ITEMS.put("archmage-orb", new int[]{162, 221, 14, 14});
        ITEMS.put("arrow", new int[]{162, 205, 14, 14});
        ITEMS.put("ash", new int[]{162, 189, 14, 14});
        ITEMS.put("atlas-stone", new int[]{162, 173, 14, 14});
        ITEMS.put("autumn-leaf", new int[]{162, 157, 14, 14});
        ITEMS.put("backstab", new int[]{162, 141, 14, 14});
        ITEMS.put("bag-of-holding", new int[]{162, 125, 14, 14});
        ITEMS.put("balisong", new int[]{162, 109, 14, 14});
        ITEMS.put("ballet-shoes", new int[]{162, 93, 14, 14});
        ITEMS.put("banana-peel", new int[]{162, 77, 14, 14});
        ITEMS.put("bandana", new int[]{683, 723, 14, 14});
        ITEMS.put("banned", new int[]{699, 727, 14, 14});
        ITEMS.put("barkskin", new int[]{682, 707, 14, 14});
        ITEMS.put("barrel-hoops", new int[]{699, 711, 14, 14});
        ITEMS.put("basilisk-scale", new int[]{682, 691, 14, 14});
        ITEMS.put("bent-fork", new int[]{439, 675, 14, 14});
        ITEMS.put("bent-spoon", new int[]{698, 695, 14, 14});
        ITEMS.put("bent-spork", new int[]{455, 675, 14, 14});
        ITEMS.put("big-fish", new int[]{460, 659, 14, 14});
        ITEMS.put("big-hammer", new int[]{471, 675, 14, 14});
        ITEMS.put("big-heart", new int[]{476, 659, 14, 14});
        ITEMS.put("big-shield", new int[]{467, 643, 14, 14});
        ITEMS.put("bismuth", new int[]{467, 627, 14, 14});
        ITEMS.put("blessed-ring", new int[]{467, 611, 14, 14});
        ITEMS.put("blessed-water", new int[]{467, 595, 14, 14});
        ITEMS.put("blindfold", new int[]{467, 579, 14, 14});
        ITEMS.put("blinding-bolt", new int[]{483, 643, 14, 14});
        ITEMS.put("blood-amulet", new int[]{483, 627, 14, 14});
        ITEMS.put("blood-chalice", new int[]{483, 611, 14, 14});
        ITEMS.put("blue-skink", new int[]{483, 579, 14, 14});
        ITEMS.put("boarhide-bracers", new int[]{484, 563, 14, 14});
        ITEMS.put("bond-certificate", new int[]{484, 547, 14, 14});
        ITEMS.put("bone-charm", new int[]{484, 531, 14, 14});
        ITEMS.put("bonesaw", new int[]{484, 515, 14, 14});
        ITEMS.put("boots-of-speed", new int[]{484, 499, 14, 14});
        ITEMS.put("botany", new int[]{487, 675, 14, 14});
        ITEMS.put("bowl", new int[]{492, 659, 14, 14});
        ITEMS.put("braids", new int[]{499, 643, 14, 14});
        ITEMS.put("brick", new int[]{499, 627, 14, 14});
        ITEMS.put("brimstone", new int[]{499, 611, 14, 14});
        ITEMS.put("brittle", new int[]{499, 595, 14, 14});
        ITEMS.put("broadsword", new int[]{499, 579, 14, 14});
        ITEMS.put("broken-heart", new int[]{500, 563, 14, 14});
        ITEMS.put("broken-spirit", new int[]{500, 547, 14, 14});
        ITEMS.put("bronze-bell", new int[]{500, 531, 14, 14});
        ITEMS.put("broomstick", new int[]{500, 515, 14, 14});
        ITEMS.put("buckler", new int[]{503, 675, 14, 14});
        ITEMS.put("bullseye", new int[]{524, 727, 14, 14});
        ITEMS.put("burning-blade", new int[]{515, 643, 14, 14});
        ITEMS.put("burning-halo", new int[]{515, 627, 14, 14});
        ITEMS.put("burred-shield", new int[]{515, 611, 14, 14});
        ITEMS.put("camomile", new int[]{515, 595, 14, 14});
        ITEMS.put("can", new int[]{515, 579, 14, 14});
        ITEMS.put("candle", new int[]{516, 563, 14, 14});
        ITEMS.put("card", new int[]{516, 547, 14, 14});
        ITEMS.put("cart", new int[]{516, 531, 14, 14});
        ITEMS.put("castor-root", new int[]{516, 515, 14, 14});
        ITEMS.put("catnip", new int[]{516, 499, 14, 14});
        ITEMS.put("cauldron", new int[]{516, 483, 14, 14});
        ITEMS.put("chainmail", new int[]{519, 675, 14, 14});
        ITEMS.put("chakram", new int[]{524, 659, 14, 14});
        ITEMS.put("chalk", new int[]{531, 643, 14, 14});
        ITEMS.put("change-of-heart", new int[]{531, 627, 14, 14});
        ITEMS.put("chaos-wand", new int[]{531, 611, 14, 14});
        ITEMS.put("charge-link", new int[]{531, 595, 14, 14});
        ITEMS.put("charged-hammer", new int[]{531, 579, 14, 14});
        ITEMS.put("charged-skull", new int[]{532, 563, 14, 14});
        ITEMS.put("cheating-sleeves", new int[]{532, 547, 14, 14});
        ITEMS.put("chocolate-bar", new int[]{532, 531, 14, 14});
        ITEMS.put("cholesterol", new int[]{532, 515, 14, 14});
        ITEMS.put("cigarette-end", new int[]{532, 483, 14, 14});
        ITEMS.put("citrine-ring", new int[]{535, 675, 14, 14});
        ITEMS.put("clef", new int[]{540, 659, 14, 14});
        ITEMS.put("cloak", new int[]{547, 643, 14, 14});
        ITEMS.put("clover", new int[]{547, 627, 14, 14});
        ITEMS.put("clumsy-hammer", new int[]{547, 611, 14, 14});
        ITEMS.put("clumsy-shoes", new int[]{547, 595, 14, 14});
        ITEMS.put("cocoon", new int[]{547, 579, 14, 14});
        ITEMS.put("coffee", new int[]{548, 563, 14, 14});
        ITEMS.put("coiled-snake", new int[]{548, 547, 14, 14});
        ITEMS.put("coin", new int[]{548, 531, 14, 14});
        ITEMS.put("collar", new int[]{548, 515, 14, 14});
        ITEMS.put("compass", new int[]{551, 675, 14, 14});
        ITEMS.put("compulsion", new int[]{556, 659, 14, 14});
        ITEMS.put("conduit", new int[]{563, 643, 14, 14});
        ITEMS.put("conjuring-rings", new int[]{563, 627, 14, 14});
        ITEMS.put("conscience", new int[]{563, 611, 14, 14});
        ITEMS.put("copper-ring", new int[]{563, 595, 14, 14});
        ITEMS.put("corruption", new int[]{563, 579, 14, 14});
        ITEMS.put("corset", new int[]{564, 563, 14, 14});
        ITEMS.put("courage-potion", new int[]{564, 547, 14, 14});
        ITEMS.put("cracked-emerald", new int[]{564, 531, 14, 14});
        ITEMS.put("cracked-phylactery", new int[]{564, 515, 14, 14});
        ITEMS.put("cracked-plate", new int[]{564, 499, 14, 14});
        ITEMS.put("cracked-wheel", new int[]{564, 483, 14, 14});
        ITEMS.put("crescent-shield", new int[]{567, 675, 14, 14});
        ITEMS.put("crystallise", new int[]{579, 627, 14, 14});
        ITEMS.put("cursed-bolt", new int[]{579, 611, 14, 14});
        ITEMS.put("cyanide-pill", new int[]{579, 595, 14, 14});
        ITEMS.put("d4", new int[]{579, 579, 14, 14});
        ITEMS.put("dead-branch", new int[]{580, 563, 14, 14});
        ITEMS.put("dead-crow", new int[]{580, 547, 14, 14});
        ITEMS.put("deadly-bolt", new int[]{580, 531, 14, 14});
        ITEMS.put("decree", new int[]{580, 515, 14, 14});
        ITEMS.put("demon-claw", new int[]{580, 499, 14, 14});
        ITEMS.put("demon-eye", new int[]{580, 483, 14, 14});
        ITEMS.put("demon-heart", new int[]{596, 703, 14, 14});
        ITEMS.put("demon-horn", new int[]{583, 675, 14, 14});
        ITEMS.put("demonic-deal", new int[]{588, 659, 14, 14});
        ITEMS.put("determination", new int[]{595, 627, 14, 14});
        ITEMS.put("diamond-ring", new int[]{595, 611, 14, 14});
        ITEMS.put("diamond-skull", new int[]{595, 595, 14, 14});
        ITEMS.put("diving-suit", new int[]{595, 579, 14, 14});
        ITEMS.put("doll", new int[]{596, 563, 14, 14});
        ITEMS.put("dolphin", new int[]{596, 547, 14, 14});
        ITEMS.put("doom-blade", new int[]{596, 531, 14, 14});
        ITEMS.put("door", new int[]{596, 515, 14, 14});
        ITEMS.put("dragon-pipe", new int[]{596, 499, 14, 14});
        ITEMS.put("dragonhide-gloves", new int[]{596, 483, 14, 14});
        ITEMS.put("droopy-hat", new int[]{599, 675, 14, 14});
        ITEMS.put("duck", new int[]{604, 659, 14, 14});
        ITEMS.put("duelling-pistol", new int[]{611, 643, 14, 14});
        ITEMS.put("dull-wit", new int[]{611, 627, 14, 14});
        ITEMS.put("dumbbell", new int[]{611, 611, 14, 14});
        ITEMS.put("dusty-emerald", new int[]{611, 595, 14, 14});
        ITEMS.put("duvet", new int[]{611, 579, 14, 14});
        ITEMS.put("dynamo", new int[]{612, 563, 14, 14});
        ITEMS.put("early-grave", new int[]{612, 547, 14, 14});
        ITEMS.put("economancy", new int[]{612, 531, 14, 14});
        ITEMS.put("egg-basket", new int[]{612, 515, 14, 14});
        ITEMS.put("eggshell", new int[]{612, 499, 14, 14});
        ITEMS.put("emerald-mirror", new int[]{612, 483, 14, 14});
        ITEMS.put("emerald-satchel", new int[]{615, 675, 14, 14});
        ITEMS.put("emerald-shard", new int[]{620, 659, 14, 14});
        ITEMS.put("empathy", new int[]{627, 643, 14, 14});
        ITEMS.put("enchanted-harp", new int[]{627, 611, 14, 14});
        ITEMS.put("enchanted-shield", new int[]{627, 595, 14, 14});
        ITEMS.put("enhance-wand", new int[]{627, 579, 14, 14});
        ITEMS.put("erythrocyte", new int[]{628, 563, 14, 14});
        ITEMS.put("ethereal-cloak", new int[]{628, 547, 14, 14});
        ITEMS.put("eucalyptus", new int[]{628, 531, 14, 14});
        ITEMS.put("exhaustion", new int[]{628, 515, 14, 14});
        ITEMS.put("extra-pocket", new int[]{628, 499, 14, 14});
        ITEMS.put("eye-of-horus", new int[]{628, 483, 14, 14});
        ITEMS.put("eyepatch", new int[]{631, 675, 14, 14});
        ITEMS.put("face-of-horus", new int[]{636, 659, 14, 14});
        ITEMS.put("faerie-dust", new int[]{643, 643, 14, 14});
        ITEMS.put("faerie-pact", new int[]{643, 627, 14, 14});
        ITEMS.put("faint-halo", new int[]{643, 611, 14, 14});
        ITEMS.put("false-idol", new int[]{643, 595, 14, 14});
        ITEMS.put("fangs", new int[]{643, 579, 14, 14});
        ITEMS.put("farewell", new int[]{644, 563, 14, 14});
        ITEMS.put("fearless", new int[]{644, 547, 14, 14});
        ITEMS.put("fertiliser", new int[]{644, 531, 14, 14});
        ITEMS.put("fidget-spinner", new int[]{644, 515, 14, 14});
        ITEMS.put("first-aid-kit", new int[]{644, 499, 14, 14});
        ITEMS.put("flawed-diamond", new int[]{644, 483, 14, 14});
        ITEMS.put("flea", new int[]{647, 675, 14, 14});
        ITEMS.put("fletching", new int[]{652, 659, 14, 14});
        ITEMS.put("flickering-blade", new int[]{659, 643, 14, 14});
        ITEMS.put("flute", new int[]{659, 627, 14, 14});
        ITEMS.put("fly", new int[]{659, 611, 14, 14});
        ITEMS.put("foil", new int[]{659, 595, 14, 14});
        ITEMS.put("friendship-bracelet", new int[]{659, 579, 14, 14});
        ITEMS.put("full-moon", new int[]{660, 531, 14, 14});
        ITEMS.put("full-plate", new int[]{660, 515, 14, 14});
        ITEMS.put("garnet", new int[]{660, 499, 14, 14});
        ITEMS.put("gauntlet", new int[]{660, 483, 14, 14});
        ITEMS.put("ghost-shield", new int[]{663, 675, 14, 14});
        ITEMS.put("gizmo", new int[]{668, 659, 14, 14});
        ITEMS.put("glass-blade", new int[]{679, 675, 14, 14});
        ITEMS.put("glass-heart", new int[]{684, 659, 14, 14});
        ITEMS.put("glass-helm", new int[]{675, 643, 14, 14});
        ITEMS.put("glowing-egg", new int[]{675, 627, 14, 14});
        ITEMS.put("glyph-of-purity", new int[]{675, 611, 14, 14});
        ITEMS.put("golden-cup", new int[]{675, 595, 14, 14});
        ITEMS.put("golden-d6", new int[]{675, 579, 14, 14});
        ITEMS.put("golden-thread", new int[]{676, 563, 14, 14});
        ITEMS.put("grass", new int[]{676, 547, 14, 14});
        ITEMS.put("greatsword", new int[]{676, 531, 14, 14});
        ITEMS.put("handcuffs", new int[]{676, 483, 14, 14});
        ITEMS.put("harpoon", new int[]{691, 643, 14, 14});
        ITEMS.put("healing-wand", new int[]{691, 611, 14, 14});
        ITEMS.put("heart-of-light", new int[]{691, 595, 14, 14});
        ITEMS.put("helm-of-power", new int[]{691, 579, 14, 14});
        ITEMS.put("hidden-strength", new int[]{692, 563, 14, 14});
        ITEMS.put("hissing-ring", new int[]{692, 547, 14, 14});
        ITEMS.put("holy-book", new int[]{692, 531, 14, 14});
        ITEMS.put("honeycomb", new int[]{692, 515, 14, 14});
        ITEMS.put("horned-viper", new int[]{692, 499, 14, 14});
        ITEMS.put("hourglass", new int[]{692, 483, 14, 14});
        ITEMS.put("huge-scabbard", new int[]{695, 675, 14, 14});
        ITEMS.put("huge-sword", new int[]{700, 659, 14, 14});
        ITEMS.put("ice-cube", new int[]{707, 643, 14, 14});
        ITEMS.put("ichor-chalice", new int[]{707, 627, 14, 14});
        ITEMS.put("idol-of-aiiu", new int[]{707, 611, 14, 14});
        ITEMS.put("idol-of-chrzktx", new int[]{707, 595, 14, 14});
        ITEMS.put("idol-of-pythagoras", new int[]{707, 579, 14, 14});
        ITEMS.put("illegal", new int[]{708, 563, 14, 14});
        ITEMS.put("incense", new int[]{708, 531, 14, 14});
        ITEMS.put("infiniheal", new int[]{708, 515, 14, 14});
        ITEMS.put("infused-herbs", new int[]{708, 499, 14, 14});
        ITEMS.put("ink-bottle", new int[]{708, 483, 14, 14});
        ITEMS.put("inner-strength", new int[]{882, 740, 14, 14});
        ITEMS.put("iron-crown", new int[]{245, 445, 14, 14});
        ITEMS.put("iron-heart", new int[]{245, 429, 14, 14});
        ITEMS.put("iron-helm", new int[]{245, 413, 14, 14});
        ITEMS.put("iron-pendant", new int[]{245, 397, 14, 14});
        ITEMS.put("ironblood-pendant", new int[]{245, 381, 14, 14});
        ITEMS.put("jester-cap", new int[]{245, 317, 14, 14});
        ITEMS.put("jewel-loupe", new int[]{245, 301, 14, 14});
        ITEMS.put("juice", new int[]{261, 441, 14, 14});
        ITEMS.put("jump", new int[]{277, 441, 14, 14});
        ITEMS.put("justice", new int[]{261, 425, 14, 14});
        ITEMS.put("karma", new int[]{261, 409, 14, 14});
        ITEMS.put("katar", new int[]{277, 425, 14, 14});
        ITEMS.put("kilt", new int[]{277, 409, 14, 14});
        ITEMS.put("kite-shield", new int[]{261, 377, 14, 14});
        ITEMS.put("knife-bag", new int[]{277, 393, 14, 14});
        ITEMS.put("knot", new int[]{261, 361, 14, 14});
        ITEMS.put("ladder", new int[]{277, 377, 14, 14});
        ITEMS.put("lawnmower", new int[]{261, 345, 14, 14});
        ITEMS.put("lead-boots", new int[]{277, 361, 14, 14});
        ITEMS.put("lead-weight", new int[]{261, 329, 14, 14});
        ITEMS.put("leaden-handle", new int[]{277, 345, 14, 14});
        ITEMS.put("leather-gloves", new int[]{261, 313, 14, 14});
        ITEMS.put("leather-vest", new int[]{277, 329, 14, 14});
        ITEMS.put("lens", new int[]{277, 313, 14, 14});
        ITEMS.put("lich-eye", new int[]{293, 427, 14, 14});
        ITEMS.put("lich-finger", new int[]{293, 411, 14, 14});
        ITEMS.put("life-bolt", new int[]{293, 395, 14, 14});
        ITEMS.put("lightning-rod", new int[]{293, 363, 14, 14});
        ITEMS.put("lion", new int[]{293, 347, 14, 14});
        ITEMS.put("liqueur", new int[]{293, 331, 14, 14});
        ITEMS.put("locket", new int[]{293, 315, 14, 14});
        ITEMS.put("longbow", new int[]{261, 297, 14, 14});
        ITEMS.put("longsword", new int[]{277, 297, 14, 14});
        ITEMS.put("magic-staff", new int[]{293, 299, 14, 14});
        ITEMS.put("magnet", new int[]{309, 414, 14, 14});
        ITEMS.put("mana-bomb", new int[]{309, 398, 14, 14});
        ITEMS.put("mana-jelly", new int[]{325, 414, 14, 14});
        ITEMS.put("mana-potion", new int[]{309, 382, 14, 14});
        ITEMS.put("martyr", new int[]{325, 398, 14, 14});
        ITEMS.put("memory", new int[]{309, 366, 14, 14});
        ITEMS.put("metal-studs", new int[]{325, 382, 14, 14});
        ITEMS.put("mini-crossbow", new int[]{309, 350, 14, 14});
        ITEMS.put("mirror-mask", new int[]{325, 366, 14, 14});
        ITEMS.put("mithril-shields", new int[]{309, 334, 14, 14});
        ITEMS.put("monocle", new int[]{325, 350, 14, 14});
        ITEMS.put("monster-grin", new int[]{309, 318, 14, 14});
        ITEMS.put("mould", new int[]{325, 334, 14, 14});
        ITEMS.put("mushroom", new int[]{309, 302, 14, 14});
        ITEMS.put("natural", new int[]{325, 318, 14, 14});
        ITEMS.put("necromancer-tome", new int[]{325, 302, 14, 14});
        ITEMS.put("needle", new int[]{540, 714, 14, 14});
        ITEMS.put("nunchaku", new int[]{556, 709, 14, 14});
        ITEMS.put("obol", new int[]{572, 709, 14, 14});
        ITEMS.put("obsidian-edge", new int[]{846, 737, 14, 14});
        ITEMS.put("ocular-amulet", new int[]{862, 737, 14, 14});
        ITEMS.put("ogre-blood", new int[]{178, 290, 14, 14});
        ITEMS.put("old-root", new int[]{194, 290, 14, 14});
        ITEMS.put("olympian-trident", new int[]{178, 274, 14, 14});
        ITEMS.put("orbit", new int[]{178, 258, 14, 14});
        ITEMS.put("ordinary-triangle", new int[]{194, 274, 14, 14});
        ITEMS.put("origami", new int[]{226, 290, 14, 14});
        ITEMS.put("ornate-hilt", new int[]{178, 242, 14, 14});
        ITEMS.put("overflowing-chalice", new int[]{194, 258, 14, 14});
        ITEMS.put("overprepared", new int[]{210, 274, 14, 14});
        ITEMS.put("pair-of-kings", new int[]{178, 226, 14, 14});
        ITEMS.put("paper", new int[]{194, 242, 14, 14});
        ITEMS.put("parasite", new int[]{210, 258, 14, 14});
        ITEMS.put("pauldron", new int[]{226, 274, 14, 14});
        ITEMS.put("peaked-cap", new int[]{178, 210, 14, 14});
        ITEMS.put("peanut-shell", new int[]{194, 226, 14, 14});
        ITEMS.put("pendulum", new int[]{210, 242, 14, 14});
        ITEMS.put("pentagram", new int[]{226, 258, 14, 14});
        ITEMS.put("pharaoh-curse", new int[]{178, 194, 14, 14});
        ITEMS.put("pillow", new int[]{194, 210, 14, 14});
        ITEMS.put("pin", new int[]{210, 226, 14, 14});
        ITEMS.put("placeholder", new int[]{178, 178, 14, 14});
        ITEMS.put("placeholder-border", new int[]{194, 194, 14, 14});
        ITEMS.put("placeholder-border2", new int[]{210, 210, 14, 14});
        ITEMS.put("placeholder-red", new int[]{226, 226, 14, 14});
        ITEMS.put("pocket-mirror", new int[]{178, 162, 14, 14});
        ITEMS.put("pocket-phylactery", new int[]{194, 178, 14, 14});
        ITEMS.put("poem", new int[]{210, 194, 14, 14});
        ITEMS.put("poison-dip", new int[]{226, 210, 14, 14});
        ITEMS.put("polearm", new int[]{178, 146, 14, 14});
        ITEMS.put("polished-emerald", new int[]{194, 162, 14, 14});
        ITEMS.put("poodle", new int[]{210, 178, 14, 14});
        ITEMS.put("poseidon-charm", new int[]{226, 194, 14, 14});
        ITEMS.put("potion-shard", new int[]{178, 130, 14, 14});
        ITEMS.put("powdered-mana", new int[]{194, 146, 14, 14});
        ITEMS.put("powerstone", new int[]{210, 162, 14, 14});
        ITEMS.put("prism", new int[]{226, 178, 14, 14});
        ITEMS.put("pulley", new int[]{178, 114, 14, 14});
        ITEMS.put("pure-heart-pendant", new int[]{194, 130, 14, 14});
        ITEMS.put("puzzle-box", new int[]{226, 162, 14, 14});
        ITEMS.put("quicksilver", new int[]{178, 98, 14, 14});
        ITEMS.put("quiver", new int[]{194, 114, 14, 14});
        ITEMS.put("rain-of-arrows", new int[]{210, 130, 14, 14});
        ITEMS.put("reagents", new int[]{226, 146, 14, 14});
        ITEMS.put("red-flag", new int[]{194, 98, 14, 14});
        ITEMS.put("refactor", new int[]{210, 114, 14, 14});
        ITEMS.put("rejuvenation-wand", new int[]{226, 130, 14, 14});
        ITEMS.put("relic", new int[]{194, 82, 14, 14});
        ITEMS.put("revive-potion", new int[]{210, 98, 14, 14});
        ITEMS.put("ritual-dagger", new int[]{226, 114, 14, 14});
        ITEMS.put("rorrim-tekcop", new int[]{210, 82, 14, 14});
        ITEMS.put("ruby", new int[]{226, 98, 14, 14});
        ITEMS.put("ruby-shards", new int[]{226, 82, 14, 14});
        ITEMS.put("rusty-longsword", new int[]{242, 285, 14, 14});
        ITEMS.put("rusty-plate", new int[]{242, 269, 14, 14});
        ITEMS.put("sack-of-mana", new int[]{242, 253, 14, 14});
        ITEMS.put("sapphire", new int[]{242, 237, 14, 14});
        ITEMS.put("sapphire-ring", new int[]{242, 221, 14, 14});
        ITEMS.put("sapphire-skull", new int[]{242, 205, 14, 14});
        ITEMS.put("scales", new int[]{242, 189, 14, 14});
        ITEMS.put("scalpel", new int[]{242, 173, 14, 14});
        ITEMS.put("scar", new int[]{242, 157, 14, 14});
        ITEMS.put("sceptre", new int[]{242, 141, 14, 14});
        ITEMS.put("scissors", new int[]{242, 125, 14, 14});
        ITEMS.put("scorpion-tail", new int[]{242, 109, 14, 14});
        ITEMS.put("scoundrel-stash", new int[]{242, 93, 14, 14});
        ITEMS.put("second-chance", new int[]{258, 281, 14, 14});
        ITEMS.put("second-heart", new int[]{258, 265, 14, 14});
        ITEMS.put("seedling", new int[]{274, 281, 14, 14});
        ITEMS.put("serration", new int[]{274, 265, 14, 14});
        ITEMS.put("sharp-wit", new int[]{258, 233, 14, 14});
        ITEMS.put("shimmering-halo", new int[]{274, 249, 14, 14});
        ITEMS.put("shining-bow", new int[]{258, 217, 14, 14});
        ITEMS.put("shining-emerald", new int[]{274, 233, 14, 14});
        ITEMS.put("shiny-gauntlets", new int[]{258, 201, 14, 14});
        ITEMS.put("shortsword", new int[]{274, 217, 14, 14});
        ITEMS.put("shroud", new int[]{258, 185, 14, 14});
        ITEMS.put("shuriken", new int[]{274, 201, 14, 14});
        ITEMS.put("sickle", new int[]{258, 169, 14, 14});
        ITEMS.put("silk-cape", new int[]{274, 185, 14, 14});
        ITEMS.put("silver-imp", new int[]{258, 153, 14, 14});
        ITEMS.put("silver-pendant", new int[]{274, 169, 14, 14});
        ITEMS.put("simplicity", new int[]{258, 137, 14, 14});
        ITEMS.put("singularity", new int[]{274, 153, 14, 14});
        ITEMS.put("siphon", new int[]{258, 121, 14, 14});
        ITEMS.put("sleeper-agent", new int[]{274, 137, 14, 14});
        ITEMS.put("slimed", new int[]{258, 105, 14, 14});
        ITEMS.put("sling", new int[]{274, 121, 14, 14});
        ITEMS.put("smelly-manure", new int[]{274, 105, 14, 14});
        ITEMS.put("snake-oil", new int[]{258, 89, 14, 14});
        ITEMS.put("sorcery-notes", new int[]{274, 89, 14, 14});
        ITEMS.put("soul-link", new int[]{242, 77, 14, 14});
        ITEMS.put("soup", new int[]{258, 73, 14, 14});
        ITEMS.put("spanner", new int[]{274, 73, 14, 14});
        ITEMS.put("sparks", new int[]{178, 66, 14, 14});
        ITEMS.put("special/bug", new int[]{508, 659, 14, 14});
        ITEMS.put("special/cast", new int[]{576, 743, 16, 16});
        ITEMS.put("special/combined", new int[]{548, 499, 14, 14});
        ITEMS.put("special/destiny", new int[]{595, 643, 14, 14});
        ITEMS.put("special/enchant", new int[]{627, 627, 14, 14});
        ITEMS.put("special/full", new int[]{660, 563, 14, 14});
        ITEMS.put("special/hat", new int[]{691, 627, 14, 14});
        ITEMS.put("special/keyword/blue", new int[]{483, 595, 14, 14});
        ITEMS.put("special/keyword/green", new int[]{676, 515, 14, 14});
        ITEMS.put("special/keyword/grey", new int[]{676, 499, 14, 14});
        ITEMS.put("special/keyword/light", new int[]{293, 379, 14, 14});
        ITEMS.put("special/keyword/orange", new int[]{210, 290, 14, 14});
        ITEMS.put("special/keyword/pink", new int[]{226, 242, 14, 14});
        ITEMS.put("special/keyword/purple", new int[]{210, 146, 14, 14});
        ITEMS.put("special/keyword/red", new int[]{178, 82, 14, 14});
        ITEMS.put("special/keyword/yellow", new int[]{978, 725, 14, 14});
        ITEMS.put("special/old/combined", new int[]{548, 483, 14, 14});
        ITEMS.put("special/old/full", new int[]{660, 547, 14, 14});
        ITEMS.put("special/old/keyword", new int[]{261, 393, 14, 14});
        ITEMS.put("special/old/tier", new int[]{306, 206, 14, 14});
        ITEMS.put("special/old/trait", new int[]{322, 126, 14, 14});
        ITEMS.put("special/self", new int[]{258, 249, 14, 14});
        ITEMS.put("special/sticker", new int[]{191, 396, 16, 16});
        ITEMS.put("special/summon", new int[]{191, 378, 16, 16});
        ITEMS.put("special/tier", new int[]{322, 238, 14, 14});
        ITEMS.put("special/trait", new int[]{306, 110, 14, 14});
        ITEMS.put("spike-stone", new int[]{194, 66, 14, 14});
        ITEMS.put("spinach", new int[]{210, 66, 14, 14});
        ITEMS.put("splinter", new int[]{226, 66, 14, 14});
        ITEMS.put("splitting-arrows", new int[]{242, 61, 14, 14});
        ITEMS.put("sponge", new int[]{258, 57, 14, 14});
        ITEMS.put("sprinkles", new int[]{274, 57, 14, 14});
        ITEMS.put("square-wheel", new int[]{309, 286, 14, 14});
        ITEMS.put("stake", new int[]{325, 286, 14, 14});
        ITEMS.put("stale-bread", new int[]{293, 283, 14, 14});
        ITEMS.put("standard", new int[]{290, 267, 14, 14});
        ITEMS.put("static-tome", new int[]{290, 251, 14, 14});
        ITEMS.put("statuette", new int[]{290, 235, 14, 14});
        ITEMS.put("stilts", new int[]{290, 219, 14, 14});
        ITEMS.put("stoneskin", new int[]{290, 203, 14, 14});
        ITEMS.put("stream", new int[]{290, 187, 14, 14});
        ITEMS.put("sushi", new int[]{290, 171, 14, 14});
        ITEMS.put("syringe", new int[]{290, 155, 14, 14});
        ITEMS.put("taboo", new int[]{290, 139, 14, 14});
        ITEMS.put("tankard", new int[]{290, 123, 14, 14});
        ITEMS.put("tattered-robes", new int[]{290, 107, 14, 14});
        ITEMS.put("taxes", new int[]{290, 91, 14, 14});
        ITEMS.put("telescope", new int[]{290, 75, 14, 14});
        ITEMS.put("tentacle", new int[]{290, 59, 14, 14});
        ITEMS.put("terrarium", new int[]{309, 270, 14, 14});
        ITEMS.put("thimble", new int[]{325, 270, 14, 14});
        ITEMS.put("third-heart", new int[]{306, 254, 14, 14});
        ITEMS.put("three-of-a-kind", new int[]{306, 238, 14, 14});
        ITEMS.put("tiara", new int[]{322, 254, 14, 14});
        ITEMS.put("tie", new int[]{306, 222, 14, 14});
        ITEMS.put("timestone", new int[]{322, 222, 14, 14});
        ITEMS.put("tin-foil-hat", new int[]{306, 190, 14, 14});
        ITEMS.put("tincture", new int[]{322, 206, 14, 14});
        ITEMS.put("titan-blade", new int[]{306, 174, 14, 14});
        ITEMS.put("titanbane-amulet", new int[]{322, 190, 14, 14});
        ITEMS.put("titanbane-potion", new int[]{306, 158, 14, 14});
        ITEMS.put("tooth-necklace", new int[]{322, 174, 14, 14});
        ITEMS.put("tourmaline-paraiba", new int[]{306, 142, 14, 14});
        ITEMS.put("tower-shield", new int[]{322, 158, 14, 14});
        ITEMS.put("toy-sword", new int[]{306, 126, 14, 14});
        ITEMS.put("tracked", new int[]{322, 142, 14, 14});
        ITEMS.put("treasure-chest", new int[]{306, 94, 14, 14});
        ITEMS.put("trick-deck", new int[]{322, 110, 14, 14});
        ITEMS.put("triple-shuriken", new int[]{322, 94, 14, 14});
        ITEMS.put("troll-blood", new int[]{306, 63, 14, 14});
        ITEMS.put("troll-nose", new int[]{322, 78, 14, 14});
        ITEMS.put("trowel", new int[]{322, 62, 14, 14});
        ITEMS.put("tusk", new int[]{306, 47, 14, 14});
        ITEMS.put("twiddle", new int[]{322, 46, 14, 14});
        ITEMS.put("twin-daggers", new int[]{306, 31, 14, 14});
        ITEMS.put("twisted-bar", new int[]{322, 30, 14, 14});
        ITEMS.put("twisted-flax", new int[]{338, 254, 14, 14});
        ITEMS.put("two-of-clubs", new int[]{338, 238, 14, 14});
        ITEMS.put("two-reeds", new int[]{338, 222, 14, 14});
        ITEMS.put("unholy-strength", new int[]{338, 206, 14, 14});
        ITEMS.put("updog", new int[]{338, 190, 14, 14});
        ITEMS.put("urn", new int[]{338, 174, 14, 14});
        ITEMS.put("viscera", new int[]{338, 158, 14, 14});
        ITEMS.put("void", new int[]{338, 142, 14, 14});
        ITEMS.put("wand-grips", new int[]{338, 126, 14, 14});
        ITEMS.put("wand-of-stun", new int[]{338, 110, 14, 14});
        ITEMS.put("wand-of-wand", new int[]{338, 94, 14, 14});
        ITEMS.put("wandcraft", new int[]{338, 78, 14, 14});
        ITEMS.put("wandify", new int[]{338, 62, 14, 14});
        ITEMS.put("water", new int[]{338, 46, 14, 14});
        ITEMS.put("wax-seal", new int[]{338, 30, 14, 14});
        ITEMS.put("weariness", new int[]{429, 796, 14, 14});
        ITEMS.put("wedding-rings", new int[]{445, 796, 14, 14});
        ITEMS.put("whetstone", new int[]{428, 780, 14, 14});
        ITEMS.put("whey", new int[]{428, 764, 14, 14});
        ITEMS.put("whirlpool", new int[]{428, 748, 14, 14});
        ITEMS.put("whirlwind", new int[]{444, 780, 14, 14});
        ITEMS.put("whiskers", new int[]{444, 764, 14, 14});
        ITEMS.put("whiskey", new int[]{444, 748, 14, 14});
        ITEMS.put("wild-seeds", new int[]{447, 732, 14, 14});
        ITEMS.put("wine", new int[]{447, 716, 14, 14});
        ITEMS.put("wolf-ears", new int[]{460, 760, 14, 14});
        ITEMS.put("wooden-armour", new int[]{898, 740, 14, 14});
        ITEMS.put("wooden-bracelet", new int[]{914, 741, 14, 14});
        ITEMS.put("worn-arms", new int[]{930, 738, 14, 14});
        ITEMS.put("wrench", new int[]{914, 725, 14, 14});
        ITEMS.put("wretched-crown", new int[]{930, 722, 14, 14});
        ITEMS.put("wristblade", new int[]{946, 724, 14, 14});
        ITEMS.put("yearn", new int[]{962, 724, 14, 14});
    }
    
    /**
     * 获取 SND atlas 的纹理（共享缓存）
     * @return SmartTexture 对象
     */
    public static SmartTexture texture() {
        return TextureCache.get(ATLAS);
    }
    
    /**
     * 获取指定名称物品的 UV 坐标
     * @param name 物品名称
     * @return RectF UV 坐标，如果找不到则返回 null
     */
    public static RectF frame(String name) {
        int[] coords = ITEMS.get(name);
        if (coords == null) return null;
        SmartTexture tex = texture();
        return tex.uvRect(coords[0], coords[1], coords[0]+coords[2], coords[1]+coords[3]);
    }
    
    /**
     * 加载指定名称的物品图标
     * @param name 物品名称（不含 item/ 前缀），如 "arrow", "longsword"
     * @return Image 对象，如果找不到则返回 null
     */
    public static Image get(String name) {
        int[] coords = ITEMS.get(name);
        if (coords == null) {
            return null;
        }
        return new Image(ATLAS, coords[0], coords[1], coords[2], coords[3]);
    }
    
    /**
     * 加载指定名称的物品图标，如果找不到则返回占位符
     * @param name 物品名称
     * @return Image 对象
     */
    public static Image getOrPlaceholder(String name) {
        Image img = get(name);
        if (img != null) {
            return img;
        }
        // 返回默认占位符（第一个物品图标）
        return new Image(ATLAS, 162, 205, DEFAULT_SIZE, DEFAULT_SIZE);
    }
    
    /**
     * 检查是否存在指定名称的物品图标
     */
    public static boolean has(String name) {
        return ITEMS.containsKey(name);
    }
    
    /**
     * 获取所有可用的物品名称
     */
    public static String[] names() {
        return ITEMS.keySet().toArray(new String[0]);
    }
}
