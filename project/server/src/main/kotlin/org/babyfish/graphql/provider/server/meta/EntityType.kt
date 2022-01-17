package org.babyfish.graphql.provider.server.meta

import org.babyfish.kimmer.Connection
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.server.EntityTypeProvider
import java.lang.IllegalStateException
import kotlin.reflect.KClass

interface EntityType {

    val immutableType: ImmutableType

    val kotlinType: KClass<*>
        get() = immutableType.kotlinType

    val superTypes: List<EntityType>

    val database: Database

    val redis: Redis

    val graphql: GraphQL

    val idProp: EntityProp

    val declaredProps: Map<String, EntityProp>

    val props: Map<String, EntityProp>

    interface Database {
        val tableName: String
    }

    interface Redis {
        val enabled: Boolean
    }

    interface GraphQL {
        val defaultBatchSize: Int?
        val defaultCollectionBatchSize: Int?
    }
}

internal class EntityTypeImpl(
    kotlinType: KClass<out Immutable>
): EntityType {

    init {
        if (Connection::class.java.isAssignableFrom(kotlinType.java)) {
            throw IllegalArgumentException("Type '${kotlinType.qualifiedName}' cannot be considered as entity because it implements '${Connection::class.qualifiedName}'")
        }
    }

    override val immutableType: ImmutableType = ImmutableType.of(kotlinType)

    override val database = DatabaseImpl()

    override val redis = RedisImpl()

    override val graphql = GraphQLImpl()

    private val _superTypes = mutableListOf<EntityTypeImpl>()

    private var _props: Map<String, EntityProp>? = null

    override val superTypes: List<EntityType>
        get() = _superTypes

    override val idProp: EntityProp by lazy {
        val props = declaredProps.values.filter { it.category == EntityProp.Category.ID }
        if (props.isEmpty()) {
            throw IllegalStateException("No id property is configured for type '${immutableType.qualifiedName}'")
        }
        props[0]
    }

    override val declaredProps = mutableMapOf<String, EntityPropImpl>()

    override val props: Map<String, EntityProp>
        get() = _props ?: error("Internal bug")

    fun resolve(provider: EntityTypeProvider, phase: ResolvingPhase) {
        when (phase) {
            ResolvingPhase.SUPER_TYPE ->
                for (superImmutableType in immutableType.superTypes) {
                    val superType = provider.tryGet(superImmutableType.kotlinType) as EntityTypeImpl?
                    if (superType !== null) {
                        _superTypes += superType
                    }
                }
            ResolvingPhase.DECLARED_PROPS ->
                for (immutableProp in immutableType.declaredProps.values) {
                    if (!declaredProps.containsKey(immutableProp.name)) {
                        if (immutableProp.isAssociation) {
                            throw IllegalStateException("The property '${immutableProp}' is not configured")
                        }
                        declaredProps[immutableProp.name] = EntityPropImpl(
                            this,
                            EntityProp.Category.SCALAR,
                            immutableProp.kotlinProp
                        )
                    }
                }
            ResolvingPhase.PROPS -> {
                if (_props === null) {
                    for (superType in _superTypes) {
                        superType.resolve(provider, phase)
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
                                        throw IllegalArgumentException("Duplicate property defined '$superProp' and '$prop'")
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
            ResolvingPhase.PROP_DETAIL -> {
                for (declaredProp in declaredProps.values) {
                    declaredProp.resolve(provider)
                }
            }
        }
    }

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
    }

    class GraphQLImpl: EntityType.GraphQL {
        override var defaultBatchSize: Int? = null
        override var defaultCollectionBatchSize: Int? = null
    }

    enum class ResolvingPhase {
        SUPER_TYPE,
        DECLARED_PROPS,
        PROPS,
        PROP_DETAIL,
    }
}