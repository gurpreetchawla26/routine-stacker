package com.routinestacker.controller;

import com.routinestacker.dto.CreateHabitRequest;
import com.routinestacker.dto.HabitResponse;
import com.routinestacker.service.HabitService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService habits;

    public HabitController(HabitService habits) {
        this.habits = habits;
    }

    @PostMapping
    public ResponseEntity<HabitResponse> create(@RequestBody CreateHabitRequest request) {
        HabitResponse created = habits.create(request);
        return ResponseEntity.created(URI.create("/api/habits/" + created.id())).body(created);
    }

    @GetMapping
    public List<HabitResponse> list() {
        return habits.list();
    }

    @GetMapping("/{id}")
    public HabitResponse get(@PathVariable String id) {
        return habits.get(id);
    }
}
