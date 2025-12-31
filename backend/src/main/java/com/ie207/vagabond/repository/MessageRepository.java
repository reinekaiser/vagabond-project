package com.ie207.vagabond.repository;

import com.ie207.vagabond.model.Message;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message,String> {
    @Query("{ $or: [" +
            "{'sender._id': ?0, 'receiver._id': ?1}, " +
            "{'sender._id': ?1, 'receiver._id': ?0}  " +
            "] }")
    List<Message> findMessagesBetweenUsers(String userId1, String userId2, Sort sort);

    @Query("{ $or: [ " +
            "  { 'sender._id': ?0, 'receiver._id': ?1 }, " +
            "  { 'sender._id': ?1, 'receiver._id': ?0 } " +
            "] }")
    List<Message> findLatestMessageBetweenUsers(String userId1, String userId2, Sort sort);

    @Query(value = "{ 'sender._id': ?0, 'receiver._id': ?1, 'isRead': false }", count = true)
    Long countUnreadMessages(String senderId, String receiverId);

    @Query("{ 'sender._id': ?0, 'receiver._id': ?1, 'isRead': false }")
    List<Message> findUnreadMessages(String senderId, String receiverId);
}
