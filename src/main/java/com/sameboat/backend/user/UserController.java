package com.sameboat.backend.user;

import com.sameboat.backend.auth.AuthPrincipal;
import com.sameboat.backend.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"","/api"})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication, HttpServletRequest request) {
        if (Boolean.TRUE.equals(request.getAttribute("sameboat.sessionExpired"))) {
            return ResponseEntity.status(401).body(new ErrorResponse("SESSION_EXPIRED", "Session expired"));
        }
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal ap)) {
            return ResponseEntity.status(401).body(new ErrorResponse("UNAUTHENTICATED", "Authentication required"));
        }
        return userService.findById(ap.userId())
                .map(UserMapper::toDto)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(401).body(new ErrorResponse("UNAUTHENTICATED", "Authentication required")));
    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateMe(Authentication authentication, @RequestBody @Valid UpdateUserRequest request, HttpServletRequest httpRequest) {
        if (Boolean.TRUE.equals(httpRequest.getAttribute("sameboat.sessionExpired"))) {
            return ResponseEntity.status(401).body(new ErrorResponse("SESSION_EXPIRED", "Session expired"));
        }
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal ap)) {
            return ResponseEntity.status(401).body(new ErrorResponse("UNAUTHENTICATED", "Authentication required"));
        }
        if (request.displayName() == null && request.avatarUrl() == null && request.bio() == null && request.timezone() == null) {
            return ResponseEntity.status(400).body(new ErrorResponse("VALIDATION_ERROR", "At least one field must be provided"));
        }
        UUID uid = ap.userId();
        var user = userService.findById(uid).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(new ErrorResponse("UNAUTHENTICATED", "Authentication required"));
        }
        var updated = userService.updatePartial(user, request);
        return ResponseEntity.ok(UserMapper.toDto(updated));
    }
}
