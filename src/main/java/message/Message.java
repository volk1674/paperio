package message;

import java.util.Map;

public class Message {
	public static class PlayerBonus {
		public BonusType type;
		public int ticks;
	}

	public static class Bonus {
		public BonusType type;
		public int[] position;
		public int active_ticks;

		public BonusType getType() {
			return type;
		}
	}

	public static class Player {
		public int score;
		public Direction direction;
		public int[][] territory;
		public int[][] lines;
		public int[] position;
		public PlayerBonus[] bonuses;
	}

	public static class Params {
		public int x_cells_count;
		public int y_cells_count;
		public int speed;
		public int width;

		public Map<String, Player> players;
		public Bonus[] bonuses;
		public int tick_num;
	}

	public String type;
	public Params params;
}
