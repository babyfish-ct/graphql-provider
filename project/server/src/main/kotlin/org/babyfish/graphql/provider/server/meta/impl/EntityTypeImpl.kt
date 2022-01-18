package org.babyfish.graphql.provider.server.meta.impl

import org.babyfish.graphql.provider.server.EntityTypeProvider
import org.babyfish.graphql.provider.server.meta.EntityProp
import org.babyfish.graphql.provider.server.meta.EntityType
import org.babyfish.graphql.provider.server.meta.MetadataException
import org.babyfish.graphql.provider.server.meta.databaseIdentifier
import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import java.lang.IllegalStateException
import kotlin.reflect.KClass
import kotlin.time.Duration

internal class EntityTypeImpl(
    kotlinType: KClass<out Immutable>
): EntityType {

    private val _superTypes = mutableListOf<EntityTypeImpl>()

    private var _props: Map<String, EntityProp>? = null

    private var _idProp: EntityProp? = null

    override val immutableType: ImmutableType =
        if (Connection::class.java.isAssignableFrom(kotlinType.java)) {
            throw IllegalArgumentException(
                "Type '${kotlinType.qualifiedName}' cannot be considered as entity " +
                    "because it implements '${Connection::class.qualifiedName}'")
        } else {
            ImmutableType.of(kotlinType)
        }

    override val database = DatabaseImpl()

    override val redis = RedisImpl()

    override val graphql = GraphQLImpl()

    override val superTypes: List<EntityType>
        get() = _superTypes

    override val idProp: EntityProp
        get() = _idProp ?: error("Id property has not been resolved")

    override val declaredProps = mutableMapOf<String, EntityPropImpl>()

    override val props: Map<String, EntityProp>
        get() = _props ?: error("Properties have not been resolved")

    fun resolve(provider: EntityTypeProvider, phase: ResolvingPhase) {
        when (phase) {
            ResolvingPhase.SUPER_TYPE -> resolveSuperTypes(provider)
            ResolvingPhase.DECLARED_PROPS -> resolveDeclaredProps(provider)
            ResolvingPhase.PROPS -> resolveProps(provider)
            ResolvingPhase.PROP_DETAIL -> resolvePropDetail(provider)
            ResolvingPhase.ID_PROP -> resolveIdProp()
        }
    }

    private fun resolveSuperTypes(provider: EntityTypeProvider) {
        for (superImmutableType in immutableType.superTypes) {
            val superType = provider.tryGet(superImmutableType.kotlinType) as EntityTypeImpl?
            if (superType !== null) {
                _superTypes += superType
            }
        }
    }

    private fun resolveDeclaredProps(provider: EntityTypeProvider) {
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
                    EntityProp.Category.SCALAR,
                    immutableProp.kotlinProp
                )
            }
        }
    }

    private fun resolveProps(provider: EntityTypeProvider) {
        if (_props === null) {
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
                            if (superProp.category !== EntityProp.Category.ID) {
                                throw MetadataException(
                                    "Duplicate properties: '$superProp' and '$prop'"
                                )
                            }
                        } else {
                            map[superProp.name] = superProp
                        }
                    }
                }
                _props = map
            }
        }
    }

    private fun resolvePropDetail(provider: EntityTypeProvider) {
        for (declaredProp in declaredProps.values) {
            declaredProp.resolve(provider)
        }
    }

    private fun resolveIdProp(): EntityProp {
        if (_idProp === null) {
            val props = declaredProps.values.filter { it.category == EntityProp.Category.ID }
            if (superTypes.isNotEmpty()) {
                if (props.isNotEmpty()) {
                    throw MetadataException(
                        "'${this}' inherits other entity types so that " +
                            "it cannot have its own id property"
                    )
                }
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
                if (props.isEmpty()) {
                    throw MetadataException(
                        "No id property is configured for type '${immutableType.qualifiedName}'"
                    )
                }
                if (props.size > 1) {
                    throw MetadataException(
                        "More than one 1 id properties is specified for type '${immutableType}'"
                    )
                }
                _idProp = props[0]
            }
        }
        return _idProp ?: error("Internal bug")
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