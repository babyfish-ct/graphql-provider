package org.babyfish.graphql.provider.starter.meta

import org.babyfish.graphql.provider.starter.dsl.BatchImplementationContext
import org.babyfish.graphql.provider.starter.dsl.ImplementationContext

interface UserImplementation {
    val batchSize: Int?
    val single: (suspend (ImplementationContext<*>) -> Any?)?
    val batch: (suspend (BatchImplementationContext<*>) -> Map<*, *>)?
}