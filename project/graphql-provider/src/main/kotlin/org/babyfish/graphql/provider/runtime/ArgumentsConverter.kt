package org.babyfish.graphql.provider.runtime

import graphql.schema.DataFetchingEnvironment
import org.babyfish.graphql.provider.InputMapper
import org.babyfish.graphql.provider.meta.Argument
import org.babyfish.graphql.provider.meta.ImplicitInputType
import org.babyfish.kimmer.Draft
import org.babyfish.kimmer.Immutable
import org.babyfish.kimmer.graphql.Input
import org.babyfish.kimmer.meta.ImmutableType
import org.babyfish.kimmer.produce
import org.babyfish.kimmer.produceDraft
import org.babyfish.kimmer.sql.Entity
import kotlin.reflect.KClass

open class ArgumentsConverter(
    private val rootImplicitInputTypeMap: Map<KClass<out InputMapper<*, *>>, ImplicitInputType>
) {

    fun convert(
        arguments: List<Argument>,
        owner: Any,
        env: DataFetchingEnvironment
    ): Array<Any?> {
        val arr = Array<Any?>(1 + arguments.size) { null }
        arr[0] = owner
        for (index in arguments.indices) {
            arr[index + 1] = convert(arguments[index], env)
        }
        return arr
    }

    private fun convert(
        argument: Argument,
        env: DataFetchingEnvironment
    ): Any? {
        val value = env.arguments[argument.name] ?: return null
        return if (argument.isList) {
            (value as List<Any?>).map {
                it?.let {
                    convert(it, argument.elementType!!, argument.inputMapperType)
                }
            }
        } else {
            convert(value, argument.type, argument.inputMapperType)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun convert(
        value: Any,
        type: KClass<*>,
        inputMapperType: KClass<out InputMapper<*, *>>?
    ): Any =
        when {
            Input::class.java.isAssignableFrom(type.java) ->
                convertExplicitInput(
                    value as Map<String, Any?>,
                    ImmutableType.of(type as KClass<out Immutable>)
                )
            Entity::class.java.isAssignableFrom(type.java) ->
                convertImplicitInput(
                    value as Map<String, Any?>,
                    rootImplicitInputTypeMap[
                        inputMapperType ?: error("Internal bug: InputMapperType is missed")
                    ] ?: error("Internal bug: ${inputMapperType.qualifiedName} is not manged by spring")
                )
            else ->
                value
        }

    @Suppress("UNCHECKED_CAST")
    private fun convertExplicitInput(
        map: Map<String, Any?>,
        inputType: ImmutableType
    ): Immutable =
        produce(inputType.kotlinType) {
            for (prop in inputType.props.values) {
                val value = map[prop.name]
                if (value === null && !prop.isNullable) {
                    continue
                }
                val finalValue = when {
                    prop.isList && prop.targetType !== null ->
                        (value as List<Map<String, Any?>>).map {
                            convertExplicitInput(it, prop.targetType!!)
                        }
                    prop.targetType !== null ->
                        convertExplicitInput(value as Map<String, Any?>, prop.targetType!!)
                    else ->
                        value
                }
                Draft.set(this, prop, finalValue)
            }
        }

    @Suppress("UNCHECKED_CAST")
    private fun convertImplicitInput(
        map: Map<String, Any?>,
        implicitInputType: ImplicitInputType
    ): Entity<*> =
        produce(implicitInputType.modelType.kotlinType) {
            for (prop in implicitInputType.props.values) {
                val value = map[prop.name]
                if (value === null && !prop.modelProp.isNullable) {
                    continue
                }
                val finalValue = when {
                    prop.isList && prop.targetImplicitType !== null ->
                        (value as List<Map<String, Any?>>).map {
                            convertImplicitInput(it, prop.targetImplicitType!!)
                        }
                    prop.isList && prop.targetScalarType !== null ->
                        prop.modelProp.targetType!!.let { targetType ->
                            (value as List<Any>).map {
                                produce(targetType.kotlinType) {
                                    Draft.set(this, targetType.idProp.immutableProp, it)
                                }
                            }
                        }
                    prop.targetImplicitType !== null ->
                        convertImplicitInput(value as Map<String, Any?>, prop.targetImplicitType!!)
                    prop.targetScalarType !== null ->
                        prop.modelProp.targetType!!.let { targetType ->
                            produce(targetType.kotlinType) {
                                Draft.set(this, targetType.idProp.immutableProp, value)
                            }
                        }
                    else ->
                        value
                }
                Draft.set(this, prop.modelProp.immutableProp, finalValue)
            }
        }
}
