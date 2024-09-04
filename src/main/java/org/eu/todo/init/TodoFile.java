package org.eu.todo.init;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Collectors;

import static org.eu.todo.init.ApplicationStartup.VALID_FILE;

record TodoFile(List<List<String>> data) {

	public TodoFile {
		if (!VALID_FILE.isBound()) {
			throw new IllegalStateException("The file path state is not bound");
		} else {
			try {
				if (Files.lines(Path.of(VALID_FILE.get())).toList().size() > 0) {
					throw new IllegalStateException("The provided path is not a file");
				}
			} catch (IOException e) {
				throw new IllegalStateException("The file path state is not valid");
			}
		}
	}

	public static class FileScope extends StructuredTaskScope<TodoFile> {
	//...
		private final List<TodoFile> todos = new ArrayList<>();

		@Override
		protected void handleComplete(Subtask<? extends TodoFile> subtask) {
			switch (subtask.state()) {
				case UNAVAILABLE -> throw new IllegalStateException("File content pending processing...");
				case SUCCESS -> this.todos.add(subtask.get());
				case FAILED -> subtask.exception();
			}
		}

		public List<List<String>> processContent() {
			return this.todos.stream().flatMap(p -> p.data().stream()).collect(Collectors.toList());
		}
	}

	public static List<List<String>> processContent(String todoFilePath, String urlFilePath, String mixFilePath) {

		try (var scope = new FileScope()) {

			scope.fork(() -> parseCSV(todoFilePath));
			scope.fork(() -> parseCSV(urlFilePath));
			scope.fork(() -> parseCSV(mixFilePath));

			scope.join();

			return scope.processContent();

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private static TodoFile parseCSV(String filePath) throws IOException {
		List<List<String>> result = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				List<String> parsedLine = List.of(line.split(","));
				result.add(parsedLine);
			}
		}
		return new TodoFile(result);
	}



}
