package org.babyfish.graphql.provider.kimmer.runtime

import org.babyfish.graphql.provider.kimmer.meta.ImmutableType

internal interface ImmutableImpl {
    fun `{type}`(): ImmutableType
    fun `{loaded}`(prop: String): Boolean
    fun `{throwable}`(prop: String): Throwable?
    fun `{value}`(prop: String): Any?
}