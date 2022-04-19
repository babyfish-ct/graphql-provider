package org.babyfish.graphql.provider.security

import graphql.schema.DataFetchingEnvironment
import org.springframework.security.core.Authentication

interface AuthenticationExtractor {

    fun get(env: DataFetchingEnvironment): Authentication?
}