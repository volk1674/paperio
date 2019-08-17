package strategy.model;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import strategy.Game;

import java.util.concurrent.ThreadLocalRandom;

import static strategy.Game.cell;

class PlayerTerritoryTest {

	@Test
	void test1() {
		PlayerTerritory playerTerritory = new PlayerTerritory();

		for (int i = 0; i < Game.sizeX; i++) {
			for (int j = 0; j < Game.sizeX; j++) {
				Assert.assertFalse(playerTerritory.isTerritory(cell(i, j)));
				playerTerritory.addTerritory(cell(i, j));
				Assert.assertTrue(playerTerritory.isTerritory(cell(i, j)));
				playerTerritory.clear(cell(i, j));
				Assert.assertFalse(playerTerritory.isTerritory(cell(i, j)));
				if (ThreadLocalRandom.current().nextBoolean()) playerTerritory.addTerritory(cell(i, j));
			}
		}

		PlayerTerritory clone = new PlayerTerritory(playerTerritory);
		for (int i = 0; i < Game.sizeX; i++) {
			for (int j = 0; j < Game.sizeX; j++) {
				Assert.assertEquals(playerTerritory.isTerritory(cell(i, j)), clone.isTerritory(cell(i, j)));
			}
		}
	}

	@Test
	void test2() {
		PlayerTerritory playerTerritory = new PlayerTerritory();
		Cell cell_17x0 = cell(17,0);
		Cell cell_16x30 = cell(16, 30);

		playerTerritory.addTerritory(cell_17x0);
		Assert.assertTrue(playerTerritory.isTerritory(cell_17x0));
		Assert.assertFalse(playerTerritory.isTerritory(cell_16x30));
	}
}