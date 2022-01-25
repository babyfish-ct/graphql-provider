package org.babyfish.graphql.provider.server.meta.impl

import org.babyfish.graphql.provider.server.ModelException
import org.babyfish.graphql.provider.server.runtime.EntityTypeGenerator
import org.babyfish.graphql.provider.server.meta.*
import org.babyfish.kimmer.meta.ImmutableProp
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.time.Duration

internal class EntityPropImpl(
    override val declaringType: EntityType,
    kotlinProp: KProperty1<*, *>,
    override val isId: Boolean = false,
    mappedBy: KProperty1<*, *>? = null
): EntityProp {

    var _mappedBy: Any? = mappedBy?.name

    override val immutableProp: ImmutableProp =
        declaringType.immutableType.props[kotlinProp.name]
            ?: throw IllegalArgumentException("No prop '${kotlinProp.name}' of type '${declaringType.kotlinType.qualifiedName}'")

    override var targetType: EntityType? = null

    override var oppositeProp: EntityProp? = null

    override var column: ColumnImpl? = null

    override var middleTable: EntityProp.MiddleTable? = null

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

    override val isTargetNullable: Boolean
        get() = immutableProp.isTargetNullable

    override val mappedBy: EntityProp?
        get() = _mappedBy as EntityProp?

    fun resolve(generator: EntityTypeGenerator, phase: ResolvingPhase) {
        if (phase == ResolvingPhase.PROP_TARGET) {
            resolveTarget(generator)
        } else if (phase == ResolvingPhase.PROP_MAPPED_BY) {
            resolvedMappedBy(generator)
        }
    }

    private fun resolveTarget(generator: EntityTypeGenerator) {
        immutableProp.targetType?.let {
            val tgtType = generator[it]
            targetType = tgtType
            if (_mappedBy !== null) {
                val opposite = tgtType.props[_mappedBy] as EntityPropImpl? ?: error("Internal bug")
                oppositeProp = opposite
                opposite.oppositeProp = this
            }
        }
    }

    private fun resolvedMappedBy(generator: EntityTypeGenerator) {
        if (_mappedBy is String) {
            val mappedByProp = (targetType!!.props[_mappedBy] as EntityPropImpl?)
                ?: throw ModelException(
                    "The attribute 'mappedBy' of property '${kotlinProp}' is specified as '${_mappedBy}', " +
                        "but there is not property named '${_mappedBy}' in the target type '${targetType}' "
                )
            if (mappedByProp.targetType !== declaringType) {
                throw ModelException(
                    "The attribute 'mappedBy' of property '${kotlinProp}' is specified as '${_mappedBy}'," +
                        "but the property named '${_mappedBy}' in the target type '${targetType}' is not " +
                        "association point to current type '${declaringType}'"
                )
            }
            if (mappedByProp._mappedBy !== null) {
                throw ModelException(
                    "The attribute 'mappedBy' of property '${kotlinProp}' is specified as '${_mappedBy}'," +
                        "but the property named '${_mappedBy}' in the target type '${targetType}' is specified " +
                        "with 'mappedBy' too, there is not allowed"
                )
            }
            _mappedBy = mappedByProp
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
            val tgt = targetType ?: error("Cannot get the default table name because target is not resolved")
            "${declaringType.database.tableName}_${tgt.database.tableName}_MAPPING"
        }

        private val defaultJoinColumnName by lazy {
            databaseIdentifier(declaringType.kotlinType.simpleName!!) + "_ID"
        }

        private val defaultTargetJoinColumnName by lazy {
            val tgt = targetType ?: error("Cannot get the default table name because target is not resolved")
            databaseIdentifier(tgt.kotlinType.simpleName!!) + "_ID"
        }
    }

    class RedisImpl: EntityProp.Redis {
        override var enabled: Boolean = false
        override var timeout: Duration? = null
        override var nullTimeout: Duration? = null
    }
}