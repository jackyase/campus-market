package org.example.demo4.message;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("select m from Message m where m.senderId = :userId or m.receiverId = :userId order by m.createdAt desc")
    List<Message> findRecentByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("select m from Message m where (m.senderId = :a and m.receiverId = :b) or (m.senderId = :b and m.receiverId = :a) order by m.createdAt asc")
    List<Message> findConversation(@Param("a") Long a, @Param("b") Long b);

    @Query("select m.senderId, count(m) from Message m where m.receiverId = :userId and m.read = false group by m.senderId")
    List<Object[]> countUnreadBySender(@Param("userId") Long userId);

    @Query("select count(m) from Message m where m.receiverId = :userId and m.read = false")
    long countUnread(@Param("userId") Long userId);

    @Modifying
    @Query("update Message m set m.read = true where m.senderId = :other and m.receiverId = :me and m.read = false")
    int markConversationRead(@Param("me") Long me, @Param("other") Long other);
}
