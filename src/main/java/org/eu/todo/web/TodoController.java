package org.eu.todo.web;

import org.eu.todo.domain.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class TodoController {

	private final TodoRepository todoRepository;

	public TodoController(TodoRepository todoRepository) {
		this.todoRepository = todoRepository;
	}

	@GetMapping("/")
	public String index(Model model) {
		List<Memo> memos = new ArrayList<>();
		for (TodoItem item : todoRepository.findAll()) {
			Highlight color = switch (item.getDeadline()) {
				case LocalDate d when d.isBefore(LocalDate.now()) -> Highlight.RED;
				case LocalDate d when d.isBefore(LocalDate.now().plusDays(10)) -> Highlight.YELLOW;
				case LocalDate d when d.isBefore(LocalDate.now().plusDays(20)) -> Highlight.BLUE;
				case LocalDate _ -> Highlight.WHITE;
			};

			Memo memo = switch (item) {
				case ImageTodoItem i ->
						new Memo(i.getId(), i.getTitle(), i.getDescription(), null, i.getImage(), color, i.getCreatedOn(), i.getDeadline());
				case URLTodoItem u ->
						new Memo(u.getId(), u.getTitle(), u.getDescription(), u.getUrl(), null, color, u.getCreatedOn(), u.getDeadline());
				case TodoItem t ->
						new Memo(t.getId(), t.getTitle(), t.getDescription(), null, null, color, t.getCreatedOn(), t.getDeadline());
			};
			memos.add(memo);
		}
		model.addAttribute("todos", memos);
		return "index";
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

	@DeleteMapping("/{id}")
	public String delete(@PathVariable Long id) {
        todoRepository.deleteById(id);
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