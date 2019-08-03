package debug;

import com.google.gson.Gson;
import message.Direction;
import strategy.Game;
import strategy.Strategy;
import strategy.model.Cell;
import strategy.model.Move;
import strategy.model.MovePlan;
import strategy.model.Player;
import strategy.utils.MessagePlayer2PlayerConverter;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.awt.Image.SCALE_DEFAULT;

public class DebugWindow {
	private static Color[] playerColors = {Color.BLUE, Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.PINK, Color.YELLOW};

	private static MovePlan debugPlan;
	private static JFrame frame;
	private static DrawPanel drawPanel;
	private static JLabel timeToAnalise;
	private static JLabel tickLabel;
	private static MovePlan movePlan;
	private static int playerIndexToAnalise = 1;
	private static Player me;
	static Strategy strategy = new Strategy() {
		@Override
		protected void generatePlans(Set<Direction> directions, List<MovePlan> plans) {
			if (debugPlan == null) {
				super.generatePlans(directions, plans);
			} else {
				plans.add(debugPlan);
			}
		}
	};

	private static int tick = 1;
	private static VisioConfig visioConfig;
	private static List<Player> playerList = new ArrayList<>();
	private static DefaultListModel<String> movePlansModel = new DefaultListModel<>();

	private static void init() {
		javax.swing.SwingUtilities.invokeLater(() -> {
			try {
				createAndShowGUI();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private static ImageIcon createImageIcon(String name) throws IOException {
		return new ImageIcon(ImageIO.read(DebugWindow.class.getResourceAsStream(name)).getScaledInstance(16, 16, SCALE_DEFAULT));

	}

	private static void update() {
		playerList.clear();
		MessagePlayer2PlayerConverter playerConverter = new MessagePlayer2PlayerConverter();
		VisioConfig.Message tickMessage = null;
		if (visioConfig != null) {
			tickMessage = visioConfig.visio_info.get(tick);
			tickLabel.setText("Тик: " + tickMessage.tick_num);
			tickMessage.players.forEach((key, messagePlayer) -> {
				Player player = playerConverter.convert(key, messagePlayer);
				playerList.add(player);
			});
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
			tick += Game.width / me.getState().getSpeed();
			update();
		});


		JButton firstButton = new JButton(createImageIcon("/icons/first.png"));
		firstButton.addActionListener(e -> {
			tick -= Game.width / me.getState().getSpeed();
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
		buttonPanel.add(selectFileButton, BorderLayout.EAST);
		buttonPanel.add(tickLabel);

		JButton analiseButton = new JButton("Анализ");
		analiseButton.addActionListener(e -> {
			analise();
		});

		JPanel analisePanel2 = new JPanel();
		analisePanel2.setLayout(new BoxLayout(analisePanel2, 1));


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
			debugPlan = DebugWindow.strategy.getPlans().get(planList.getSelectedIndex());
			analise();
		});

		JButton cancelDebugPlanButton = new JButton("cancel");
		cancelDebugPlanButton.addActionListener(e -> {
			debugPlan = null;
			analise();
		});

		timeToAnalise = new JLabel();

		analisePanel.add(debugPlanButton);
		analisePanel.add(cancelDebugPlanButton);
		analisePanel.add(timeToAnalise);

		movePlansModel = new DefaultListModel<String>();
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
		movePlan = null;
		movePlansModel.clear();

		me = playerList.stream().filter(player -> player.getIndex() == playerIndexToAnalise).findFirst().orElse(null);
		List<Player> other = playerList.stream().filter(player -> player.getIndex() != playerIndexToAnalise).collect(Collectors.toList());
		if (me != null && !Game.isNotCellCenter(me.getState().getX(), me.getState().getY())) {

			long start = System.currentTimeMillis();
			strategy.calculate(tick, me, other, Collections.emptyMap());
			long end = System.currentTimeMillis();
			timeToAnalise.setText(String.valueOf(end - start));

			movePlan = strategy.getPlans().stream().findFirst().orElse(null);
			strategy.getPlans().forEach(mp -> movePlansModel.addElement(mp.toString()));
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

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			pain(g);
		}

		private void pain(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;

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
						if (player.getState().getPlayerTerritory().get(cell)) {
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
					g.fillRect(cellLeft + 2, cellTop + 2, cellSize - 4, cellSize - 4);

					g.setColor(borderColor);
					g2.setStroke(new BasicStroke(2));
					g.drawRect(cellLeft + 2, cellTop + 2, cellSize - 4, cellSize - 4);
				}
			}

			g2.setStroke(new BasicStroke(2));
			for (Player player : playerList) {
				int x = boardLeft + (player.getState().getX() - Game.width / 2) * cellSize / Game.width;
				int y = dim.height - boardTop - (player.getState().getY() - Game.width / 2) * cellSize / Game.width - cellSize;
				g.setColor(playerColors[player.getIndex()]);
				g.drawRect(x + 1, y + 1, cellSize - 2, cellSize - 2);
			}


			if (movePlan != null && me != null) {
				g.setColor(Color.black);
				Cell cell = Game.point2cell(me.getState().getX(), me.getState().getY());
				for (Move move : movePlan.getMoves()) {
					for (int i = 0; i < move.getCells() && cell != null; i++) {
						cell = cell.getNeighborsMap().get(move.getDirection());
						if (cell != null) {
							int centerX = boardLeft + cell.getX() * cellSize + cellSize / 2;
							int centerY = dim.height - boardTop - cellSize * (cell.getY() + 1) + cellSize / 2;
							g.drawOval(centerX, centerY, 4, 4);
						}
					}
				}
			}
		}
	}


}
