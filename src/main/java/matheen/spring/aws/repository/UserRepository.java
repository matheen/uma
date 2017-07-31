package matheen.spring.aws.repository;

import matheen.spring.aws.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {

    public List<User> findByFirstName(String firstName);
}
