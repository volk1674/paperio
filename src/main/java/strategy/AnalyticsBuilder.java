package strategy;

import message.Direction;
import strategy.model.Bonus;
import strategy.model.Cell;
import strategy.model.CellDetails;
import strategy.model.Player;
import strategy.model.PlayerState;
import strategy.model.PlayerTail;
import strategy.model.PlayerTerritory;

import java.util.List;
import java.util.Map;

import static strategy.Game.calculateSpeed;
import static strategy.Game.capture;
import static strategy.Game.isNotCellCenter;
import static strategy.Game.point2cell;
import static strategy.Game.width;

@SuppressWarnings("WeakerAccess")
public class AnalyticsBuilder {
	public final static int MAXIMUM_DEPTH = 11 * width / Game.speed;

	private final static int MAX_LAG = 5;

	private final CellDetails[] cellDetailsMatrix;
	private final Map<Cell, Bonus> bonusMap;
	private final int startTick;

	private PlayerTerritory territory;
	private int playerIndex;

	public AnalyticsBuilder(int startTick, Map<Cell, Bonus> bonusMap) {
		cellDetailsMatrix = new CellDetails[Game.sizeX * Game.sizeY];
		this.bonusMap = bonusMap;
		this.startTick = startTick;
		init();
	}

	private void init() {
		for (int i = 0, n = cellDetailsMatrix.length; i < n; i++) {
			cellDetailsMatrix[i] = new CellDetails(startTick + MAXIMUM_DEPTH);
		}
	}

	public void build(Player player) {
		int tick = startTick;
		PlayerState state = player.getState();
		playerIndex = player.getIndex();

		int posX = state.getX();
		int posY = state.getY();
		int nb = state.getNb();
		int sb = state.getSb();
		PlayerTail tail = new PlayerTail(state.getTail());
		territory = state.getPlayerTerritory();

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

			if (territory.isNotTerritory(cell)) {
				tail.addToTail(cell);
			}
		} else {
			cell = point2cell(posX, posY);
		}

		CellDetails cellDetails = cellDetailsMatrix[cell.getIndex()];
		cellDetails.enterTick = startTick;
		cellDetails.tick = tick;
		cellDetails.enterTailLength = tail.length() > 0 ? tail.length() - 1 : 0;
		cellDetails.playerIndex = playerIndex;

		if (tail.length() > 0 && territory.isTerritory(cell)) {
			cellDetails.captureTargetTick = tick;
			List<Cell> capturedCells = capture(territory, tail);
			for (Cell capturedCell : capturedCells) {
				CellDetails capturedCellDetails = cellDetailsMatrix[capturedCell.getIndex()];
				if (tick < capturedCellDetails.capturedTick) {
					capturedCellDetails.capturedTick = tick;
				}
			}
		}

		internalBuild(tick, direction, cell, tail, nb, sb);
	}

	private void internalBuild(int tick, Direction direction, Cell cell, PlayerTail tail, int nb, int sb) {
		int speed = calculateSpeed(nb, sb);
		if (nb > 0) nb--;
		if (sb > 0) sb--;
		int nextTick = tick + width / speed;

		CellDetails cellDetails = cellDetailsMatrix[cell.getIndex()];
		if (nextTick < cellDetails.leaveTick) {
			cellDetails.leaveTick = nextTick;
			cellDetails.leaveTailLength = tail.length();
		} else if (nextTick == cellDetails.leaveTick) {
			if (tail.length() < cellDetails.leaveTailLength) {
				cellDetails.leaveTailLength = tail.length();
			}
		}

		for (Direction nextDirection : cell.directions()) {
			Cell nextCell = cell.nextCell(nextDirection);
			if (nextDirection.isOpposite(direction) || tail.isTail(nextCell)) continue;

			CellDetails nextCellDetails = cellDetailsMatrix[nextCell.getIndex()];
			if (nextTick < nextCellDetails.tick) {
				nextCellDetails.tick = nextTick;
				nextCellDetails.enterTailLength = tail.length();
				nextCellDetails.enterTick = tick + 1;
				nextCellDetails.playerIndex = playerIndex;
			} else if (nextTick == nextCellDetails.tick) {
				if (tail.length() < nextCellDetails.enterTailLength) {
					nextCellDetails.enterTailLength = tail.length();
				}
			}

			if (nextTick <= nextCellDetails.tick + MAX_LAG) {

				Bonus bonus = bonusMap.get(nextCell);
				if (bonus != null) {
					switch (bonus.getBonusType()) {
						case n:
							nb += bonus.getCells();
							break;
						case s:
							sb += bonus.getCells();
							break;
					}
				}

				if (territory.isTerritory(nextCell)) {
					if (tail.length() > 0) {
						nextCellDetails.captureTargetTick = nextTick;

						List<Cell> capturedCells = capture(territory, tail);
						for (Cell capturedCell : capturedCells) {
							CellDetails capturedCellDetails = cellDetailsMatrix[capturedCell.getIndex()];
							if (nextTick < capturedCellDetails.capturedTick) {
								capturedCellDetails.capturedTick = nextTick;
							}
						}
						internalBuild(nextTick, nextDirection, nextCell, new PlayerTail(), nb, sb);
					} else {
						internalBuild(nextTick, nextDirection, nextCell, tail, nb, sb);
					}
				} else {
					tail.addToTail(nextCell);
					internalBuild(nextTick, nextDirection, nextCell, tail, nb, sb);
					tail.removeLast();
				}
			}
		}
	}


	public CellDetails getCellDetails(Cell cell) {
		return cellDetailsMatrix[cell.getIndex()];
	}

	public CellDetails[] getCellDetailsMatrix() {
		return cellDetailsMatrix;
	}
}
