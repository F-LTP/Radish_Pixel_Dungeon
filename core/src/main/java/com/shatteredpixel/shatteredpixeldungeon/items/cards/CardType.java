package com.shatteredpixel.shatteredpixeldungeon.items.cards;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public enum CardType {

	BLANK       (ItemSpriteSheet.CARD_BLANK,        Target.ANY,   1f),
	SMALL_RATION(ItemSpriteSheet.CARD_SMALL_RATION, Target.NONE,  3f),
	RATION      (ItemSpriteSheet.CARD_RATION,       Target.NONE,  3f),
	IDENTIFY    (ItemSpriteSheet.CARD_IDENTIFY,     Target.NONE,  1f),
	RIGID       (ItemSpriteSheet.CARD_RIGID,        Target.ANY,   1f),
	HORMONE     (ItemSpriteSheet.CARD_HORMONE,      Target.NONE,  1f),
	SPIN        (ItemSpriteSheet.CARD_SPIN,         Target.NONE,  1f),
	GRAND_FINALE(ItemSpriteSheet.CARD_FINALE,       Target.ENEMY, 1f),
	PREPARED    (ItemSpriteSheet.CARD_PREPARED,     Target.NONE,  1f),
	ATTACK      (ItemSpriteSheet.CARD_ATTACK,       Target.ENEMY, 1f),
	DEFENSE     (ItemSpriteSheet.CARD_DEFENSE,      Target.NONE,  1f),
	// 恶魔之力：凝视卡
	GAZE        (ItemSpriteSheet.CARD_IDENTIFY,     Target.NONE,  1f),
	// 陷阱师：陷阱卡（记录一个陷阱，打出后释放并退回空白卡）
	TRAP        (ItemSpriteSheet.CARD_RIGID,        Target.CELL,  1f),
	// 护甲技能：临时传送卡
	TELEPORT    (ItemSpriteSheet.CARD_PREPARED,     Target.CELL,  1f);

	public enum Target {
		NONE, ENEMY, SELF, ANY, CELL
	}

	public final String image;
	public final Target target;
	public final float time;

	CardType(String image, Target target, float time) {
		this.image = image;
		this.target = target;
		this.time = time;
	}

	public String title() {
		return Messages.get(CardType.class, name().toLowerCase() + ".name");
	}

	public String desc() {
		return Messages.get(CardType.class, name().toLowerCase() + ".desc");
	}

	public boolean needsTarget() {
		return target == Target.ENEMY || target == Target.ANY || target == Target.CELL;
	}
}
