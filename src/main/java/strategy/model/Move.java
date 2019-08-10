package strategy.model;

import message.Direction;

public class Move {
	private Direction direction;
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
