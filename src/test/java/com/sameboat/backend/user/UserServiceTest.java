package com.sameboat.backend.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UserService.class)
class UserServiceTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;

    @Test
    void findOrCreateCreates() {
        var u = userService.findOrCreateByEmail("new@example.com");
        assertThat(u.getId()).isNotNull();
        assertThat(userRepository.findByEmailIgnoreCase("new@example.com")).isPresent();
    }

    @Test
    void updatePartialAppliesOnlyProvided() {
        var u = userService.findOrCreateByEmail("upd@example.com");
        var originalDisplay = u.getDisplayName();
        UpdateUserRequest req = new UpdateUserRequest(null, "http://a", null, null);
        var updated = userService.updatePartial(u, req);
        assertThat(updated.getAvatarUrl()).isEqualTo("http://a");
        assertThat(updated.getDisplayName()).isEqualTo(originalDisplay); // unchanged
    }
}

