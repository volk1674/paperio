package strategy;

import message.Direction;
import strategy.model.Bonus;
import strategy.model.Cell;
import strategy.model.Player;

import java.util.List;
import java.util.Map;

public interface Strategy {

	Direction calculate(int tick, Player me, List<Player> others, Map<Cell, Bonus> bonusMap);

}
