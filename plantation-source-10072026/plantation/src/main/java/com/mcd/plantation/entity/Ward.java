/*
 * package com.mcd.plantation.entity;
 * 
 * import jakarta.persistence.*; import lombok.*; import
 * org.hibernate.annotations.CreationTimestamp; import java.time.OffsetDateTime;
 * import java.util.UUID;
 * 
 * @Entity @Table(name = "wards", uniqueConstraints
 * = @UniqueConstraint(columnNames = {"zone_id", "name"}))
 * 
 * @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder public class
 * Ward {
 * 
 * @Id @GeneratedValue(strategy = GenerationType.UUID)
 * 
 * @Column(name = "ward_id") private UUID wardId;
 * 
 * @ManyToOne(fetch = FetchType.LAZY)
 * 
 * @JoinColumn(name = "zone_id", nullable = false) private Zone zone;
 * 
 * @Column(nullable = false, length = 100) private String name;
 * 
 * @Column(length = 20) private String wardNumber;
 * 
 * @Column(nullable = false) private boolean isActive = true;
 * 
 * @CreationTimestamp @Column(updatable = false) private OffsetDateTime
 * createdAt; }
 */