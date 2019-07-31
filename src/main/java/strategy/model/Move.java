package strategy.model;

import message.Direction;

public class Move {
	private Direction direction;
	private int cells;

	public Move(Direction direction) {
		this.direction = direction;
		this.cells = Integer.MAX_VALUE;
	}

	public Move(Direction direction, int cells) {
		this.direction = direction;
		this.cells = cells;
	}

	public Direction getDirection() {
		return direction;
	}

	public int getCells() {
		return cells;
	}

	@Override
	public String toString() {
		return "" + direction + "(" + cells + ")";
	}
}
