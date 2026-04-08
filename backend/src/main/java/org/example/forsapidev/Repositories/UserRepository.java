package org.example.forsapidev.Repositories;
import org.example.forsapidev.entities.UserManagement.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.Date;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
  Optional<User> findByEmail(String email);
  Boolean existsByUsername(String username);
  Boolean existsByEmail(String email);


  // Methods for dashboard statistics
  long countByIsActive(boolean isActive);
  long countByCreatedAtAfter(Date date);
  long countByRole_Name(org.example.forsapidev.entities.UserManagement.ERole roleName);
}
