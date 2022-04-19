package org.babyfish.graphql.provider.security.cfg

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import org.babyfish.graphql.provider.security.jwt.JwtTokenException
import org.babyfish.graphql.provider.security.jwt.JwtUpdatePasswordException

fun Throwable.toGraphQLErrorOrNull(): GraphQLError? =
    when (this) {
        is JwtTokenException ->
            "JWT_TOKEN.${reason.name}"
        is JwtUpdatePasswordException ->
            reason.name
        else ->
            null
    }?.let {
        GraphqlErrorBuilder.newError()
            .message("%s: %s", this::class.simpleName, this.message)
            .extensions(
                mapOf("reason" to it)
            )
            .build()
    }

fun Throwable.toGraphQLError(): GraphQLError =
    toGraphQLErrorOrNull() ?:
    GraphqlErrorBuilder.newError()
        .message("%s: %s", this::class.simpleName, this.message)
        .build()