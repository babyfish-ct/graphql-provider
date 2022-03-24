package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.meta.*
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.spi.EntityTypeImpl
import org.babyfish.kimmer.sql.meta.spi.MetaFactory

internal class ModelTypeImpl(
    metaFactory: MetaFactory,
    immutableType: ImmutableType
): EntityTypeImpl(metaFactory, immutableType), ModelType {

    private var _isMapped: Boolean = false

    private var _graphql: ModelGraphQL = ModelGraphQL(
        immutableType.simpleName,
        null,
        null
    )

    override val superType: ModelType?
        get() = super.superType as ModelType?

    @Suppress("UNCHECKED_CAST")
    override val derivedTypes: List<ModelType>
        get() = super.derivedTypes as List<ModelType>

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

    val isMapped: Boolean
        get() = _isMapped

    override val graphql: ModelGraphQL
        get() = _graphql

    internal fun setMapped() {
        _isMapped = true
    }

    internal fun setGraphQL(graphql: ModelGraphQL) {
        _graphql = graphql
    }
}