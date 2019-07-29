package message;

public enum Direction {
	left(2), up(3), right(0), down(1);

	private final int opposite;

	Direction(int opposite) {
		this.opposite = opposite;
	}

	public Direction opposite() {
		return values()[opposite];
	}

	public boolean isOpposite(Direction direction) {
		return opposite() == direction;
	}
}
