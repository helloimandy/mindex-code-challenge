package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.service.ICompensationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class CompensationController {

    private final ICompensationService compensationService;

    @Autowired
    public CompensationController(ICompensationService compensationService) {
        this.compensationService = compensationService;
    }

    @GetMapping("/compensation/{id}")
    public Compensation read(@PathVariable String id) {
        return compensationService.read(id);
    }

    @PostMapping("/compensation")
    public Compensation create(@RequestBody Compensation compensation) {
        return compensationService.create(compensation);
    }
}
