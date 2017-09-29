package de.nerogar.noiseTest.event;

import de.nerogar.noise.event.EventListenerPositionConstraint;
import de.nerogar.noise.util.BoundingSphere;

public class Entity {

	private BoundingSphere bounding;

	public Entity(BoundingSphere bounding) {
		this.bounding = bounding;

		Main.eventManager.register(MoveEvent.class, this::event, new EventListenerPositionConstraint(bounding));
	}

	private void event(MoveEvent event) {
		System.out.println("event triggered for: " + bounding.getCenter().getX());
	}

}
