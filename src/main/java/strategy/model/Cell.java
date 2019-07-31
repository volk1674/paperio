package strategy.model;

import message.Direction;
import strategy.Game;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public class Cell {
	private final int index;
	private final int x;
	private final int y;
	private final boolean border;
	private final Map<Direction, Cell> neighborsMap = new EnumMap<>(Direction.class);

	public Cell(int x, int y, boolean border) {
		this.index = x * Game.width + y;
		this.x = x;
		this.y = y;
		this.border = border;
	}

	public Collection<Cell> neighbors() {
		return neighborsMap.values();
	}

	public Collection<Direction> directions() {
		return neighborsMap.keySet();
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

	public Map<Direction, Cell> getNeighborsMap() {
		return neighborsMap;
	}
}
