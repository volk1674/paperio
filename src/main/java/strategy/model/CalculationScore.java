package strategy.model;

public class CalculationScore {
	public static final CalculationScore MINIMAL_SCORE = new CalculationScore(Integer.MIN_VALUE, Integer.MAX_VALUE);
	public static final CalculationScore ZERO_SCORE = new CalculationScore(0, 0);

	private double score;
	private double risk;

	public CalculationScore() {
	}

	public CalculationScore(double score, double risk) {
		this.score = score;
		this.risk = risk;
	}

	public double getScore() {
		return score;
	}

	public double getRisk() {
		return risk;
	}

	@Override
	public String toString() {
		return "{" + "score=" + score + ", risk=" + risk + '}';
	}
}
