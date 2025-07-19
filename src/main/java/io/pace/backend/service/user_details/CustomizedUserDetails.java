package io.pace.backend.service.user_details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.pace.backend.data.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor
@Data
public class CustomizedUserDetails implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public CustomizedUserDetails(Long id, String username, String email,
                                 String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static CustomizedUserDetails build(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getRoleState().name());
        return new CustomizedUserDetails(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getPassword(),
                List.of(authority) // wrapping the single authority in a list
        );
    }

}

