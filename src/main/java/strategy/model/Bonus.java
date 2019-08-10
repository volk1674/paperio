package strategy.model;

import message.BonusType;

public class Bonus {
	private final BonusType bonusType;
	private final int cells;

	public Bonus(BonusType bonusType, int cells) {
		this.bonusType = bonusType;
		this.cells = cells;
	}

	public BonusType getBonusType() {
		return bonusType;
	}

	public int getCells() {
		return cells;
	}
}
