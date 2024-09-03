package org.eu.todo.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<ImageTodoItem, Long> {

	@Override
	Optional<ImageTodoItem> findById(Long aLong);
}
