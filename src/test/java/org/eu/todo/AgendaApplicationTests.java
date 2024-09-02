package org.eu.todo;

import org.eu.todo.domain.ImageTodoItem;
import org.eu.todo.domain.TodoItem;
import org.eu.todo.domain.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AgendaIntegrationTests {

	@LocalServerPort
	int port;

	@Autowired
	private TodoRepository repo;

	@Autowired
	private RestTemplateBuilder builder;


	private TodoItem todo;

	@BeforeEach
	public void setup() throws IOException {
		byte[] imageData = StreamUtils.copyToByteArray(new ClassPathResource("diagram.png").getInputStream());
		todo = repo.save(new ImageTodoItem("Sample", "Sample description", imageData, LocalDate.now(), LocalDate.now().plusDays(10)));
	}

	@Test
	void testFindAll() throws Exception {
		repo.findAll(Sort.by(Sort.Direction.ASC, "deadline"));
		repo.findAll(Sort.by(Sort.Direction.ASC, "deadline")); // served from cache
	}

	@Test
	void testImageDetails() {
		RestTemplate template = builder.rootUri("http://localhost:" + port).build();
		ResponseEntity<String> result = template.exchange(RequestEntity.get("/image/" + todo.getId()).build(), String.class);
		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	public static void main(String[] args) {
		SpringApplication.run(AgendaApplication.class, args);
	}

}
