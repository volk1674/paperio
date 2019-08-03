package strategy;

import com.google.gson.Gson;
import message.BonusType;
import message.Direction;
import message.Message;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import strategy.model.Cell;
import strategy.model.Move;
import strategy.model.MovePlan;
import strategy.model.Player;
import strategy.utils.MessagePlayer2PlayerConverter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static strategy.Game.point2cell;

class StrategyTest {

	@Test
	void calculate() {
		MessagePlayer2PlayerConverter playerConverter = new MessagePlayer2PlayerConverter();

		Gson gson = new Gson();
		BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/tick1.json")));
		Message message = gson.fromJson(reader, Message.class);

		Player me = null;
		List<Player> others = new ArrayList<>();
		for (Map.Entry<String, Message.Player> entry : message.params.players.entrySet()) {
			Player player = playerConverter.convert(entry.getKey(), entry.getValue());
			if (entry.getKey().equals("i")) {
				me = player;
			} else {
				others.add(player);
			}
		}

		Map<Cell, List<BonusType>> bonusTypeMap = stream(message.params.bonuses).collect(groupingBy(bonus -> point2cell(bonus.position[0], bonus.position[1]), HashMap::new, mapping(Message.Bonus::getType, toList())));

		Strategy strategy = new Strategy() {
//			@Override
//			protected void generatePlans(Set<Direction> directions, List<MovePlan> plans) {
//				s
//				plans.add(new MovePlan(new Move(Direction.left, 2), new Move(Direction.up, 1), new Move(Direction.right)));
//			}
		};

		Direction result = strategy.calculate(message.params.tick_num, me, others, bonusTypeMap);
		Assert.assertEquals(result, Direction.left);

	}
}