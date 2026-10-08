package com.mcd.plantation.entity;

import com.mcd.plantation.enums.TreeCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @Table(name = "tree_species")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TreeSpecies {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "species_id")
    private UUID speciesId;

    @Column(nullable = false)
    private String commonName;

    private String scientificName;
    private String emojiCode;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false)
    private TreeCategory category;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(columnDefinition = "TEXT")
    private String careNotes;

    @Column(nullable = false)
    private boolean isActive = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "tree_species_occasions",
        joinColumns = @JoinColumn(name = "species_id"),
        inverseJoinColumns = @JoinColumn(name = "occasion_id")
    )
    @Builder.Default
    private List<Occasion> preferredOccasions = new ArrayList<>();

    @CreationTimestamp @Column(updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
