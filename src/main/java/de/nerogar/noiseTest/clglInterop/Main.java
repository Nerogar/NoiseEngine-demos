package de.nerogar.noiseTest.clglInterop;

import de.nerogar.noise.Noise;
import de.nerogar.noise.opencl.*;
import de.nerogar.noise.render.*;
import de.nerogar.noise.util.Logger;
import de.nerogar.noise.util.Matrix4f;
import de.nerogar.noise.util.Matrix4fUtils;
import de.nerogar.noise.util.Timer;
import org.lwjgl.opengl.GL11;

import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public class Main {

	private static final int POINTS = 2_000_000;

	private static VertexBufferObject generatePoints() {
		float[] position = new float[POINTS * 2];
		float[] velocity = new float[POINTS * 2];

		Random rand = new Random();

		for (int i = 0; i < position.length; i++) {
			position[i] = rand.nextFloat();
			velocity[i] = 0;
		}

		return new VertexBufferObjectStandard(
				VertexBufferObject.POINTS,
				new int[] { 2, 2 }, // 2 for position, 2 for velocity
				position, velocity
		);
	}

	private static void loop(GLWindow window, VertexBufferObject vbo, Shader shader, CLKernel clKernel, CLBuffer clBuffer) {

		Random rand = new Random();

		float gravityCenterX = (float) (rand.nextGaussian() * 0.2f) + 0.5f;
		float gravityCenterY = (float) (rand.nextGaussian() * 0.2f) + 0.5f;
		float gravity = (float) (rand.nextGaussian() * 0.5f) + 0.5f;

		/*
		// mouse control
		gravityCenterX = window.getInputHandler().getCursorPosX() / window.getWidth();
		gravityCenterY = window.getInputHandler().getCursorPosY() / window.getHeight();
		gravity = 0;
		if (window.getInputHandler().isButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) gravity = 1;
		else if (window.getInputHandler().isButtonDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) gravity = -1;
		*/

		// update
		clKernel.setArgBuffer(0, clBuffer);
		clKernel.setArg2f(1, gravityCenterX, gravityCenterY);
		clKernel.setArg1f(2, gravity);
		clKernel.enqueueExecution(true);

		// render
		shader.activate();
		vbo.render();
		shader.deactivate();
	}

	public static void main(String[] args) {
		// initialize NoiseEngine
		Logger.instance.addStream(Logger.DEBUG, System.out);
		Noise.init();

		// create the window
		GLWindow window = new GLWindow("Title", 1000, 1000, true, 0, null, null);
		CLContext clContext = new CLContext(window.getGLContext());

		Matrix4f projectionMatrix = Matrix4fUtils.getOrthographicProjection(0, 1, 1, 0, 1, -1);

		VertexBufferObject vbo = generatePoints();
		CLBuffer clBuffer = new CLBuffer(clContext, vbo);

		Shader shader = ShaderLoader.loadShader("res/noiseTest/clglInterop/points.vert", "res/noiseTest/clglInterop/points.frag");
		CLProgram clProgram = CLLoader.loadCLProgram(clContext, "res/noiseTest/clglInterop/update.cl");
		CLKernel clKernel = new CLKernel(clProgram, "update", POINTS);

		// set the projection matrix as a uniform
		shader.activate();
		shader.setUniformMat4f("projectionMatrix", projectionMatrix.asBuffer());
		shader.deactivate();

		// set openGL blend mode
		glEnable(GL_BLEND);
		glDisable(GL_DEPTH_TEST);
		glBlendFunc(GL_ONE, GL_ONE);

		// main loop
		Timer timer = new Timer();
		while (!window.shouldClose()) {
			GLWindow.updateAll();
			timer.update(1f / 60f);

			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			loop(window, vbo, shader, clKernel, clBuffer);

			window.setTitle(String.format("FPS: %.2f frame time: %.4f calc time: %.4f", timer.getFrequency(), timer.getDelta(), timer.getCalcTime()));
		}

		// clean up openGL objects
		vbo.cleanup();
		shader.cleanup();

	}

}
