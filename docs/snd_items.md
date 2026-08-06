# Slice&Dice 物品图标加载系统

## 概述

`SNDItems` 类提供了从 Slice&Dice 的贴图集中加载物品图标的功能。贴图集位于 `core/src/main/assets/snd/atlas_image.png`。

## 文件位置

- **Java 类**: `core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/ui/SNDItems.java`
- **贴图集**: `core/src/main/assets/snd/atlas_image.png`
- **名称列表**: `core/src/main/assets/snd_items_list.txt`

## 使用方式

```java
import com.shatteredpixel.shatteredpixeldungeon.ui.SNDItems;
import com.watabou.noosa.Image;

// 获取物品图标
Image arrow = SNDItems.get("arrow");
Image longsword = SNDItems.get("longsword");
Image shield = SNDItems.get("buckler");

// 安全获取（找不到时返回占位符）
Image img = SNDItems.getOrPlaceholder("some-item");

// 检查是否存在
if (SNDItems.has("ruby")) {
    // ...
}

// 获取所有名称
String[] allNames = SNDItems.names();
```

## 图标规格

- **尺寸**: 14x14 像素（统一规格）
- **数量**: 475 个物品图标
- **用途**: 物品、buff、法术等界面

## 完整物品名称列表 (475个)

```
abacus
ace-of-spades
aegis
affliction
alembic
ambrosia
amnesia
anchor
angel-feather
antivenom
antlers
anvil
apple
archmage-orb
arrow
ash
atlas-stone
autumn-leaf
backstab
bag-of-holding
balisong
ballet-shoes
banana-peel
bandana
banned
barkskin
barrel-hoops
basilisk-scale
bent-fork
bent-spoon
bent-spork
big-fish
big-hammer
big-heart
big-shield
bismuth
blessed-ring
blessed-water
blindfold
blinding-bolt
blood-amulet
blood-chalice
blue-skink
boarhide-bracers
bond-certificate
bone-charm
bonesaw
boots-of-speed
botany
bowl
braids
brick
brimstone
brittle
broadsword
broken-heart
broken-spirit
bronze-bell
broomstick
buckler
bullseye
burning-blade
burning-halo
burred-shield
camomile
can
candle
card
cart
castor-root
catnip
cauldron
chainmail
chakram
chalk
change-of-heart
chaos-wand
charged-hammer
charged-skull
charge-link
cheating-sleeves
chocolate-bar
cholesterol
cigarette-end
citrine-ring
clef
cloak
clover
clumsy-hammer
clumsy-shoes
cocoon
coffee
coiled-snake
coin
collar
compass
compulsion
conduit
conjuring-rings
conscience
copper-ring
corruption
corset
courage-potion
cracked-emerald
cracked-phylactery
cracked-plate
cracked-wheel
crescent-shield
crystallise
cursed-bolt
cyanide-pill
d4
dead-branch
dead-crow
deadly-bolt
decree
demon-claw
demon-eye
demon-heart
demon-horn
demonic-deal
determination
diamond-ring
diamond-skull
diving-suit
doll
dolphin
doom-blade
door
dragonhide-gloves
dragon-pipe
droopy-hat
duck
duelling-pistol
dull-wit
dumbbell
dusty-emerald
duvet
dynamo
early-grave
economancy
egg-basket
eggshell
emerald-mirror
emerald-satchel
emerald-shard
empathy
enchanted-harp
enchanted-shield
enhance-wand
erythrocyte
ethereal-cloak
eucalyptus
exhaustion
extra-pocket
eye-of-horus
eyepatch
face-of-horus
faerie-dust
faerie-pact
faint-halo
false-idol
fangs
farewell
fearless
fertiliser
fidget-spinner
first-aid-kit
flawed-diamond
flea
fletching
flickering-blade
flute
fly
foil
friendship-bracelet
full-moon
full-plate
garnet
gauntlet
ghost-shield
gizmo
glass-blade
glass-heart
glass-helm
glowing-egg
glyph-of-purity
golden-cup
golden-d6
golden-thread
grass
greatsword
handcuffs
harpoon
healing-wand
heart-of-light
helm-of-power
hidden-strength
hissing-ring
holy-book
honeycomb
horned-viper
hourglass
huge-scabbard
huge-sword
ice-cube
ichor-chalice
idol-of-aiiu
idol-of-chrzktx
idol-of-pythagoras
illegal
incense
infiniheal
infused-herbs
ink-bottle
inner-strength
ironblood-pendant
iron-crown
iron-heart
iron-helm
iron-pendant
jester-cap
jewel-loupe
juice
jump
justice
karma
katar
kilt
kite-shield
knife-bag
knot
ladder
lawnmower
lead-boots
leaden-handle
lead-weight
leather-gloves
leather-vest
lens
lich-eye
lich-finger
life-bolt
lightning-rod
lion
liqueur
locket
longbow
longsword
magic-staff
magnet
mana-bomb
mana-jelly
mana-potion
martyr
memory
metal-studs
mini-crossbow
mirror-mask
mithril-shields
monocle
monster-grin
mould
mushroom
natural
necromancer-tome
needle
nunchaku
obol
obsidian-edge
ocular-amulet
ogre-blood
old-root
olympian-trident
orbit
ordinary-triangle
origami
ornate-hilt
overflowing-chalice
overprepared
pair-of-kings
paper
parasite
pauldron
peaked-cap
peanut-shell
pendulum
pentagram
pharaoh-curse
pillow
pin
placeholder
placeholder-border
placeholder-border2
placeholder-red
pocket-mirror
pocket-phylactery
poem
poison-dip
polearm
polished-emerald
poodle
poseidon-charm
potion-shard
powdered-mana
powerstone
prism
pulley
pure-heart-pendant
puzzle-box
quicksilver
quiver
rain-of-arrows
reagents
red-flag
refactor
rejuvenation-wand
relic
revive-potion
ritual-dagger
rorrim-tekcop
ruby
ruby-shards
rusty-longsword
rusty-plate
sack-of-mana
sapphire
sapphire-ring
sapphire-skull
scales
scalpel
scar
sceptre
scissors
scorpion-tail
scoundrel-stash
second-chance
second-heart
seedling
serration
sharp-wit
shimmering-halo
shining-bow
shining-emerald
shiny-gauntlets
shortsword
shroud
shuriken
sickle
silk-cape
silver-imp
silver-pendant
simplicity
singularity
siphon
sleeper-agent
slimed
sling
smelly-manure
snake-oil
sorcery-notes
soul-link
soup
spanner
sparks
special/bug
special/cast
special/combined
special/destiny
special/enchant
special/full
special/hat
special/keyword/blue
special/keyword/green
special/keyword/grey
special/keyword/light
special/keyword/orange
special/keyword/pink
special/keyword/purple
special/keyword/red
special/keyword/yellow
special/old/combined
special/old/full
special/old/keyword
special/old/tier
special/old/trait
special/self
special/sticker
special/summon
special/tier
special/trait
spike-stone
spinach
splinter
splitting-arrows
sponge
sprinkles
square-wheel
stake
stale-bread
standard
static-tome
statuette
stilts
stoneskin
stream
sushi
syringe
taboo
tankard
tattered-robes
taxes
telescope
tentacle
terrarium
thimble
third-heart
three-of-a-kind
tiara
tie
timestone
tincture
tin-foil-hat
titanbane-amulet
titanbane-potion
titan-blade
tooth-necklace
tourmaline-paraiba
tower-shield
toy-sword
tracked
treasure-chest
trick-deck
triple-shuriken
troll-blood
troll-nose
trowel
tusk
twiddle
twin-daggers
twisted-bar
twisted-flax
two-of-clubs
two-reeds
unholy-strength
updog
urn
viscera
void
wandcraft
wand-grips
wandify
wand-of-stun
wand-of-wand
water
wax-seal
weariness
wedding-rings
whetstone
whey
whirlpool
whirlwind
whiskers
whiskey
wild-seeds
wine
wolf-ears
wooden-armour
wooden-bracelet
worn-arms
wrench
wretched-crown
wristblade
yearn
```

## 分类索引（常用图标）

### 武器类
- arrow, longsword, shortsword, broadsword, greatsword
- dagger, foil, shuriken, chakram, sickle
- bow, longbow, crossbow, harpoon
- wand-of-wand, chaos-wand, healing-wand

### 防具类
- buckler, tower-shield, aegis, kite-shield
- chainmail, full-plate, leather-vest, rusty-plate
- cloak, silk-cape, ethereal-cloak

### 饰品类
- copper-ring, citrine-ring, diamond-ring, blessed-ring
- bone-charm, silver-pendant, iron-pendant
- locket, collar, eyepatch

### 消耗品类
- mana-potion, revive-potion, courage-potion
- apple, spinach, juice, soup, wine
- reagents, infused-herbs, powdered-mana

### 法术/特殊类
- special/cast, special/summon, special/enchant
- special/keyword/blue, special/keyword/green, special/keyword/red
- special/tier, special/trai