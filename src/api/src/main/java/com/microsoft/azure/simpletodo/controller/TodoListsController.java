package com.microsoft.azure.simpletodo.controller;

import com.microsoft.azure.simpletodo.api.ListsApi;
import com.microsoft.azure.simpletodo.model.TodoList;
import com.microsoft.azure.simpletodo.repository.TodoListRepository;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // 追加
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/lists") // ← これで「/api/lists」というURLを受け付けるようになります
public class TodoListsController implements ListsApi {

    private final TodoListRepository todoListRepository;

    public TodoListsController(TodoListRepository todoListRepository) {
        this.todoListRepository = todoListRepository;
    }

    @PostMapping // 作成（POST /api/lists）
    public ResponseEntity<TodoList> createList(@RequestBody TodoList todoList) {
        final TodoList savedTodoList = todoListRepository.save(todoList);
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(savedTodoList.getId())
            .toUri();
        return ResponseEntity.created(location).body(savedTodoList);
    }

    @DeleteMapping("/{listId}") // 削除（DELETE /api/lists/{id}）
    public ResponseEntity<Void> deleteListById(@PathVariable String listId) {
        return todoListRepository
            .findById(listId)
            .map(l -> {
                todoListRepository.deleteTodoListById(l.getId());
                return ResponseEntity.noContent().<Void>build();
            })
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{listId}") // 特定取得（GET /api/lists/{id}）
    public ResponseEntity<TodoList> getListById(@PathVariable String listId) {
        return todoListRepository
            .findById(listId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping // 一覧取得（GET /api/lists）
    public ResponseEntity<List<TodoList>> getLists(
            @RequestParam(defaultValue = "20") BigDecimal top, 
            @RequestParam(defaultValue = "0") BigDecimal skip) {
        return ResponseEntity.ok(todoListRepository.findAll(skip.intValue(), top.intValue()));
    }

    @PutMapping("/{listId}") // 更新（PUT /api/lists/{id}）
    public ResponseEntity<TodoList> updateListById(@PathVariable String listId, @NotNull @RequestBody TodoList todoList) {
        todoList.setId(listId);
        return todoListRepository
            .findById(listId)
            .map(t -> ResponseEntity.ok(todoListRepository.save(todoList)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}