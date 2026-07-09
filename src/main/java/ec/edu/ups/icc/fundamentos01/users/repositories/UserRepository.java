package ec.edu.ups.icc.fundamentos01.users.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByEmailAndDeletedFalse(String email);
    Optional<UserEntity> findByIdAndDeletedFalse(Long id);
    Optional<UserEntity> findByIdAndDeleted(Long id, boolean deleted);
    Optional<UserEntity> findByNameAndId(String name, long id);

    boolean existsByIdAndDeletedFalse(Long id);
    boolean existsByEmail(String email);



}
