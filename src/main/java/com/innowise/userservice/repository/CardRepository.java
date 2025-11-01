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
    @Query(value = """
            UPDATE card_info
               SET holder = :holder,
                   expiration_date = to_date(:expirationDate, 'YYYY-MM-DD')
             WHERE id = :id
            """, nativeQuery = true)
    int updateCardInfo(@Param("id") Long id,
                       @Param("holder") String holder,
                       @Param("expirationDate") String expirationDate);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Card c WHERE c.number = :number")
    int deleteByNumberInternal(@Param("number") String number);

    default long deleteByNumber(String number) {
        return (long) deleteByNumberInternal(number);
    }
}
