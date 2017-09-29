package de.nerogar.noiseTest.event;

import de.nerogar.noise.event.EventManager;
import de.nerogar.noise.util.BoundingSphere;
import de.nerogar.noise.util.Timer;
import de.nerogar.noise.util.Vector3f;

import java.util.HashSet;
import java.util.Set;

public class Main {

	public static EventManager eventManager;

	/**
	 * This demo simulates entities that listen for events near them.
	 * All entities are positioned in a straight line.
	 * A single bounding is moving along that line and triggers a MoveEvent ery time it updates.
	 * Entities near the moving bounding print their position to the console.
	 */
	public static void main(String[] args) {
		eventManager = new EventManager();

		Set<Entity> entities = new HashSet<>();

		for (int i = 0; i < 10000; i++) {
			Entity entity = new Entity(new BoundingSphere(new Vector3f(i, 0, 0), 1));
			entities.add(entity);
		}

		BoundingSphere movingBounding = new BoundingSphere(new Vector3f(0), 1);

		Timer timer = new Timer();
		while (true) {
			timer.update(1f);
			eventManager.trigger(new MoveEvent(movingBounding));
			movingBounding.getCenter().addX(0.2f);
			System.out.println("-----");
		}

	}

}
