package org.babyfish.graphql.provider.server.meta.impl

import org.babyfish.graphql.provider.server.runtime.EntityTypeResolver
import org.babyfish.graphql.provider.server.meta.*
import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import kotlin.reflect.KClass
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

    fun resolve(provider: EntityTypeResolver, phase: ResolvingPhase) {
        when (phase) {
            ResolvingPhase.SUPER_TYPE -> if (_expectedPhase == ResolvingPhase.SUPER_TYPE.ordinal) {
                resolveSuperTypes(provider)
                _expectedPhase++
            }
            ResolvingPhase.DECLARED_PROPS -> if (_expectedPhase == ResolvingPhase.DECLARED_PROPS.ordinal) {
                resolveDeclaredProps(provider)
                _expectedPhase++
            }
            ResolvingPhase.PROPS -> if (_expectedPhase == ResolvingPhase.PROPS.ordinal) {
                resolveProps(provider)
                _expectedPhase++
            }
            ResolvingPhase.PROP_DETAIL -> if (_expectedPhase == ResolvingPhase.PROP_DETAIL.ordinal) {
                resolvePropDetail(provider)
                _expectedPhase++
            }
            ResolvingPhase.ID_PROP -> if (_expectedPhase == ResolvingPhase.ID_PROP.ordinal) {
                resolveIdProp()
                _expectedPhase++
            }
        }
    }

    private fun resolveSuperTypes(provider: EntityTypeResolver) {
        for (superImmutableType in immutableType.superTypes) {
            val superType = provider[superImmutableType.kotlinType]
            _superTypes += superType
            superType._derivedTypes += this
        }
    }

    private fun resolveDeclaredProps(provider: EntityTypeResolver) {
        for (immutableProp in immutableType.declaredProps.values) {
            if (!declaredProps.containsKey(immutableProp.name)) {
                if (immutableProp.isAssociation) {
                    throw MetadataException(
                        "The property '${immutableProp}' is association " +
                            "but it is not configured by any EntityAssembler"
                    )
                }
                declaredProps[immutableProp.name] = EntityPropImpl(
                    this,
                    EntityPropCategory.SCALAR,
                    immutableProp.kotlinProp
                )
            }
        }
    }

    private fun resolveProps(provider: EntityTypeResolver) {
        for (superType in _superTypes) {
            superType.resolve(provider, ResolvingPhase.PROPS)
        }
        if (_superTypes.isEmpty()) {
            _props = declaredProps
        } else {
            val map = mutableMapOf<String, EntityProp>()
            map += declaredProps
            for (superType in _superTypes) {
                for (superProp in superType.props.values) {
                    val prop = map[superProp.name]
                    if (prop !== null) {
//                        if (superProp.category !== EntityPropCategory.ID) {
//                            throw MetadataException(
//                                "Duplicate properties: '$superProp' and '$prop'"
//                            )
//                        }
                    } else {
                        map[superProp.name] = superProp
                    }
                }
            }
            _props = map
        }
    }

    private fun resolvePropDetail(provider: EntityTypeResolver) {
        for (declaredProp in declaredProps.values) {
            declaredProp.resolve(provider)
        }
    }

    private fun resolveIdProp(): EntityProp? {
        val props = declaredProps.values.filter { it.category == EntityPropCategory.ID }
        if (superTypes.isNotEmpty()) {
//            if (props.isNotEmpty()) {
//                throw MetadataException(
//                    "'${this}' inherits other entity types so that " +
//                        "it cannot have its own id property"
//                )
//            }
            var superIdProp: EntityProp? = null
            for (superType in _superTypes) {
                var prop = superType.resolveIdProp()
                if (superIdProp !== null) {
                    throw MetadataException(
                        "'${this}' inherits two id properties:" +
                            "'$superIdProp' and '$prop'"
                    )
                }
                superIdProp = prop
            }
            _idProp = superIdProp
        } else {
            if (props.size > 1) {
                throw MetadataException(
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