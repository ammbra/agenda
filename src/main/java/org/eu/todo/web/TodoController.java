package org.eu.todo.web;

import org.eu.todo.domain.*;
import org.eu.todo.web.display.Highlight;
import org.eu.todo.web.operations.MapCountByGatherer;
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

	public TodoController(TodoRepository todoRepository) {
		this.todoRepository = todoRepository;
	}

	@GetMapping("/")
	public String index(Model model) {
		List<Memo> memos = new ArrayList<>();

		for (TodoItem item : todoRepository.findAll(Sort.by(Sort.Direction.ASC, "deadline"))) {
			Highlight color = findColor(item);
			Memo memo = paint(item, color);
			memos.add(paint(item, color));
		}

		List<Statistic> stats = memos.stream().map(m -> new Statistic(m.highlight(), 0))
				.gather(new MapCountByGatherer<>(Statistic::highlight)).collect(Collectors.toList());

		model.addAttribute("stats", stats);
		model.addAttribute("todos", memos);
		return "index";
	}

	private static Memo paint(TodoItem item, Highlight color) {
		Memo memo = switch (item) {
			case ImageTodoItem i ->
					new Memo(i.getId(), i.getTitle(), i.getDescription(), null, i.getImage(), color, i.getCreatedOn(), i.getDeadline());
			case URLTodoItem u ->
					new Memo(u.getId(), u.getTitle(), u.getDescription(), u.getUrl(), null, color, u.getCreatedOn(), u.getDeadline());
			case TodoItem t ->
					new Memo(t.getId(), t.getTitle(), t.getDescription(), null, null, color, t.getCreatedOn(), t.getDeadline());
		};
		return memo;
	}

	private static Highlight findColor(TodoItem item) {
		return switch (item.getDeadline()) {
			case LocalDate d when d.isBefore(LocalDate.now()) -> Highlight.RED;
			case LocalDate d when d.isBefore(LocalDate.now().plusDays(10)) -> Highlight.YELLOW;
			case LocalDate d when d.isBefore(LocalDate.now().plusDays(20)) -> Highlight.BLUE;
			case LocalDate _ -> Highlight.WHITE;
		};
	}

	@PostMapping("/add")
	public String addTodo(@ModelAttribute Memo todo, @RequestParam("imageFile") MultipartFile imageFile) throws IOException {
        TodoItem item = switch (todo.url()) {
			case null -> new TodoItem(todo.title(), todo.description(), LocalDate.now(), todo.deadline());
			case String _ when  !imageFile.isEmpty() -> new ImageTodoItem(todo.title(), todo.description(), imageFile.getBytes(), LocalDate.now(), todo.deadline());
			case String _ -> new URLTodoItem(todo.title(), todo.description(), todo.url(), LocalDate.now(), todo.deadline());
		};

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
		ImageTodoItem todo = (ImageTodoItem) todoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid todo ID:" + id));
        if (todo != null) {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(todo.getImage());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}