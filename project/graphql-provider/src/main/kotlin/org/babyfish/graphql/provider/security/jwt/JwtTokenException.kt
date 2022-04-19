package org.babyfish.graphql.provider.security.jwt

import io.jsonwebtoken.JwtException
import org.springframework.security.core.AuthenticationException

class JwtTokenException(
    val reason: Reason,
    cause: JwtException
) : AuthenticationException(cause.message, cause) {

    enum class Reason {
        UNSUPPORTED,
        MALFORMED,
        ILLEGAL_SIGNATURE,
        EXPIRED
    }
}