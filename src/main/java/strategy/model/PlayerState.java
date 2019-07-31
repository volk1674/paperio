package strategy.model;

import message.Direction;
import strategy.Game;

import static strategy.Game.calculateSpeed;


@SuppressWarnings("WeakerAccess")
public class PlayerState {
	private Direction direction;
	private int score;
	private int x;
	private int y;
	private PlayerTerritory playerTerritory;
	private PlayerTail tail;
	private int nitroCells;
	private int slowCells;

	public PlayerState(Direction direction, int score, int x, int y, PlayerTerritory playerTerritory, PlayerTail tail, int nitroCells, int slowCells) {
		this.direction = direction;
		this.score = score;
		this.x = x;
		this.y = y;
		this.playerTerritory = playerTerritory;
		this.tail = tail;
		this.nitroCells = nitroCells;
		this.slowCells = slowCells;
	}

	public PlayerState(PlayerState other) {
		direction = other.direction;
		score = other.score;
		x = other.x;
		y = other.y;
		playerTerritory = new PlayerTerritory(other.playerTerritory);
		tail = new PlayerTail(other.tail);
		nitroCells = other.nitroCells;
		slowCells = other.slowCells;
	}

	public Direction getDirection() {
		return direction;
	}

	public int getScore() {
		return score;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public PlayerTerritory getPlayerTerritory() {
		return playerTerritory;
	}

	public PlayerTail getTail() {
		return tail;
	}

	public int getNitroCells() {
		return nitroCells;
	}

	public int getSlowCells() {
		return slowCells;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setPlayerTerritory(PlayerTerritory playerTerritory) {
		this.playerTerritory = playerTerritory;
	}


	public void setNitroCells(int nitroCells) {
		this.nitroCells = nitroCells;
	}

	public void setSlowCells(int slowCells) {
		this.slowCells = slowCells;
	}

	public void moveTickDown() {
		this.y -= getSpeed();
	}

	public void moveDown() {
		this.y = this.y - Game.width;
	}

	public void moveTickUp() {
		this.y += getSpeed();
	}

	public void moveUp() {
		this.y = this.y + Game.width;
	}

	public void moveTickLeft() {
		this.x -= getSpeed();
	}

	public void moveLeft() {
		this.x = this.x - Game.width;
	}

	public void moveTickRight() {
		this.x += getSpeed();
	}

	public void moveRight() {
		this.x = this.x + Game.width;
	}

	public void useBonuses() {
		if (nitroCells > 0) nitroCells--;
		if (slowCells > 0) slowCells--;
	}

	public int getSpeed() {
		return calculateSpeed(nitroCells, slowCells);
	}


}
