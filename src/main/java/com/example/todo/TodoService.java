package com.example.todo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class TodoService {
    private final List<TodoItem> todoItems = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public void addTodoItem(String title, String description) {
        TodoItem item = new TodoItem();
        item.setId(idCounter.getAndIncrement());
        item.setTitle(title);
        item.setDescription(description);
        item.setCompleted(false);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        todoItems.add(item);
    }

    public void updateTodoItem(Long id, String title, String description, Boolean completed) {
        for (TodoItem item : todoItems) {
            if (item.getId().equals(id)) {
                if (title != null) item.setTitle(title);
                if (description != null) item.setDescription(description);
                if (completed != null) item.setCompleted(completed);
                item.setUpdatedAt(LocalDateTime.now());
                return;
            }
        }
    }

    public boolean deleteTodoItem(Long id) {
        return todoItems.removeIf(item -> item.getId().equals(id));
    }

    public List<TodoItem> getAllTodoItems() {
        return new ArrayList<>(todoItems);
    }
}