package org.eu.todo.web;

import org.eu.todo.domain.*;
import org.eu.todo.web.display.Highlight;
import org.eu.todo.web.display.Memo;
import org.eu.todo.web.display.Statistic;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

//import module java.base;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TodoController {

	private final TodoRepository todoRepository;

	private final ImageRepository imageRepository;

	public TodoController(TodoRepository todoRepository, ImageRepository imageRepository) {
		this.todoRepository = todoRepository;
		this.imageRepository = imageRepository;
	}

	@GetMapping("/")
	public String index(Model model) {
		List<Memo> memos = new ArrayList<>();

		for (TodoItem item : todoRepository.findAll(Sort.by(Sort.Direction.ASC, "deadline"))) {
			Highlight color = findColor(item);
			Memo memo = paint(item, color);
			memos.add(memo);
		}

		List<Statistic> stats = memos.stream()
				.collect(Collectors.groupingBy(Memo::highlight, Collectors.counting()))
				.entrySet().stream()
				.map(entry -> new Statistic(entry.getKey(), entry.getValue().intValue()))
				.toList();

		model.addAttribute("stats", stats);
		model.addAttribute("todos", memos);
		return "index";
	}

	/**
	 * Paints a given TodoItem into a Memo object with a specified highlight color.
	 *
	 * @param item  the TodoItem to be painted
	 * @param color the Highlight color to apply to the Memo
	 * @return a Memo object containing details from the input TodoItem and the applied highlight color
	 */
	private static Memo paint(TodoItem item, Highlight color) {
		return switch (item) {
			case ImageTodoItem i ->
					new Memo(i.getId(), i.getTitle(), i.getDescription(), null, i.getImage(), color, i.getDeadline());
			case URLTodoItem u ->
					new Memo(u.getId(), u.getTitle(), u.getDescription(), u.getUrl(), null, color, u.getDeadline());
			case TodoItem t ->
					new Memo(t.getId(), t.getTitle(), t.getDescription(), null, null, color, t.getDeadline());
		};
	}

	/**
	 * Determines the appropriate highlight color for a given TodoItem based on its deadline.
	 *
	 * @param item the TodoItem whose deadline will determine the highlight color
	 * @return the Highlight enum value representing the determined color
	 */
	private static Highlight findColor(TodoItem item) {
		return switch (item.getPriority()) {
			case long l when l < 0 -> Highlight.RED;
			case 0L -> Highlight.YELLOW;
			case 1L -> Highlight.BLUE;
			case long _ -> Highlight.WHITE;
		};
	}

	@PostMapping("/add")
	public String addTodo(@ModelAttribute Memo todo, @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        TodoItem item = switch (todo.url()) {
			case null -> new TodoItem(todo.title(), todo.description(), LocalDate.now(), todo.deadline());
			case String _ when  !imageFile.isEmpty() -> new ImageTodoItem(todo.title(), todo.description(), imageFile.getBytes(), LocalDate.now(), todo.deadline());
			case String _ -> new URLTodoItem(todo.title(), todo.description(), todo.url(), LocalDate.now(), todo.deadline());
		};
		item.setPriority(item.determineUrgency());
		todoRepository.save(item);
		return "redirect:/";
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String deleteItemById(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		todoRepository.deleteById(id);
		redirectAttributes.addFlashAttribute("message", "Item successfully deleted.");
		return "redirect:/";
	}

    @GetMapping("/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
		ImageTodoItem item = imageRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid todo ID:" + id));
		return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(item.getImage());
	}
}