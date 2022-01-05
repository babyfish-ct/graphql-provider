package org.babyfish.graphql.provider.kimmer.runtime

interface ImmutableImpl {
    fun `{loaded}`(prop: String): Boolean
    fun `{throwabe}`(prop: String): Throwable?
    fun `{value}`(prop: String): Any?
}