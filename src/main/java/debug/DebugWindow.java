package debug;

import com.google.gson.Gson;
import message.Direction;
import strategy.BestStrategy;
import strategy.Game;
import strategy.MoveNode;
import strategy.model.Bonus;
import strategy.model.CalculationScore;
import strategy.model.Cell;
import strategy.model.Player;
import strategy.model.PlayerTail;
import strategy.utils.MessagePlayer2PlayerConverter;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.awt.Image.SCALE_DEFAULT;
import static java.util.Arrays.stream;
import static strategy.Game.point2cell;

public class DebugWindow {
	private static Color[] playerColors = {
			new Color(0x82D91A00, true),
			new Color(0x8A008F1B, true),
			new Color(0xE5B00002, true),
			new Color(0xA5B006),
			new Color(0x8E0319C0, true),
			new Color(0xB601AB),
			new Color(0x0088B0)};

	private static MessagePlayer2PlayerConverter playerConverter = new MessagePlayer2PlayerConverter();
	private static JFrame frame;
	private static JTextField debugPlanField;
	private static DrawPanel drawPanel;
	private static JLabel timeToAnalise;
	private static JLabel tickLabel;
	private static int playerIndexToAnalise = 1;
	private static Player me;


	private static String planString(Collection<Direction> directions) {
		StringBuilder buffer = new StringBuilder();
		if (directions != null)
			for (Direction direction : directions) {
				switch (direction) {
					case right:
						buffer.append('r');
						break;
					case left:
						buffer.append('l');
						break;
					case up:
						buffer.append('u');
						break;
					case down:
						buffer.append('d');
						break;
				}
			}
		return buffer.toString();
	}


	private static BestStrategy strategy = new BestStrategy() {
		ArrayDeque<Direction> directions = new ArrayDeque<>();
		List<Direction> bestPlan;
		double bestScore = Integer.MIN_VALUE;
		double bestRisk = Integer.MAX_VALUE;


		@Override
		public void debug(Direction direction, CalculationScore score) {

			movePlansModel.addElement(planString(bestPlan));
			movePlansModel.addElement(direction.name() + " " + score.toString());
			movePlansModel.addElement("");
			bestScore = Integer.MIN_VALUE;
			bestRisk = Integer.MAX_VALUE;
		}

		boolean f = false;

		@Override
		protected CalculationScore estimate(int tick, Cell currentCell, MoveNode node, PlayerTail tail, int nb, int sb, int ticksLeft) {
			directions.addLast(node.getDirection());
			f = true;
			CalculationScore result = super.estimate(tick, currentCell, node, tail, nb, sb, ticksLeft);
			if (f) {
				if (result.getRisk() < bestRisk) {
					bestPlan = new ArrayList<>(directions);
					bestRisk = result.getRisk();
					bestScore = result.getScore();
				} else if (result.getRisk() == bestRisk && result.getScore() > bestScore) {
					bestPlan = new ArrayList<>(directions);
					bestRisk = result.getRisk();
					bestScore = result.getScore();
				}
			}
			f = false;
			directions.removeLast();
			return result;
		}
	};

	private static int tick = 1;
	private static VisioConfig visioConfig;
	private static VisioConfig.Message tickMessage;
	private static Map<Cell, Bonus> bonusMap;

	private static List<Player> playerList = new ArrayList<>();
	private static MyListModel<String> movePlansModel = new MyListModel<>();

