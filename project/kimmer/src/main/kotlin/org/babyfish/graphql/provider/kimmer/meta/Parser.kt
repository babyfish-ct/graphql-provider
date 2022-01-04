package org.babyfish.graphql.provider.kimmer.meta

import org.babyfish.graphql.provider.kimmer.Connection
import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.springframework.core.GenericTypeResolver
import kotlin.reflect.*

internal class Parser(
    private val map: MutableMap<Class<*>, ImmutableType>
) {
    private var tmpMap = mutableMapOf<Class<*>, TypeImpl>()

    fun get(type: Class<out Immutable>): ImmutableType =
        map[type]
            ?: tmpMap[type]
            ?: create(type).also {
                tmpMap[type] = it
            }

    private fun create(type: Class<out Immutable>): TypeImpl {
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

    fun terminate(): Map<Class<*>, ImmutableType> {
        val secondaryResolvedTypes = mutableSetOf<TypeImpl>()
        var size = 0
        while (tmpMap.size > size) {
           size = tmpMap.size
           val m = tmpMap.toMap()
           for (type in m.values) {
               if (secondaryResolvedTypes.add(type)) {
                   type.secondaryResolve(this)
               }
           }
        }
        return tmpMap
    }
}

private class TypeImpl(
    override val kotlinType: KClass<out Immutable>
): ImmutableType {

    init {
        if (kotlinType.typeParameters.isNotEmpty()) {
            throw MetadataException("Type parameter is not allowed to immutable type '${kotlinType.qualifiedName}'")
        }
    }

    private val _superTypes = mutableSetOf<ImmutableType>()

    private val _declaredProps = mutableMapOf<String, PropImpl>()

    override val superTypes: Set<ImmutableType>
        get() = _superTypes

    override val declaredProps: Map<String, ImmutableProp>
        get() = _declaredProps

    override val props: Map<String, ImmutableProp> by lazy {
        if (this.superTypes.isEmpty()) {
            _declaredProps
        } else {
            val props = _declaredProps.toMutableMap<String, ImmutableProp>()
            for (superType in _superTypes) {
                for (superProp in superType.props.values) {
                    props.putIfAbsent(superProp.kotlinProp.name, superProp)
                }
            }
            props
        }
    }

    fun resolve(parser: Parser) {
        for (supertype in kotlinType.supertypes) {
            this.resolveSuper(supertype, parser)
        }
        resolveDeclaredProps()
    }

    private fun resolveSuper(superType: KType, parser: Parser) {
        val classifier = superType.classifier
        if (classifier !is KClass<*>) {
            error("Internal bug: classifier of super interface must be KClass")
        }
        if (classifier.java.isInterface) {
            _superTypes += parser.get(classifier.java as Class<out Immutable>)
            for (deeperSuperType in classifier.supertypes) {
                this.resolveSuper(deeperSuperType, parser)
            }
        }
    }

    private fun resolveDeclaredProps() {
        for (member in kotlinType.members) {
            if (member.parameters.isNotEmpty() && member.parameters[0].kind == KParameter.Kind.INSTANCE) {
                if (member is KMutableProperty) {
                    throw MetadataException("'Illegal setter '${member.name}' in type ${kotlinType.qualifiedName}', setter is not allowed in immutable type")
                }
                if (member is KProperty1<*, *> && !isSuperProp(member)) {
                    _declaredProps[member.name] = PropImpl(this, member)
                }
            }
        }
    }

    private fun isSuperProp(kotlinProp: KProperty<*>): Boolean {
        var result = false
        for (superType in _superTypes) {
            val superProp = superType.props[kotlinProp.name]
            if (superProp !== null) {
                if (superProp.kotlinProp.returnType != kotlinProp.returnType) {
                    throw MetadataException("Prop '${kotlinProp}' overrides '${superProp.kotlinProp}' but changes the return type")
                }
                result = true
            }
        }
        return result
    }

    fun secondaryResolve(parser: Parser) {
        for (declaredProp in _declaredProps.values) {
            declaredProp.resolve(parser)
        }
    }
}

private class PropImpl(
    override val declaringType: ImmutableType,
    override val kotlinProp: KProperty1<*, *>
): ImmutableProp {

    override val isConnection: Boolean

    override val isList: Boolean

    override val isReference: Boolean

    override val isTargetNullable: Boolean

    private var _targetType: ImmutableType? = null

    init {
        val classifier = kotlinProp.returnType.classifier as? KClass<*>
            ?: error("Internal bug: '${kotlinProp}' does not returns class")

        if (Map::class.java.isAssignableFrom(classifier.java)) {
            throw MetadataException("Illegal property '${kotlinProp}', map is not allowed")
        }
        if (classifier.java.isArray) {
            throw MetadataException("Illegal property '${kotlinProp}', array is not allowed")
        }
        isConnection = Connection::class.java.isAssignableFrom(classifier.java)
        isList = !isConnection && Collection::class.java.isAssignableFrom(classifier.java)
        isReference = !isConnection && !isList && Immutable::class.java.isAssignableFrom(classifier.java)
        isTargetNullable = if (isList) {
            kotlinProp.returnType.arguments[0].type?.isMarkedNullable ?: false
        } else {
            false
        }
    }

    override val isNullable: Boolean
        get() = kotlinProp.returnType.isMarkedNullable

    override val isAssociation: Boolean
        get() = isConnection || isList || isReference

    override val targetType: ImmutableType?
        get() = _targetType

    fun resolve(parser: Parser) {
        val cls = kotlinProp.returnType.classifier as KClass<*>
        if (isConnection) {
            if (cls.typeParameters.isNotEmpty()) {
                throw MetadataException("Illegal property '${kotlinProp}', connection property of immutable object must return derived type of '${Connection::class.qualifiedName}' without type parameters")
            }
            val targetJavaType = GenericTypeResolver.resolveTypeArgument(cls.java, Connection::class.java)
            _targetType = parser.get(targetJavaType as Class<out Immutable>)
        } else if (isList) {
            if (cls != List::class) {
                throw MetadataException("Illegal property '${kotlinProp}', list property of immutable object must return 'kotlin.List'")
            }
            val targetClassifier = kotlinProp.returnType.arguments[0].type?.classifier
            if (targetClassifier !is KClass<*> || !Immutable::class.java.isAssignableFrom(targetClassifier.java)) {
                throw MetadataException("Illegal property '${kotlinProp}', generic argument of list is not immutable type")
            }
            _targetType = parser.get(targetClassifier.java as Class<out Immutable>)
        } else if (isReference) {
            _targetType = parser.get(cls.java as Class<out Immutable>)
        }
    }

    override fun toString(): String =
        kotlinProp.toString()
}
