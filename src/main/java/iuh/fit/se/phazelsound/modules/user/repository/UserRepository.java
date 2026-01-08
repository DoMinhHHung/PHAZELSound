package iuh.fit.se.phazelsound.modules.user.repository;

import iuh.fit.se.phazelsound.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    Optional<User> findByEmailOrPhone(String email, String phone);
    Optional<User> findByEmail(String email);
}