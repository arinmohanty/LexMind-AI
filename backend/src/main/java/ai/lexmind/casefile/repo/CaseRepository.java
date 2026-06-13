package ai.lexmind.casefile.repo;

import ai.lexmind.casefile.domain.CaseFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CaseRepository extends JpaRepository<CaseFile, UUID> {

    Page<CaseFile> findByOwnerIdAndStatusNot(UUID ownerId, String status, Pageable pageable);

    Page<CaseFile> findByOrganizationIdAndStatusNot(UUID organizationId, String status, Pageable pageable);

    @Query("select c from CaseFile c left join fetch c.parties where c.id = :id")
    Optional<CaseFile> findByIdWithParties(@Param("id") UUID id);
}
