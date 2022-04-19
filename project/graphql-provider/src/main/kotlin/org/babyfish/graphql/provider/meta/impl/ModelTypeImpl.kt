package org.babyfish.graphql.provider.meta.impl

import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.meta.*
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.spi.EntityTypeImpl
import org.babyfish.kimmer.sql.meta.spi.MetaFactory

internal class ModelTypeImpl(
    metaFactory: MetaFactory,
    immutableType: ImmutableType,
): EntityTypeImpl(metaFactory, immutableType), ModelType {

    private var _isMapped: Boolean = false

    private var _graphql: ModelGraphQL = ModelGraphQL(
        immutableType.simpleName,
        null,
        null
    )

    private var _securityPredicate: SecurityPredicate? = null

    private var _flags = 0

    override val name: String
        get() = immutableType.simpleName

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

    override val securityPredicate: SecurityPredicate?
        get() = _securityPredicate

    internal fun setMapped() {
        _isMapped = true
    }

    internal fun setGraphQL(graphql: ModelGraphQL) {
        if ((_flags and GRAPHQL_CONFIGURED) != 0) {
            throw ModelException("The 'graphql {...}' of '${immutableType.qualifiedName}' can only be configured once")
        }
        _graphql = graphql
        _flags = _flags or GRAPHQL_CONFIGURED
    }

    internal fun setSecurityPredicate(predicate: SecurityPredicate?) {
        if ((_flags and SECURITY_PREDICATE_CONFIGURED) != 0) {
            throw ModelException("The 'security {...}' of '${immutableType.qualifiedName}' can only be configured once")
        }
        _securityPredicate = predicate
        _flags = _flags or SECURITY_PREDICATE_CONFIGURED
    }

    companion object {
        private const val GRAPHQL_CONFIGURED = 1 shl 0
        private const val SECURITY_PREDICATE_CONFIGURED = 1 shl 1
    }
}