package strategy;

import message.BonusType;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import strategy.model.Bonus;
import strategy.model.Cell;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static strategy.Game.cell;

class AnalyticsBuilderTest {

	@Test
	void build1() {
		int n = 20;
		while (n > 0) {
			long start = System.currentTimeMillis();
			AnalyticsBuilder analyticsBuilder = new AnalyticsBuilder(1, Collections.emptyMap());
			analyticsBuilder.build(TestUtils.createPlayerState());
			System.out.println(System.currentTimeMillis() - start);

			Assert.assertEquals(13, analyticsBuilder.getCellDetails(cell(9, 5)).capturedTick);
			Assert.assertEquals(13, analyticsBuilder.getCellDetails(cell(10, 4)).capturedTick);
			Assert.assertEquals(43, analyticsBuilder.getCellDetails(cell(10, 4)).tick);
			//Assert.assertEquals(55, analyticsBuilder.getCellDetails(cell(11, 3)).tick);

			Assert.assertEquals(7, analyticsBuilder.getCellDetails(cell(12, 10)).tick);
			Assert.assertEquals(13, analyticsBuilder.getCellDetails(cell(12, 11)).tick);
			Assert.assertEquals(31, analyticsBuilder.getCellDetails(cell(12, 14)).tick);
			//Assert.assertEquals(49, analyticsBuilder.getCellDetails(cell(10, 15)).tick);

			n--;
		}
	}

	@Test
	void build2() {
		int n = 20;

		Map<Cell, Bonus> bonusMap = new HashMap<>();
		bonusMap.put(cell(12, 11), new Bonus(BonusType.n, 50));

		while (n > 0) {
			long start = System.currentTimeMillis();
			AnalyticsBuilder analyticsBuilder = new AnalyticsBuilder(1, bonusMap);
			analyticsBuilder.build(TestUtils.createPlayerState());

			Assert.assertEquals(8, analyticsBuilder.getCellDetails(cell(12, 11)).enterTick);
			Assert.assertEquals(13, analyticsBuilder.getCellDetails(cell(12, 11)).tick);
			Assert.assertEquals(18, analyticsBuilder.getCellDetails(cell(12, 11)).leaveTick);


			Assert.assertEquals(18, analyticsBuilder.getCellDetails(cell(12, 12)).tick);
			Assert.assertEquals(23, analyticsBuilder.getCellDetails(cell(12, 13)).tick);
			Assert.assertEquals(28, analyticsBuilder.getCellDetails(cell(12, 14)).tick);

			Assert.assertEquals(33, analyticsBuilder.getCellDetails(cell(12, 15)).tick);
			Assert.assertEquals(38, analyticsBuilder.getCellDetails(cell(11, 15)).tick);
			Assert.assertEquals(43, analyticsBuilder.getCellDetails(cell(10, 15)).tick);
			System.out.println(System.currentTimeMillis() - start);

			n--;
		}
	}
}