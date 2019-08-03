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
import java.util.Random;
import java.util.Set;

import static strategy.Game.buildTimeMatrix;

@SuppressWarnings("FieldCanBeLocal")
public class Strategy {
	private static Random random = new Random(System.currentTimeMillis());

	private static double EPSILON = 1e-6;
	private static long REQUEST_MAX_TIME = 4_000;
	private static long MAX_EXECUTION_TIME = 100_000;

	private List<MovePlan> plans = new ArrayList<>();
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


		plans.clear();
		generatePlans(directions, plans);
		estimatePlans(tick, me, plans, bonusTypeMap);

		plans.sort(Comparator.reverseOrder());
		MovePlan plan = plans.get(0);
		return plan.getMoves()[0].getDirection();
	}

	protected void estimatePlans(int tick, Player me, List<MovePlan> plans, Map<Cell, List<BonusType>> bonusTypeMap) {


		int oldTa1lLength = me.getState().getTail().length();
		int oldTailTick = Integer.MAX_VALUE;
		for (Cell cell : me.getState().getTail().getCells()) {
			oldTailTick = Math.min(oldTailTick, timeMatrix[cell.getIndex()]);
		}

		// проигрываем все ходы
		FOR_PLANS:
		for (MovePlan plan : plans) {
			me.saveState();
			try {
				PlayerState state = me.getState();
				int currentTick = tick;
				int newTailTick = Integer.MAX_VALUE;
				for (Move move : plan.getMoves()) {
					int n = 0;
					while (n < move.getCells()) {
						n++;
						int tailLength = me.getState().getTail().length();
						int ticks = me.move(move.getDirection());
						if (ticks > 0) {
							currentTick += ticks;
							if (currentTick > Game.maxTickCount) {
								plan.setScore(Double.MIN_VALUE);
								continue FOR_PLANS;
							}

							Cell cell = Game.point2cell(state.getX(), state.getY());
							newTailTick = Math.min(newTailTick, timeMatrix[cell.getIndex()]);

							if (newTailTick <= currentTick && oldTa1lLength == 0 && tailLength > 0) {
								// скорее всего не успеваем по времени (могут перехватить)
								plan.setRisk(Double.MAX_VALUE);
								continue FOR_PLANS;
							}

							double moveScore = calculateMoveScore(me.getCapturedCells(), bonusTypeMap);
							if (moveScore > 0) {

								moveScore += random.nextDouble();
								plan.setScore(moveScore / (tailLength + 1));
								double tickLag = currentTick - Math.min(newTailTick, oldTailTick);
								if (tickLag > 0) {
									plan.setRisk(tickLag);
								}
								move.setCells(n);
								continue FOR_PLANS;
							}

						} else {
							// невозможный ход
							plan.setRisk(Double.MAX_VALUE);
							continue FOR_PLANS;
						}
					}
				}
			} finally {
				me.restoreState();
			}
		}
	}

	protected void generatePlans(Set<Direction> directions, List<MovePlan> plans) {
		for (Direction m1d : directions) {
			plans.add(new MovePlan(new Move(m1d))); // пока не упремся или не замкнем территорию
			for (int m1 = 1; m1 < 10; m1++) {
				for (Direction m2d : Direction.values()) {
					if (m2d.isOpposite(m1d) || m2d == m1d) continue;
					plans.add(new MovePlan(new Move(m1d, m1), new Move(m2d))); // для 2го хода пока не упремся
					for (int m2 = 1; m2 <= 10; m2++) {
						plans.add(new MovePlan(new Move(m1d, m1), new Move(m2d, m2), new Move(m1d.opposite())));
					}
				}
			}
		}
	}


	private int calculateMoveScore(List<Cell> capturedCells, Map<Cell, List<BonusType>> bonusTypeMap) {
		int result = capturedCells.size();
		int bonusResult = 0;

		for (Cell cell : capturedCells) {
			if (otherPlayerTerritory.get(cell)) {
				result += 4;
			}

			List<BonusType> cellBonuses = bonusTypeMap.get(cell);
			if (cellBonuses != null) {
				if (cellBonuses.contains(BonusType.n)) {
					bonusResult += 10;
				}
				if (cellBonuses.contains(BonusType.s)) {
					bonusResult -= 10;
				}
				if (cellBonuses.contains(BonusType.saw)) {
					bonusResult += 20;
				}
			}

		}

		if (result > 0) {
			result += bonusResult;
		}

		return result;
	}

	public List<MovePlan> getPlans() {
		return plans;
	}
}
