package strategy.model;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import strategy.Game;

import java.util.concurrent.ThreadLocalRandom;

import static strategy.Game.cell;

class TerritoryBitMaskTest {

	@Test
	void isOccupied() {
		TerritoryBitMask territory = new TerritoryBitMask();

		for (int i = 0; i < Game.sizeX; i++) {
			for (int j = 0; j < Game.sizeX; j++) {
				Assert.assertFalse(territory.isOccupied(cell(i, j)));
				territory.setOccupied(cell(i, j));
				Assert.assertTrue(territory.isOccupied(cell(i, j)));
				territory.setVoid(cell(i, j));
				Assert.assertFalse(territory.isOccupied(cell(i, j)));
				if (ThreadLocalRandom.current().nextBoolean()) territory.setOccupied(cell(i, j));
			}
		}

		TerritoryBitMask clone = new TerritoryBitMask(territory);
		for (int i = 0; i < Game.sizeX; i++) {
			for (int j = 0; j < Game.sizeX; j++) {
				Assert.assertEquals(territory.isOccupied(cell(i, j)), clone.isOccupied(cell(i, j)));
			}
		}

	}
}