package strategy.model;

import java.util.BitSet;

public class PlayerTerritory extends BitSet {

	public PlayerTerritory() {
	}

	public PlayerTerritory(PlayerTerritory other) {
		this.or(other);
	}

	public boolean get(Cell cell) {
		return this.get(cell.getIndex());
	}

	public void set(Cell cell) {
		this.set(cell.getIndex());
	}

	public void clear(Cell cell) {
		this.clear(cell.getIndex());
	}

	public int getSize() {
		return this.cardinality();
	}
}
