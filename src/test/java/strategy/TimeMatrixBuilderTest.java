package strategy;

import message.Direction;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import strategy.model.PlayerState;
import strategy.model.PlayerTail;
import strategy.model.PlayerTerritory;

import java.util.Collections;

import static strategy.Game.cell;
import static strategy.Game.width;

class TimeMatrixBuilderTest {

	private PlayerState createPlayerState() {
		PlayerTerritory playerTerritory = new PlayerTerritory();
		playerTerritory.set(cell(10, 10));
		playerTerritory.set(cell(9, 9));
		playerTerritory.set(cell(10, 9));
		playerTerritory.set(cell(9, 10));
		playerTerritory.set(cell(12, 15));


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

		return new PlayerState(Direction.up, 0, 12 * width + width / 2, 9 * width + width / 2, playerTerritory, tail, 0, 0);
	}


	@Test
	void buildTimeMatrix() {
		int n = 20;
		while (n > 0) {
			long start = System.currentTimeMillis();
			TimeMatrixBuilder timeMatrixBuilder = new TimeMatrixBuilder();
			timeMatrixBuilder.buildTimeMatrix(1, createPlayerState(), Collections.emptyMap());
			System.out.println(System.currentTimeMillis() - start);


			Assert.assertEquals(49, timeMatrixBuilder.getTickMatrix()[cell(10, 3).getIndex()]);
			Assert.assertEquals(0, timeMatrixBuilder.getTailMatrix()[cell(10, 3).getIndex()]);
			Assert.assertEquals(1, timeMatrixBuilder.getTailMatrix()[cell(10, 2).getIndex()]);
			Assert.assertEquals(22, timeMatrixBuilder.getTailMatrix()[cell(11, 9).getIndex()]);
			Assert.assertEquals(13, timeMatrixBuilder.getTickMatrix()[cell(10, 9).getIndex()]);
			Assert.assertEquals(23, timeMatrixBuilder.getTailMatrix()[cell(10, 9).getIndex()]);

			Assert.assertEquals(19, timeMatrixBuilder.getTickMatrix()[cell(10, 10).getIndex()]);
			Assert.assertEquals(0, timeMatrixBuilder.getTailMatrix()[cell(10, 10).getIndex()]);
			Assert.assertEquals(0, timeMatrixBuilder.getTailMatrix()[cell(9, 10).getIndex()]);
			Assert.assertEquals(0, timeMatrixBuilder.getTailMatrix()[cell(9, 9).getIndex()]);
			Assert.assertEquals(73, timeMatrixBuilder.getTickMatrix()[cell(16, 3).getIndex()]);
			Assert.assertEquals(13, timeMatrixBuilder.getTickMatrix()[cell(11, 4).getIndex()]);

			Assert.assertEquals(1, timeMatrixBuilder.getTailMatrix()[cell(10, 2).getIndex()]);
			Assert.assertEquals(33, timeMatrixBuilder.getTailMatrix()[cell(16, 3).getIndex()]);
			Assert.assertEquals(27, timeMatrixBuilder.getTailMatrix()[cell(12, 15).getIndex()]);
			Assert.assertEquals(0, timeMatrixBuilder.getTailMatrix()[cell(12, 16).getIndex()]);
			Assert.assertEquals(0, timeMatrixBuilder.getTailMatrix()[cell(13, 15).getIndex()]);
			Assert.assertEquals(1, timeMatrixBuilder.getTailMatrix()[cell(14, 15).getIndex()]);

			n--;
		}


	}
}