package org.eu.todo.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Inheritance(
		strategy = InheritanceType.JOINED
)
public class TodoItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	private String description;

	private int priority;

	@Temporal(TemporalType.DATE)
	private LocalDate createdOn;

	@Temporal(TemporalType.DATE)
	private LocalDate deadline;

	public TodoItem() {}

	public TodoItem(String title, String description, LocalDate createdOn, LocalDate deadline) {
		this.title = title;
		this.description = description;
		this.createdOn = createdOn;
		this.deadline = deadline;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public LocalDate getCreatedOn() {
		return createdOn;
	}

	public LocalDate getDeadline() {
		return deadline;
	}

	public void setDeadline(LocalDate deadline) {
		this.deadline = deadline;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TodoItem todoItem)) return false;
		return Objects.equals(id, todoItem.id)
				&& Objects.equals(title, todoItem.title)
				&& Objects.equals(description, todoItem.description)
				&& Objects.equals(createdOn, todoItem.createdOn)
				&& Objects.equals(deadline, todoItem.deadline);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, title, description, createdOn, deadline);
	}

}
