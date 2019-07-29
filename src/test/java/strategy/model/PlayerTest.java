package strategy.model;

import message.Direction;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import strategy.Game;

import java.util.List;

class PlayerTest {


	@Test
	void getPossibleDirections() {
		TerritoryBitMask territory = new TerritoryBitMask();
		TerritoryBitMask lines = new TerritoryBitMask();
		territory.setOccupied(Game.point2cell(15,45));

		Player player = new Player(1);
		player.setState(new PlayerState(Direction.left, 0, 15, 45, territory, lines, 0, 0));

		List<Direction> directions = player.getPossibleDirections();

		Assert.assertEquals(2, directions.size());
		Assert.assertTrue(directions.contains(Direction.up));
		Assert.assertTrue(directions.contains(Direction.down));

		player.move(Direction.down);
		directions = player.getPossibleDirections();
		Assert.assertEquals(1, directions.size());
		Assert.assertTrue(directions.contains(Direction.right));
		Assert.assertEquals(1, lines.getOccupiedCount());

		player.move(Direction.right);
		directions = player.getPossibleDirections();
		Assert.assertEquals(2, directions.size());
		Assert.assertTrue(directions.contains(Direction.up));
		Assert.assertTrue(directions.contains(Direction.right));
		Assert.assertEquals(2, lines.getOccupiedCount());

		player.move(Direction.up);
		directions = player.getPossibleDirections();
		Assert.assertEquals(3, directions.size());
		Assert.assertTrue(directions.contains(Direction.up));
		Assert.assertTrue(directions.contains(Direction.left));
		Assert.assertTrue(directions.contains(Direction.right));
		Assert.assertEquals(3, lines.getOccupiedCount());

		player.move(Direction.left);
		directions = player.getPossibleDirections();
		Assert.assertEquals(2, directions.size());
		Assert.assertTrue(directions.contains(Direction.up));
		Assert.assertTrue(directions.contains(Direction.down));
		Assert.assertEquals(0, lines.getOccupiedCount());
		Assert.assertEquals(4, territory.getOccupiedCount());

	}
}