	private static void init() {
		javax.swing.SwingUtilities.invokeLater(() -> {
			try {
				createAndShowGUI();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private static Image createImage(String name) {
		try {
			return ImageIO.read(DebugWindow.class.getResourceAsStream(name)).getScaledInstance(16, 16, SCALE_DEFAULT);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static ImageIcon createImageIcon(String name) {
		try {
			return new ImageIcon(ImageIO.read(DebugWindow.class.getResourceAsStream(name)).getScaledInstance(16, 16, SCALE_DEFAULT));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void update() {
		playerList.clear();

		if (visioConfig != null && visioConfig.visio_info != null && !visioConfig.visio_info.isEmpty()) {

			if (tick >= visioConfig.visio_info.size()) {
				tick = visioConfig.visio_info.size() - 1;
			}
			tickLabel.setText("Тик: " + tick);

			tickMessage = visioConfig.visio_info.get(tick);
			if (tickMessage != null) {
				tickMessage.players.forEach((key, messagePlayer) -> {
					Player player = playerConverter.convert(key, messagePlayer);
					playerList.add(player);

					if (tick > 1) {
						VisioConfig.Message prevTickMessage = visioConfig.visio_info.get(tick - 1);
						int px = prevTickMessage.players.get(key).position[0];
						int py = prevTickMessage.players.get(key).position[1];
						if (player.getState().getX() > px) {
							player.getState().setDirection(Direction.right);
						} else if (player.getState().getX() < px) {
							player.getState().setDirection(Direction.left);
						} else if (player.getState().getY() < py) {
							player.getState().setDirection(Direction.down);
						} else if (player.getState().getY() > py) {
							player.getState().setDirection(Direction.up);
						} else {
							player.getState().setDirection(null);
						}
					}
				});

				bonusMap = stream(tickMessage.bonuses)
						.collect(Collectors.toMap(bonus -> point2cell(bonus.position[0], bonus.position[1]), bonus -> new Bonus(bonus.type, bonus.active_ticks)));
			}
		}

		me = playerList.stream().filter(player -> player.getIndex() == playerIndexToAnalise).findFirst().orElse(null);
		analise();
	}

	private static void createAndShowGUI() throws IOException {
		frame = new JFrame("Отладка");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(1000, 800));
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);


		JButton nextButton = new JButton(createImageIcon("/icons/next.png"));
		nextButton.addActionListener(e -> {
			tick++;
			update();
		});

		JButton prevButton = new JButton(createImageIcon("/icons/prev.png"));
		prevButton.addActionListener(e -> {
			tick--;
			update();
		});

		JButton lastButton = new JButton(createImageIcon("/icons/last.png"));
		lastButton.addActionListener(e -> {
			int speed = (me != null) ? me.getState().getSpeed() : Game.speed;
			tick += Game.width / speed;
			update();
		});


		JButton lastButton2 = new JButton(createImageIcon("/icons/last.png"));
		lastButton2.addActionListener(e -> {
			int speed = (me != null) ? me.getState().getSpeed() : Game.speed;
			tick += 10 * Game.width / speed;
			update();
		});


		JButton firstButton = new JButton(createImageIcon("/icons/first.png"));
		firstButton.addActionListener(e -> {
			int speed = (me != null) ? me.getState().getSpeed() : Game.speed;
			tick -= Game.width / speed;
			update();
		});

		JButton selectFileButton = new JButton(createImageIcon("/icons/file.png"));
		selectFileButton.addActionListener(e -> {
			final JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory(new File("../"));
			fc.setFileFilter(new FileNameExtensionFilter("visio.json", "json"));
			if (fc.showOpenDialog(selectFileButton) == JFileChooser.APPROVE_OPTION) {
				openFile(fc.getSelectedFile());
			}
		});

		tickLabel = new JLabel("Тик: " + tick);

		JPanel buttonPanel = new JPanel();
		frame.getContentPane().add(buttonPanel, BorderLayout.NORTH);
		buttonPanel.add(firstButton);
		buttonPanel.add(prevButton);
		buttonPanel.add(nextButton);
		buttonPanel.add(lastButton);
		buttonPanel.add(lastButton2);
		buttonPanel.add(selectFileButton, BorderLayout.EAST);
		buttonPanel.add(tickLabel);

		JButton analiseButton = new JButton("Анализ");
		analiseButton.addActionListener(e -> {
			analise();
		});

		JPanel analisePanel2 = new JPanel();
		analisePanel2.setLayout(new BoxLayout(analisePanel2, 1));

		debugPlanField = new JTextField();
		analisePanel2.add(debugPlanField);


		JPanel analisePanel = new JPanel();

		analisePanel.add(analiseButton);

		JPanel colorPanel = new JPanel();
		colorPanel.setBackground(playerColors[playerIndexToAnalise]);
		JSpinner jSpinner = new JSpinner();
		jSpinner.setValue(playerIndexToAnalise);
		jSpinner.addChangeListener(e -> {
			playerIndexToAnalise = (Integer) jSpinner.getValue();
			if (playerIndexToAnalise < 1) {
				playerIndexToAnalise = 1;
			}
			if (playerIndexToAnalise > 6) {
				playerIndexToAnalise = 6;
			}
			jSpinner.setValue(playerIndexToAnalise);
			colorPanel.setBackground(playerColors[playerIndexToAnalise]);
			analise();
		});
		analisePanel.add(jSpinner);
		analisePanel.add(colorPanel);
		analisePanel2.add(analisePanel);


		JList<String> planList = new JList<>();


		JButton debugPlanButton = new JButton("debug");
		debugPlanButton.addActionListener(e -> {

		});

		JButton cancelDebugPlanButton = new JButton("cancel");
		cancelDebugPlanButton.addActionListener(e -> {
			debugPlanField.setText("");
			analise();
		});

		timeToAnalise = new JLabel();

		analisePanel.add(debugPlanButton);
		analisePanel.add(cancelDebugPlanButton);
		analisePanel.add(timeToAnalise);


		planList.setModel(movePlansModel);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(planList);
		analisePanel2.add(scrollPane);
		scrollPane.setPreferredSize(new Dimension(400, 800));


		frame.getContentPane().add(analisePanel2, BorderLayout.WEST);
		drawPanel = new DrawPanel();
		frame.getContentPane().add(drawPanel);

		frame.pack();
		frame.setVisible(true);
	}

	private static void analise() {

		movePlansModel.clear();
		String debugPlan = debugPlanField.getText();
		if (debugPlan != null && !debugPlan.isEmpty()) {
			MoveNode rootNode = new MoveNode(null);
			MoveNode currNode = rootNode;
			for (char c : debugPlan.toCharArray()) {
				switch (c) {
					case 'u':
						currNode = currNode.createPath(Direction.up, 1);
						break;
					case 'd':
						currNode = currNode.createPath(Direction.down, 1);
						break;
					case 'l':
						currNode = currNode.createPath(Direction.left, 1);
						break;
					case 'r':
						currNode = currNode.createPath(Direction.right, 1);
						break;
					default:
						throw new IllegalArgumentException("Unkonow plan");
				}
			}
			strategy.setRootNode(rootNode);
		} else {
			strategy.setRootNode(new MoveNode());
		}

		movePlansModel.clear();
		me = playerList.stream().filter(player -> player.getIndex() == playerIndexToAnalise).findFirst().orElse(null);
		List<Player> other = playerList.stream().filter(player -> player.getIndex() != playerIndexToAnalise).collect(Collectors.toList());

		if (me != null && !Game.isNotCellCenter(me.getState().getX(), me.getState().getY())) {
			long start = System.currentTimeMillis();
			strategy.calculate(tick, me, other, bonusMap);
			long end = System.currentTimeMillis();
			timeToAnalise.setText(String.valueOf(end - start));
		}
		frame.repaint();
	}

	public static void main(String[] args) {
		init();
	}

	private static void openFile(File file) {
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			try {
				Gson gson = new Gson();
				visioConfig = gson.fromJson(reader, VisioConfig.class);
				update();
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}


	static class DrawPanel extends JPanel {

		static Image nImage = DebugWindow.createImage("/icons/flash.png");
		static Image sImage = DebugWindow.createImage("/icons/explorer.png");
		static Image sawImage = DebugWindow.createImage("/icons/saw.png");

		Color invertColor(Color color) {
			double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;
			return y >= 128 ? Color.BLACK : Color.WHITE;
		}


		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			pain(g);
		}

		private void pain(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setFont(new Font("TimesRoman", Font.PLAIN, 10));

			Dimension dim = getSize();
			int cellSize = Math.min((dim.height - 10) / Game.sizeY, (dim.width - 10) / Game.sizeX);
			int boardLeft = (dim.width - cellSize * Game.sizeX) / 2;
			int boardTop = (dim.height - cellSize * Game.sizeY) / 2;

			for (int i = 0; i < Game.sizeX; i++) {
				for (int j = 0; j < Game.sizeY; j++) {
					Cell cell = Game.cell(i, j);

					Color backgroundColor = Color.WHITE;
					Color borderColor = Color.WHITE;

					for (Player player : playerList) {
						if (player.getState().getPlayerTerritory().isTerritory(cell)) {
							backgroundColor = playerColors[player.getIndex()];
						}

						if (player.getState().getTail().isTail(cell)) {
							borderColor = playerColors[player.getIndex()];
						}
					}

					int cellLeft = boardLeft + i * cellSize;
					int cellTop = dim.height - boardTop - cellSize * (j + 1);
					int cellRight = cellLeft + cellSize - 2;
					int cellBottom = cellTop + cellSize - 2;

					g.setColor(Color.BLACK);
					g2.setStroke(new BasicStroke(1));
					g.drawRect(cellLeft, cellTop, cellSize, cellSize);

					g.setColor(backgroundColor);
					g.fillRect(cellLeft + 2, cellTop + 2, cellSize - 3, cellSize - 3);


					g.setColor(borderColor);
					g2.setStroke(new BasicStroke(4));
					g.drawRect(cellLeft + 3, cellTop + 3, cellSize - 5, cellSize - 5);


					int timeLimit = 0;
					int capturedTick = 0;
					if (strategy.getCellDetailsMatrix() != null) {
						timeLimit = strategy.getCellDetailsMatrix()[cell.getIndex()].tick - tick;
						capturedTick = strategy.getCellDetailsMatrix()[cell.getIndex()].capturedTick - tick;
					}
					g.setColor(Color.BLACK);

					g.setColor(invertColor(backgroundColor));
					g.drawString(String.valueOf(timeLimit), cellLeft + 8, cellTop + 12);
					g.drawString(String.valueOf(capturedTick), cellLeft + 8, cellTop + 22);

					if (bonusMap != null) {
						Bonus bonus = bonusMap.get(cell);
						if (bonus != null) {
							switch (bonus.getBonusType()) {
								case saw:
									g2.drawImage(sawImage, cellLeft + 8, cellTop + 16, null);
									break;
								case n:
									g2.drawImage(nImage, cellLeft + 8, cellTop + 16, null);
									break;
								case s:
									g2.drawImage(sImage, cellLeft + 8, cellTop + 16, null);
									break;
							}
						}
					}
				}
			}

			g2.setStroke(new BasicStroke(4));
			for (Player player : playerList) {
				int x = boardLeft + (player.getState().getX() - Game.width / 2) * cellSize / Game.width;
				int y = dim.height - boardTop - (player.getState().getY() - Game.width / 2) * cellSize / Game.width - cellSize;
				g.setColor(playerColors[player.getIndex()]);
				g.drawRect(x + 1, y + 1, cellSize - 1, cellSize - 1);
			}


//			if (movePlanWithScore != null && me != null) {
//				g.setColor(Color.black);
//				Cell cell = Game.point2cell(me.getState().getX(), me.getState().getY());
//				for (Move move : movePlanWithScore.getMoves()) {
//					for (int i = 0; i < move.getLength() && cell != null; i++) {
//						cell = cell.nextCell(move.getDirection());
//						if (cell != null) {
//							int centerX = boardLeft + cell.getX() * cellSize + cellSize / 2;
//							int centerY = dim.height - boardTop - cellSize * (cell.getY() + 1) + cellSize / 2;
//							g.drawOval(centerX, centerY, 4, 4);
//						}
//					}
//				}
//			}
		}
	}


}
