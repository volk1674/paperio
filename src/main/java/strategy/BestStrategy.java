package strategy;

import message.Direction;
import strategy.model.Bonus;
import strategy.model.CalculationScore;
import strategy.model.Cell;
import strategy.model.CellDetails;
import strategy.model.Player;
import strategy.model.PlayerState;
import strategy.model.PlayerTail;
import strategy.model.PlayerTerritory;

import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static strategy.Game.calculateSpeed;
import static strategy.Game.capture;
import static strategy.Game.point2cell;
import static strategy.Game.sizeX;
import static strategy.Game.sizeY;
import static strategy.Game.width;
import static strategy.model.CalculationScore.MINIMAL_SCORE;

public class BestStrategy implements Strategy {

	private MoveNode rootNode;

	private SimpleTickMatrixBuilder simpleTickMatrixBuilder;
	private CellDetails[] cellDetailsMatrix;
	private Map<Cell, Bonus> bonusMap;
	private PlayerTerritory otherPlayerTerritory;

	private int startCalculationTick;
	private int startPlanTick = 1;
	private int oldTailLength;
	private int oldTailTick;
	private PlayerTerritory territory;
	private boolean startedOnOwnTerritory;
	private List<Player> others;
	private PlayerTerritory otherPlayersHeads = new PlayerTerritory();
	private PlayerTerritory otherPlayerTails = new PlayerTerritory();


	public BestStrategy() {
		rootNode = new MoveNode();
	}

	private void initTick(int tick, Player me, List<Player> others, Map<Cell, Bonus> bonusMap) {
		this.bonusMap = bonusMap;
		otherPlayerTerritory = new PlayerTerritory();
		startCalculationTick = tick;
		this.others = others;
		otherPlayersHeads.clear();
		otherPlayerTails.clear();

		simpleTickMatrixBuilder = new SimpleTickMatrixBuilder(tick, bonusMap);
		AnalyticsBuilder analyticsBuilder = new AnalyticsBuilder(tick, bonusMap);
		for (Player player : others) {
			analyticsBuilder.build(player.getState());
			simpleTickMatrixBuilder.build(player.getState());
			otherPlayerTerritory.or(player.getState().getPlayerTerritory());
			otherPlayerTails.or(player.getState().getTail());

			otherPlayersHeads.set(point2cell(player.getState().getX(), player.getState().getY()).getIndex());
		}
		cellDetailsMatrix = analyticsBuilder.getCellDetailsMatrix();

		PlayerTail oldTail = me.getState().getTail();

		oldTailTick = Game.maxTickCount;
		for (Cell tailCell : oldTail.getCells()) {
			CellDetails cd = cellDetailsMatrix[tailCell.getIndex()];

			int cellTick;
			if (cd.tick - startCalculationTick < AnalyticsBuilder.MAXIMUM_DEPTH) {
				cellTick = cd.tick;
			} else {
				cellTick = simpleTickMatrixBuilder.getTick(tailCell);
			}

			if (cellTick < oldTailTick) {
				oldTailTick = cellTick;
			}
		}
		territory = me.getState().getPlayerTerritory();


		if (oldTail.length() == 0) {
			startPlanTick = tick;
		}
		oldTailLength = oldTail.length();
	}

	public void debug(Direction direction, CalculationScore score) {
	}

	@Override
	public Direction calculate(int tick, Player me, List<Player> others, Map<Cell, Bonus> bonusMap) {
		initTick(tick, me, others, bonusMap);

		PlayerState state = me.getState();

		Cell cell = Game.point2cell(state.getX(), state.getY());
		startedOnOwnTerritory = territory.isTerritory(cell);

		PlayerTail tail = new PlayerTail(state.getTail());
		// для каждого возможного направления движения вычисляем рейтинг

		Direction bestDirection = Direction.up;

		CalculationScore bestCalculationScore = MINIMAL_SCORE;

		for (Direction direction : cell.directions()) {
			if (direction.isOpposite(state.getDirection())) continue;
			MoveNode node = rootNode.getChildNode(direction);
			if (node == null) continue;

			CalculationScore calculationScore = estimate(tick, cell, node, tail, state.getNb(), state.getSb(), oldTailTick - tick);
			debug(direction, calculationScore);

			if (calculationScore.getRisk() < bestCalculationScore.getRisk()) {
				bestCalculationScore = calculationScore;
				bestDirection = direction;
			} else if (calculationScore.getRisk() == bestCalculationScore.getRisk() && calculationScore.getScore() > bestCalculationScore.getScore()) {
				bestCalculationScore = calculationScore;
				bestDirection = direction;
			}
		}

		return bestDirection;
	}

