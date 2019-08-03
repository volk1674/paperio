package strategy.model;

import java.util.Arrays;

public class MovePlan implements Comparable<MovePlan> {
	private final Move[] moves;
	private double score;
	private double risk;

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

	public double getRisk() {
		return risk;
	}

	public void setRisk(double risk) {
		this.risk = risk;
	}

	@Override
	public String toString() {
		return  Arrays.toString(moves) + " : " + score + " : " + risk;
	}

	@Override
	public int compareTo(MovePlan other) {
		int result = -Double.compare(this.risk, other.risk);
		if (result == 0) {
			result = Double.compare(this.score, other.score);
		}
		return result;
	}
}
