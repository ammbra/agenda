package org.eu.todo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "url")
public final class URLTodoItem extends TodoItem {

	private String url;

	public URLTodoItem() {}

	public URLTodoItem(String title, String description, String url, LocalDate createdOn, LocalDate deadline) {
		if (deadline.isBefore(createdOn))
			throw new IllegalArgumentException("Cannot create store with deadline before its creation date");
		super(title, description, createdOn, deadline);
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof URLTodoItem that)) return false;
		if (!super.equals(o)) return false;
		return Objects.equals(url, that.url);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), url);
	}

	@Override
	public String toString() {
		return "URLTodoItem{" +
				"url='" + url + '\'' +
				'}';
	}
}
