package strategy;

import message.Direction;
import strategy.model.Bonus;
import strategy.model.Cell;
import strategy.model.PlayerState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static strategy.Game.calculateSpeed;
import static strategy.Game.isNotCellCenter;
import static strategy.Game.point2cell;
import static strategy.Game.width;

@SuppressWarnings({"WeakerAccess", "unused"})
public class SimpleTickMatrixBuilder {
	public final static int MAXIMUM_DEPTH = 30 * width / Game.speed;

	private final int[] tickMatrix;
	private final Map<Cell, Bonus> bonusMap;
	private final int startTick;

	public SimpleTickMatrixBuilder(int startTick, Map<Cell, Bonus> bonusMap) {
		this.tickMatrix = new int[Game.sizeY * Game.sizeX];
		this.bonusMap = bonusMap;
		this.startTick = startTick;
		Arrays.fill(tickMatrix, startTick + MAXIMUM_DEPTH);
	}

	public void build(PlayerState state) {
		int tick = startTick;

		int posX = state.getX();
		int posY = state.getY();
		int nb = state.getNb();
		int sb = state.getSb();
		Direction direction = state.getDirection();

		Cell cell;
		if (isNotCellCenter(posX, posY)) {
			int speed = calculateSpeed(nb, sb);
			while (isNotCellCenter(posX, posY)) {
				switch (direction) {
					case up:
						posY += speed;
						break;
					case down:
						posY -= speed;
						break;
					case left:
						posX -= speed;
						break;
					case right:
						posX += speed;
						break;
					default:
						throw new IllegalStateException("direction is null");
				}
				tick++;
			}
			if (nb > 0) nb--;
			if (sb > 0) sb--;

			cell = point2cell(posX, posY);
		} else {
			cell = point2cell(posX, posY);
		}

		internalBuild(tick, cell, nb, sb);
	}

	private void internalBuild(int tick, Cell cell, int nb, int sb) {
		tickMatrix[cell.getIndex()] = tick;
		List<Cell> currList = new ArrayList<>();
		currList.add(cell);
		List<Cell> nextList = new ArrayList<>();
		while (!currList.isEmpty()) {
			int speed = Game.calculateSpeed(nb, sb);
			tick += width / speed;
			if (nb > 0) nb--;
			if (sb > 0) sb--;
			for (Cell curr : currList) {
				for (Direction dir : curr.directions()) {
					Cell next = curr.nextCell(dir);
					if (tickMatrix[next.getIndex()] > tick) {
						tickMatrix[next.getIndex()] = tick;
						nextList.add(next);

						Bonus bonus = bonusMap.get(next);
						if (bonus != null) {
							switch (bonus.getBonusType()) {
								case n:
									internalBuild(tick, next, nb + bonus.getCells(), sb);
									break;
								case s:
									internalBuild(tick, next, nb, sb + bonus.getCells());
									break;
							}
						}
					}
				}
			}

			List<Cell> tmp = nextList;
			nextList = currList;
			currList = tmp;
			nextList.clear();
		}
	}

	public int[] getTickMatrix() {
		return tickMatrix;
	}

	public int getTick(Cell cell) {
		return tickMatrix[cell.getIndex()];
	}

	public static void mergeTickMatrix(int[] tickMatrixFrom, int[] tickMatrixTo) {
		for (int i = 0; i < tickMatrixFrom.length; i++) {
			if (tickMatrixFrom[i] < tickMatrixTo[i]) {
				tickMatrixTo[i] = tickMatrixFrom[i];
			}
		}
	}
}
