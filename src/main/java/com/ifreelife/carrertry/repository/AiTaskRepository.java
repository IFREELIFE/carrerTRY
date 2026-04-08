package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.AiTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiTaskRepository extends JpaRepository<AiTask, Long> {
    List<AiTask> findByTaskStatusOrderByUpdatedAtDesc(String taskStatus);
}
