package org.babyfish.graphql.provider.starter.meta.impl

import org.babyfish.graphql.provider.starter.ModelException
import org.babyfish.graphql.provider.starter.runtime.GraphQLTypeGenerator
import org.babyfish.graphql.provider.starter.meta.*
import org.babyfish.kimmer.meta.ImmutableType

internal class EntityTypeImpl(
    override val immutableType: ImmutableType
): EntityType {

    private val _superTypes = mutableListOf<EntityTypeImpl>()

    private val _derivedTypes = mutableListOf<EntityTypeImpl>()

    private var _props: Map<String, EntityProp>? = null

    private var _idProp: EntityProp? = null

    private var _expectedPhase = ResolvingPhase.SUPER_TYPE.ordinal

    override val name: String
        get() = immutableType.simpleName

    override var isMapped: Boolean = false

    override val database = DatabaseImpl()

    override val redis = RedisImpl()

    override val graphql = GraphQLImpl()

    override val superTypes: List<EntityType>
        get() = _superTypes

    override val derivedTypes: List<EntityType>
        get() = _derivedTypes

    override val idProp: EntityProp
        get() = _idProp ?: error("Id property has not been resolved")

    override val declaredProps = mutableMapOf<String, EntityPropImpl>()

    override val props: Map<String, EntityProp>
        get() = _props ?: error("Properties have not been resolved")

    fun resolve(generator: GraphQLTypeGenerator, phase: ResolvingPhase) {
        if (shouldResolve(phase)) {
            when (phase) {
                ResolvingPhase.SUPER_TYPE -> resolveSuperTypes(generator)
                ResolvingPhase.DECLARED_PROPS -> resolveDeclaredProps(generator)
                ResolvingPhase.PROPS -> resolveProps(generator)
                ResolvingPhase.ID_PROP -> resolveIdProp()
                else -> resolvePropDetail(generator, phase)
            }
        }
    }

    private fun shouldResolve(phase: ResolvingPhase): Boolean =
        if (_expectedPhase == phase.ordinal) {
            _expectedPhase++
            true
        } else {
            false
        }

    private fun resolveSuperTypes(generator: GraphQLTypeGenerator) {
        for (superImmutableType in immutableType.superTypes) {
            val superType = generator[superImmutableType]
            _superTypes += superType
            superType._derivedTypes += this
        }
    }

    private fun resolveDeclaredProps(generator: GraphQLTypeGenerator) {
        for (immutableProp in immutableType.declaredProps.values) {
            if (!declaredProps.containsKey(immutableProp.name)) {
                if (immutableProp.isAssociation) {
                    throw ModelException(
                        "The property '${immutableProp}' is association " +
                            "but it is not configured by any EntityAssembler"
                    )
                }
                declaredProps[immutableProp.name] = EntityPropImpl(
                    this,
                    immutableProp.kotlinProp
                )
            }
        }
    }

    private fun resolveProps(generator: GraphQLTypeGenerator) {
        for (superType in _superTypes) {
            superType.resolve(generator, ResolvingPhase.PROPS)
        }
        if (_superTypes.isEmpty()) {
            _props = declaredProps
        } else {
            val map = mutableMapOf<String, EntityProp>()
            map += declaredProps
            for (superType in _superTypes) {
                if (generator[superType.immutableType].isMapped) {
                    for (superProp in superType.props.values) {
                        val prop = map[superProp.name]
                        if (prop !== null) {
                            if (!superProp.isId) {
                                throw ModelException(
                                    "Duplicate properties: '$superProp' and '$prop'"
                                )
                            }
                        } else {
                            map[superProp.name] = superProp
                        }
                    }
                }
            }
            _props = map
        }
    }

    private fun resolvePropDetail(generator: GraphQLTypeGenerator, phase: ResolvingPhase) {
        for (declaredProp in declaredProps.values) {
            declaredProp.resolve(generator, phase)
        }
    }

    private fun resolveIdProp(): EntityProp? {

        if (superTypes.isNotEmpty()) {
            var superIdProp: EntityProp? = null
            for (superType in _superTypes) {
                var prop = superType.resolveIdProp()
                if (superIdProp !== null) {
                    throw ModelException(
                        "'${this}' inherits two id properties:" +
                            "'$superIdProp' and '$prop'"
                    )
                }
                superIdProp = prop
            }
            if (superIdProp !== null) {
                _idProp = superIdProp
                return _idProp
            }
        }
        val idProps = declaredProps.values.filter { it.isId }
        if (idProps.size > 1) {
            throw ModelException(
                "More than one 1 id properties is specified for type '${immutableType}'"
            )
        }
        _idProp = idProps.firstOrNull()
        return _idProp
    }

    override fun toString(): String =
        immutableType.qualifiedName

    inner class DatabaseImpl: EntityType.Database {

        var userTableName: String? = null

        override val tableName: String
            get() = userTableName ?: defaultTableName

        private val defaultTableName by lazy {
            databaseIdentifier(kotlinType.simpleName!!)
        }
    }

    class GraphQLImpl: EntityType.GraphQL {
        override var defaultBatchSize: Int? = null
        override var defaultCollectionBatchSize: Int? = null
    }
}