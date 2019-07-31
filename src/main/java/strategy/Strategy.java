package strategy;

import message.BonusType;
import message.Direction;
import strategy.model.Cell;
import strategy.model.Move;
import strategy.model.MovePlan;
import strategy.model.Player;
import strategy.model.PlayerState;
import strategy.model.PlayerTerritory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static strategy.Game.buildTimeMatrix;

@SuppressWarnings("FieldCanBeLocal")
public class Strategy {
	private static double EPSILON = 1e-6;
	private static long REQUEST_MAX_TIME = 4_000;
	private static long MAX_EXECUTION_TIME = 100_000;


	private PlayerTerritory otherPlayerTerritory = new PlayerTerritory();
	private int[] timeMatrix = new int[Game.sizeX * Game.sizeY];

	private void initTick(int tick, Player me, List<Player> others, Map<Cell, List<BonusType>> bonusTypeMap) {
		Arrays.fill(timeMatrix, Integer.MAX_VALUE);

		otherPlayerTerritory.clear();
		for (Player player : others) {
			otherPlayerTerritory.or(player.getState().getPlayerTerritory());
			buildTimeMatrix(tick, player.getState().getNitroCells(), player.getState().getSlowCells(), player.getState().getDirection(), player.getState().getX(), player.getState().getY(), timeMatrix);
		}
	}

	public Direction calculate(int tick, Player me, List<Player> others, Map<Cell, List<BonusType>> bonusTypeMap) {
		long startTime = System.currentTimeMillis();

		initTick(tick, me, others, bonusTypeMap);

		Set<Direction> directions = me.getPossibleDirections();
		if (directions.isEmpty()) {
			return Direction.up;
		}

		List<MovePlan> plans = new ArrayList<>();
		for (Direction m1d : directions) {
			plans.add(new MovePlan(new Move(m1d))); // пока не упремся или не замкнем территорию
			for (int m1 = 1; m1 < 20; m1++) {
				for (Direction m2d : Direction.values()) {
					if (m2d.isOpposite(m1d) || m2d == m1d) continue;
					plans.add(new MovePlan(new Move(m1d, m1), new Move(m2d))); // для 2го хода пока не упремся
					for (int m2 = 1; m2 < 20; m2++) {
						plans.add(new MovePlan(new Move(m1d, m1), new Move(m2d, m2), new Move(m1d.opposite())));
					}
				}
			}
		}

		int lastTick = Integer.MAX_VALUE;
		for (Cell cell : me.getState().getTail().getCells()) {
			lastTick = Math.min(lastTick, timeMatrix[cell.getIndex()]);
		}

		// проигрываем все ходы
		FOR_PLANS:
		for (MovePlan plan : plans) {
			me.saveState();
			try {
				PlayerState state = me.getState();
				int currentTick = tick;
				for (Move move : plan.getMoves()) {
					int cells = move.getCells();
					while (cells > 0) {
						int ticks = me.move(move.getDirection());
						if (ticks > 0) {
							currentTick += ticks;
							if (currentTick > Game.maxTickCount) {
								plan.setScore(Double.MIN_VALUE);
								continue FOR_PLANS;
							}
							Cell cell = Game.point2cell(state.getX(), state.getY());

							if (timeMatrix[cell.getIndex()] <= currentTick) {
								// скорее всего не успеваем по времени (могут перехватить)
								plan.setScore(Double.MIN_VALUE);
								continue FOR_PLANS;
							}

							int moveScore = calculateMoveScore(me.getCapturedCells());
							if (moveScore > 0) {
								moveScore += ThreadLocalRandom.current().nextDouble();

								if (currentTick >= lastTick) {
									plan.setScore(1. * moveScore / ((currentTick - tick) * (currentTick - lastTick + 2) * (currentTick - lastTick + 2)));
								} else {
									if (timeMatrix[cell.getIndex()] - currentTick < Game.speed) {
										plan.setScore(1. * moveScore / ((currentTick - tick) * 2));
									} else {
										plan.setScore(1. * moveScore / (currentTick - tick));
									}
								}
								continue FOR_PLANS;
							}

						} else {
							// невозможный ход
							plan.setScore(-Double.MAX_VALUE);
							continue FOR_PLANS;
						}
						cells--;
					}
				}
			} finally {
				me.restoreState();
			}
		}

		plans.sort(Comparator.comparingDouble(MovePlan::getScore).reversed());
		MovePlan plan = plans.get(0);
		return plan.getMoves()[0].getDirection();
	}


	private int calculateMoveScore(List<Cell> capturedCells) {
		int result = capturedCells.size();
		for (Cell cell : capturedCells) {
			if (otherPlayerTerritory.get(cell)) {
				result += 4;
			}
		}
		return result;
	}


}
