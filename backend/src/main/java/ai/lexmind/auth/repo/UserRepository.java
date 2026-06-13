package ai.lexmind.auth.repo;

import ai.lexmind.auth.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmailIgnoreCase(String email);

    @Query("select u from User u join fetch u.role r left join fetch r.permissions where lower(u.email) = lower(:email)")
    Optional<User> findByEmailWithRole(@Param("email") String email);

    @Query("select u from User u join fetch u.role r left join fetch r.permissions where u.id = :id")
    Optional<User> findByIdWithRole(@Param("id") UUID id);

    Page<User> findByOrganizationId(UUID organizationId, Pageable pageable);
}
