package org.babyfish.graphql.provider.example.security

import org.babyfish.graphql.provider.example.model.security.AppUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class AppUserDetails(
    val appUser: AppUser
): UserDetails {

    private val _authorities = appUser.roles.map { SimpleGrantedAuthority(it.name) }

    override fun getUsername(): String =
        appUser.email

    override fun getPassword(): String =
        appUser.password

    override fun getAuthorities(): Collection<GrantedAuthority> =
        _authorities

    override fun isEnabled(): Boolean = true

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true
}