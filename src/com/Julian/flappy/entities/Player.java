package com.Julian.flappy.entities;

import com.Julian.flappy.InputHandler;
import com.Julian.flappy.gfx.Colors;
import com.Julian.flappy.gfx.Screen;
import com.Julian.flappy.level.Level;

public class Player extends Mob {

	private InputHandler input;
	private int color = Colors.get(-1, 000, 411, 555);
	private int scale = 1;

	public Player(Level level, int x, int y, InputHandler input) {
		super(level, "Player", x, y, 1);
		this.input = input;
	}

	// This updates the game, it updates the internal variables and the logic of the
	// game
	public void tick() {
		int yDir = 0;

		if (input.up.isPressed()) {
			yDir -= 2;
		} 
		else {
			yDir += 1;
		}

		if (yDir != 0) {
			move(0, yDir);
			isMoving = true;
		} else {
			isMoving = false;
		}
	}

	public void render(Screen screen) {
		int xTile = 0;
		int yTile = 28;
		int walkingSpeed = 4;

		// When the player is facing towards the camera the x place for getting the Tile
		// pixels increases by 2 (because the player is 2 tiles wide)
		if (movingDir == 1) {
			xTile += 2;
		} else if (movingDir > 1) {
			xTile += 4 + ((numSteps >> walkingSpeed) & 1) * 2;
		}

		int modifier = 8 * scale;
		int xOffset = x - modifier / 2;
		int yOffset = y - modifier / 2 - 4;

		screen.render(xOffset + modifier - (modifier), yOffset, xTile + yTile * 32, color, false, false, scale);
		screen.render(xOffset + (modifier), yOffset, (xTile + 1) + yTile * 32, color, false, false, scale);

		screen.render(xOffset + modifier - (modifier), yOffset + modifier, xTile + (yTile + 1) * 32, color, false,
				false, scale);
		screen.render(xOffset + (modifier), yOffset + modifier, (xTile + 1) + (yTile + 1) * 32, color, false, false,
				scale);
	}

	public boolean hasCollided(int xDir, int yDir) {
		int xMin = 0;
		int xMax = 7;
		int yMin = -4;
		int yMax = 5;

		for (int x = xMin; x < xMax; x += 1) {
			if (isSolidTile(xDir, yDir, x, yMin)) {
				return true;
			}
		}
		for (int x = xMin; x < xMax; x += 1) {
			if (isSolidTile(xDir, yDir, x, yMax)) {
				return true;
			}
		}
		for (int y = yMin; y < yMax; y += 1) {
			if (isSolidTile(xDir, yDir, xMin, y)) {
				return true;
			}
		}
		for (int y = yMin; y < yMax; y += 1) {
			if (isSolidTile(xDir, yDir, xMax, y)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasCrashed(int bottomPipeY, int topPipeY, int pipeX) {
		if ((this.y + 9 > bottomPipeY || this.y - 9 < topPipeY) && (pipeX > this.x - 9 && pipeX < this.x + 9)) {
			return true;
		}
		return false;
	}

}
