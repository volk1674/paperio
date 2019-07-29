package strategy;

import message.BonusType;
import message.Direction;
import strategy.model.Cell;
import strategy.model.Player;
import strategy.model.PlayerState;
import strategy.model.TerritoryBitMask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static java.lang.Math.log;
import static java.lang.Math.sqrt;
import static strategy.Game.buildTimeMatrix;

@SuppressWarnings("FieldCanBeLocal")
public class Strategy {
	private static double EPSILON = 1e-6;
	private static long REQUEST_MAX_TIME = 4_000;
	private static long MAX_EXECUTION_TIME = 100_000;

	private CalculationNode rootNode = null;

	public Direction calculate(int tick, Player me, List<Player> others, Map<Cell, List<BonusType>> bonusTypeMap) {
		long startTime = System.currentTimeMillis();

		if (rootNode == null) {
			rootNode = new CalculationNode(me.getState().getDirection());
		}

		int[][] timeMatrix = new int[Game.sizeX][Game.sizeY];
		for (int i = 0; i < Game.sizeX; i++) {
			Arrays.fill(timeMatrix[i], Integer.MAX_VALUE);
		}

		TerritoryBitMask otherTerritory = new TerritoryBitMask();
		TerritoryBitMask otherLines = new TerritoryBitMask();

		for (Player player : others) {
			otherTerritory.add(player.getState().getTerritory());
			otherLines.add(player.getState().getLines());
			buildTimeMatrix(tick, player.getState().getNitroCells(), player.getState().getSlowCells(), player.getState().getDirection(), player.getState().getX(), player.getState().getY(), timeMatrix);
		}











		List<CalculationNode> visited = new ArrayList<>();
		while (System.currentTimeMillis() - startTime < 300) {
			int depth = 1;
			me.saveState();
			try {
				int currentTick = tick;
				CalculationNode currentNode = rootNode;
				visited.add(currentNode);
				while (!currentNode.isLeaf()) {
					currentNode = currentNode.select();
					visited.add(currentNode);
					currentTick += me.move(currentNode.direction);
					depth++;
				}

				currentNode.expand(me.getPossibleDirections());
				CalculationNode newNode = currentNode.select();
				double value = 0;
				if (newNode != null) {
					visited.add(newNode);
					value = estimateMove(depth, currentTick, newNode, me, otherTerritory, otherLines, timeMatrix);
				}
				for (CalculationNode node : visited) {
					node.updateStats(value);
				}

				if (value > 1) break;
			} finally {
				me.restoreState();
			}
			if (depth > 30) {
				break;
			}
		}

		rootNode = rootNode.select();
		if (rootNode != null) {
			return rootNode.direction;
		}

		return Direction.up;
	}


	private int calculateMoveScore(List<Cell> capturedCells, TerritoryBitMask otherTerritory) {
		int result = capturedCells.size();
		for (Cell cell : capturedCells) {
			if (otherTerritory.isOccupied(cell)) {
				result += 4;
			}
		}
		return result;
	}

	private double estimateMove(int depth, int currentTick, CalculationNode newNode, Player player, TerritoryBitMask otherTerritory, TerritoryBitMask otherLines, int[][] timeMatrix) {
		double result = EPSILON;
		PlayerState state = player.getState();
		if (state.getDirection() == newNode.direction) {
			result += EPSILON;
		}

		player.move(newNode.direction);

		Cell cell = Game.point2cell(state.getX(), state.getY());
		if (timeMatrix[cell.x][cell.y] < currentTick) {
			return 0;
		}

		result += calculateMoveScore(player.getCapturedCells(), otherTerritory) / log(depth);
		return result;
	}

	private static class CalculationNode {
		final Direction direction;
		int visits;
		double value;
		List<CalculationNode> children;

		CalculationNode(Direction direction) {
			this.direction = direction;
		}

		boolean isLeaf() {
			return children == null || children.isEmpty();
		}

		void expand(List<Direction> directions) {
			this.children = directions.stream().map(CalculationNode::new).collect(Collectors.toList());
		}

		void updateStats(double addValue) {
			visits++;
			value += addValue;
		}

		CalculationNode select() {
			Random random = ThreadLocalRandom.current();
			CalculationNode result = null;
			double bestValue = Double.MIN_VALUE;

			for (CalculationNode candidate : children) {
				double uctValue = candidate.value / (candidate.visits + EPSILON) + sqrt(log(visits + 1) / (candidate.visits + EPSILON)) + random.nextDouble() * EPSILON;
				if (uctValue >= bestValue) {
					result = candidate;
					bestValue = uctValue;
				}
			}
			return result;
		}

		@Override
		public String toString() {
			return direction.name();
		}
	}

}
