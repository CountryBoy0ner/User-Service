package com.innowise.userservice.repository;

import com.innowise.userservice.model.Card;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Transactional
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    // Named
    Optional<Card> findByNumber(String number);

    // JPQL
    @Query("SELECT c FROM Card c WHERE c.holder LIKE %:holder%")
    List<Card> findCardsByHolder(@Param("holder") String holder);

    //
    @Query(value = "SELECT * FROM card_info WHERE user_id = :userId", nativeQuery = true)
    List<Card> findAllByUserIdNative(@Param("userId") Long userId);

    // JPQL
    @Query("SELECT c FROM Card c")
    Page<Card> findAllCards(Pageable pageable);

    // JPQL
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Card c SET c.holder = :holder, c.expirationDate = :expirationDate WHERE c.id = :id")
    int updateCardInfo(@Param("id") Long id,
                       @Param("holder") String holder,
                       @Param("expirationDate") String expirationDate);

    // Native SQL
    @Modifying
    @Query(value = "DELETE FROM card_info WHERE number = :number", nativeQuery = true)
    int deleteCardByNumber(@Param("number") String number);

    // built-in:
    // save(Card card)
    // deleteById(Long id)
}
