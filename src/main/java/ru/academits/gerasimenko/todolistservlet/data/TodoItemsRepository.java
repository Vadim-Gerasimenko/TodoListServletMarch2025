package ru.academits.gerasimenko.todolistservlet.data;

import java.util.List;

public interface TodoItemsRepository {
    List<TodoItem> getAll();

    int create(TodoItem item);

    void update(TodoItem item);

    void delete(int itemId);
}