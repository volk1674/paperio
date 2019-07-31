package strategy;

import message.Direction;
import strategy.model.Cell;
import strategy.model.PlayerState;
import strategy.model.PlayerTail;
import strategy.model.PlayerTerritory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.round;

@SuppressWarnings("WeakerAccess")
public class Game {
	public static int maxTickCount = 1500;
	public static int sizeX = 31;
	public static int sizeY = 31;
	public static int width = 30;
	public static int speed = 5;
	public static Cell[][] cells = new Cell[sizeX][sizeY];

	static {
		initCells();
	}

	public static void init(int sizeX, int sizeY, int width, int speed) {
		if (Game.sizeX != sizeX || Game.sizeY != sizeY) {
			Game.sizeX = sizeX;
			Game.sizeY = sizeY;
			initCells();
		}

		Game.width = width;
		Game.speed = speed;
	}

	static private void initCells() {
		cells = new Cell[Game.sizeX][Game.sizeY];
		for (int i = 0; i < Game.sizeX; i++) {
			for (int j = 0; j < Game.sizeY; j++) {
				cells[i][j] = new Cell(i, j, i == 0 || j == 0 || i == sizeX - 1 || j == sizeY - 1);
			}
		}

		for (int i = 0; i < Game.sizeX; i++) {
			for (int j = 0; j < Game.sizeY; j++) {
				if (i > 0) {
					cells[i][j].getNeighborsMap().put(Direction.left, cells[i - 1][j]);
				}
				if (j > 0) {
					cells[i][j].getNeighborsMap().put(Direction.down, cells[i][j - 1]);
				}
				if (i < Game.sizeX - 1) {
					cells[i][j].getNeighborsMap().put(Direction.right, cells[i + 1][j]);
				}
				if (j < Game.sizeY - 1) {
					cells[i][j].getNeighborsMap().put(Direction.up, cells[i][j + 1]);
				}
			}
		}
	}

	public static Cell cell(int cellX, int cellY) {
		return cells[cellX][cellY];
	}

	public static Collection<Cell> neighborsCells(int cellX, int cellY) {
		return cell(cellX, cellY).neighbors();
	}

	public static int point2cell(int point) {
		return (point - round(width / 2f)) / width;
	}

	public static Cell point2cell(int pointX, int pointY) {
		return cell(point2cell(pointX), point2cell(pointY));
	}

	public static boolean isNotCellCenter(int pointX, int pointY) {
		return (pointX - round(width / 2f)) % width != 0 || (pointY - round(width / 2f)) % width != 0;
	}

	public static void buildTimeMatrix(int tick, int nb, int sb, Direction direction, int posX, int posY, int[] result) {
		int speed = calculateSpeed(nb, sb);

		if (isNotCellCenter(posX, posY)) {
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
		}

		Cell cell = point2cell(posX, posY);
		speed = calculateSpeed(nb, sb);

		if (result[cell.getIndex()] > tick) {
			result[cell.getIndex()] = tick;
			for (Direction nextDirection : cell.directions()) {
				if (nextDirection.isOpposite(direction)) continue;

				int nextX = posX;
				int nextY = posY;
				switch (nextDirection) {
					case up:
						nextY = posY + width;
						break;
					case down:
						nextY = posY - width;
						break;
					case left:
						nextX = nextX - width;
						break;
					case right:
						nextX = nextX + width;
						break;
					default:
						throw new IllegalStateException("direction is null");
				}

				buildTimeMatrix(tick + width / speed, nb - 1, sb - 1, nextDirection, nextX, nextY, result);
			}
		}
	}

	/**
	 * Возвращает захваченные игроком ячейки
	 *
	 * @param playerState состояние игрока
	 * @return список захваченных игроком ячеек
	 */
	public static List<Cell> capture(PlayerState playerState) {
		List<Cell> result = new ArrayList<>();
		int[] voids = new int[sizeX * sizeY];
		List<Boolean> zones = new ArrayList<>();

		PlayerTerritory playerTerritory = playerState.getPlayerTerritory();
		PlayerTail tail = playerState.getTail();

		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				Cell cell = cell(i, j);
				if (!playerTerritory.get(cell) && !tail.isTail(cell)) {
					if (voids[cell.getIndex()] == 0) {
						boolean captured = markVoids(playerState, zones.size() + 1, voids, cell(i, j));
						zones.add(captured);
					}
					if (zones.get(voids[cell.getIndex()] - 1)) {
						result.add(cell);
					}
				} else if (tail.isTail(cell)) {
					result.add(cell);
				}
			}
		}
		return result;
	}

	private static boolean markVoids(PlayerState playerState, int zone, int[] voids, Cell cell) {
		voids[cell.getIndex()] = zone;

		PlayerTerritory playerTerritory = playerState.getPlayerTerritory();
		PlayerTail tail = playerState.getTail();

		boolean result = !cell.isBorder();
		for (Cell neighbor : cell.neighbors()) {
			if (voids[neighbor.getIndex()] == 0 && !playerTerritory.get(neighbor) && !tail.isTail(neighbor))
				result = markVoids(playerState, zone, voids, neighbor) && result;
		}
		return result;
	}


	public static int calculateSpeed(int nb, int sb) {
		int result = speed;

		if (nb > 0) {
			result++;
			while (width % result != 0) result++;
		}

		if (sb > 0) {
			result--;
			while (width % result != 0) result--;
		}

		return result;
	}


}
