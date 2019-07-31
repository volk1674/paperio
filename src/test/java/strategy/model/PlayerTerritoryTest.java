package strategy.model;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import strategy.Game;

import java.util.concurrent.ThreadLocalRandom;

import static strategy.Game.cell;

class PlayerTerritoryTest {

	@Test
	void isOccupied() {
		PlayerTerritory playerTerritory = new PlayerTerritory();

		for (int i = 0; i < Game.sizeX; i++) {
			for (int j = 0; j < Game.sizeX; j++) {
				Assert.assertFalse(playerTerritory.get(cell(i, j)));
				playerTerritory.set(cell(i, j));
				Assert.assertTrue(playerTerritory.get(cell(i, j)));
				playerTerritory.clear(cell(i, j));
				Assert.assertFalse(playerTerritory.get(cell(i, j)));
				if (ThreadLocalRandom.current().nextBoolean()) playerTerritory.set(cell(i, j));
			}
		}

		PlayerTerritory clone = new PlayerTerritory(playerTerritory);
		for (int i = 0; i < Game.sizeX; i++) {
			for (int j = 0; j < Game.sizeX; j++) {
				Assert.assertEquals(playerTerritory.get(cell(i, j)), clone.get(cell(i, j)));
			}
		}

	}
}