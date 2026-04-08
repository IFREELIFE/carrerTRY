package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "acceptance_checklist_item")
public class AcceptanceChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer stepNo;

    @Column(nullable = false, length = 256)
    private String itemName;

    @Column(nullable = false)
    private Boolean doneFlag = Boolean.FALSE;

    @Column(length = 512)
    private String note;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
