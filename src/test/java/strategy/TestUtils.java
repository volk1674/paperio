package strategy;

import message.Direction;
import strategy.model.Player;
import strategy.model.PlayerState;
import strategy.model.PlayerTail;
import strategy.model.PlayerTerritory;

import static strategy.Game.cell;
import static strategy.Game.cell2point;

public class TestUtils {
	public static PlayerState createPlayerState() {
		PlayerTerritory playerTerritory = new PlayerTerritory();
		playerTerritory.addTerritory(cell(10, 10));
		playerTerritory.addTerritory(cell(9, 9));
		playerTerritory.addTerritory(cell(10, 9));
		playerTerritory.addTerritory(cell(9, 10));
		playerTerritory.addTerritory(cell(12, 15));


		PlayerTail tail = new PlayerTail();
		tail.addToTail(cell(8, 9));
		tail.addToTail(cell(8, 8));
		tail.addToTail(cell(8, 7));
		tail.addToTail(cell(8, 6));
		tail.addToTail(cell(8, 5));
		tail.addToTail(cell(8, 4));
		tail.addToTail(cell(9, 4));
		tail.addToTail(cell(10, 4));
		tail.addToTail(cell(11, 4));
		tail.addToTail(cell(12, 4));
		tail.addToTail(cell(13, 4));
		tail.addToTail(cell(14, 4));
		tail.addToTail(cell(15, 4));
		tail.addToTail(cell(15, 5));
		tail.addToTail(cell(15, 6));
		tail.addToTail(cell(15, 7));
		tail.addToTail(cell(14, 7));
		tail.addToTail(cell(13, 7));
		tail.addToTail(cell(13, 8));
		tail.addToTail(cell(13, 9));
		tail.addToTail(cell(12, 9));

		return new PlayerState(Direction.left, 0, cell2point(12), cell2point(9), playerTerritory, tail, 0, 0);
	}


	public static Player createPlayer() {
		Player player = new Player(1);
		player.setState(createPlayerState());
		return player;
	}
}
