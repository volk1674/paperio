package strategy.model;

import strategy.Game;

import static java.util.Arrays.fill;

public class TerritoryBitMask {
	private static final long[] masks = new long[64];

	static {
		long mask = 1;
		for (int i = 0; i < 64; i++) {
			masks[i] = mask;
			mask <<= 1;
		}
	}

	private final long[] territory;
	private int occupiedCount = 0;

	public TerritoryBitMask() {
		this.territory = new long[Game.sizeY];
	}

	public TerritoryBitMask(TerritoryBitMask other) {
		this.territory = other.territory.clone();
		this.occupiedCount = other.occupiedCount;
	}

	public static TerritoryBitMask empty() {
		return new TerritoryBitMask() {
			@Override
			public boolean isOccupied(Cell cell) {
				return false;
			}

			@Override
			public boolean isNotOccupied(Cell cell) {
				return true;
			}
		};
	}

	public boolean isOccupied(Cell cell) {
		return (territory[cell.y] & masks[cell.x]) == masks[cell.x];
	}

	public boolean isNotOccupied(Cell cell) {
		return (territory[cell.y] & masks[cell.x]) != masks[cell.x];
	}

	public void setOccupied(Cell cell) {
		if (isNotOccupied(cell)) {
			occupiedCount++;
		}
		territory[cell.y] |= masks[cell.x];
	}

	public void setVoid(Cell cell) {
		if (isOccupied(cell)) {
			occupiedCount--;
		}

		territory[cell.y] &= ~masks[cell.x];
	}

	public int getOccupiedCount() {
		return occupiedCount;
	}

	public void clear() {
		occupiedCount = 0;
		fill(territory, 0);
	}

	public void add(TerritoryBitMask other) {
		for (int i = 0; i < territory.length; i++) {
			territory[i] |= other.territory[i];
			occupiedCount += other.occupiedCount;
		}
	}
}
