package org.eu.todo.web;

public enum Highlight {
	RED("table-danger"),
	YELLOW("table-warning"),
	BLUE("table-info"),
	WHITE("table-light");

	private final String content;

	Highlight(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return content;
	}
}
