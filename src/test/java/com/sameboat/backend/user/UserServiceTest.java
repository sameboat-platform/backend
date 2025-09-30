package com.sameboat.backend.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UserService.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "sameboat.security.enabled=false")
class UserServiceTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void registerNewCreates() {
        var u = userService.registerNew("new@example.com", "StrongPass1!", encoder);
        assertThat(u.getId()).isNotNull();
        assertThat(userRepository.findByEmailIgnoreCase("new@example.com")).isPresent();
    }

    @Test
    void updatePartialAppliesOnlyProvided() {
        var u = userService.registerNew("upd@example.com", "StrongPass2!", encoder);
        var originalDisplay = u.getDisplayName();
        UpdateUserRequest req = new UpdateUserRequest(null, "http://a", null, null);
        var updated = userService.updatePartial(u, req);
        assertThat(updated.getAvatarUrl()).isEqualTo("http://a");
        assertThat(updated.getDisplayName()).isEqualTo(originalDisplay); // unchanged
    }
}
