package org.babyfish.graphql.provider.server.meta.impl

import org.babyfish.graphql.provider.server.ModelException
import org.babyfish.graphql.provider.server.runtime.EntityTypeGenerator
import org.babyfish.graphql.provider.server.meta.*
import org.babyfish.kimmer.meta.ImmutableType
import kotlin.time.Duration

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

    override var isAssembled: Boolean = false

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

    fun resolve(generator: EntityTypeGenerator, phase: ResolvingPhase) {
        if (shouldResolve(phase)) {
            when (phase) {
                ResolvingPhase.SUPER_TYPE -> resolveSuperTypes(generator)
                ResolvingPhase.DECLARED_PROPS -> resolveDeclaredProps(generator)
                ResolvingPhase.PROPS -> resolveProps(generator)
                ResolvingPhase.PROP_DETAIL -> resolvePropDetail(generator)
                ResolvingPhase.ID_PROP -> resolveIdProp()
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

    private fun resolveSuperTypes(generator: EntityTypeGenerator) {
        for (superImmutableType in immutableType.superTypes) {
            val superType = generator[superImmutableType]
            _superTypes += superType
            superType._derivedTypes += this
        }
    }

    private fun resolveDeclaredProps(generator: EntityTypeGenerator) {
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

    private fun resolveProps(generator: EntityTypeGenerator) {
        for (superType in _superTypes) {
            superType.resolve(generator, ResolvingPhase.PROPS)
        }
        if (_superTypes.isEmpty()) {
            _props = declaredProps
        } else {
            val map = mutableMapOf<String, EntityProp>()
            map += declaredProps
            for (superType in _superTypes) {
                if (generator[superType.immutableType].isAssembled) {
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

    private fun resolvePropDetail(generator: EntityTypeGenerator) {
        for (declaredProp in declaredProps.values) {
            declaredProp.resolve(generator)
        }
    }

    private fun resolveIdProp(): EntityProp? {
        val props = declaredProps.values.filter { it.isId }
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
            _idProp = superIdProp
        } else {
            if (props.size > 1) {
                throw ModelException(
                    "More than one 1 id properties is specified for type '${immutableType}'"
                )
            }
            _idProp = props.firstOrNull()
        }
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

    class RedisImpl: EntityType.Redis {
        override var enabled: Boolean = true
        override var timeout: Duration? = null
        override var nullTimeout: Duration? = null
    }

    class GraphQLImpl: EntityType.GraphQL {
        override var defaultBatchSize: Int? = null
        override var defaultCollectionBatchSize: Int? = null
    }
}