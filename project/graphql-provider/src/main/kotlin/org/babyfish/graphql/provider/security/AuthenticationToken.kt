package org.babyfish.graphql.provider.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class AuthenticationToken(
    principal: Any,
    authorities: Collection<GrantedAuthority>
): UsernamePasswordAuthenticationToken(
    principal,
    "",
    authorities.distinct()
)