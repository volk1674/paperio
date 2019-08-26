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

	private boolean capturedRisk;
	private int startCalculationTick;
	private int startPlanTick = 1;
	private int oldTailLength;
	private int oldTailTick;
	private PlayerTerritory territory;
	private boolean startedOnOwnTerritory;
	private List<Player> others;
	private Player me;
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
		this.me = me;

		simpleTickMatrixBuilder = new SimpleTickMatrixBuilder(tick, bonusMap);
		AnalyticsBuilder analyticsBuilder = new AnalyticsBuilder(tick, bonusMap);
		for (Player player : others) {
			analyticsBuilder.build(player);
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

		CellDetails cellDetails = cellDetailsMatrix[cell.getIndex()];
		capturedRisk = cellDetails.capturedTick - tick < AnalyticsBuilder.MAXIMUM_DEPTH;

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

		if (ticksLeft < 0) {
			risk = -ticksLeft;
		}

		if (ccd.sawTick - startCalculationTick < AnalyticsBuilder.MAXIMUM_DEPTH) {
			if (ccd.sawTick >= tick && ccd.sawTick < nextTick) {
				risk += 12. / (1 + ccd.sawTick - startCalculationTick);
			}

			if (tail.length() > 0) {
				ticksLeft = min(ncd.sawTick - tick, ticksLeft);
			}

		}

		if (ncd.sawTick - startCalculationTick < AnalyticsBuilder.MAXIMUM_DEPTH) {
			if (ncd.sawTick > tick && ncd.sawTick <= nextTick) {
				risk += 12. / (1 + ccd.sawTick - startCalculationTick);
			}

			if (tail.length() > 0) {
				ticksLeft = min(ncd.sawTick - nextTick, ticksLeft);
			}
		}

		{ // расчет столкновения головами
			int ncdEnterTick = ncd.enterTick;
			int ncdLeaveTick = ncd.leaveTick;
			int ncdTick = ncd.tick;
			Direction ncdLeaveDirection = ncd.leaveDirection;
			int ncdLeaveTailLength = ncd.leaveTailLength;
			int ncdEnterTailLength = ncd.enterTailLength;

			int ccdTick = ccd.tick;
			int ccdEnterTick = ccd.enterTick;
			int ccdEnterTailLength = ccd.enterTailLength;

			if (ncdTick - startCalculationTick >= AnalyticsBuilder.MAXIMUM_DEPTH) {
				ncdTick = max(ncdTick, simpleTickMatrixBuilder.getTick(nextCell));
				ncdEnterTick = max(ncdEnterTick, ncdTick - 5);
				ncdLeaveTick = max(ncdLeaveTick, ncdTick + 6);
			}

			if (ccdTick - startCalculationTick >= AnalyticsBuilder.MAXIMUM_DEPTH) {
				ccdTick = max(ccdTick, simpleTickMatrixBuilder.getTick(currentCell));
				ccdEnterTick = max(ccdEnterTick, ccdTick - 5);
			}

			// находимся в клетке и надо проверить успеем ли выйти из клетки
			if (ccdEnterTick < nextTick && ccdEnterTick >= tick) {

				Direction enterDirection = null;
				Player cp = others.stream().filter(player -> player.getIndex() == ccd.playerIndex).findAny().orElse(null);
				if (cp != null) {
					int mx = Game.cell2point(currentCell.getX());
					int my = Game.cell2point(currentCell.getY());
					int ox = cp.getState().getX();
					int oy = cp.getState().getY();
					if (mx == ox && my > oy) {
						enterDirection = Direction.up;
					} else if (mx == ox && my < oy) {
						enterDirection = Direction.down;
					} else if (mx > ox && my == oy) {
						enterDirection = Direction.right;
					} else if (mx < ox && my == oy && node.getDirection() == Direction.left) {
						enterDirection = Direction.left;
					}
				}

				if (enterDirection != node.getDirection() || nextTick > ccdTick) {
					if (tail.length() < ccdEnterTailLength) {
						if (sb == 0) {
							rate += .1 / (nextTick - startCalculationTick);
						} else {
							rate -= .1 / (nextTick - startCalculationTick);
						}
					} else if (tail.length() == ncdLeaveTailLength) {
						// погибнем вместе
						risk++;
					} else {
						// наш хвост длинее :(
						if (startedOnOwnTerritory) {
							return MINIMAL_SCORE;
						} else {
							risk += 5;
						}
					}
				}
			}

			// соперник или уже в клетке или может войти в нее до того как я в нее полностью войду.
			// ncd.tick > ncd.leaveTick - может быть только для клеток в которых уже двигается соперник на начале расчета
			if ((ncdEnterTick < nextTick || tick == startCalculationTick && ncdTick > ncdLeaveTick) && ncdLeaveTick > tick) {
				if (ncdTick <= tick + 1 || tick == startCalculationTick && ncdTick > ncdLeaveTick) {
					// соперник уже полностью в клетке или уже пошел на выход и мы его догоняем
					if (ncdLeaveDirection != node.getDirection() || ncdLeaveTick > nextTick) {
						if (tail.length() < ncdLeaveTailLength) {
							// наш хвост короче и мы его победим :)
							if (sb == 0) {
								rate += .1 / (nextTick - startCalculationTick);
							} else {
								rate -= .1 / (nextTick - startCalculationTick);
							}
						} else if (tail.length() == ncdLeaveTailLength) {
							// погибнем вместе
							risk++;
						} else {
							// наш хвост длинее :(
							if (startedOnOwnTerritory) {
								return MINIMAL_SCORE;
							} else {
								risk += 5;
							}
						}
					}
				} else {
					// соперник только входит в клетку
					if (tail.length() < ncdEnterTailLength) {
						// наш хвост короче и мы его победим :)
						if (sb == 0) {
							rate += .1 / (nextTick - startCalculationTick);
						} else {
							rate -= .1 / (nextTick - startCalculationTick);
						}
					} else if (tail.length() == ncdEnterTailLength) {
						// погибнем вместе
						risk++;
					} else {
						// наш хвост длинее :(
						if (startedOnOwnTerritory) {
							return MINIMAL_SCORE;
						} else {
							risk += 5;
						}
					}
				}
			}
		}

		if ((ncd.capturedTick - startCalculationTick) < AnalyticsBuilder.MAXIMUM_DEPTH && otherPlayerTails.isTerritory(nextCell)) {
			if (nextTick <= ncd.capturedTick) {
				rate += 30;
			}
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
					rate -= bonus.getCells();
					break;
				case saw:
					rate += 5;
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
				risk += nextTick - ncd.capturedTick;
			}

			rate += capturedCells.size();
			for (Cell capturedCell : capturedCells) {
				if (otherPlayerTerritory.isTerritory(capturedCell)) {
					rate += 4;
				} else {
					rate -= sqrt(pow2(1. * abs(sizeX / 2. - capturedCell.getX()) / sizeX) +
							pow2(1. * abs(sizeY / 2. - capturedCell.getY()) / sizeY));
				}

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
					rate += 50;
				}

//				CellDetails captureCellDetail = cellDetailsMatrix[capturedCell.getIndex()];
//				if (captureCellDetail.captureTargetTick - startCalculationTick < AnalyticsBuilder.MAXIMUM_DEPTH && captureCellDetail.captureTargetTick > nextTick) {
//					rate += 10;
//				}
				CellDetails captureCellDetail = cellDetailsMatrix[capturedCell.getIndex()];
				if (captureCellDetail.captureTargetTick - startCalculationTick < AnalyticsBuilder.MAXIMUM_DEPTH) {
					rate += capturedRisk ? 100 : 1;
				}

				//надо давать очки за граничные клетки противника - снижаем риск окружения
				if (captureCellDetail.ownerIndex > 0) {
					if (capturedCell.isBorder()) {
						rate += capturedRisk ? 100 : .1;
					}

					boolean f = false;
					for (Cell capturedNeighbor : capturedCell.neighbors()) {
						CellDetails capturedNeighborCellDetails = cellDetailsMatrix[capturedNeighbor.getIndex()];
						if (capturedNeighborCellDetails.ownerIndex != captureCellDetail.ownerIndex && capturedNeighborCellDetails.ownerIndex != me.getIndex()) {
							f = true;
							break;
						}
					}
					if (f) {
						rate += capturedRisk ? 100 : .1;
					}
				}
			}

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


	private double pow2(double v) {
		return v * v;
	}
}
