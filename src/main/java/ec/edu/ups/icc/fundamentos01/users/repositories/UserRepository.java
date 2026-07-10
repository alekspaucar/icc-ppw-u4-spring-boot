package ec.edu.ups.icc.fundamentos01.users.repositories;

import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByIdAndDeletedFalse(Long id);
    Optional<UserEntity> findByIdAndDeletedFalse(Long id);
}