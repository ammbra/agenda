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
import java.util.ArrayList;
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
		List<List<String>> data = ScopedValue.callWhere(VALID_FILE, todoFilePath, () -> TodoFile.processContent(todoFilePath, urlFilePath, mixFilePath));

		List<TodoItem> items = new ArrayList<>();
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		for (List<String> row : data) {
			TodoItem item = switch (row.get(2)) {
				case String s when row.size() == 4 ->
						new TodoItem(row.getLast(), s, LocalDate.parse(row.getFirst(), df), LocalDate.parse(row.get(1), df));
				case String s when s.startsWith("http") || s.startsWith("www") ->
						new URLTodoItem(row.getLast(), row.get(3), s, LocalDate.parse(row.getFirst(), df), LocalDate.parse(row.get(1), df));
				case String s when row.size() == 5 ->
						new ImageTodoItem(row.getLast(), row.get(3), s.getBytes(), LocalDate.parse(row.getFirst(), df), LocalDate.parse(row.get(1), df));
				case String _ -> throw new IllegalStateException("Cannot process details...");
			};
			item.setPriority(item.determineUrgency());
			items.add(item);
		}
		todoRepository.saveAll(items);
	}
}