	protected CalculationScore estimate(int tick, Cell currentCell, MoveNode node, PlayerTail tail, int nb, int sb, int ticksLeft) {
		double rate = 0;
		double risk = 0;

		Cell nextCell = currentCell.nextCell(node.getDirection());
		if (nextCell == null || tail.isTail(nextCell)) {
			//выходим за пределы или натыкаемся на свой хвост
			return MINIMAL_SCORE;
		}

		int speed = calculateSpeed(nb, sb);
		int ticksForMove = width / speed;
		int nextTick = tick + ticksForMove;
		ticksLeft -= ticksForMove;

		CellDetails ncd = cellDetailsMatrix[nextCell.getIndex()];
		CellDetails ccd = cellDetailsMatrix[currentCell.getIndex()];

		if (ncd.capturedTick - startCalculationTick < AnalyticsBuilder.MAXIMUM_DEPTH) {
			ticksLeft = min(ncd.capturedTick - nextTick, ticksLeft);
		}

		if (ticksLeft < 0 && oldTailLength == 0) {
			return MINIMAL_SCORE;
		} else if (ticksLeft < 0) {
			risk = -ticksLeft;
		}


		if (ccd.enterTick - startCalculationTick < AnalyticsBuilder.MAXIMUM_DEPTH) {
			if (ccd.enterTick < nextTick && ccd.enterTick >= tick) {
				// todo добавить условие проверки что соперник находится на одной линии и он пытается догнать и не сможет
				if (ccd.enterTailLength > tail.length() && startCalculationTick == tick) {
					rate += 30;
				} else if (ccd.enterTailLength < tail.length()) {
					if (startedOnOwnTerritory) {
						return MINIMAL_SCORE;
					} else {
						risk += 100;
					}
				} else {
					risk++;
				}
			}

			if (tick + 1 < ncd.tick && ncd.enterTick < nextTick) {
				// todo добавить условие проверки что соперник убегает и я его не догоняю
				if (ncd.enterTailLength > tail.length() && startCalculationTick == tick) {
					rate += 30;
				} else if (ncd.enterTailLength < tail.length()) {
					if (startedOnOwnTerritory) {
						return MINIMAL_SCORE;
					} else {
						risk += 100;
					}
				} else {
					risk++;
				}
			} else if (tick + 1 < ncd.leaveTick && ncd.tick < tick) {
				if (ncd.leaveTailLength > tail.length() && startCalculationTick == tick) {
					rate += 30;
				} else if (ncd.leaveTailLength < tail.length()) {
					if (startedOnOwnTerritory) {
						return MINIMAL_SCORE;
					} else {
						risk += 100;
					}
				} else {
					risk++;
				}
			}
		}

		if (otherPlayerTails.isTerritory(nextCell) && ncd.capturedTick >= nextTick && (ncd.capturedTick - startCalculationTick) < AnalyticsBuilder.MAXIMUM_DEPTH) {
			rate += 30;
		}


		if (nb > 0) nb--;
		if (sb > 0) sb--;
		Bonus bonus = bonusMap.get(nextCell);
		if (bonus != null) {
			switch (bonus.getBonusType()) {
				case n:
					nb += bonus.getCells();
					rate += bonus.getCells() / 2;
					break;
				case s:
					sb += bonus.getCells();
					rate -= bonus.getCells() / 2;
					break;
				case saw:
					rate += 5;
					// todo надо начислить немнго очком за то что взял бонус (если взьмет кто нибудь другой то можно и пострадать)
					// 	+ дополнительные очки если на пути лежит чужая территория
					// 	+ дополнительные очки если на пути соперник (убьем его)
					// 	+ дополнительные очки если отризаем соперника от его территории
					Cell rc = nextCell;
					while (rc != null) {
						rate += otherPlayerTerritory.isTerritory(rc) ? 1 : 0;
						rate += otherPlayerTails.isTerritory(rc) ? 10 : 0;
						rc = rc.nextCell(node.getDirection());
					}
					break;
			}
		}


		if (territory.isTerritory(nextCell) && (tail.length() > 0 || !startedOnOwnTerritory)) {
			List<Cell> capturedCells = capture(territory, tail);

			if ((ncd.capturedTick - startCalculationTick) < AnalyticsBuilder.MAXIMUM_DEPTH && ncd.capturedTick < nextTick) {
				risk += 100;
			}

			rate += capturedCells.size();
			for (Cell capturedCell : capturedCells) {
				if (otherPlayerTerritory.isTerritory(capturedCell)) {
					rate += 4;
				}

				rate -= 2 * sqrt(pow2(1. * abs(sizeX / 2. - capturedCell.getX()) / sizeX) +
						pow2(1. * abs(sizeY / 2. - capturedCell.getY()) / sizeY));


				bonus = bonusMap.get(capturedCell);
				if (bonus != null) {
					switch (bonus.getBonusType()) {
						case n:
							nb += bonus.getCells();
							rate += bonus.getCells();
							break;
						case s:
							sb += bonus.getCells();
							rate -= bonus.getCells();
							break;
						case saw:
							rate += 5;
							// todo надо начислить немнго очком за то что взял бонус (если взьмет кто нибудь другой то можно и пострадать)
							// 	+ дополнительные очки если на пути лежит чужая территория
							// 	+ дополнительные очки если на пути соперник (убьем его)
							// 	+ дополнительные очки если отризаем соперника от его территории
							Cell rc = nextCell;
							while (rc != null) {
								rate += otherPlayerTerritory.isTerritory(rc) ? 1 : 0;
								rate += otherPlayerTails.isTerritory(rc) ? 10 : 0;
								rc = rc.nextCell(node.getDirection());
							}


							break;
					}
				}

				if (otherPlayersHeads.get(capturedCell.getIndex())) {
					rate *= 2;
				}

				CellDetails captureCellDetail = cellDetailsMatrix[capturedCell.getIndex()];
				if (captureCellDetail.captureTargetTick - startCalculationTick < AnalyticsBuilder.MAXIMUM_DEPTH && captureCellDetail.captureTargetTick < nextTick) {
					rate += 150;
				}
			}

			//rate /= 1 + 4. * abs(sizeX / 2 - nextCell.getX()) / sizeX + 4. * abs(sizeY / 2 - nextCell.getY()) / sizeY;

			return new CalculationScore(rate / (nextTick - startPlanTick + 1), risk);
		}


		if (territory.isNotTerritory(nextCell)) {
			tail.addToTail(nextCell);

			if (ncd.tick - startCalculationTick < AnalyticsBuilder.MAXIMUM_DEPTH) {
				ticksLeft = min(ncd.tick - nextTick, ticksLeft);
			} else {
				ticksLeft = min(max(simpleTickMatrixBuilder.getTick(nextCell), AnalyticsBuilder.MAXIMUM_DEPTH) - nextTick, ticksLeft);
			}
		}


		try {
			CalculationScore bestCalculationScore = new CalculationScore(rate / (nextTick - startPlanTick + 1), tail.length() > 0 ? Integer.MAX_VALUE : risk);

			for (Direction nextDirection : node.directions()) {
				CalculationScore calculationScore = estimate(nextTick, nextCell, node.getChildNode(nextDirection), tail, nb, sb, ticksLeft);

				if (risk != 0 || rate != 0) {
					calculationScore = new CalculationScore(calculationScore.getScore() + rate / (nextTick - startPlanTick + 1), max(risk, calculationScore.getRisk()));
				}

				if (calculationScore.getRisk() < bestCalculationScore.getRisk()) {
					bestCalculationScore = calculationScore;
				} else if (calculationScore.getRisk() == bestCalculationScore.getRisk() && calculationScore.getScore() > bestCalculationScore.getScore()) {
					bestCalculationScore = calculationScore;
				}

			}
			return bestCalculationScore;
		} finally {
			if (territory.isNotTerritory(nextCell)) {
				tail.removeLast();
			}
		}

	}


	public CellDetails[] getCellDetailsMatrix() {
		return cellDetailsMatrix;
	}

	public MoveNode getRootNode() {
		return rootNode;
	}

	public void setRootNode(MoveNode rootNode) {
		this.rootNode = rootNode;
	}


	double pow2(double v) {
		return v * v;
	}
}
