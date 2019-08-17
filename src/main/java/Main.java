import com.google.gson.Gson;
import message.Direction;
import message.Message;
import strategy.BestStrategy;
import strategy.Game;
import strategy.SimpleStrategy;
import strategy.Strategy;
import strategy.model.Bonus;
import strategy.model.Cell;
import strategy.model.Player;
import strategy.utils.MessagePlayer2PlayerConverter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static strategy.Game.point2cell;

public class Main {

	public static void main(String args[]) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));
		MessagePlayer2PlayerConverter playerConverter = new MessagePlayer2PlayerConverter();
		//Strategy simpleStrategy = new SimpleStrategy();
		Strategy simpleStrategy = new BestStrategy();

		String line = "";
		try {
			final EnumMap<Direction, String> commands = new EnumMap<>(Direction.class);
			for (Direction command : Direction.values()) {
				commands.put(command, String.format("{\"command\": \"%s\"}\n", command.name()));
			}

			Gson gson = new Gson();
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			while (true) {
				line = reader.readLine();
				Message message = gson.fromJson(line, Message.class);
				if (message == null || message.type.equals("end_game")) {
					break;
				} else if (message.type.equals("start_game")) {
					Game.init(
							message.params.x_cells_count,
							message.params.y_cells_count,
							message.params.width,
							message.params.speed
					);
				} else if (message.type.equals("tick")) {

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

					Map<Cell, Bonus> bonusMap = stream(message.params.bonuses)
							.collect(Collectors.toMap(bonus -> point2cell(bonus.position[0], bonus.position[1]), bonus -> new Bonus(bonus.type, bonus.active_ticks)));

					out.print(commands.get(simpleStrategy.calculate(message.params.tick_num, me, others, bonusMap)));
					out.flush();
				}
			}
		} catch (Exception ex) {
			FileOutputStream fs = new FileOutputStream("error.log");
			PrintWriter writer = new PrintWriter(fs);
			writer.println(line);
			writer.println();
			ex.printStackTrace(writer);
			writer.close();
		}
	}
}


//import java.util.Scanner;
//import java.util.Random;
//
//public class Main {
//	public static String getRandom(String[] array) {
//		int rnd = new Random().nextInt(array.length);
//		return array[rnd];
//	}
//
//	public static void main(String args[]) {
//		String[] commands = {"left", "right", "up", "down"};
//		Scanner scanner = new Scanner(System.in);
//		while (true) {
//			String input = scanner.nextLine();
//			String command = Main.getRandom(commands);
//			System.out.printf("{\"command\": \"%s\"}\n", command);
//		}
//	}
//}
