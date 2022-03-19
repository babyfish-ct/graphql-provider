package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.*
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.meta.spi.EntityTypeImpl
import org.babyfish.kimmer.sql.meta.spi.MetaFactory

internal class ModelTypeImpl(
    metaFactory: MetaFactory,
    immutableType: ImmutableType
): EntityTypeImpl(metaFactory, immutableType), ModelType {

    private var _isMapped: Boolean = false

    override val cache: Cache = Cache(CacheLevel.NO_CACHE)

    override var graphql: ModelType.GraphQL = ModelType.GraphQL(immutableType.simpleName, null, null)

    override val idProp: ModelProp
        get() = super.idProp as ModelProp

    override val versionProp: ModelProp?
        get() = super.versionProp as ModelProp?

    @Suppress("UNCHECKED_CAST")
    override val declaredProps: Map<String, ModelProp>
        get() = super.declaredProps as Map<String, ModelProp>

    @Suppress("UNCHECKED_CAST")
    override val props: Map<String, ModelProp>
        get() = super.props as Map<String, ModelProp>

    @Suppress("UNCHECKED_CAST")
    override val backProps: Set<ModelProp>
        get() = super.backProps as Set<ModelProp>

    var isMapped: Boolean
        get() = _isMapped
        set(value) { _isMapped = value }
}