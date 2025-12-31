package com.ie207.vagabond.response;

import com.ie207.vagabond.model.User;
import com.ie207.vagabond.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserWithMessageResponse {
    private String _id;
    private String email;
    private Role role;
    private LocalDateTime latestMessageTime;
    private Long unreadCount;
    private String firstName;
    private String lastName;
    private String avatarUrl = "";

    public UserWithMessageResponse(User user, LocalDateTime latestMessageTime, Long unreadCount) {
        this._id = user.get_id();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.avatarUrl = user.getAvatarUrl();
        this.latestMessageTime = latestMessageTime;
        this.unreadCount = unreadCount;
    }
}
