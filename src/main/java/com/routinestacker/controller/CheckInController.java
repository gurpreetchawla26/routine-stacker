package com.routinestacker.controller;

import com.routinestacker.dto.CheckInResponse;
import com.routinestacker.service.CheckInService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/habits/{habitId}/check-ins")
public class CheckInController {

    private final CheckInService checkIns;

    public CheckInController(CheckInService checkIns) {
        this.checkIns = checkIns;
    }

    /** 201 when today's check-in is new, 200 when it already existed. */
    @PostMapping
    public ResponseEntity<CheckInResponse> checkIn(@PathVariable String habitId) {
        CheckInResponse result = checkIns.checkIn(habitId);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result);
    }

    @DeleteMapping("/today")
    public ResponseEntity<Void> undoToday(@PathVariable String habitId) {
        checkIns.undoToday(habitId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<LocalDate> history(@PathVariable String habitId) {
        return checkIns.history(habitId);
    }
}
