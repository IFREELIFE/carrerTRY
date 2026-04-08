package com.ifreelife.carrertry.service;

import com.ifreelife.carrertry.entity.AiTask;
import com.ifreelife.carrertry.repository.AiTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AiTaskRepository aiTaskRepository;

    public List<AiTask> listAiTasks() {
        return aiTaskRepository.findAll();
    }
}
