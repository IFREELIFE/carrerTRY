package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.AcceptanceChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcceptanceChecklistItemRepository extends JpaRepository<AcceptanceChecklistItem, Long> {
    List<AcceptanceChecklistItem> findByStepNoOrderByIdAsc(Integer stepNo);
}
