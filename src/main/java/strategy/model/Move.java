package strategy.model;

import message.Direction;

/**
 * Класс для описания маршрута движения бота. Использовался в начальных версиях стратегии.
 * @see strategy.SimpleStrategy
 * @see MovePlanWithScore
 */
public class Move {
	// Направление движения
	private Direction direction;
	//Сколько клеток двигаться в направлении direction
	private int length;

	public Move(Direction direction) {
		this.direction = direction;
		this.length = Integer.MAX_VALUE;
	}

	public Move(Direction direction, int length) {
		this.direction = direction;
		this.length = length;
	}

	public Direction getDirection() {
		return direction;
	}

	public int getLength() {
		return length;
	}

	@Override
	public String toString() {
		return "" + direction + "(" + length + ")";
	}

	public void setLength(int n) {
		length = n;
	}
}
