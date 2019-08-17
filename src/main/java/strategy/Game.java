package strategy;

import message.Direction;
import strategy.model.Cell;
import strategy.model.PlayerState;
import strategy.model.PlayerTail;
import strategy.model.PlayerTerritory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.round;

@SuppressWarnings("WeakerAccess")
public class Game {
	public static final int MAX_PLAN_LENGTH = 25;

	public static int maxTickCount = 2499;
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

		Cell dummy = new Cell(-1, -1, true, Collections.emptyMap());

		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				Map<Direction, Cell> directions = new EnumMap<>(Direction.class);
				for (Direction direction : Direction.values()) {
					if (i == 0 && direction == Direction.left) continue;
					if (i == sizeX - 1 && direction == Direction.right) continue;
					if (j == 0 && direction == Direction.down) continue;
					if (j == sizeY - 1 && direction == Direction.up) continue;

					directions.put(direction, dummy);
				}

				cells[i][j] = new Cell(i, j, i == 0 || j == 0 || i == sizeX - 1 || j == sizeY - 1, directions);
			}
		}

		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				for (Direction direction : cell(i, j).directions()) {
					cell(i, j).getDirectionsMap().put(direction, nextCell(cell(i, j), direction));
				}
			}
		}
	}

	public static Cell cell(int cellX, int cellY) {
		return cells[cellX][cellY];
	}

	public static int point2cell(int point) {
		return (point - round(width / 2f)) / width;
	}

	public static Cell point2cell(int pointX, int pointY) {
		return cell(point2cell(pointX), point2cell(pointY));
	}

	public static int cell2point(int c) {
		return c * width + round(width / 2f);
	}

	public static boolean isNotCellCenter(int pointX, int pointY) {
		return (pointX - round(width / 2f)) % width != 0 || (pointY - round(width / 2f)) % width != 0;
	}

	public static boolean isCellCenter(int pointX, int pointY) {
		return (pointX - round(width / 2f)) % width == 0 && (pointY - round(width / 2f)) % width == 0;
	}

	/**
	 * Возвращает захваченные игроком ячейки
	 *
	 * @param playerState состояние игрока
	 * @return список захваченных игроком ячеек
	 */
	public static List<Cell> capture(PlayerState playerState) {
		return capture(playerState.getPlayerTerritory(), playerState.getTail());
	}

	public static List<Cell> capture(PlayerTerritory playerTerritory, PlayerTail tail) {
		List<Cell> result = new ArrayList<>();
		int[] voids = new int[sizeX * sizeY];
		List<Boolean> zones = new ArrayList<>();

		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				Cell cell = cell(i, j);
				if (!playerTerritory.isTerritory(cell) && !tail.isTail(cell)) {
					if (voids[cell.getIndex()] == 0) {
						boolean captured = markVoids(playerTerritory, tail, zones.size() + 1, voids, cell(i, j));
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

	private static boolean markVoids(PlayerTerritory playerTerritory, PlayerTail tail, int zone, int[] voids, Cell cell) {
		voids[cell.getIndex()] = zone;

		boolean result = !cell.isBorder();
		for (Direction direction : cell.directions()) {
			Cell neighbor = nextCell(cell, direction);
			if (voids[neighbor.getIndex()] == 0 && !playerTerritory.isTerritory(neighbor) && !tail.isTail(neighbor))
				result = markVoids(playerTerritory, tail, zone, voids, neighbor) && result;
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

	private static Cell nextCell(Cell cell, Direction direction) {
		switch (direction) {
			case right:
				return cell(cell.getX() + 1, cell.getY());
			case left:
				return cell(cell.getX() - 1, cell.getY());
			case up:
				return cell(cell.getX(), cell.getY() + 1);
			case down:
				return cell(cell.getX(), cell.getY() - 1);
			default:
				throw new IllegalStateException();
		}
	}
}
