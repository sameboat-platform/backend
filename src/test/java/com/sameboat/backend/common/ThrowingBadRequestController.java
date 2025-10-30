package com.sameboat.backend.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ThrowingBadRequestController {
    @GetMapping("/test/badrequest")
    public void bad() { throw new IllegalArgumentException("Invalid state"); }
}

