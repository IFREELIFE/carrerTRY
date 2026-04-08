package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.AchievementDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AchievementDefinitionRepository extends JpaRepository<AchievementDefinition, Long> {
    boolean existsByCode(String code);
}
