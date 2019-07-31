package strategy.model;

import message.Direction;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import strategy.Game;

import java.util.Set;

class PlayerTest {


	@Test
	void getPossibleDirections() {
		PlayerTerritory playerTerritory = new PlayerTerritory();
		PlayerTail tail = new PlayerTail();
		playerTerritory.set(Game.point2cell(15, 45));

		Player player = new Player(1);
		player.setState(new PlayerState(Direction.left, 0, 15, 45, playerTerritory, tail, 0, 0));

		Set<Direction> directions = player.getPossibleDirections();

		Assert.assertEquals(2, directions.size());
		Assert.assertTrue(directions.contains(Direction.up));
		Assert.assertTrue(directions.contains(Direction.down));

		player.move(Direction.down);
		directions = player.getPossibleDirections();
		Assert.assertEquals(1, directions.size());
		Assert.assertTrue(directions.contains(Direction.right));
		Assert.assertEquals(1, tail.length());

		player.move(Direction.right);
		directions = player.getPossibleDirections();
		Assert.assertEquals(2, directions.size());
		Assert.assertTrue(directions.contains(Direction.up));
		Assert.assertTrue(directions.contains(Direction.right));
		Assert.assertEquals(2, tail.length());

		player.move(Direction.up);
		directions = player.getPossibleDirections();
		Assert.assertEquals(3, directions.size());
		Assert.assertTrue(directions.contains(Direction.up));
		Assert.assertTrue(directions.contains(Direction.left));
		Assert.assertTrue(directions.contains(Direction.right));
		Assert.assertEquals(3, tail.length());

		player.move(Direction.left);
		directions = player.getPossibleDirections();
		Assert.assertEquals(2, directions.size());
		Assert.assertTrue(directions.contains(Direction.up));
		Assert.assertTrue(directions.contains(Direction.down));
		Assert.assertEquals(0, tail.length());
		Assert.assertEquals(4, playerTerritory.getSize());

	}
}