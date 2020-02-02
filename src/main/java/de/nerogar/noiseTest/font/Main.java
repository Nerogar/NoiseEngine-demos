package de.nerogar.noiseTest.font;

import de.nerogar.noise.Noise;
import de.nerogar.noise.render.GLWindow;
import de.nerogar.noise.render.fontRenderer.Font;
import de.nerogar.noise.render.fontRenderer.FontRenderableString;
import de.nerogar.noise.util.*;
import org.lwjgl.opengl.GL11;

public class Main {

	private static Matrix4f projectionMatrix;

	private static Font[]                 fonts;
	private static FontRenderableString[] strings;

	private static final String testString = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam\n" +
			"nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam\n" +
			"erat, sed diam voluptua. At vero eos et accusam et justo duo dolores\n" +
			"et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est\n" +
			"Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur\n" +
			"sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et\n" +
			"dolore magna aliquyam erat, sed diam voluptua. At vero eos et\n" +
			"accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren,\n" +
			"no sea takimata sanctus est Lorem ipsum dolor sit amet.\n" +
			"iiiiiiiiii wwwwwwwwww iwiwiwiwiw";

	private static void generateStrings() {

		fonts = new Font[10];
		strings = new FontRenderableString[10];

		fonts[0] = new Font("calibri", 14);
		fonts[1] = new Font("impact", 14);
		fonts[2] = new Font("segoe print", 14);
		fonts[3] = new Font("segoe script", 14);
		fonts[4] = new Font("comic sans ms", 14);

		fonts[5] = new Font("calibri", 8);
		fonts[6] = new Font("calibri", 10);
		fonts[7] = new Font("calibri", 12);
		fonts[8] = new Font("calibri", 16);
		fonts[9] = new Font("calibri", 18);

		Color color = new Color(1.0f, 1.0f, 1.0f, 1.0f);

		for (int i = 0; i < strings.length; i++) {
			strings[i] = new FontRenderableString(fonts[i], testString, color, projectionMatrix, 1f, 1f);
		}
	}

	private static void render(GLWindow window) {
		strings[0].render(0, window.getHeight() - 20);
		strings[1].render(0, window.getHeight() - 220);
		strings[2].render(0, window.getHeight() - 420);
		strings[3].render(0, window.getHeight() - 620);
		strings[4].render(0, window.getHeight() - 820);

		strings[5].render(800, window.getHeight() - 20);
		strings[6].render(800, window.getHeight() - 180);
		strings[7].render(800, window.getHeight() - 380);
		strings[8].render(800, window.getHeight() - 580);
		strings[9].render(800, window.getHeight() - 800);
	}

	public static void main(String[] args) {
		// initialize NoiseEngine
		Logger.instance.addStream(Logger.DEBUG, System.out);
		Noise.init();

		// create the window
		GLWindow window = new GLWindow("Title", 800, 600, true, 0);

		// create a projection matrix
		projectionMatrix = Matrix4fUtils.getOrthographicProjection(0f, window.getWidth(), window.getHeight(), 0f, 1, -1);

		window.setSizeChangeListener((width, height) -> {
			Matrix4fUtils.setOrthographicProjection(projectionMatrix, 0f, window.getWidth(), window.getHeight(), 0f, 1, -1);
		});

		// create objects
		generateStrings();

		// main loop
		Timer timer = new Timer();
		while (!window.shouldClose()) {
			GLWindow.updateAll();
			timer.update(1f / 60f);

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			render(window);

			window.setTitle(String.format("FPS: %.2f frame time: %.4f", timer.getFrequency(), timer.getDelta()));
		}

		// clean up openGL objects
		for (Font font : fonts) {
			font.cleanup();
		}

		for (FontRenderableString string : strings) {
			string.cleanup();
		}

	}

}
