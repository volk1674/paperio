package strategy;

import message.Direction;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;


@SuppressWarnings({"unused", "WeakerAccess"})
public class MoveNode {
	public static int count = 0;

	private final Map<Direction, MoveNode> nodes = new EnumMap<>(Direction.class);
	private final Direction direction;

	public MoveNode() {
		this.direction = null;
		init(this, Game.MAX_PLAN_LENGTH);
	}

	public MoveNode(Direction direction) {
		this.direction = direction;
		count++;
	}

	public Map<Direction, MoveNode> getNodes() {
		return nodes;
	}

	public Set<Direction> directions() {
		return nodes.keySet();
	}

	public MoveNode getChildNode(Direction direction) {
		return nodes.get(direction);
	}

	public MoveNode createPath(Direction direction, int length) {
		MoveNode node = this;
		for (int i = 0; i < length; i++) {
			MoveNode next = node.nodes.get(direction);
			if (next == null) {
				next = new MoveNode(direction);
				node.nodes.put(direction, next);
			}
			node = next;
		}
		return node;
	}

	public Direction getDirection() {
		return direction;
	}


	@Override
	public String toString() {
		return "MoveNode{" +
				"direction=" + direction +
				'}';
	}

	public static void init(MoveNode root, int planLength) {
		for (Direction d1 : Direction.values()) {
			for (int m1 = 1; m1 <= planLength; m1++) {
				MoveNode node1 = root.createPath(d1, m1);
				for (Direction d2 : Direction.values()) {
					if (d2 == d1 || d2 == d1.opposite()) continue;
					for (int m2 = 1; m2 <= planLength - m1; m2++) {
						MoveNode node2 = node1.createPath(d2, m2);
						for (Direction d3 : Direction.values()) {
							if (d3 == d2 || d3 == d2.opposite()) continue;
							for (int m3 = 1; m3 <= planLength - m1 - m2; m3++) {
								MoveNode node3 = node2.createPath(d3, m3);
								int m4 = planLength - m1 - m2 - m3;
								if (m4 > 0) {
									for (Direction d4 : Direction.values()) {
										if (d4 == d3 || d4 == d3.opposite()) continue;
										node3.createPath(d4, m4);
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
