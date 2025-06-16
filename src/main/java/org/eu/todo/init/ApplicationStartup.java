package org.eu.todo.init;

import org.eu.todo.domain.ImageTodoItem;
import org.eu.todo.domain.TodoItem;
import org.eu.todo.domain.TodoRepository;
import org.eu.todo.domain.URLTodoItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {

	public static ScopedValue<String> VALID_FILE = ScopedValue.newInstance();

	TodoRepository todoRepository;

	@Value("${todo.file}")
	private String todoFilePath;

	@Value("${mix.file}")
	private String mixFilePath;

	@Value("${url.file}")
	private String urlFilePath;

	public ApplicationStartup(TodoRepository todoRepository) {
		this.todoRepository = todoRepository;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		ScopedValue.where(VALID_FILE, todoFilePath).run(this::processFiles);
	}

	private void processFiles() {
		List<List<String>> data = TodoFile.processContent(todoFilePath, urlFilePath, mixFilePath);
		List<TodoItem> items = data.stream()
				.map(this::mapItem)
				.peek(todoItem -> todoItem.setPriority(todoItem.determineUrgency()))
				.toList();
		todoRepository.saveAll(items);
	}

	private TodoItem mapItem(List<String> row) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String title = row.getLast();
		String content = row.get(2);
		LocalDate startDate = LocalDate.parse(row.getFirst(), df);
		LocalDate endDate = LocalDate.parse(row.get(1), df);

		return switch (content) {
			case String s when row.size() == 4 ->
					new TodoItem(title, s, startDate, endDate);
			case String s when s.startsWith("http") || s.startsWith("www") ->
					new URLTodoItem(row.getLast(), row.get(3), s, startDate, endDate);
			case String s when row.size() == 5 ->
					new ImageTodoItem(row.getLast(), row.get(3), s.getBytes(), startDate, endDate);
			case String _ -> throw new IllegalStateException("Cannot process details...");
		};
	}
}
