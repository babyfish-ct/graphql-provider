package org.babyfish.graphql.provider.security.jwt

import io.jsonwebtoken.*
import io.jsonwebtoken.io.Encoders
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.babyfish.graphql.provider.runtime.cfg.GraphQLProviderProperties
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component
internal class JwtTokenService(
    properties: GraphQLProviderProperties
) {
    private val jwt = properties.security.jwt

    private val key = properties.security.jwt.secret.takeIf { it.isNotBlank() }?.let {
        Keys.hmacShaKeyFor(it.toByteArray())
    } ?: Keys.secretKeyFor(SignatureAlgorithm.HS256).also {
        if (logger.isWarnEnabled) {
            logger.warn(
                "The generated security key of JWT is '{}'. " +
                    "If you do not want to use the generated value, " +
                    "please configure '${
                        GraphQLProviderProperties.Security.Jwt.PROPERTY_PATH
                    }.${
                        GraphQLProviderProperties.Security.Jwt::secret.name
                    }'",
                Encoders.BASE64.encode(it.encoded)
            )
        }
    }

    fun accessTokenString(userDetails: UserDetails): String {
        val now = Instant.now()
        return Jwts.builder()
            .setSubject(userDetails.username)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now + jwt.accessTimeout))
            .apply {
                if (jwt.thinAuthentication) {
                    setClaims(mapOf(
                        "roles" to userDetails.authorities.map { it.authority }
                    ))
                }
            }
            .compact()
    }

    fun refreshTokenString(userDetails: UserDetails): String {
        val now = Instant.now()
        return Jwts.builder()
            .setSubject(userDetails.username)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now + jwt.refreshTimeout))
            .compact()
    }

    @Suppress("UNCHECKED_CAST")
    fun fromTokenString(token: String): JwtToken {
        val parser = Jwts.parserBuilder().setSigningKey(key).build()
        val body = try {
            parser.parseClaimsJwt(token).body
        } catch (ex: UnsupportedJwtException) {
            throw JwtTokenException(JwtTokenException.Reason.UNSUPPORTED, ex)
        } catch (ex: MalformedJwtException) {
            throw JwtTokenException(JwtTokenException.Reason.MALFORMED, ex)
        } catch (ex: SignatureException) {
            throw JwtTokenException(JwtTokenException.Reason.ILLEGAL_SIGNATURE, ex)
        } catch (ex: ExpiredJwtException) {
            throw JwtTokenException(JwtTokenException.Reason.EXPIRED, ex)
        } catch (ex: Throwable) {
            throw ex
        }
        return JwtToken(
            body.subject,
            (body["roles"] as List<String>?)
                ?.map { SimpleGrantedAuthority(it) }
        )
    }

    companion object {

        @JvmStatic
        private val logger = LoggerFactory.getLogger(JwtTokenService::class.java)
    }
}
