package com.sameboat.backend.user;

import com.sameboat.backend.auth.AuthPrincipal;
import com.sameboat.backend.common.ResourceNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Hardened user read endpoint returning a public DTO (no email). Disabled by default and
 * enabled only when property sameboat.endpoints.user-read=true. Access is allowed only
 * for the authenticated user (self) or users with role ADMIN.
 * This class is stubbed for future expansion when more public user fields are added.
 */
@RestController
@RequestMapping({"","/api"})
@ConditionalOnProperty(prefix = "sameboat.endpoints", name = "user-read", havingValue = "true")
public class UserReadController {

    private final UserService userService;

    public UserReadController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getPublicUser(Authentication authentication, @PathVariable("id") UUID id) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal ap)) {
            // Security config usually handles 401, but return 401 if unauthenticated reaches here
            return ResponseEntity.status(401).build();
        }
        boolean isSelf = ap.userId().equals(id);
        boolean isAdmin = ap.role() != null && ap.role().equalsIgnoreCase("ADMIN");
        if (!isSelf && !isAdmin) {
            return ResponseEntity.status(403).build();
        }
        var user = userService.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(UserMapper.toPublicDto(user));
    }
}

