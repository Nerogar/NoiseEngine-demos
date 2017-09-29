package de.nerogar.noiseTest.triangle;

import de.nerogar.noise.Noise;
import de.nerogar.noise.render.*;
import de.nerogar.noise.util.Logger;
import de.nerogar.noise.util.Matrix4f;
import de.nerogar.noise.util.Matrix4fUtils;
import de.nerogar.noise.util.Timer;
import org.lwjgl.opengl.GL11;

public class Main {

	private static VertexBufferObject generateTriangle() {
		return new VertexBufferObjectIndexed(
				new int[] { 2, 3 }, // 2 for position, 3 for color
				3,
				3,
				new int[] { 0, 1, 2 },
				new float[] { // position array
						0.5f, 0.8f, // top
						0.2f, 0.2f, // left
						0.8f, 0.2f  // right
				},
				new float[] { // color array
						1.0f, 0.0f, 0.0f, // top
						0.0f, 1.0f, 0.0f, // left
						0.0f, 0.0f, 1.0f  // right
				}
		);
	}

	public static void main(String[] args) {
		// initialize NoiseEngine
		Logger.instance.addStream(Logger.DEBUG, System.out);
		Noise.init();

		// create the window
		GLWindow window = new GLWindow("Title", 800, 600, true, 0, null, null);

		// create a projection matrix
		Matrix4f projectionMatrix = Matrix4fUtils.getOrthographicProjection(0, 1, 1, 0, 1, -1);

		// create openGL objects
		VertexBufferObject vbo = generateTriangle();
		Shader shader = ShaderLoader.loadShader("res/noiseTest/triangle/triangle.vert", "res/noiseTest/triangle/triangle.frag");

		shader.activate();
		shader.setUniformMat4f("projectionMatrix", projectionMatrix.asBuffer());

		// main loop
		Timer timer = new Timer();
		while (!window.shouldClose()) {
			GLWindow.updateAll();
			timer.update(1f / 60f);

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			vbo.render();

			window.setTitle(String.format("FPS: %.2f frame time: %.4f", timer.getFrequency(), timer.getDelta()));
		}

		shader.deactivate();

		// clean up openGL objects
		vbo.cleanup();
		shader.cleanup();

	}

}
