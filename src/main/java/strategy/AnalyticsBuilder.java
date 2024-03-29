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
import static strategy.Game.cell;
import static strategy.Game.isNotCellCenter;
import static strategy.Game.point2cell;
import static strategy.Game.sizeX;
import static strategy.Game.sizeY;
import static strategy.Game.width;

@SuppressWarnings("WeakerAccess")
public class AnalyticsBuilder {
	public final static int MAXIMUM_DEPTH = 10 * width / Game.speed;

	private final static int MAX_LAG = 6;

	private final CellDetails[] cellDetailsMatrix;
	private final Map<Cell, Bonus> bonusMap;
	private final int startTick;

	private PlayerTerritory territory;
	private int playerIndex;

	public AnalyticsBuilder(int startTick, Map<Cell, Bonus> bonusMap) {
		cellDetailsMatrix = new CellDetails[sizeX * sizeY];
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

		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				Cell c = cell(i, j);
				if (territory.isTerritory(c)) {
					cellDetailsMatrix[c.getIndex()].ownerIndex = playerIndex;
				}
			}
		}

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

			Cell prevCell = cell.nextCell(direction.opposite());
			CellDetails prevDetails = cellDetailsMatrix[prevCell.getIndex()];
			prevDetails.leaveTick = tick;
			prevDetails.leaveTailLength = tail.length();
			prevDetails.leaveDirection = direction;

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
			cellDetails.leaveDirection = direction;
		} else if (nextTick == cellDetails.leaveTick) {
			if (tail.length() < cellDetails.leaveTailLength) {
				cellDetails.leaveTailLength = tail.length();
				cellDetails.leaveDirection = direction;
			}
		}

		for (Direction nextDirection : cell.directions()) {
			int nnb = nb;
			int nsb = sb;

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
				{
					Bonus bonus = bonusMap.get(nextCell);
					if (bonus != null) {
						switch (bonus.getBonusType()) {
							case n:
								nnb += bonus.getCells();
								break;
							case s:
								nsb += bonus.getCells();
								break;
							case saw:
								Cell sawCell = nextCell.nextCell(nextDirection);
								while (sawCell != null) {
									CellDetails sawCellDetails = cellDetailsMatrix[sawCell.getIndex()];
									if (nextTick < sawCellDetails.sawTick) {
										sawCellDetails.sawTick = nextTick;
										sawCellDetails.sawDirection = nextDirection;
									}
									sawCell = sawCell.nextCell(nextDirection);
								}
								break;
						}
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

							{
								Bonus bonus = bonusMap.get(capturedCell);
								if (bonus != null) {
									switch (bonus.getBonusType()) {
										case n:
											nnb += bonus.getCells();
											break;
										case s:
											nsb += bonus.getCells();
											break;
										case saw:
											Cell sawCell = nextCell.nextCell(nextDirection);
											while (sawCell != null) {
												CellDetails sawCellDetails = cellDetailsMatrix[sawCell.getIndex()];
												if (nextTick < sawCellDetails.sawTick) {
													sawCellDetails.sawTick = nextTick;
													sawCellDetails.sawDirection = nextDirection;
												}
												sawCell = sawCell.nextCell(nextDirection);
											}
											break;
									}
								}
							}
						}
						internalBuild(nextTick, nextDirection, nextCell, new PlayerTail(), nnb, nsb);
					} else {
						internalBuild(nextTick, nextDirection, nextCell, tail, nnb, nsb);
					}
				} else {
					tail.addToTail(nextCell);
					internalBuild(nextTick, nextDirection, nextCell, tail, nnb, nsb);
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
