package org.eu.todo.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "image")
public final class ImageTodoItem extends TodoItem {

	@Lob
	private byte[] image;


	public ImageTodoItem() {

	}

	public ImageTodoItem(String title, String description, byte[] image, LocalDate createdOn, LocalDate deadline) {
		if (deadline.isBefore(createdOn))
			throw new IllegalArgumentException("Cannot create store with deadline before its creation date");
		super(title, description, createdOn, deadline);
		this.image = image;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ImageTodoItem that)) return false;
		if (!super.equals(o)) return false;
		return Objects.deepEquals(image, that.image);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), Arrays.hashCode(image));
	}
}
