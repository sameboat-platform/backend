package com.sameboat.backend.user;

import com.sameboat.backend.common.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(UserService.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "sameboat.security.enabled=false")
class UserServiceNotFoundTest {

    @Autowired
    UserService userService;

    @Test
    @DisplayName("getByIdOrThrow throws ResourceNotFoundException when user missing")
    void getByIdOrThrow_missing() {
        UUID random = UUID.randomUUID();
        assertThatThrownBy(() -> userService.getByIdOrThrow(random))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }
}

