/*
 * package com.mcd.plantation.entity;
 * 
 * import jakarta.persistence.*; import lombok.*; import
 * org.hibernate.annotations.CreationTimestamp; import java.time.OffsetDateTime;
 * import java.util.ArrayList; import java.util.List; import java.util.UUID;
 * 
 * @Entity @Table(name = "zones")
 * 
 * @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder public class
 * Zone {
 * 
 * @Id @GeneratedValue(strategy = GenerationType.UUID)
 * 
 * @Column(name = "zone_id") private UUID zoneId;
 * 
 * @Column(nullable = false, unique = true, length = 100) private String name;
 * 
 * @Column(length = 20) private String code;
 * 
 * @Column(nullable = false) private boolean isActive = true;
 * 
 * @CreationTimestamp @Column(updatable = false) private OffsetDateTime
 * createdAt;
 * 
 * @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY)
 * 
 * @Builder.Default private List<Ward> wards = new ArrayList<>(); }
 */