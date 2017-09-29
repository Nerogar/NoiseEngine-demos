package de.nerogar.noiseTest.event;

import de.nerogar.noise.event.ConstraintEventManager;
import de.nerogar.noise.event.Event;
import de.nerogar.noise.event.EventConstraint;
import de.nerogar.noise.event.EventPositionConstraint;
import de.nerogar.noise.util.Bounding;

public class MoveEvent implements Event, EventPositionConstraint {

	private Bounding bounding;

	public MoveEvent(Bounding bounding) {
		this.bounding = bounding;
	}

	@Override
	public Class<? extends EventConstraint> getSpecialConstraintClass() {
		return EventPositionConstraint.class;
	}

	@Override
	public ConstraintEventManager getNewEventManager() {
		return getNewConstraintEventManager();
	}

	@Override
	public Bounding getBounding() {
		return bounding;
	}

}
