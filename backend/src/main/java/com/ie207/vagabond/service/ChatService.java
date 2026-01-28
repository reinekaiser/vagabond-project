package com.ie207.vagabond.service;

import com.ie207.vagabond.exception.ReceiverNotFound;
import com.ie207.vagabond.exception.SenderNotFound;
import com.ie207.vagabond.model.Message;
import com.ie207.vagabond.model.User;
import com.ie207.vagabond.model.enums.Role;
import com.ie207.vagabond.repository.MessageRepository;
import com.ie207.vagabond.repository.UserRepository;
import com.ie207.vagabond.response.UserWithMessageResponse;
import com.ie207.vagabond.websocket.listener.WebSocketEventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final WebSocketEventListener eventListener;
    private final SimpMessagingTemplate messagingTemplate;

    public List<User> getAllUsers(String currentUserId){
        User currentUser = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("User not found"));
        if (currentUser == null){
            throw new RuntimeException("Something went wrong...");
        }

        List<User> allUsers = userRepository.findAll();

        return allUsers.stream()
                .filter(user -> !user.get_id().equals(currentUser.get_id()))
                .filter(user -> {
                    if (Role.USER.equals(currentUser.getRole())) {
                        return Role.ADMIN.equals(user.getRole());
                    }
                    // Admin chỉ thấy user
                    else if (Role.ADMIN.equals(currentUser.getRole())) {
                        return Role.USER.equals(user.getRole());
                    }
                    return true;
                }).collect(Collectors.toList());
    }

    @Transactional
    public Message sendMessage(String senderId, String receiverId, String text) {

        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Message text cannot be empty");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = null;
        if (receiverId != null) {
            receiver = userRepository.findById(receiverId)
                    .orElseThrow(() -> new RuntimeException("Receiver not found"));
        }

        Message message = Message.builder()
                                .sender(sender)
                                .receiver(receiver)
                                .text(text)
                                .build();

        Message savedMessage = messageRepository.save(message);

        System.out.println("✅ Message saved to DB: " + savedMessage.get_id());


        String receiverSocketId = eventListener.getReceiverSocketId(receiver.get_id());

        if (receiverSocketId != null) {
            messagingTemplate.convertAndSendToUser(
                    receiver.get_id(),
                    "/queue/messages",
                    message
            );
            System.out.println("✅ Sent to user: " + receiver.get_id());
        } else {
            messagingTemplate.convertAndSendToUser(
                    receiver.get_id(),
                    "/queue/messages",
                    message
            );
            System.out.println("⚠️ User offline, message saved but not delivered");
        }

        return message;
    }

    public List<UserWithMessageResponse> getUsersToChat (String currentUserId) {
        User currentUser = userRepository.findById(currentUserId).orElseThrow(() -> new RuntimeException("User not found"));
        if (currentUser == null){
            throw new RuntimeException("Something went wrong...");
        }

        List<User> allUsers = userRepository.findAll();

        List<User> filteredUsers = allUsers.stream()
                .filter(user -> !user.get_id().equals(currentUser.get_id()))
                .filter(user -> {
                    if (Role.USER.equals(currentUser.getRole())) {
                        return Role.ADMIN.equals(user.getRole());
                    }
                    // Admin chỉ thấy user
                    else if (Role.ADMIN.equals(currentUser.getRole())) {
                        return Role.USER.equals(user.getRole());
                    }
                    return true;
                })
                .toList();


        List<UserWithMessageResponse> userWithMessages = new ArrayList<>();

        for(User user : filteredUsers){
            List<Message> messages = messageRepository.findMessagesBetweenUsers(
                    currentUser.get_id(),
                    user.get_id(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );

            Message latestMessage = messages.isEmpty() ? null : messages.getFirst();
            Long unreadCount = messageRepository.countUnreadMessages(user.get_id(), currentUser.get_id());

            if(Role.USER.equals(currentUser.getRole()) || latestMessage != null){
                userWithMessages.add(new UserWithMessageResponse(user, latestMessage != null ? latestMessage.getCreatedAt() : null, unreadCount));
            }
        }
        userWithMessages.sort((a, b) -> {
            if (a.getUnreadCount() > 0 && b.getUnreadCount() == 0) {
                return -1;
            }
            if (a.getUnreadCount() == 0 && b.getUnreadCount() == 0) {
                return 1;
            }
            if (a.getLatestMessageTime() == null && b.getLatestMessageTime() == null) return 0;

            if(a.getLatestMessageTime() == null) return 1;
            if(b.getLatestMessageTime() == null) return -1;

            return b.getLatestMessageTime().compareTo(a.getLatestMessageTime());
        });
        return userWithMessages;
    }
    @Transactional
    public void markMessageAsRead(String currentUserId, String senderId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new SenderNotFound("Sender not found"));

        User receiver = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ReceiverNotFound("Receiver not found"));

        List<Message> unreadMessages = messageRepository.findUnreadMessages(sender.get_id(), receiver.get_id());

        for (Message message : unreadMessages) {
            message.setIsRead(true);
        }

        messageRepository.saveAll(unreadMessages);
    }

    @Transactional
    public List<Message> getChatHistory(String currentUserId, String otherUserId) {

        List<Message> messages = messageRepository.findMessagesBetweenUsers(
                currentUserId,
                otherUserId,
                Sort.by(Sort.Direction.ASC, "createdAt")
        );

        return messages;
    }
}
