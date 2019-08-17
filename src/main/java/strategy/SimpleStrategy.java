package strategy;

import message.Direction;
import strategy.model.Bonus;
import strategy.model.Cell;
import strategy.model.Move;
import strategy.model.MovePlanWithScore;
import strategy.model.Player;
import strategy.model.PlayerState;
import strategy.model.PlayerTerritory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static strategy.Game.point2cell;

@SuppressWarnings("FieldCanBeLocal")
public class SimpleStrategy implements Strategy {
	private static Random random = new Random(System.currentTimeMillis());

	private static double EPSILON = 1e-6;
	private static long REQUEST_MAX_TIME = 4_000;
	private static long MAX_EXECUTION_TIME = 100_000;

	private List<MovePlanWithScore> bestPlans = new ArrayList<>();

	private PlayerTerritory otherPlayerTerritory = new PlayerTerritory();
	private List<Player> others;
	private Player me;

	private int[] tickMatrix = new int[Game.sizeX * Game.sizeY];
	private int[] tailMatrix = new int[Game.sizeX * Game.sizeY];
	private int startTailTick = 0;

	private void initTick(int tick, Map<Cell, Bonus> bonusMap) {
		Arrays.fill(tickMatrix, Integer.MAX_VALUE);
		Arrays.fill(tailMatrix, Integer.MAX_VALUE);

		otherPlayerTerritory.clear();
		for (Player player : others) {
			otherPlayerTerritory.or(player.getState().getPlayerTerritory());

			TimeMatrixBuilder timeMatrixBuilder = new TimeMatrixBuilder();
			player.setTimeMatrixBuilder(timeMatrixBuilder);
			timeMatrixBuilder.buildTimeMatrix(tick, player.getState(), bonusMap);

			TimeMatrixBuilder.mergeTickMatrix(timeMatrixBuilder.getTickMatrix(), timeMatrixBuilder.getTailMatrix(), tickMatrix, tailMatrix);
		}
	}

	public Direction calculate(int tick, Player me, List<Player> others, Map<Cell, Bonus> bonusMap) {
		long startTime = System.currentTimeMillis();
		this.me = me;
		this.others = others;
		initTick(tick, bonusMap);

		Set<Direction> directions = me.getPossibleDirections();
		if (directions.isEmpty()) {
			return Direction.up;
		}

		bestPlans.clear();
		List<MovePlanWithScore> bestPlans = estimatePlans(tick, me, generatePlans(directions), bonusMap);
		bestPlans.sort(Comparator.reverseOrder());

		Direction result = directions.stream().findFirst().orElse(Direction.up);
		if (bestPlans.isEmpty()) {
			Cell cell = point2cell(me.getState().getX(), me.getState().getY());
			if (abs(cell.getX() - Game.sizeX) > abs(cell.getY() - Game.sizeY)) {
				if (cell.getX() > Game.sizeX / 2 && directions.contains(Direction.left)) {
					return Direction.left;
				} else if (cell.getX() < Game.sizeX / 2 && directions.contains(Direction.right)) {
					return Direction.right;
				}
			} else {
				if (cell.getY() > Game.sizeY / 2 && directions.contains(Direction.down)) {
					return Direction.down;
				} else if (cell.getY() < Game.sizeY / 2 && directions.contains(Direction.up)) {
					return Direction.up;
				}
			}
		} else {
			result = bestPlans.get(0).getMoves()[0].getDirection();
		}

		if (me.getState().getTail().length() == 0) {
			startTailTick = tick;
		}

		return result;
	}

