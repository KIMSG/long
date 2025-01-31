package com.longleg.repository;

import com.longleg.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Modifying
    @Query(value = """
        UPDATE users u
        SET u.reward = u.reward + :points
        WHERE u.id = :userId
        """, nativeQuery = true)
    void updateUserReward(@Param("userId") Long userId, @Param("points") int points);

}
