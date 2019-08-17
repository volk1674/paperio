package strategy;

import message.Direction;
import strategy.model.Bonus;
import strategy.model.Cell;
import strategy.model.PlayerState;
import strategy.model.PlayerTail;
import strategy.model.PlayerTerritory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static strategy.Game.calculateSpeed;
import static strategy.Game.capture;
import static strategy.Game.isNotCellCenter;
import static strategy.Game.point2cell;

public class TimeMatrixBuilder {
	private final static int TICK_MAX_VALUE = 15 * 6;

	private final int[] tickMatrix1;
	private final int[] tailMatrix1;
	private final int[] tickMatrix2;
	private final int[] tailMatrix2;

	public TimeMatrixBuilder() {
		this.tickMatrix1 = new int[Game.sizeY * Game.sizeX];
		this.tailMatrix1 = new int[Game.sizeY * Game.sizeX];
		this.tickMatrix2 = new int[Game.sizeY * Game.sizeX];
		this.tailMatrix2 = new int[Game.sizeY * Game.sizeX];
	}

	public void buildTimeMatrix(int startTick, PlayerState state, Map<Cell, Bonus> bonusMap) {
		PlayerTail tail = state.getTail();
		int tick = startTick;

		Arrays.fill(tickMatrix1, tick + TICK_MAX_VALUE);
		Arrays.fill(tickMatrix2, tick + TICK_MAX_VALUE);
		Arrays.fill(tailMatrix1, tailMatrix1.length);
		Arrays.fill(tailMatrix2, tailMatrix2.length);

		int posX = state.getX();
		int posY = state.getY();
		int nb = state.getNb();
		int sb = state.getSb();
		Direction direction = state.getDirection();

		boolean cutTailFlag = false;
		Cell cell = point2cell(posX, posY);

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
			Cell prev = cell.nextCell(direction.opposite());
			tickMatrix1[prev.getIndex()] = startTick;
			tickMatrix2[prev.getIndex()] = startTick;
			tailMatrix1[prev.getIndex()] = tail.length();
			tailMatrix2[prev.getIndex()] = tail.length();
			if (!state.getPlayerTerritory().isTerritory(cell)) {
				tail.addToTail(cell);
				cutTailFlag = true;
			}
		}

		if (tail.length() == 0) {
			buildTimeMatrix2(tick, nb, sb, direction, cell, 0, state.getPlayerTerritory());
		} else {
			buildTimeMatrix1(tick, nb, sb, direction, cell, state.getTail(), state.getPlayerTerritory());
			mergeTickMatrix(tickMatrix1, tailMatrix1, tickMatrix2, tailMatrix2);
		}

		if (cutTailFlag) {
			tail.removeLast();
		}
	}

	private void buildTimeMatrix2(int tick, int nb, int sb, Direction direction, Cell cell, int tailLength, PlayerTerritory playerTerritory) {
		if (tick < tickMatrix1[cell.getIndex()]) {
			tickMatrix1[cell.getIndex()] = tick;
		}

		if (tick < tickMatrix2[cell.getIndex()] || tick == tickMatrix2[cell.getIndex()] && tailLength < tailMatrix2[cell.getIndex()]) {
			tickMatrix2[cell.getIndex()] = tick;
			tailMatrix2[cell.getIndex()] = tailLength;

			for (Direction nextDirection : cell.directions()) {
				Cell nextCell = cell.nextCell(nextDirection);
				if (nextDirection.isOpposite(direction)) continue;
				int speed = calculateSpeed(nb, sb);
				buildTimeMatrix2(tick + Game.width / speed, nb > 0 ? nb - 1 : 0, sb > 0 ? sb - 1 : 0, nextDirection, nextCell, playerTerritory.isTerritory(cell) ? 0 : tailLength + 1, playerTerritory);
			}
		}
	}

	private void buildTimeMatrix1(int tick, int nb, int sb, Direction direction, Cell cell, PlayerTail tail, PlayerTerritory playerTerritory) {
		if (tick < tickMatrix1[cell.getIndex()]) {
			tickMatrix1[cell.getIndex()] = tick;
			tailMatrix1[cell.getIndex()] = tail.length();

			if (playerTerritory.isTerritory(cell)) {
				// находим захваченные клетки
				List<Cell> capturedCells = capture(playerTerritory, tail);
				playerTerritory = new PlayerTerritory(playerTerritory);
				for (Cell capturedCell : capturedCells) {
					playerTerritory.addTerritory(capturedCell);
					if (tick <= tickMatrix1[capturedCell.getIndex()]) {  // все захваченные клеточки становятся ядовитыми на тике захвата
						tailMatrix1[capturedCell.getIndex()] = 0;
						tickMatrix1[capturedCell.getIndex()] = tick;
					}
				}
				buildTimeMatrix2(tick, nb, sb, direction, cell, tail.length(), playerTerritory);
			} else {
				for (Direction nextDirection : cell.directions()) {
					Cell nextCell = cell.nextCell(nextDirection);
					if (nextDirection.isOpposite(direction) || tail.isTail(nextCell)) continue;
					int speed = calculateSpeed(nb, sb);
					tail.addToTail(nextCell);
					buildTimeMatrix1(tick + Game.width / speed, (nb > 0) ? nb - 1 : 0, (sb > 0) ? sb - 1 : 0, nextDirection, nextCell, tail, playerTerritory);
					tail.removeLast();
				}
			}
		}
	}

	public int[] getTickMatrix() {
		return tickMatrix2;
	}

	public int[] getTailMatrix() {
		return tailMatrix2;
	}

	public static void mergeTickMatrix(int[] tickMatrixFrom, int[] tailMatrixFrom, int[] tickMatrixTo, int[] tailMatrixTo) {
		for (int i = 0; i < tickMatrixFrom.length; i++) {
			if (tickMatrixFrom[i] < tickMatrixTo[i]) {
				tickMatrixTo[i] = tickMatrixFrom[i];
				tailMatrixTo[i] = tailMatrixFrom[i];
			} else if (tickMatrixFrom[i] == tickMatrixTo[i] && tailMatrixFrom[i] < tailMatrixTo[i]) {
				tailMatrixTo[i] = tailMatrixFrom[i];
			}
		}
	}
}
