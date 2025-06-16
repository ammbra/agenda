package org.eu.todo.init;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;

import static java.util.stream.Collectors.toList;
import static org.eu.todo.init.ApplicationStartup.VALID_FILE;
import static java.util.concurrent.StructuredTaskScope.Joiner;

record TodoFile(List<List<String>> data) {

	public TodoFile {
		if (!VALID_FILE.isBound()) {
			throw new IllegalStateException("The file path state is not bound");
		} else {
			if (Files.isDirectory(Path.of(VALID_FILE.get()))) {
				throw new IllegalStateException("The provided path is not a file");
			}
		}
	}

	public static List<List<String>> processContent(String todoFilePath, String urlFilePath, String mixFilePath) {

		try (var scope = StructuredTaskScope.<TodoFile>open()) {

			var todoTask = scope.fork(() -> parseCSV(todoFilePath));
			var urlTask = scope.fork(() -> parseCSV(urlFilePath));
			var mixTask = scope.fork(() -> parseCSV(mixFilePath));

			scope.join();

			List<List<String>> result = new ArrayList<>();
			result.addAll(todoTask.get().data());
			result.addAll(urlTask.get().data());
			result.addAll(mixTask.get().data());

			return result;

		} catch (InterruptedException e) {
			throw new RuntimeException("Processing was interrupted", e);
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
