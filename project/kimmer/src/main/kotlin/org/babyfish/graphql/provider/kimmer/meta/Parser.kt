package org.babyfish.graphql.provider.kimmer.meta

import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType

internal class Parser(
    private val map: Map<Class<*>, ImmutableType>
) {
    private val tmpMap = mutableMapOf<Class<*>, ImmutableType>()

    fun get(type: Class<*>): ImmutableType =
        map[type]
            ?: tmpMap[type]
            ?: create(type).also {
                tmpMap[type] = it
            }

    private fun create(type: Class<*>): ImmutableType {
        if (!type.isInterface) {
            throw IllegalArgumentException("type '${type.name}' is not interface")
        }
        if (!Immutable::class.java.isAssignableFrom(type)) {
            throw IllegalArgumentException("type '${type.name}' does not inherit '${Immutable::class.qualifiedName}'")
        }
        if (Draft::class.java.isAssignableFrom(type)) {
            throw IllegalArgumentException("type '${type.name}' cannot inherit '${Draft::class.qualifiedName}'")
        }
        if (ImmutableType::class.java === type) {
            throw IllegalArgumentException("type cannot be built-in type '${type.name}'")
        }
        val result = TypeImpl(type.kotlin)
        tmpMap[type] = result
        result.resolve(this)
        return result
    }

    val createdTypes: Map<Class<*>, ImmutableType>
        get() = tmpMap
}

private class TypeImpl(
    override val kotlinType: KClass<*>
): ImmutableType {

    init {
        if (kotlinType.typeParameters.isNotEmpty()) {
            throw MetadataException("Type parameter is not allowed to immutable type '${kotlinType.qualifiedName}'")
        }
    }

    private val _superTypes = mutableSetOf<ImmutableType>()

    private val _declaredProps = mutableMapOf<String, ImmutableProp>()

    override val superTypes: Set<ImmutableType>
        get() = TODO("Not yet implemented")

    override val declaredProps: Map<String, ImmutableProp>
        get() = TODO("Not yet implemented")

    override val props: Map<String, ImmutableProp>
        get() = TODO("Not yet implemented")

    fun resolve(parser: Parser) {
        for (supertype in kotlinType.supertypes) {
            this.resolveSuper(supertype, parser)
        }
    }

    private fun resolveSuper(superType: KType, parser: Parser) {
        val classifier = superType.classifier
        if (classifier !is KClass<*>) {
            error("Internal bug: classifier of super interface must be KClass")
        }
        _superTypes += parser.get(classifier.java)
        for (supertype in kotlinType.supertypes) {
            this.resolveSuper(supertype, parser)
        }
    }

    private fun resolveDeclaredProps() {
        for (member in kotlinType.members) {
            if (member is KMutableProperty.Setter<*>) {
                throw MetadataException("'Illegal setter '${member.name}' in type ${kotlinType.qualifiedName}', setter is not allowed in immutable type")
            }
            if (member is KProperty.Getter) {
                _declaredProps[member.name] = PropImpl()
            }
        }
    }
}

private class PropImpl: ImmutableProp {

    override val declaringType: ImmutableType
        get() = TODO("Not yet implemented")
    override val name: String
        get() = TODO("Not yet implemented")
    override val kotlinType: KClass<*>
        get() = TODO("Not yet implemented")
    override val isNullable: Boolean
        get() = TODO("Not yet implemented")
    override val isAssociation: Boolean
        get() = TODO("Not yet implemented")
    override val isCollection: Boolean
        get() = TODO("Not yet implemented")
    override val isReference: Boolean
        get() = TODO("Not yet implemented")
    override val targetType: ImmutableType?
        get() = TODO("Not yet implemented")
    override val isTargetNullable: Boolean
        get() = TODO("Not yet implemented")
}
