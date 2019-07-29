package strategy.model;

import message.Direction;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class Cell {
	public final int x;
	public final int y;
	public final boolean border;
	public final Map<Direction, Cell> neighborsMap = new EnumMap<>(Direction.class);

	public Cell(int x, int y, boolean border) {
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
		return "Cell{" +
				"x=" + x +
				", y=" + y +
				", border=" + border +
				'}';
	}
}
