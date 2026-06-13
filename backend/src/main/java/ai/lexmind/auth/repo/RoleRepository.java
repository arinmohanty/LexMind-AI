package ai.lexmind.auth.repo;

import ai.lexmind.auth.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    @Query("select r from Role r left join fetch r.permissions where r.name = :name")
    Optional<Role> findByNameWithPermissions(@Param("name") String name);
}
