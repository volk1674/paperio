package strategy.model;

import message.Direction;
import strategy.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static strategy.Game.capture;
import static strategy.Game.point2cell;

public class Player {
	private Deque<PlayerState> savedStates = new LinkedList<>();

	private final int index;
	private PlayerState state;

	transient List<Cell> capturedCells = Collections.emptyList();

	public Player(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public PlayerState getState() {
		return state;
	}

	public void setState(PlayerState state) {
		this.state = state;
	}

	public List<Direction> getPossibleDirections() {
		if (Game.isNotCellCenter(state.getX(), state.getY()))
			return Collections.emptyList();

		List<Direction> result = new ArrayList<>();
		Cell cell = Game.point2cell(state.getX(), state.getY());
		for (Map.Entry<Direction, Cell> entry : cell.neighborsMap.entrySet()) {
			if (state.getLines().isOccupied(entry.getValue()))
				continue;

			if (entry.getKey().isOpposite(state.getDirection()))
				continue;

			result.add(entry.getKey());
		}
		return result;
	}

	public int move(Direction direction) {
		capturedCells = Collections.emptyList();
		int ticksForMove = Game.width / Game.calculateSpeed(state.getNitroCells(), state.getSlowCells());
		state.setDirection(direction);

		switch (direction) {
			case down:
				state.moveDown();
				break;
			case up:
				state.moveUp();
				break;
			case left:
				state.moveLeft();
				break;
			case right:
				state.moveRight();
				break;
			default:
				throw new IllegalStateException("direction is null");
		}

		TerritoryBitMask territory = state.getTerritory();
		TerritoryBitMask lines = state.getLines();

		Cell cell = point2cell(state.getX(), state.getY());
		if (lines.getOccupiedCount() > 0) {
			if (territory.isOccupied(cell)) {
				capturedCells = capture(state);
				for (Cell capturedCell : capturedCells) {
					territory.setOccupied(capturedCell);
				}
				lines.clear();
			} else {
				lines.setOccupied(cell);
			}
		} else {
			if (territory.isNotOccupied(cell)) {
				lines.setOccupied(cell);
			}
		}

		state.useBonuses();
		return ticksForMove;
	}

	public void saveState() {
		savedStates.addFirst(new PlayerState(state));
	}

	public void restoreState() {
		state = savedStates.removeFirst();
	}

	public List<Cell> getCapturedCells() {
		return capturedCells;
	}
}
