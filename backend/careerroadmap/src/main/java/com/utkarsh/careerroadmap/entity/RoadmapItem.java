package com.utkarsh.careerroadmap.entity;

import com.utkarsh.careerroadmap.enums.RoadmapItemStatus;
import com.utkarsh.careerroadmap.enums.SkillLevel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "roadmap_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"roadmap_id", "skill_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class RoadmapItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_level", length = 20)
    private SkillLevel currentLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_level", nullable = false, length = 20)
    private SkillLevel targetLevel;

    @Column(name = "estimated_hours", nullable = false)
    private Integer estimatedHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoadmapItemStatus status = RoadmapItemStatus.PENDING;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}