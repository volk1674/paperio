package strategy.model;

import message.Direction;
import strategy.Game;
import strategy.TimeMatrixBuilder;

import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static strategy.Game.capture;
import static strategy.Game.point2cell;

public class Player {
	private Deque<PlayerState> savedStates = new LinkedList<>();

	private final int index;
	private PlayerState state;
	private TimeMatrixBuilder timeMatrixBuilder;

	private transient List<Cell> capturedCells = Collections.emptyList();

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

	public Set<Direction> getPossibleDirections() {
		if (Game.isNotCellCenter(state.getX(), state.getY()))
			return Collections.emptySet();

		Set<Direction> result = EnumSet.noneOf(Direction.class);
		Cell cell = Game.point2cell(state.getX(), state.getY());
		for (Direction direction : cell.directions()) {
			if (direction.isOpposite(state.getDirection()))
				continue;

			Cell nextCell = cell.nextCell(direction);
			if (state.getTail().isTail(nextCell))
				continue;

			result.add(direction);
		}
		return result;
	}

	public int move(Direction direction, Map<Cell, Bonus> bonusMap) {
		Set<Direction> mpd = this.getPossibleDirections();
		if (!mpd.contains(direction)) {
			return 0;
		}

		capturedCells = Collections.emptyList();
		int ticksForMove = Game.width / Game.calculateSpeed(state.getNb(), state.getSb());
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

		PlayerTerritory playerTerritory = state.getPlayerTerritory();
		PlayerTail tail = state.getTail();

		Cell cell = point2cell(state.getX(), state.getY());
		if (tail.length() > 0) {
			if (playerTerritory.isTerritory(cell)) {
				capturedCells = capture(state);
				for (Cell capturedCell : capturedCells) {
					playerTerritory.addTerritory(capturedCell);
				}
				tail.clear();
			} else {
				tail.addToTail(cell);
			}
		} else {
			if (playerTerritory.isNotTerritory(cell)) {
				tail.addToTail(cell);
			}
		}

		Bonus bonus = bonusMap.get(cell);
		if (bonus != null) {
			switch (bonus.getBonusType()) {
				case n:
					state.addNitro(bonus.getCells());
					break;
				case s:
					state.addSlow(bonus.getCells());
					break;
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

	public TimeMatrixBuilder getTimeMatrixBuilder() {
		return timeMatrixBuilder;
	}

	public void setTimeMatrixBuilder(TimeMatrixBuilder timeMatrixBuilder) {
		this.timeMatrixBuilder = timeMatrixBuilder;
	}
}
