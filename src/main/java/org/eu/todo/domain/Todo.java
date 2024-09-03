package org.eu.todo.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public sealed interface Todo permits TodoItem {

	default long determineUrgency() {
		LocalDate deadline = getDeadline();
		long urgency;
		if (deadline.isBefore(LocalDate.now()) ) {
			urgency = -1;
		} else {
			urgency = (ChronoUnit.DAYS.between(LocalDate.now(), deadline)/10);
		}
		return urgency;
	}

	LocalDate getDeadline();
}
