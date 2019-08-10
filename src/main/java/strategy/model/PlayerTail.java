package strategy.model;

import strategy.Game;

import java.util.ArrayDeque;
import java.util.BitSet;

public class PlayerTail extends BitSet {
	private final ArrayDeque<Cell> cells;

	public PlayerTail() {
		cells = new ArrayDeque<>(Game.sizeX * Game.sizeY);
	}

	public PlayerTail(PlayerTail other) {
		this();
		cells.addAll(other.cells);
		this.or(other);
	}

	public void addToTail(Cell cell) {
		cells.add(cell);
		set(cell.getIndex());
	}

	public boolean isTail(Cell cell) {
		return get(cell.getIndex());
	}

	public int length() {
		return cells.size();
	}

	public void clear() {
		cells.clear();
		super.clear();
	}

	public ArrayDeque<Cell> getCells() {
		return cells;
	}

	public void removeLast() {
		clear(cells.removeLast().getIndex());
	}
}
