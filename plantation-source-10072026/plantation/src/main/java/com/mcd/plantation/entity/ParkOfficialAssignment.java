package com.mcd.plantation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "park_official_assignments",
    uniqueConstraints = @UniqueConstraint(name = "uq_park_official", columnNames = {"park_id", "official_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ParkOfficialAssignment {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "assignment_id")
    private UUID assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "park_id", nullable = false)
    private Park park;

	/*
	 * @ManyToOne(fetch = FetchType.LAZY)
	 * 
	 * @JoinColumn(name = "official_id", nullable = false) private McdOfficial
	 * official;
	 */
    
    @Column(name = "official_id")
    private String official;

	/*
	 * @ManyToOne(fetch = FetchType.LAZY)
	 * 
	 * @JoinColumn(name = "assigned_by") private McdOfficial assignedBy;
	 */
    
    @Column(name = "assigned_by")
    private String assignedBy;

    @CreationTimestamp @Column(updatable = false)
    private OffsetDateTime assignedAt;
}
