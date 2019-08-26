package strategy.model;

import message.Direction;

public class CellDetails {
	/**
	 * Индекс игрога который сможет добраться самым первым
	 */
	public int playerIndex;

	/**
	 * Владелец клетки
	 */
	public int ownerIndex;

	/**
	 * тик полного заполнения клетки
	 */
	public int tick;

	/**
	 * тик входа в клетку
	 */
	public int enterTick;

	/**
	 * тик выхода из клетки
	 */
	public int leaveTick;

	/**
	 * Направление выхода из клетки
	 */
	public Direction leaveDirection;

	/**
	 * рамер хвоста при входе в клетку (минимальной при этом времени входа)
	 */
	public int enterTailLength;

	/**
	 * размер хвоста при выходе из клетки (enterTailLength + 1 или 0)
	 */
	public int leaveTailLength;


	/**
	 * тик на котором клетка может быть захвачена.
	 * находится на такой клетке может быть опасно (если это своя территория)
	 */
	public int capturedTick;

	/**
	 * тик в который на эту клетку зайдет соперник и замкнет путь
	 */
	public int captureTargetTick;


	/**
	 * самый ранний тик в который по этой клетке может быть нанесен удар пилой
	 */
	public int sawTick;

	/**
	 * направление в котом может быть нанесен удар пилой
	 */
	public Direction sawDirection;


	public CellDetails(int maxTick) {
		this.tick = maxTick;
		this.enterTick = maxTick;
		this.capturedTick = maxTick;
		this.leaveTick = maxTick;
		this.captureTargetTick = maxTick;
		this.sawTick = maxTick;
	}


	@Override
	public String toString() {
		return "{" +
				"tick=" + tick +
				", enterTick=" + enterTick +
				", leaveTick=" + leaveTick +
				", enterTailLength=" + enterTailLength +
				", leaveTailLength=" + leaveTailLength +
				", capturedTick=" + capturedTick +
				", sawTick=" + sawTick +
				'}';
	}
}
