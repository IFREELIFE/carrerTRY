package com.ifreelife.carrertry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "achievement_definition",
    uniqueConstraints = {@UniqueConstraint(name = "uk_achievement_code", columnNames = "code")}
)
public class AchievementDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 512)
    private String description;

    @Column(nullable = false, length = 256)
    private String triggerCondition;
}
