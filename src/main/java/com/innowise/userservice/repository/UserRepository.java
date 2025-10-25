package com.innowise.userservice.repository;

import com.innowise.userservice.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;


public interface UserRepository extends JpaRepository<User, Long> {

    //Named
    Optional<User> findByEmail(String email);

    // JPQL
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name% ORDER BY u.surname ASC")
    List<User> searchUsersByName(@Param("name") String name);

    // JPQL
    @Query(value = "SELECT * FROM users u JOIN card_info c ON u.id = c.user_id WHERE c.number = :number", nativeQuery = true)
    Optional<User> findUserByCardNumber(@Param("number") String number);

    // JPQL
    @Query("SELECT u FROM User u")
    Page<User> findAllUsers(Pageable pageable);

    //JPQL
    @Modifying(clearAutomatically = true) //todo for tests
    @Query("UPDATE User u SET u.name = :name, u.surname = :surname, u.email = :email WHERE u.id = :id")
    int updateUserInfo(@Param("id") Long id,
                       @Param("name") String name,
                       @Param("surname") String surname,
                       @Param("email") String email);


    // Native SQL
    @Modifying
    @Query(value = "DELETE FROM users WHERE email = :email", nativeQuery = true)
    int deleteUserByEmail(@Param("email") String email);

    // built-in:
    // save(User user)
    // deleteById(Long id)

}