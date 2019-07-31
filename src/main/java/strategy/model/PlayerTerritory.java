package strategy.model;

import java.util.BitSet;

public class PlayerTerritory {
	private final BitSet cellsBitSet;

	public PlayerTerritory() {
		this.cellsBitSet = new BitSet();
	}

	public PlayerTerritory(PlayerTerritory other) {
		this.cellsBitSet = new BitSet();
		this.cellsBitSet.or(other.cellsBitSet);
	}

	public boolean get(Cell cell) {
		return cellsBitSet.get(cell.getIndex());
	}

	public void set(Cell cell) {
		cellsBitSet.set(cell.getIndex());
	}

	public void clear(Cell cell) {
		cellsBitSet.clear(cell.getIndex());
	}

	public void clear() {
		cellsBitSet.clear();
	}

	public void or(PlayerTerritory other) {
		this.cellsBitSet.or(other.cellsBitSet);
	}

	public int getSize() {
		return this.cellsBitSet.cardinality();
	}
}
