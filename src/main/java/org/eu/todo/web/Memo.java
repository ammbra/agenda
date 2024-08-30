package org.eu.todo.web;

import java.time.LocalDate;

public record Memo (Long id, String title, String description, String url, byte[] image, Highlight highlight, LocalDate createdOn, LocalDate deadline) {}
