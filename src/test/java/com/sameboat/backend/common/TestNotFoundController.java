package com.sameboat.backend.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestNotFoundController {

    @GetMapping("/test/notfound")
    public void triggerNotFound() {
        throw new ResourceNotFoundException("User not found");
    }
}

