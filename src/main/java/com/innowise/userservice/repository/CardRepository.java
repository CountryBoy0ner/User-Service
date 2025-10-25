package com.innowise.userservice.repository;

import com.innowise.userservice.model.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {



    Optional<Card> findByNumber(String number);

    Page<Card> findByHolderContainingIgnoreCase(String holder, Pageable pageable);

    @Query(value = "SELECT * FROM card_info WHERE user_id = :userId", nativeQuery = true)
    List<Card> findAllByUserIdNative(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Card c SET c.holder = :holder, c.expirationDate = :expirationDate WHERE c.id = :id")
    int updateCardInfo(@Param("id") Long id,
                       @Param("holder") String holder,
                       @Param("expirationDate") String expirationDate);

    long deleteByNumber(String number);
    //BeanPostProcessor postProcessor();
}
