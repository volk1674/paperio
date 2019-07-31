package strategy.model;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class PlayerTail {
	private final BitSet cellsBitSet;
	private final List<Cell> cells;

	public PlayerTail() {
		cellsBitSet = new BitSet();
		cells = new ArrayList<>();
	}

	public PlayerTail(PlayerTail other) {
		this();
		cells.addAll(other.cells);
		cellsBitSet.or(other.cellsBitSet);
	}

	public void addToTail(Cell cell) {
		cells.add(cell);
		cellsBitSet.set(cell.getIndex());
	}

	public boolean isTail(Cell cell) {
		return cellsBitSet.get(cell.getIndex());
	}

	public int length() {
		return cells.size();
	}

	public void clear() {
		cells.clear();
		cellsBitSet.clear();
	}

	public List<Cell> getCells() {
		return cells;
	}
}
