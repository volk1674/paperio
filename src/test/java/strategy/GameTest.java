package strategy;

import message.Direction;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import strategy.model.Cell;
import strategy.model.PlayerState;
import strategy.model.TerritoryBitMask;

import java.util.List;

import static strategy.Game.cell;
import static strategy.Game.sizeX;
import static strategy.Game.sizeY;
import static strategy.Game.width;

class GameTest {

	@Test
	void capture() {
		TerritoryBitMask territory = new TerritoryBitMask();
		territory.setOccupied(cell(10, 10));
		territory.setOccupied(cell(9, 9));
		territory.setOccupied(cell(10, 9));
		territory.setOccupied(cell(9, 10));

		TerritoryBitMask lines = new TerritoryBitMask();
		lines.setOccupied(cell(8, 9));
		lines.setOccupied(cell(8, 8));
		lines.setOccupied(cell(8, 7));
		lines.setOccupied(cell(8, 6));
		lines.setOccupied(cell(8, 5));
		lines.setOccupied(cell(8, 4));
		lines.setOccupied(cell(9, 4));
		lines.setOccupied(cell(10, 4));
		lines.setOccupied(cell(11, 4));
		lines.setOccupied(cell(12, 4));
		lines.setOccupied(cell(13, 4));
		lines.setOccupied(cell(14, 4));
		lines.setOccupied(cell(15, 4));
		lines.setOccupied(cell(15, 5));
		lines.setOccupied(cell(15, 6));
		lines.setOccupied(cell(15, 7));
		lines.setOccupied(cell(14, 7));
		lines.setOccupied(cell(13, 7));
		lines.setOccupied(cell(13, 8));
		lines.setOccupied(cell(13, 9));
		lines.setOccupied(cell(12, 9));
		lines.setOccupied(cell(11, 9));

		PlayerState playerState = new PlayerState(
				Direction.down,
				0,
				Game.width * 10 + Game.width / 2,
				Game.width * 10 + Game.width / 2,
				territory,
				lines,
				0,
				0);

		List<Cell> captured = Game.capture(playerState);
		Assert.assertEquals(42, captured.size());
	}

	@Test
	void buildTimeMatrix() {
		int[][] result = new int[sizeX][sizeY];
		for (int i = 0; i < sizeX; i++) {
			for (int j = 0; j < sizeY; j++) {
				result[i][j] = Integer.MAX_VALUE;
			}
		}

		TerritoryBitMask territory = new TerritoryBitMask();
		territory.setOccupied(cell(10, 10));
		territory.setOccupied(cell(9, 9));
		territory.setOccupied(cell(10, 9));
		territory.setOccupied(cell(9, 10));

		TerritoryBitMask lines = new TerritoryBitMask();
		lines.setOccupied(cell(8, 9));
		lines.setOccupied(cell(8, 8));
		lines.setOccupied(cell(8, 7));
		lines.setOccupied(cell(8, 6));
		lines.setOccupied(cell(8, 5));
		lines.setOccupied(cell(8, 4));
		lines.setOccupied(cell(9, 4));
		lines.setOccupied(cell(10, 4));
		lines.setOccupied(cell(11, 4));
		lines.setOccupied(cell(12, 4));
		lines.setOccupied(cell(13, 4));
		lines.setOccupied(cell(14, 4));
		lines.setOccupied(cell(15, 4));
		lines.setOccupied(cell(15, 5));
		lines.setOccupied(cell(15, 6));
		lines.setOccupied(cell(15, 7));
		lines.setOccupied(cell(14, 7));
		lines.setOccupied(cell(13, 7));
		lines.setOccupied(cell(13, 8));
		lines.setOccupied(cell(13, 9));
		lines.setOccupied(cell(12, 9));

		Game.buildTimeMatrix(1, 0, 0, Direction.up, 11 * width + width / 2, 9 * width + width / 2, result);
		Assert.assertEquals(1 + 7 * (width / 5), result[12][3]);
	}
}