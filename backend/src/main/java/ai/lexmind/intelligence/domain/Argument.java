package ai.lexmind.intelligence.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "arguments")
@Getter
@Setter
@NoArgsConstructor
public class Argument {

    @Id
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "party_side", nullable = false)
    private String partySide;   // PETITIONER | RESPONDENT

    @Column(name = "argument_text", nullable = false)
    private String argumentText;

    @Column(name = "strength")
    private String strength;     // STRONG | MODERATE | WEAK

    @Column(name = "source_excerpt")
    private String sourceExcerpt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
