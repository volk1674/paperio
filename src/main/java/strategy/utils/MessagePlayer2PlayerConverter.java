package strategy.utils;

import message.BonusType;
import message.Message;
import strategy.model.Player;
import strategy.model.PlayerState;
import strategy.model.TerritoryBitMask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static strategy.Game.cell;
import static strategy.Game.point2cell;

public class MessagePlayer2PlayerConverter {
	private int index = 1;
	private Map<String, Integer> messageKey2Index = new HashMap<>();

	private int getIndex(String id) {
		return messageKey2Index.computeIfAbsent(id, s -> index++);
	}

	public Player convert(String messageKey, Message.Player messagePlayer) {
		Player result = new Player(getIndex(messageKey));

		TerritoryBitMask territory = new TerritoryBitMask();
		Arrays.stream(messagePlayer.territory).map(ints -> point2cell(ints[0], ints[1])).forEach(territory::setOccupied);
		TerritoryBitMask lines = new TerritoryBitMask();
		Arrays.stream(messagePlayer.lines).map(ints -> point2cell(ints[0], ints[1])).forEach(lines::setOccupied);

		int nitroCells = 0;
		int slowCells = 0;
		for (Message.PlayerBonus playerBonus : messagePlayer.bonuses) {
			if (playerBonus.type == BonusType.n) {
				nitroCells += playerBonus.ticks;
			} else if (playerBonus.type == BonusType.s) {
				slowCells += playerBonus.ticks;
			}
		}

		PlayerState playerState = new PlayerState(
				messagePlayer.direction,
				messagePlayer.score,
				messagePlayer.position[0],
				messagePlayer.position[1],
				territory,
				lines,
				nitroCells,
				slowCells
		);

		result.setState(playerState);

		return result;
	}

}
