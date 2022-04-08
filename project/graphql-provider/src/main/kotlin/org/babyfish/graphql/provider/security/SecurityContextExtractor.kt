package org.babyfish.graphql.provider.security

import graphql.schema.DataFetchingEnvironment
import org.springframework.security.core.context.SecurityContext

interface SecurityContextExtractor {

    fun get(env: DataFetchingEnvironment): SecurityContext?
}