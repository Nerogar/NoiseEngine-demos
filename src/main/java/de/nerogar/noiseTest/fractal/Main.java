package de.nerogar.noiseTest.fractal;

import de.nerogar.noise.Noise;
import de.nerogar.noise.input.KeyboardKeyEvent;
import de.nerogar.noise.input.MouseButtonEvent;
import de.nerogar.noise.opencl.*;
import de.nerogar.noise.render.GLWindow;
import de.nerogar.noise.render.RenderHelper;
import de.nerogar.noise.render.Texture2D;
import de.nerogar.noise.util.Logger;
import de.nerogar.noise.util.Timer;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class Main {
	private static GLWindow window;

	private static CLContext clContext;
	private static CLProgram clProgram;
	private static CLKernel  clKernelBuddhaBrotNaive;
	private static CLKernel  clKernelBuddhaBrotImportance;
	private static CLKernel  clKernelBlitTexture;
	private static CLKernel  clKernelBlitMap;
	private static CLKernel  clKernelClearData;
	private static CLKernel  clKernelClearMap;

	private static CLProgram clProgramSum;
	private static CLKernel  clKernelLineSum;
	private static CLKernel  clKernelLineNorm;

	private static CLBuffer clRandomBuffer1;
	private static CLBuffer clRandomBuffer2;
	private static CLBuffer clImageBuffer;
	private static CLBuffer clBuddhaDataBuffer;
	private static CLBuffer clPropDataBuffer;
	private static CLBuffer clPropDataSumBuffer;

	private static Texture2D imageTexture;

	//out variables
	private static final int WIDTH  = 2048;
	private static final int HEIGHT = 1024;

	private static final int MAP_WIDTH  = 512;
	private static final int MAP_HEIGHT = 512;

	private static final int SAMPLES = 1024 * 1024;

	private static final int NAIVE_FRAMES = 200;

	private static float x          = -1.0f;
	private static float y          = -0.0f;
	private static float zoom       = 1.0f;
	private static int   iterations = 100;

	private static float tilt = 0.0f;

	public static void start() {

		Logger.instance.addStream(Logger.DEBUG, Logger.INFO, System.out);
		Logger.instance.addStream(Logger.WARNING, System.err);

		//gl context
		window = new GLWindow("particles", WIDTH, HEIGHT, false, -1);
		window.bind();

		//cl context
		clContext = new CLContext(window.getGLContext());
		clProgram = CLLoader.loadCLProgram(clContext, "res/noiseTest/fractal/kernel.cl");
		clKernelBuddhaBrotNaive = new CLKernel(clProgram, "buddhaBrotNaive", SAMPLES);
		clKernelBuddhaBrotImportance = new CLKernel(clProgram, "buddhaBrotImportance", SAMPLES);
		clKernelBlitTexture = new CLKernel(clProgram, "copyTexture", WIDTH, HEIGHT);
		clKernelBlitMap = new CLKernel(clProgram, "copyMap", WIDTH, HEIGHT);
		clKernelClearData = new CLKernel(clProgram, "clearData", WIDTH, HEIGHT);
		clKernelClearMap = new CLKernel(clProgram, "clearMap", MAP_WIDTH, MAP_HEIGHT);

		imageTexture = new Texture2D("", WIDTH, HEIGHT);
		clImageBuffer = new CLBuffer(clContext, imageTexture);

		clBuddhaDataBuffer = new CLBuffer(clContext, new int[WIDTH * HEIGHT], true, true);

		Random rand = new Random();
		int[] randomStart1 = new int[SAMPLES];
		int[] randomStart2 = new int[SAMPLES];
		for (int i = 0; i < randomStart1.length; i++) {
			randomStart1[i] = rand.nextInt();
			randomStart2[i] = rand.nextInt();
		}
		clRandomBuffer1 = new CLBuffer(clContext, randomStart1, true, true);
		clRandomBuffer2 = new CLBuffer(clContext, randomStart2, true, true);

		clProgramSum = CLLoader.loadCLProgram(clContext, "res/noiseTest/fractal/prefixSum.cl");
		clKernelLineSum = new CLKernel(clProgramSum, "lineSum", MAP_HEIGHT);
		clKernelLineNorm = new CLKernel(clProgramSum, "lineNorm", MAP_WIDTH * MAP_HEIGHT);
		clPropDataBuffer = new CLBuffer(clContext, new int[MAP_WIDTH * MAP_HEIGHT], true, true);
		clPropDataSumBuffer = new CLBuffer(clContext, new int[MAP_WIDTH * MAP_HEIGHT], true, true);

	}

	public static void run() {
		Timer timer = new Timer();
		int frame = 0;

		int renderedTexture = 0;

		float left = x - zoom * 2;
		float right = x + zoom * 2;
		float bottom = y - zoom;
		float top = y + zoom;

		while (!window.shouldClose()) {

			float scrollDelta = window.getInputHandler().getScrollDeltaY();
			float xOffset = (window.getInputHandler().isKeyDown(GLFW.GLFW_KEY_D) ? 1.0f : 0.0f) + (window.getInputHandler().isKeyDown(GLFW.GLFW_KEY_A) ? -1.0f : 0.0f);
			float yOffset = (window.getInputHandler().isKeyDown(GLFW.GLFW_KEY_W) ? 1.0f : 0.0f) + (window.getInputHandler().isKeyDown(GLFW.GLFW_KEY_S) ? -1.0f : 0.0f);
			boolean posChanged = false;
			for (MouseButtonEvent event : window.getInputHandler().getMouseButtonEvents()) {
				if (event.action == GLFW.GLFW_PRESS) {
					float mouseX = (window.getInputHandler().getCursorPosX() / window.getWidth()) * (right - left) + left;
					float mouseY = (window.getInputHandler().getCursorPosY() / window.getHeight()) * (top - bottom) + bottom;

					x = mouseX;
					y = mouseY;
					posChanged = true;
				}
			}
			if (scrollDelta != 0 || xOffset != 0 || yOffset != 0 || posChanged) {
				clKernelClearData.setArgBuffer(0, clBuddhaDataBuffer);
				clKernelClearData.setArg1i(1, 0);
				clKernelClearData.enqueueExecution(true, true);
				clKernelClearMap.setArgBuffer(0, clPropDataBuffer);
				clKernelClearMap.setArg1i(1, 0);
				clKernelClearMap.enqueueExecution(true, true);

				x += xOffset * zoom * 0.01f;
				y += yOffset * zoom * 0.01f;

				if (window.getInputHandler().isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
					iterations += 50 * scrollDelta;
				} else if (window.getInputHandler().isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
					tilt += 0.05 * scrollDelta;
				} else {
					zoom *= 1.0f - scrollDelta * 0.1f;
				}

				frame = 0;
			}

			left = x - zoom * 2;
			right = x + zoom * 2;
			bottom = y - zoom;
			top = y + zoom;

			CLKernel currentCalcCernal = null;

			if (frame < NAIVE_FRAMES) {
				currentCalcCernal = clKernelBuddhaBrotNaive;
			} else {

				if (frame == NAIVE_FRAMES) {
					clKernelLineSum.setArgBuffer(0, clPropDataBuffer);
					clKernelLineSum.setArgBuffer(1, clPropDataSumBuffer);
					clKernelLineSum.setArg1i(2, MAP_WIDTH);
					clKernelLineSum.enqueueExecution(true, true);

					clKernelLineNorm.setArgBuffer(0, clPropDataSumBuffer);
					clKernelLineNorm.setArg1i(1, MAP_WIDTH);
					clKernelLineNorm.enqueueExecution(true, true);

					clKernelClearData.setArgBuffer(0, clBuddhaDataBuffer);
					clKernelClearData.setArg1i(1, 0);
					clKernelClearData.enqueueExecution(true, true);
				}
				currentCalcCernal = clKernelBuddhaBrotImportance;
			}

			currentCalcCernal.setArgBuffer(0, clBuddhaDataBuffer);
			currentCalcCernal.setArgBuffer(1, clPropDataBuffer);
			currentCalcCernal.setArgBuffer(2, clPropDataSumBuffer);
			currentCalcCernal.setArgBuffer(3, clRandomBuffer1);
			currentCalcCernal.setArgBuffer(4, clRandomBuffer2);
			currentCalcCernal.setArg2f(5, left, bottom);
			currentCalcCernal.setArg2f(6, right, top);
			currentCalcCernal.setArg1i(7, iterations);
			currentCalcCernal.setArg4f(8, (float) Math.cos(tilt), (float) Math.sin(tilt), 0.0f, 0.0f);
			currentCalcCernal.enqueueExecution(true, true);

			int accumulateFrame = (frame > NAIVE_FRAMES ? frame - NAIVE_FRAMES : frame) + 1;
			if (renderedTexture == 0) {
				clKernelBlitTexture.setArgBuffer(0, clImageBuffer);
				clKernelBlitTexture.setArgBuffer(1, clBuddhaDataBuffer);
				clKernelBlitTexture.setArg1f(2, 1.0f / (accumulateFrame + 1) * 0.000008f / zoom / zoom);
				clKernelBlitTexture.enqueueExecution(true, true);
			} else if (renderedTexture == 1) {
				clKernelBlitMap.setArgBuffer(0, clImageBuffer);
				clKernelBlitMap.setArgBuffer(1, clPropDataBuffer);
				clKernelBlitMap.setArg1f(2, 1.0f / (accumulateFrame + 1) * 1f);
				clKernelBlitMap.enqueueExecution(true, true);
			} else {
				clKernelBlitMap.setArgBuffer(0, clImageBuffer);
				clKernelBlitMap.setArgBuffer(1, clPropDataSumBuffer);
				clKernelBlitMap.setArg1f(2, 1f);
				clKernelBlitMap.enqueueExecution(true, true);
			}

			RenderHelper.blitTexture(imageTexture);

			for (KeyboardKeyEvent event : window.getInputHandler().getKeyboardKeyEvents()) {
				if (event.key == GLFW.GLFW_KEY_P && event.action == GLFW.GLFW_PRESS) {
					renderedTexture++;
					renderedTexture %= 3;
					event.setProcessed();
				} else if (event.key == GLFW.GLFW_KEY_R && event.action == GLFW.GLFW_PRESS) {

					event.setProcessed();
				}
			}

			timer.update(1.0 / 40.0);
			window.setTitle("time: " + Math.round(timer.getCalcTime() * 1000.0) + "ms, iterations: " + iterations + ", zoom: " + (1.0f / zoom) + ", x: " + x + ", y: " + y + ", frame: " + frame);
			GLWindow.updateAll();

			frame++;
		}
		window.cleanup();

	}

	public static void main(String[] args) {
		Noise.init();
		start();
		run();
		Noise.cleanup();
	}

}
