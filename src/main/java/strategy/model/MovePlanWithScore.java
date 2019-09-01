package strategy.model;

import java.util.Arrays;

/**
 * Класс содержит план движения бота и его оценку.
 * Использовался в начальных версиях стратегии.
 *
 * @see strategy.SimpleStrategy
 * @see Move
 */
public class MovePlanWithScore implements Comparable<MovePlanWithScore> {
	// план движения бота
	private final Move[] moves;
	// оценка плана
	private double score;
	private double risk;

	public MovePlanWithScore(Move... moves) {
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
		return Arrays.toString(moves) + " : " + score + " : " + risk;
	}

	@Override
	public int compareTo(MovePlanWithScore other) {
		int result = -Double.compare(this.risk, other.risk);
		if (result == 0) {
			result = Double.compare(this.score, other.score);
		}
		return result;
	}

	/**
	 * Некоторые из планов движения бота могут частично повторять уже ранее обсчитанные планы.
	 * Т.к. расчет ведется до первого события: захватили территорию или убедились что захватить территорию невозможно
	 * (например, вышли за границы или наступили сами себе на хвост) то можно исключить из рассмотрения планы,
	 * приводящие к уже рассмотренному результату.
	 * <p>
	 * Функция проверяет, является ли текущий план частью другого плана (полностью совпадает с началом).
	 *
	 * @param other другой план
	 * @return true если текущий план полностью совпадает с other
	 */
	public boolean isPartOf(MovePlanWithScore other) {
		if (this.moves.length > other.moves.length) {
			return false;
		}

		for (int i = 0; i < moves.length - 1; i++) {
			Move thisMove = moves[i];
			Move otherMove = other.moves[i];
			if (thisMove.getDirection() != otherMove.getDirection() || thisMove.getLength() != otherMove.getLength()) {
				return false;
			}
		}

		Move thisMove = moves[this.moves.length - 1];
		Move otherMove = other.moves[this.moves.length - 1];

		return thisMove.getDirection() == otherMove.getDirection() && thisMove.getLength() <= otherMove.getLength();
	}
}
