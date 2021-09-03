package spring.learn.Repos;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.learn.Models.User;

import java.util.Optional;

public interface UserRepos extends JpaRepository<User, Long> {
    User findByUsername(String username);
    Optional<User> findById(Long id);

    User findByActivationCode(String code);
}
