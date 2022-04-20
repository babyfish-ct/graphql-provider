package org.babyfish.graphql.provider.security.jwt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.babyfish.graphql.provider.security.AuthenticationBehaviorProvider
import org.babyfish.graphql.provider.security.authenticationOrNull
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
internal open class JwtAuthenticationServiceImpl(
    private val userDetailsService: ReactiveUserDetailsService,
    private val userDetailsPasswordService: ReactiveUserDetailsPasswordService,
    private val jwtTokenService: JwtTokenService,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationBehaviorProvider: AuthenticationBehaviorProvider<*>,
) : JwtAuthenticationService {

    @Suppress("UNCHECKED_CAST")
    override suspend fun login(
        username: String,
        password: String
    ): JwtAuthenticationResult =
        userDetailsService
            .findByUsername(username)
            .awaitSingleOrNull()?.let {
                val provider = authenticationBehaviorProvider as AuthenticationBehaviorProvider<UserDetails>
                if (matches(password, it.password)) {
                    provider.afterLogin(it, true)
                    JwtAuthenticationResult(
                        jwtTokenService.accessTokenString(it),
                        jwtTokenService.refreshTokenString(it)
                    )
                } else {
                    provider.afterLogin(it, false)
                    null
                }
            } ?: throw UsernameNotFoundException("Illegal username/password")

    @Suppress("UNCHECKED_CAST")
    override suspend fun encryptedUser(
        user: UserDetails
    ): UserDetails {
        authenticationBehaviorProvider.validateRawPassword(user.password)
        val provider = authenticationBehaviorProvider as AuthenticationBehaviorProvider<UserDetails>
        return provider.recreateUser(user, encrypt(user.password))
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun createUser(
        user: UserDetails,
        isPasswordEncoded: Boolean
    ): JwtAuthenticationResult {
        val encodedUser = if (isPasswordEncoded) {
            user
        } else {
            encryptedUser(user)
        }
        val provider = authenticationBehaviorProvider as AuthenticationBehaviorProvider<UserDetails>
        provider.insertUser(encodedUser)
        return JwtAuthenticationResult(
            jwtTokenService.accessTokenString(encodedUser),
            jwtTokenService.refreshTokenString(encodedUser)
        )
    }

    override suspend fun updatePassword(
        oldPassword: String,
        newPassword: String
    ): JwtAuthenticationResult {
        authenticationBehaviorProvider.validateRawPassword(newPassword)
        val user = authenticationOrNull()?.principal as? UserDetails
            ?: throw JwtUpdatePasswordException(JwtUpdatePasswordException.Reason.UNAUTHENTICATED)
        if (!matches(oldPassword, user.password)) {
            throw JwtUpdatePasswordException(JwtUpdatePasswordException.Reason.ILLEGAL_OLD_PASSWORD)
        }
        val encodedNewPassword = encrypt(newPassword)
        userDetailsPasswordService.updatePassword(user, encodedNewPassword)
        val newUser = userDetailsService.findByUsername(user.username)
            .awaitSingleOrNull()
            ?: throw JwtUpdatePasswordException(JwtUpdatePasswordException.Reason.CONCURRENT_DELETED)
        return JwtAuthenticationResult(
            jwtTokenService.accessTokenString(newUser),
            jwtTokenService.refreshTokenString(newUser)
        )
    }

    override suspend fun refreshAccessToken(
        refreshToken: String
    ): JwtAuthenticationResult {
        val token = jwtTokenService.fromTokenString(refreshToken)
        val user = userDetailsService
            .findByUsername(token.principal)
            .awaitSingleOrNull()
            ?: throw IllegalArgumentException("Illegal user specified by refresh token")
        return JwtAuthenticationResult(
            jwtTokenService.accessTokenString(user),
            refreshToken
        )
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun encrypt(password: String): String {
        val provider = authenticationBehaviorProvider as AuthenticationBehaviorProvider<UserDetails>
        return withContext(Dispatchers.Default) {
            passwordEncoder.encode(password)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun matches(rawPassword: String, encodedPassword: String): Boolean {
        val provider = authenticationBehaviorProvider as AuthenticationBehaviorProvider<UserDetails>
        return withContext(Dispatchers.Default) {
            passwordEncoder.matches(rawPassword, encodedPassword)
        }
    }
}