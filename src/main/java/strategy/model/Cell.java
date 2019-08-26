package strategy.model;

import message.Direction;
import strategy.Game;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class Cell {
	private final int index;
	private final int x;
	private final int y;
	private final boolean border;
	private final Map<Direction, Cell> directionsMap = new EnumMap<>(Direction.class);

	public Cell(int x, int y, boolean border, Map<Direction, Cell> directionsMap) {
		this.index = x * Game.sizeY + y;
		this.x = x;
		this.y = y;
		this.border = border;
		this.directionsMap.putAll(directionsMap);
	}

	public Set<Direction> directions() {
		return directionsMap.keySet();
	}

	public Map<Direction, Cell> getDirectionsMap() {
		return directionsMap;
	}

	public Cell nextCell(Direction direction) {
		return directionsMap.get(direction);
	}

	public Collection<Cell> neighbors() {
		return directionsMap.values();
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Cell cell = (Cell) o;

		return index == cell.index;
	}

	@Override
	public int hashCode() {
		return index;
	}
}
