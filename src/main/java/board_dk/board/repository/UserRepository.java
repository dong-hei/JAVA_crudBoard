package board_dk.board.repository;

import board_dk.board.model.Board;
import board_dk.board.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User>,
CustomizedUserRepository{
    User findByUsername(String username);

    @Query("select u from User u where u.username like %?1")
    List<User> findByUsernameQuery(String username);

    @Query(value = "select * from User u where u.username like %?1",nativeQuery = true)
    List<User> findByUsernameNativeQuery(String username);

}
