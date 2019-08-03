package debug;


import java.util.List;
import java.util.Map;

public class VisioConfig {

	List<Message> visio_info;

	static class Message {
		String type;
		int x_cells_count;
		int y_cells_count;
		int speed;
		int width;
		int tick_num;

		Map<String, message.Message.Player> players;
	}

}
