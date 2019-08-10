package strategy.model;

import message.Direction;
import strategy.Game;

public class Cell {
	private final int index;
	private final int x;
	private final int y;
	private final boolean border;
	private final Direction[] directions;
	private Cell[] neighbors;


	public Cell(int x, int y, boolean border, Direction[] directions) {
		this.index = x * Game.sizeY + y;
		this.x = x;
		this.y = y;
		this.border = border;
		this.directions = directions;
	}

	public Cell[] neighbors() {
		return neighbors;
	}

	public void setNeighbors(Cell[] neighbors) {
		this.neighbors = neighbors;
	}

	public Direction[] directions() {
		return directions;
	}

	@Override
	public String toString() {
		return "Cell{" + x + ", " + y + '}';
	}

	public int getIndex() {
		return index;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean isBorder() {
		return border;
	}


}
