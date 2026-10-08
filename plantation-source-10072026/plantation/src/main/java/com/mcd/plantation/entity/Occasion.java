package com.mcd.plantation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity @Table(name = "occasions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Occasion {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "occasion_id")
    private UUID occasionId;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 10)
    private String emojiCode;

    @Column(nullable = false)
    private short displayOrder;

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true;

    @CreationTimestamp @Column(updatable = false)
    private OffsetDateTime createdAt;
}
