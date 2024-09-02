/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eu.todo;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eu.todo.domain.ImageTodoItem;
import org.eu.todo.domain.TodoItem;
import org.eu.todo.domain.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = { "spring.docker.compose.skip.in-tests=false" })
public class H2IntegrationTests {

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

	public static void main(String[] args) {
		new SpringApplicationBuilder(AgendaApplication.class) //
			.listeners(new PropertiesLogger()) //
			.run(args);
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

	static class PropertiesLogger implements ApplicationListener<ApplicationPreparedEvent> {

		private static final Log log = LogFactory.getLog(PropertiesLogger.class);

		private ConfigurableEnvironment environment;

		private boolean isFirstRun = true;

		@Override
		public void onApplicationEvent(ApplicationPreparedEvent event) {
			if (isFirstRun) {
				environment = event.getApplicationContext().getEnvironment();
				printProperties();
			}
			isFirstRun = false;
		}

		public void printProperties() {
			for (EnumerablePropertySource<?> source : findPropertiesPropertySources()) {
				log.info("PropertySource: " + source.getName());
				String[] names = source.getPropertyNames();
				Arrays.sort(names);
				for (String name : names) {
					String resolved = environment.getProperty(name);
					String value = source.getProperty(name).toString();
					if (resolved.equals(value)) {
						log.info(name + "=" + resolved);
					} else {
						log.info(name + "=" + value + " OVERRIDDEN to " + resolved);
					}
				}
			}
		}

		private List<EnumerablePropertySource<?>> findPropertiesPropertySources() {
			List<EnumerablePropertySource<?>> sources = new LinkedList<>();
			for (PropertySource<?> source : environment.getPropertySources()) {
				if (source instanceof EnumerablePropertySource enumerable) {
					sources.add(enumerable);
				}
			}
			return sources;
		}

	}

}