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

class SimpleTickMatrixBuilderTest {

	@Test
	void build1() {
		int n = 20;
		while (n > 0) {
			long start = System.currentTimeMillis();
			SimpleTickMatrixBuilder simpleTickMatrixBuilder = new SimpleTickMatrixBuilder(1, Collections.emptyMap());
			simpleTickMatrixBuilder.build(TestUtils.createPlayerState());
			Assert.assertEquals(31, simpleTickMatrixBuilder.getTick(cell(12, 14)));
			Assert.assertEquals(49, simpleTickMatrixBuilder.getTick(cell(10, 15)));
			System.out.println(System.currentTimeMillis() - start);
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
			SimpleTickMatrixBuilder simpleTickMatrixBuilder = new SimpleTickMatrixBuilder(1, bonusMap);
			simpleTickMatrixBuilder.build(TestUtils.createPlayerState());
			Assert.assertEquals(28, simpleTickMatrixBuilder.getTick(cell(12, 14)));
			Assert.assertEquals(33, simpleTickMatrixBuilder.getTick(cell(12, 15)));
			Assert.assertEquals(38, simpleTickMatrixBuilder.getTick(cell(11, 15)));
			Assert.assertEquals(43, simpleTickMatrixBuilder.getTick(cell(10, 15)));
			System.out.println(System.currentTimeMillis() - start);
			n--;
		}
	}
}