package org.babyfish.graphql.provider.security.jwt

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

internal class JwtToken(
    private val jwt: String,
    deserializedAuthorities: Collection<GrantedAuthority>? = null
) : AbstractAuthenticationToken(deserializedAuthorities) {

    val hasDeserializedAuthorities: Boolean =
        deserializedAuthorities !== null

    override fun getPrincipal(): String =
        jwt

    override fun getCredentials(): String =
        jwt
}