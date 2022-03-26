package org.babyfish.graphql.provider

import org.babyfish.graphql.provider.dsl.FilterDSL
import org.babyfish.graphql.provider.meta.impl.NoReturnValue
import org.babyfish.graphql.provider.runtime.filterExecutionContext
import org.babyfish.graphql.provider.runtime.registerQueryField
import org.babyfish.kimmer.graphql.Connection
import org.babyfish.kimmer.sql.Entity

abstract class Query {

    protected val runtime: Runtime = Runtime()

    inner class Runtime internal constructor() {

        fun <N : Entity<NID>, NID : Comparable<NID>> queryConnection(
            filterBlock: FilterDSL<N, NID>.() -> Unit
        ): Connection<N> {
            query(filterBlock)
        }

        fun <E : Entity<EID>, EID : Comparable<EID>> queryList(
            filterBlock: FilterDSL<E, EID>.() -> Unit
        ): List<E> {
            query(filterBlock)
        }

        fun <R : Entity<RID>, RID : Comparable<RID>> queryReference(
            filterBlock: FilterDSL<R, RID>.() -> Unit
        ): R {
            query(filterBlock)
        }

        private fun <E : Entity<ID>, ID : Comparable<ID>> query(
            filterBlock: FilterDSL<E, ID>.() -> Unit
        ): Nothing {
            if (registerQueryField(this@Query)) {
                throw NoReturnValue()
            }
            FilterDSL<E, ID>(filterExecutionContext).filterBlock()
            throw NoReturnValue()
        }
    }
}
