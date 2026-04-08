package com.ifreelife.carrertry.repository;

import com.ifreelife.carrertry.entity.SystemNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemNoticeRepository extends JpaRepository<SystemNotice, Long> {
    List<SystemNotice> findByAudienceRoleOrderByCreatedAtDesc(String audienceRole);
}
