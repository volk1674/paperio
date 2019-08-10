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
	private int nb;
	private int sb;

	public PlayerState(Direction direction, int score, int x, int y, PlayerTerritory playerTerritory, PlayerTail tail, int nb, int sb) {
		this.direction = direction;
		this.score = score;
		this.x = x;
		this.y = y;
		this.playerTerritory = playerTerritory;
		this.tail = tail;
		this.nb = nb;
		this.sb = sb;
	}

	public PlayerState(PlayerState other) {
		direction = other.direction;
		score = other.score;
		x = other.x;
		y = other.y;
		playerTerritory = new PlayerTerritory(other.playerTerritory);
		tail = new PlayerTail(other.tail);
		nb = other.nb;
		sb = other.sb;
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

	public int getNb() {
		return nb;
	}

	public int getSb() {
		return sb;
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


	public void setNb(int nb) {
		this.nb = nb;
	}

	public void setSb(int sb) {
		this.sb = sb;
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
		if (nb > 0) nb--;
		if (sb > 0) sb--;
	}

	public int getSpeed() {
		return calculateSpeed(nb, sb);
	}


	public void addNitro(int cells) {
		nb += cells;
	}

	public void  addSlow(int cells) {
		sb += cells;
	}
}
