package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.*
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.meta.spi.EntityTypeImpl
import org.babyfish.kimmer.sql.meta.spi.MetaFactory
import java.util.*

internal class ModelTypeImpl(
    metaFactory: MetaFactory,
    immutableType: ImmutableType
): EntityTypeImpl(metaFactory, immutableType), ModelType {

    private var _isMapped: Boolean = false

    override val cache: Cache = Cache(CacheLevel.NO_CACHE)

    override var graphql: ModelType.GraphQL = ModelType.GraphQL(null, null)

    @Suppress("UNCHECKED")
    override val props: Map<String, ModelProp>
        get() = super.props as Map<String, ModelProp>

    var isMapped: Boolean
        get() = _isMapped
        set(value) { _isMapped = true }
}