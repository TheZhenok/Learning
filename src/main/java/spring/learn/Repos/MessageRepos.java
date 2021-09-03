package spring.learn.Repos;

import org.springframework.data.repository.CrudRepository;
import spring.learn.Models.Message;

import java.util.List;

public interface MessageRepos extends CrudRepository<Message, Long> {
    List<Message> findByName(String name);
}
