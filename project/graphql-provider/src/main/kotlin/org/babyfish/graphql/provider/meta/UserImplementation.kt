package org.babyfish.graphql.provider.meta

import org.babyfish.graphql.provider.dsl.BatchImplementationContext
import org.babyfish.graphql.provider.dsl.ImplementationContext

interface UserImplementation {
    val batchSize: Int?
    val single: (suspend (ImplementationContext<*>) -> Any?)?
    val batch: (suspend (BatchImplementationContext<*>) -> Map<*, *>)?
}