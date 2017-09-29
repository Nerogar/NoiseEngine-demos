package de.nerogar.noiseTest.window;

import de.nerogar.noise.Noise;
import de.nerogar.noise.render.GLWindow;
import de.nerogar.noise.util.Logger;
import de.nerogar.noise.util.Timer;

public class Main {

	public static void main(String[] args) {
		// initialize NoiseEngine
		Logger.instance.addStream(Logger.DEBUG, System.out);
		Noise.init();

		// create the window
		GLWindow window = new GLWindow("Title", 800, 600, true, 0, null, null);

		// main loop
		Timer timer = new Timer();
		while (!window.shouldClose()) {
			GLWindow.updateAll();
			timer.update(1f / 60f);

			window.setTitle(String.format("FPS: %.2f frame time: %.4f", timer.getFrequency(), timer.getDelta()));
		}

	}

}
