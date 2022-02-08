package org.babyfish.graphql.provider.starter.meta.impl

import org.babyfish.graphql.provider.starter.ModelException
import org.babyfish.graphql.provider.starter.dsl.BatchImplementationContext
import org.babyfish.graphql.provider.starter.dsl.ImplementationContext
import org.babyfish.graphql.provider.starter.runtime.GraphQLTypeGenerator
import org.babyfish.graphql.provider.starter.meta.*
import org.babyfish.kimmer.meta.ImmutableProp
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class EntityPropImpl(
    override val declaringType: EntityType,
    kotlinProp: KProperty1<*, *>,
    override val isId: Boolean = false,
    mappedBy: KProperty1<*, *>? = null
): EntityProp {

    private var _mappedBy: Any? = mappedBy?.name

    private var _filter: Filter? = null

    override val immutableProp: ImmutableProp =
        declaringType.immutableType.props[kotlinProp.name]
            ?: throw IllegalArgumentException("No prop '${kotlinProp.name}' of type '${declaringType.kotlinType.qualifiedName}'")

    override var targetEntityType: EntityType? = null

    override var oppositeProp: EntityProp? = null

    override var column: ColumnImpl? = null

    override var middleTable: EntityProp.MiddleTable? = null

    override var userImplementation: UserImplementationImpl? = null

    override var redis = RedisImpl()

    override val returnType: KClass<*>
        get() = immutableProp.returnType

    override val isReference: Boolean
        get() = immutableProp.isReference

    override val isList: Boolean
        get() = immutableProp.isList

    override val isConnection: Boolean
        get() = immutableProp.isConnection

    override val isNullable: Boolean
        get() = immutableProp.isNullable

    override val isElementNullable: Boolean
        get() = immutableProp.isTargetNullable

    override val mappedBy: EntityProp?
        get() = _mappedBy as EntityProp?

    override val filter: Filter?
        get() = _filter

    fun resolve(generator: GraphQLTypeGenerator, phase: ResolvingPhase) {
        when (phase) {
            ResolvingPhase.PROP_FILTER -> resolveFilter(generator)
            ResolvingPhase.PROP_TARGET -> resolveTarget(generator)
            ResolvingPhase.PROP_MAPPED_BY -> resolvedMappedBy(generator)
            ResolvingPhase.PROP_DEFAULT_COLUMN -> resolveDefaultColumn()
        }
    }

    private fun resolveFilter(generator: GraphQLTypeGenerator) {
        _filter = generator.dynamicConfigurationRegistry.filter(kotlinProp)
    }

    private fun resolveTarget(generator: GraphQLTypeGenerator) {
        immutableProp.targetType?.let {
            val tgtType = generator[it]
            targetEntityType = tgtType
            if (_mappedBy !== null) {
                val opposite = tgtType.props[_mappedBy] as EntityPropImpl? ?: error("Internal bug")
                oppositeProp = opposite
                opposite.oppositeProp = this
            }
        }
    }

    private fun resolvedMappedBy(generator: GraphQLTypeGenerator) {
        if (_mappedBy is String) {
            val mappedByProp = (targetEntityType!!.props[_mappedBy] as EntityPropImpl?)
                ?: throw ModelException(
                    "The attribute 'mappedBy' of property '${kotlinProp}' is specified as '${_mappedBy}', " +
                        "but there is not property named '${_mappedBy}' in the target type '${targetEntityType}' "
                )
            if (mappedByProp.targetEntityType !== declaringType) {
                throw ModelException(
                    "The attribute 'mappedBy' of property '${kotlinProp}' is specified as '${_mappedBy}'," +
                        "but the property named '${_mappedBy}' in the target type '${targetEntityType}' is not " +
                        "association point to current type '${declaringType}'"
                )
            }
            if (mappedByProp._mappedBy !== null) {
                throw ModelException(
                    "The attribute 'mappedBy' of property '${kotlinProp}' is specified as '${_mappedBy}'," +
                        "but the property named '${_mappedBy}' in the target type '${targetEntityType}' is specified " +
                        "with 'mappedBy' too, there is not allowed"
                )
            }
            _mappedBy = mappedByProp
        }
    }

    private fun resolveDefaultColumn() {
        if (column === null &&
            _mappedBy === null &&
            userImplementation === null &&
            !isReference &&
            !isList &&
            !isConnection
        ) {
            column = ColumnImpl()
        }
    }

    override fun toString(): String =
        kotlinProp.toString()

    inner class ColumnImpl: EntityProp.Column {

        var userName: String? = null

        override var nullable = false

        override var length: Int? = null

        override var precision: Int? = null

        override var scale: Int? = null

        override var onDelete: OnDeleteAction = OnDeleteAction.NONE

        override val name: String
            get() = userName ?: defaultValue

        private val defaultValue: String by lazy {
            databaseIdentifier(kotlinProp.name)
        }
    }

    inner class MiddleTableImpl: EntityProp.MiddleTable {

        var userTableName: String? = null

        var userJoinColumnName: String? = null

        var userTargetJoinColumnName: String? = null

        override val tableName: String
            get() = userTableName ?: defaultTableName

        override val joinColumnName: String
            get() = userJoinColumnName ?: defaultJoinColumnName

        override val targetJoinColumnName: String
            get() = userTargetJoinColumnName ?: defaultTargetJoinColumnName

        private val defaultTableName by lazy {
            val tgt = targetEntityType ?: error("Cannot get the default table name because target is not resolved")
            "${declaringType.database.tableName}_${tgt.database.tableName}_MAPPING"
        }

        private val defaultJoinColumnName by lazy {
            databaseIdentifier(declaringType.kotlinType.simpleName!!) + "_ID"
        }

        private val defaultTargetJoinColumnName by lazy {
            val tgt = targetEntityType ?: error("Cannot get the default table name because target is not resolved")
            databaseIdentifier(tgt.kotlinType.simpleName!!) + "_ID"
        }
    }

    inner class UserImplementationImpl: UserImplementation {

        private var _single: (suspend (ImplementationContext<*>) -> Any?)? = null

        private var _batch: (suspend (BatchImplementationContext<*>) -> Map<*, *>)? = null

        override var batchSize: Int? = null

        override var single: (suspend (ImplementationContext<*>) -> Any?)?
            get() = _single
            set(value) {
                if (value !== null && _batch !== null) {
                    throw ModelException(
                        "Cannot specify '${kotlinProp}.userImplementation.single' " +
                            "because its '.userImplementation.batch' has been specified"
                    )
                }
                _single = value
            }

        override var batch: (suspend (BatchImplementationContext<*>) -> Map<*, *>)?
            get() = _batch
            set(value) {
                if (value !== null && _single !== null) {
                    throw ModelException(
                        "Cannot specify '${kotlinProp}.userImplementation.batch' " +
                            "because its '.userImplementation.single' has been specified"
                    )
                }
                _batch = value
            }
    }
}