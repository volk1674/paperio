package strategy.model;

import java.util.Arrays;

public class MovePlan {
	private final Move[] moves;
	private double score;

	public MovePlan(Move... moves) {
		this.moves = moves;
	}

	public Move[] getMoves() {
		return moves;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return "MovePlan{" + Arrays.toString(moves) + ": " + score + '}';
	}
}
