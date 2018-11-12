package com.Julian.flappy;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;

import javax.swing.JFrame;

import com.Julian.flappy.entities.Pipe;
import com.Julian.flappy.entities.Player;
import com.Julian.flappy.gfx.Colors;
import com.Julian.flappy.gfx.Font;
import com.Julian.flappy.gfx.Screen;
import com.Julian.flappy.gfx.SpriteSheet;
import com.Julian.flappy.level.Level;

/**
 * Flappy Bird 05/28/18
 * 
 * @author Julian Abhari
 */

// A canvas is a blank Java workspace which includes all the JFrames,
// JComponents and whatnot
public class Game extends Canvas implements Runnable {

	private static final long serialVersionUID = 1L;

	public static final int WIDTH = 160;
	public static final int HEIGHT = WIDTH / 12 * 9;
	public static final int SCALE = 4;
	public static final String NAME = "Flappy Bird";

	// The JFrame is a writable area that we can put stuff on
	// It's part of a window
	private JFrame frame;

	public boolean running = false;
	public static int tickCount = 0;

	private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	// This is representing how many pixels are inside the BufferedImage
	// We can update this pixels variable with whatever we want and it will update
	// that image
	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
	// This will contain information of the 4 colors of pixels within the tiles
	public int[] colors = new int[6 * 6 * 6];

	private Screen screen;
	public static InputHandler input;

	public static Level level;
	public static Player player;
	public ArrayList<Pipe> pipes = new ArrayList<Pipe>();

	public int PIPE_GAP = 25;
	public int score;

	public Game() {
		// Calling the Canvas functions that will keep the size of the JFrame or canvas
		// at one size
		setMinimumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		setMaximumSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));
		setPreferredSize(new Dimension(WIDTH * SCALE, HEIGHT * SCALE));

		frame = new JFrame(NAME);
		// This terminates the program when the user clicks the red exit button
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// This is a way of laying out the JFrame
		frame.setLayout(new BorderLayout());

		// This is centering and adding the canvas to the JFrame
		frame.add(this, BorderLayout.CENTER);
		// This sets the frame so the sizes are at the preferred size. It keeps
		// everything sized correctly.
		frame.pack();

		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void init() {
		int index = 0;
		for (int r = 0; r < 6; r += 1) {
			for (int g = 0; g < 6; g += 1) {
				for (int b = 0; b < 6; b += 1) {
					// This is the red shade from 0-5
					int rr = (r * 255 / 5);
					// This is the green shade from 0-5
					int gg = (g * 255 / 5);
					// This is the blue shade from 0-5
					int bb = (b * 255 / 5);
					// This populates the color array with color values.
					colors[index++] = rr << 16 | gg << 8 | bb;
				}
			}
		}

		screen = new Screen(WIDTH, HEIGHT, new SpriteSheet("/SpriteSheet.png"));
		input = new InputHandler(this);
		level = new Level("/Levels/flapWorld.png");
		player = new Player(level, 16, (level.height / 2) * 8, input);
		level.addEntity(player);
		for (int i = 0; i < 3; i += 1) {
			pipes.add(new Pipe(level, 300 + (i * 100),
					(int) JMath.map((float) Math.random(), 0, 1, 0, (level.height * 8) - PIPE_GAP), 1, PIPE_GAP));
			level.addEntity(pipes.get(i));
		}
		score = 0;
	}

	public synchronized void start() {
		running = true;
		// The Thread is an instance of Runnable
		// When the thread has started it's going to run the run function which has the
		// main game loop
		// This is creating a new thread so that we don't take away from the system's
		// main thread and we can just start a side thread which allows us to multitask
		new Thread(this).start();
	}

	public synchronized void stop() {
		running = false;
	}

	public void run() {
		// We want to limit the run loop to only 60 updates a second because otherwise
		// the game would run faster on other machines

		// This gets the current time in nanoseconds
		long lastTime = System.nanoTime();
		// This is going to come out with how many nanoseconds are in one tick
		double nsPerTick = 1000000000.0 / 60.0;

		int ticks = 0;
		int frames = 0;

		long lastTimer = System.currentTimeMillis();
		// Delta is how many nano-seconds have gone by so far. Once we hit 1 we will
		// subtract 1 from it
		double delta = 0;

		init();

		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / nsPerTick;
			lastTime = now;
			boolean shouldRender = true;

			while (delta >= 1) {
				ticks += 1;
				tick();
				delta -= 1;
				shouldRender = true;
			}

			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (shouldRender) {
				frames += 1;
				render();
			}

			if (System.currentTimeMillis() - lastTimer >= 1000) {
				lastTimer += 1000;
				System.out.println("Ticks: " + ticks + ", Frames: " + frames);
				frames = 0;
				ticks = 0;
			}
		}
	}

	// This updates the game, it updates the internal variables and the logic of the
	// game
	public void tick() {
		tickCount += 1;
		level.tick();

		for (int i = 0; i < pipes.size(); i += 1) {
			if (pipes.get(i).didFinish) {
				level.removeEntity(pipes.get(i));
				pipes.add(new Pipe(level, pipes.get(i).x + ((pipes.size() - i) * 100),
						(int) JMath.map((float) Math.random(), 0, 1, 0, (level.height * 8) - PIPE_GAP), 1,
						PIPE_GAP));
				level.addEntity(pipes.get(pipes.size() - 1));
				pipes.remove(i);
			}
		}
		
		if (pipes.get(0).x == player.x) {
			score += 1;
		}
		
		if (player.hasCrashed(pipes.get(0).getBottomPipeY(), pipes.get(0).getTopPipeY(), pipes.get(0).x)) {
			restart();
		}
	}

	public void restart() {
		for (int i = 0; i < level.entities.size(); i += 1) {
			level.entities.remove(i);
		}
		pipes = new ArrayList<Pipe>();
		init();
	}

	// This is created from the games logic
	public void render() {
		// This is an object to organize the data on this canvas.
		// It allows us to organize what we're putting on here.
		BufferStrategy bs = getBufferStrategy();
		// If the bufferStrategy is null then we're going to create one
		if (bs == null) {
			// This is tripple buffering which should be good at reducing tearing (where you
			// can see the process when I clear the image and replace it)
			createBufferStrategy(3);
			return;
		}

		// These are the coordinates of the center of the screen
		int xOffset = player.x - (screen.width / 2);
		int yOffset = player.y - (screen.height / 2);

		level.renderTiles(screen, xOffset, yOffset);

		level.renderEntities(screen);
		
		Font.render("" + score, screen, (((screen.width) / 2) + 8) - (("" + score).length() * 8), 8, Colors.get(-1, -1, -1, 555), 1);
		
		for (int y = 0; y < screen.height; y++) {
			for (int x = 0; x < screen.width; x++) {
				int ColourCode = screen.pixels[x + y * screen.width];
				if (ColourCode < 255) {
					pixels[x + y * WIDTH] = colors[ColourCode];
				}
			}
		}

		Graphics g = bs.getDrawGraphics();
		// This is drawing the BufferedImage image
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		// This will free up the memory and resources of the graphics
		g.dispose();
		// This shows the contents of the buffer
		bs.show();
	}

	public static void main(String[] args) {
		new Game().start();
	}

}
