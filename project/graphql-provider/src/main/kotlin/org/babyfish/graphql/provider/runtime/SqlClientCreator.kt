package org.babyfish.graphql.provider.runtime

import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.meta.impl.ModelPropImpl
import org.babyfish.graphql.provider.meta.impl.ModelTypeImpl
import org.babyfish.graphql.provider.meta.impl.invokeByRegistryMode
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.config.IdGenerator
import org.babyfish.kimmer.sql.meta.spi.EntityPropImpl
import org.babyfish.kimmer.sql.meta.spi.MetaFactory
import org.babyfish.kimmer.sql.runtime.*
import org.babyfish.kimmer.sql.spi.createSqlClient
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

@Suppress("UNCHECKED_CAST")
internal fun createSqlClientByEntityMappers(
    mappers: List<EntityMapper<out Entity<*>, *>>,
    jdbcExecutor: JdbcExecutor?,
    r2dbcExecutor: R2dbcExecutor?,
    dialect: Dialect?
): SqlClient {

    val dynamicConfigurationRegistry = DynamicConfigurationRegistry()

    return createSqlClient(
        jdbcExecutor = jdbcExecutor ?: DefaultJdbcExecutor,
        r2dbcExecutor = r2dbcExecutor ?: DefaultR2dbcExecutor,
        dialect = dialect,
        metaFactory = MetaFactoryImpl()
    ) {

        dynamicConfigurationRegistryScope(dynamicConfigurationRegistry) {
            for (mapper in mappers) {
                for (function in mapper::class.declaredFunctions) {
                    if (function.name != "config") {
                        if (function.visibility == KVisibility.PUBLIC) {
                            if (function.isSuspend) {
                                throw ModelException("Filter function '$function' cannot be suspend")
                            }
                            invokeByRegistryMode(mapper, function)
                        }
                    }
                }
            }
        }
        for (mapper in mappers) {
            (mapper as EntityMapper<Entity<*>, *>).apply {
                val kotlinType = mapper.immutableType.kotlinType as KClass<out Entity<*>>
                val modelType = entity(kotlinType as KClass<Entity<FakeID>>) as ModelTypeImpl
                modelType.setMapped()
                (mapper as EntityMapper<Entity<String>, String>).apply {
                    EntityTypeDSL<Entity<String>, String>(modelType, this@createSqlClient).config()
                }
            }
        }
    }.apply {
        for (entityType in this.entityTypeMap.values) {
            for (prop in entityType.declaredProps.values) {
                if (prop.isTransient) {
                    dynamicConfigurationRegistry.userImplementation(prop.kotlinProp)?.let {
                        (prop as ModelPropImpl).setUserImplementation(it)
                    }
                } else if (prop.isConnection || prop.isList) {
                    dynamicConfigurationRegistry.filter(prop.kotlinProp)?.let {
                        (prop as ModelPropImpl).setFilter(it)
                    }
                }
            }
        }
    }
}

private class MetaFactoryImpl: MetaFactory {

    override fun createEntityType(
        immutableType: ImmutableType
    ): ModelTypeImpl =
        ModelTypeImpl(this, immutableType)

    override fun createEntityProp(
        declaringType: EntityType,
        kotlinProp: KProperty1<*, *>
    ): EntityPropImpl =
        ModelPropImpl(declaringType as ModelTypeImpl, kotlinProp)
}