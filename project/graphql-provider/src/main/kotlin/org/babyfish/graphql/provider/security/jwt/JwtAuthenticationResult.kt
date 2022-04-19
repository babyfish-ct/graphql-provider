package org.babyfish.graphql.provider.security.jwt

data class JwtAuthenticationResult(
    val accessToken: String,
    val refreshToken: String
)