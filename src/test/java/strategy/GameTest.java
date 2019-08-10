package strategy;

import message.Direction;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import strategy.model.Cell;
import strategy.model.PlayerState;
import strategy.model.PlayerTail;
import strategy.model.PlayerTerritory;

import java.util.Arrays;
import java.util.List;

import static strategy.Game.cell;
import static strategy.Game.sizeX;
import static strategy.Game.sizeY;
import static strategy.Game.width;

class GameTest {

	@Test
	void capture() {
		PlayerTerritory playerTerritory = new PlayerTerritory();
		playerTerritory.set(cell(10, 10));
		playerTerritory.set(cell(9, 9));
		playerTerritory.set(cell(10, 9));
		playerTerritory.set(cell(9, 10));

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
		tail.addToTail(cell(11, 9));

		PlayerState playerState = new PlayerState(
				Direction.down,
				0,
				Game.width * 10 + Game.width / 2,
				Game.width * 10 + Game.width / 2,
				playerTerritory,
				tail,
				0,
				0);

		List<Cell> captured = Game.capture(playerState);
		Assert.assertEquals(42, captured.size());
	}

	@Test
	void buildTimeMatrix() {
		int[] result = new int[sizeX * sizeY];
		Arrays.fill(result, Integer.MAX_VALUE);

		PlayerTerritory playerTerritory = new PlayerTerritory();
		playerTerritory.set(cell(10, 10));
		playerTerritory.set(cell(9, 9));
		playerTerritory.set(cell(10, 9));
		playerTerritory.set(cell(9, 10));

		PlayerTerritory lines = new PlayerTerritory();
		lines.set(cell(8, 9));
		lines.set(cell(8, 8));
		lines.set(cell(8, 7));
		lines.set(cell(8, 6));
		lines.set(cell(8, 5));
		lines.set(cell(8, 4));
		lines.set(cell(9, 4));
		lines.set(cell(10, 4));
		lines.set(cell(11, 4));
		lines.set(cell(12, 4));
		lines.set(cell(13, 4));
		lines.set(cell(14, 4));
		lines.set(cell(15, 4));
		lines.set(cell(15, 5));
		lines.set(cell(15, 6));
		lines.set(cell(15, 7));
		lines.set(cell(14, 7));
		lines.set(cell(13, 7));
		lines.set(cell(13, 8));
		lines.set(cell(13, 9));
		lines.set(cell(12, 9));

//		Game.buildTimeMatrix(1, 0, 0, Direction.up, 11 * width + width / 2, 9 * width + width / 2, result);
//		Game.buildTimeMatrix(1, 3, 0, Direction.left, 12 * width + width / 2, 0 * width + width / 2, result);
//		Assert.assertEquals(1 + 3 * (width / 6), result[cell(12, 3).getIndex()]);
	}




}