package org.babyfish.graphql.provider.runtime

import org.babyfish.graphql.provider.EntityMapper
import org.babyfish.graphql.provider.ModelException
import org.babyfish.graphql.provider.dsl.EntityTypeDSL
import org.babyfish.graphql.provider.meta.Arguments
import org.babyfish.graphql.provider.meta.impl.ModelPropImpl
import org.babyfish.graphql.provider.meta.impl.ModelTypeImpl
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.sql.Entity
import org.babyfish.kimmer.sql.SqlClient
import org.babyfish.kimmer.sql.meta.EntityType
import org.babyfish.kimmer.sql.meta.ScalarProvider
import org.babyfish.kimmer.sql.meta.spi.EntityPropImpl
import org.babyfish.kimmer.sql.meta.spi.MetaFactory
import org.babyfish.kimmer.sql.runtime.*
import org.babyfish.kimmer.sql.spi.createSqlClient
import org.springframework.aop.support.AopUtils
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions

@Suppress("UNCHECKED_CAST")
internal fun createSqlClientByEntityMappers(
    mappers: List<EntityMapper<out Entity<*>, *>>,
    scalarProviders: List<ScalarProvider<*, *>>,
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

        for (scalarProvider in scalarProviders) {
            scalarProvider(scalarProvider)
        }
        for (mapper in mappers) {
            if (AopUtils.isAopProxy(mapper)) {
                throw ModelException(
                    "Illegal class '${AopUtils.getTargetClass(mapper).name}', " +
                        "entity mapper cannot be AOP proxy"
                )
            }
            for (function in mapper::class.declaredFunctions) {
                if (function.name != "config") {
                    if (function.visibility == KVisibility.PUBLIC) {
                        if (function.isSuspend) {
                            throw ModelException("Entity mapper function '$function' cannot be suspend")
                        }
                        mapper.register(
                            dynamicConfigurationRegistry,
                            mapper,
                            function
                        ) {
                            val arguments = Arguments.of(function)
                            val args = mutableMapOf<KParameter, Any?>()
                            args[function.parameters[0]] = mapper
                            for (argument in arguments) {
                                if (!argument.parameter.isOptional) {
                                    args[argument.parameter] = argument.defaultValue()
                                }
                            }
                            try {
                                function.callBy(args)
                            } catch (ex: InvocationTargetException) {
                                throw ex.targetException
                            }
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
                    val userImplementation =
                        dynamicConfigurationRegistry.userImplementation(prop.kotlinProp)
                            ?: throw ModelException(
                                "'${prop.kotlinProp}' is mapped as user implementation property, " +
                                    "but no implementation function is found"
                            )
                    (prop as ModelPropImpl).setUserImplementation(userImplementation)
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