package org.babyfish.graphql.provider.kimmer.runtime

import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType

internal interface ImmutableSpi {
    fun `{type}`(): ImmutableType
    fun `{loaded}`(prop: String): Boolean
    fun `{throwable}`(prop: String): Throwable?
    fun `{value}`(prop: String): Any?
    fun hashCode(shallow: Boolean): Int
    fun equals(other: Any?, shallow: Boolean): Boolean
}

internal interface DraftSpi: ImmutableSpi {
    fun `{throwable}`(prop: String, throwable: Throwable?): Unit
    fun `{value}`(prop: String, value: Any?): Unit
    fun `{resolve}`(): Immutable
}