	private List<MovePlanWithScore> estimatePlans(int tick, Player me, List<MovePlanWithScore> plans, Map<Cell, Bonus> bonusMap) {
		int oldTa1lLength = me.getState().getTail().length();
		int oldTailTick = Integer.MAX_VALUE;
		for (Cell cell : me.getState().getTail().getCells()) {
			oldTailTick = min(oldTailTick, tickMatrix[cell.getIndex()]);
		}

		MovePlanWithScore prevPlan = null;

		// проигрываем все ходы
		FOR_PLANS:
		for (MovePlanWithScore plan : plans) {
			if (prevPlan != null && prevPlan.isPartOf(plan) || isInBestPlans(plan, bestPlans)) {
				continue;
			}
			me.saveState();
			try {
				PlayerState state = me.getState();

				int score = 0;
				int currentTick = tick;
				int newTailTick = Integer.MAX_VALUE;
				for (Move move : plan.getMoves()) {
					int n = 0;
					while (n < move.getLength()) {
						n++;
						int tailLength = me.getState().getTail().length();
						int ticks = me.move(move.getDirection(), bonusMap);
						if (ticks > 0) {
							currentTick += ticks;
							if (currentTick > Game.maxTickCount) {
								plan.setScore(Double.MIN_VALUE);
								move.setLength(n);
								continue FOR_PLANS;
							}

							Cell cell = Game.point2cell(state.getX(), state.getY());


							if (newTailTick <= currentTick && oldTa1lLength == 0) {
								// скорее всего не успеваем по времени (могут перехватить)
								plan.setRisk(Double.MAX_VALUE);
								continue FOR_PLANS;
							}


							boolean isTail = false;
							for (Player other : others) {
								if (other.getState().getTail().isTail(cell)) {
									score += 10;
									isTail = true;
								}
							}

							if (!isTail && oldTa1lLength == 0 && tailMatrix[cell.getIndex()] <= tailLength && tickMatrix[cell.getIndex()] <= currentTick + 12) {
								plan.setRisk(Double.MAX_VALUE);
								continue FOR_PLANS;
							} else if (!isTail && oldTa1lLength > 0 && tailMatrix[cell.getIndex()] <= tailLength && tickMatrix[cell.getIndex()] <= currentTick + 12) {

								int k = 12;
								for (Player other : others) {
									if (other.getState().getY() == me.getState().getY() && other.getState().getX() > me.getState().getX() && move.getDirection() == Direction.left) {
										k = 1;
									} else if (other.getState().getY() == me.getState().getY() && other.getState().getX() < me.getState().getX() && move.getDirection() == Direction.right) {
										k = 1;
									} else if (other.getState().getX() == me.getState().getX() && other.getState().getY() < me.getState().getY() && move.getDirection() == Direction.up) {
										k = 1;
									} else if (other.getState().getX() == me.getState().getX() && other.getState().getY() > me.getState().getY() && move.getDirection() == Direction.down) {
										k = 1;
									}
								}

								plan.setRisk(currentTick + k - currentTick + 1);
							}

							Bonus bonus = bonusMap.get(cell);
							if (bonus != null) {
								switch (bonus.getBonusType()) {
									case n:
										score += bonus.getCells() / 2;
										break;
									case s:
										score -= bonus.getCells() / 2;
										break;
									case saw:
										score += 30;
										break;
								}
							}


							double moveScore = calculateMoveScore(me.getCapturedCells(), bonusMap);
							if (moveScore > 0) {
								moveScore += score + random.nextDouble();
								int delta = currentTick - startTailTick;
								plan.setScore(moveScore / delta);

								double tickLag = currentTick - min(newTailTick, oldTailTick);
								if (tickLag > 0) {
									plan.setRisk(max(tickLag, plan.getRisk()));
								}
								move.setLength(n);
								bestPlans.add(plan);
								continue FOR_PLANS;
							}

							if (tailLength > 0) {
								newTailTick = min(newTailTick, tickMatrix[cell.getIndex()]);
							}
						} else {
							// невозможный ход
							plan.setRisk(Double.MAX_VALUE);
							move.setLength(n);
							continue FOR_PLANS;
						}
					}
				}
			} finally {
				me.restoreState();
				prevPlan = plan;
			}
		}

		return bestPlans;
	}

	private boolean isInBestPlans(MovePlanWithScore plan, List<MovePlanWithScore> bestPlans) {
		ListIterator<MovePlanWithScore> bestPlansIterator = bestPlans.listIterator(bestPlans.size());
		while (bestPlansIterator.hasPrevious()) {
			MovePlanWithScore bestPlan = bestPlansIterator.previous();
			if (bestPlan.isPartOf(plan)) {
				return true;
			}
		}
		return false;
	}

	protected List<MovePlanWithScore> generatePlans(Set<Direction> directions) {
		List<MovePlanWithScore> plans = new ArrayList<>();
		for (Direction m1d : directions) {
			plans.add(new MovePlanWithScore(new Move(m1d))); // пока не упремся или не замкнем территорию
			for (int m1 = 1; m1 < 15; m1++) {
				for (Direction m2d : Direction.values()) {
					if (m2d.isOpposite(m1d) || m2d == m1d) continue;
					plans.add(new MovePlanWithScore(new Move(m1d, m1), new Move(m2d))); // для 2го хода пока не упремся
					for (int m2 = 1; m2 <= 10; m2 += 2) {
						plans.add(new MovePlanWithScore(new Move(m1d, m1), new Move(m2d, m2), new Move(m1d.opposite())));
						for (int m3 = 1; m3 <= 15; m3++) {
							plans.add(new MovePlanWithScore(new Move(m1d, m1), new Move(m2d, m2), new Move(m1d.opposite(), m3), new Move(m2d.opposite())));
						}
					}
				}
			}
		}
		return plans;
	}


	private int calculateMoveScore(List<Cell> capturedCells, Map<Cell, Bonus> bonusMap) {
		int result = capturedCells.size();
		int bonusResult = 0;

		for (Cell cell : capturedCells) {
			if (otherPlayerTerritory.isTerritory(cell)) {
				result += 4;
			}

			Bonus bonus = bonusMap.get(cell);
			if (bonus != null) {
				switch (bonus.getBonusType()) {
					case n:
						bonusResult += bonus.getCells() / 2;
						break;
					case s:
						bonusResult -= bonus.getCells() / 2;
						break;
					case saw:
						bonusResult += 30;
						break;
				}
			}
		}

		if (result > 0) {
			result += bonusResult;
		}

		return result;
	}

	public List<MovePlanWithScore> getBestPlans() {
		return bestPlans;
	}

	public int[] getTickMatrix() {
		return tickMatrix;
	}

	public int[] getTailMatrix() {
		return tailMatrix;
	}
}
