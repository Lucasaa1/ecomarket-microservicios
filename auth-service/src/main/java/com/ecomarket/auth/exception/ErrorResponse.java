package com.ecomarket.auth.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String mensaje
) {
}
