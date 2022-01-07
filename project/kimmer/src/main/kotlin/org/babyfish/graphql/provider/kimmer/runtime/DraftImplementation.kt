package org.babyfish.graphql.provider.kimmer.runtime

import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableProp
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.springframework.asm.*
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.jvm.javaMethod

internal fun draftImplementationOf(modelType: Class<out Immutable>): Class<out Draft<*>> =
    cacheLock.read {
        cacheMap[modelType]
    } ?: cacheLock.write {
        cacheMap[modelType]
            ?: DraftImplementationCreator(cacheMap).let {
                val result =it.create(modelType)
                cacheMap += it.tmpMap
                result
            }
    }

private class DraftImplementationCreator(
    private val map: Map<Class<out Immutable>, Class<out Draft<*>>>
) {
    val tmpMap = mutableMapOf<Class<out Immutable>, Class<out Draft<*>>?>()

    fun create(modelType: Class<out Immutable>): Class<out Draft<*>> {
        return createImpl(ImmutableType.of(modelType))
    }

    private fun tryCreateOtherTypes(immutableType: ImmutableType) {
        for (superType in immutableType.superTypes) {
            tryCreate(superType)
        }
        for (prop in immutableType.declaredProps.values) {
            prop.targetType?.let {
                tryCreate(it)
            }
        }
    }

    private fun tryCreate(immutableType: ImmutableType) {
        if (!map.containsKey(immutableType.kotlinType.java) && !tmpMap.containsKey(immutableType.kotlinType.java)) {
            createImpl(immutableType)
        }
    }

    private fun createImpl(immutableType: ImmutableType): Class<out Draft<*>> {
        tmpMap[immutableType.kotlinType.java] = null
        val draftImplementationType = createDraftImplementation(immutableType)
        tryCreateOtherTypes(immutableType)
        tmpMap[immutableType.kotlinType.java] = draftImplementationType
        return draftImplementationType
    }
}

private val cacheMap = WeakHashMap<Class<out Immutable>, Class<out Draft<*>>>()

private val cacheLock = ReentrantReadWriteLock()

private fun createDraftImplementation(immutableType: ImmutableType): Class<out Draft<*>> {
    val draftType = immutableType.draftInfo.abstractType
    if (draftType.`package` !== immutableType.kotlinType.java.`package`) {
        throw IllegalArgumentException("Draft type '${draftType.name}' and immutable type ${immutableType.kotlinType.java.name} belongs to different packages")
    }
    return ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        .apply {
            writeType(GeneratorArgs(draftType, immutableType))
        }
        .toByteArray()
        .let {
            immutableType.kotlinType.java.classLoader.defineClass(it)
        } as Class<out Draft<*>>
}

private fun ClassVisitor.writeType(args: GeneratorArgs) {
    visit(
        BYTECODE_VERSION,
        Opcodes.ACC_PUBLIC,
        args.draftImplInternalName,
        null,
        OBJECT_INTERNAL_NAME,
        arrayOf(args.draftInternalName, Type.getInternalName(DraftSpi::class.java))
    )

    writeField(
        Opcodes.ACC_PRIVATE,
        draftContextName(),
        Type.getDescriptor(DraftContext::class.java)
    )

    writeField(
        Opcodes.ACC_PRIVATE,
        baseName(),
        args.modelDescriptor
    )

    writeField(
        Opcodes.ACC_PRIVATE,
        modifiedName(),
        args.modelImplDescriptor
    )

    writeConstructor(args)
    for (prop in args.immutableType.props.values) {
        writeGetter(prop, args)
        writeSetter(prop, args)
        if (prop.targetType !== null) {
            writeCreator(prop, args)
        }
    }

    writeRuntimeType(args)
    writeThrowable(args)
    writeLoaded(args)
    writeValue(args)

    writeSetThrowable(args)
    writeSetValue(args)

    writeHashCode(args)
    writeEquals(args)

    visitEnd()
}

private fun ClassVisitor.writeConstructor(args: GeneratorArgs) {

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "<init>",
        "(${args.draftContextDescriptor}${args.modelDescriptor})V"
    ) {
        visitVarInsn(Opcodes.ALOAD, 0)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            "java/lang/Object",
            "<init>",
            "()V",
            false
        )
        visitVarInsn(Opcodes.ALOAD, 0)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitFieldInsn(
            Opcodes.PUTFIELD,
            args.draftImplInternalName,
            draftContextName(),
            args.draftContextDescriptor
        )

        visitVarInsn(Opcodes.ALOAD, 0)
        visitVarInsn(Opcodes.ALOAD, 2)
        visitFieldInsn(
            Opcodes.PUTFIELD,
            args.draftImplInternalName,
            baseName(),
            args.modelDescriptor
        )

        visitInsn(Opcodes.RETURN)
    }
}

private fun ClassVisitor.writeGetter(prop: ImmutableProp, args: GeneratorArgs) {
    val getter = prop.kotlinProp.getter.javaMethod!!
    val returnType = prop.targetType?.draftInfo?.abstractType ?: prop.returnType.java
    writeMethod(
        Opcodes.ACC_PUBLIC,
        getter.name,
        "()${Type.getDescriptor(returnType)}"
    ) {
        visitGetter(prop, args)
    }
    if (returnType !== prop.returnType.java) {
        writeMethod(
            Opcodes.ACC_PUBLIC or Opcodes.ACC_BRIDGE,
            getter.name,
            "()${Type.getDescriptor(prop.returnType.java)}"
        ) {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                args.draftImplInternalName,
                getter.name,
                "()${Type.getDescriptor(returnType)}",
                false
            )
            visitReturn(returnType)
        }
    }
}

private fun ClassVisitor.writeSetter(prop: ImmutableProp, args: GeneratorArgs) {
    val getter = prop.kotlinProp.getter.javaMethod!!
    val setterName = getter.name.let {
        if (it.startsWith("is")) {
            "set${it.substring(2)}"
        } else {
            "set${it.substring(3)}"
        }
    }
    val typeDesc = Type.getDescriptor(getter.returnType)
    writeMethod(
        Opcodes.ACC_PUBLIC,
        setterName,
        "($typeDesc)V"
    ) {
        visitSetter(prop, args)
    }
}

private fun ClassVisitor.writeCreator(prop: ImmutableProp, args: GeneratorArgs) {

    val desc = Type.getDescriptor(prop.returnType.java)

    val draftDesc = if (prop.isList) {
        "Ljava/util/List;"
    } else {
        Type.getDescriptor(prop.targetType!!.draftInfo.abstractType)
    }

    writeMethod(
        Opcodes.ACC_PUBLIC,
        prop.name,
        "()$draftDesc"
    ) {

        val modifiedLocal = 1

        visitMutableModelStorage(modifiedLocal, args)
        val loadMutableValue: MethodVisitor.() -> Unit = {
            visitVarInsn(Opcodes.ALOAD, modifiedLocal)
            visitFieldInsn(
                Opcodes.GETFIELD,
                args.modelImplInternalName,
                prop.name,
                desc
            )
        }

        visitVarInsn(Opcodes.ALOAD, modifiedLocal)
        visitFieldInsn(
            Opcodes.GETFIELD,
            args.modelImplInternalName,
            throwableName(prop),
            "Ljava/lang/Throwable;"
        )
        visitCond(Opcodes.IFNONNULL) {

            visitVarInsn(Opcodes.ALOAD, modifiedLocal)
            visitFieldInsn(
                Opcodes.GETFIELD,
                args.modelImplInternalName,
                loadedName(prop),
                "Z"
            )
            visitCond(Opcodes.IFEQ) {

                loadMutableValue()
                visitCond(Opcodes.IFNULL) {
                    visitToDraft(prop, args, loadMutableValue)
                    visitInsn(Opcodes.ARETURN)
                }
            }
        }

        visitSetter(modifiedLocal, prop, args) {
            if (prop.isList) {
                visitTypeInsn(Opcodes.NEW, "java/util/ArrayList")
                visitInsn(Opcodes.DUP)
                visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    "java/util/ArrayList",
                    "<init>",
                    "()V",
                    false
                )
            } else {
                val targetInternalName = implInternalName(prop.targetType!!.kotlinType.java)
                visitTypeInsn(Opcodes.NEW, targetInternalName)
                visitInsn(Opcodes.DUP)
                visitMethodInsn(
                    Opcodes.INVOKESPECIAL,
                    targetInternalName,
                    "<init>",
                    "()V",
                    false
                )
            }
        }
        visitToDraft(prop, args, loadMutableValue)
        visitInsn(Opcodes.ARETURN)
    }
}

private fun ClassVisitor.writeRuntimeType(args: GeneratorArgs) {
    val spiInternalName = Type.getInternalName(ImmutableSpi::class.java)
    val desc = "()${Type.getDescriptor(ImmutableType::class.java)}"
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{type}",
        desc
    ) {
        visitModelGetter(args)
        visitTypeInsn(Opcodes.CHECKCAST, spiInternalName)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            spiInternalName,
            "{type}",
            desc,
            true
        )
        visitInsn(Opcodes.ARETURN)
    }
}

private fun ClassVisitor.writeThrowable(args: GeneratorArgs) {
    val spiInternalName = Type.getInternalName(ImmutableSpi::class.java)
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{throwable}",
        "(Ljava/lang/String;)Ljava/lang/Throwable;"
    ) {
        visitModelGetter(args)
        visitTypeInsn(Opcodes.CHECKCAST, spiInternalName)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            spiInternalName,
            "{throwable}",
            "(Ljava/lang/String;)Ljava/lang/Throwable;",
            true
        )
        visitInsn(Opcodes.ARETURN)
    }
}

private fun ClassVisitor.writeLoaded(args: GeneratorArgs) {
    val spiInternalName = Type.getInternalName(ImmutableSpi::class.java)
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{loaded}",
        "(Ljava/lang/String;)Z"
    ) {
        visitModelGetter(args)
        visitTypeInsn(Opcodes.CHECKCAST, spiInternalName)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            spiInternalName,
            "{loaded}",
            "(Ljava/lang/String;)Z",
            true
        )
        visitInsn(Opcodes.IRETURN)
    }
}

private fun ClassVisitor.writeValue(args: GeneratorArgs) {
    val spiInternalName = Type.getInternalName(ImmutableSpi::class.java)
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{value}",
        "(Ljava/lang/String;)Ljava/lang/Object;"
    ) {
        visitModelGetter(args)
        visitTypeInsn(Opcodes.CHECKCAST, spiInternalName)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            spiInternalName,
            "{value}",
            "(Ljava/lang/String;)Ljava/lang/Object;",
            true
        )
        visitInsn(Opcodes.ARETURN)
    }
}

private fun ClassVisitor.writeHashCode(args: GeneratorArgs) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "hashCode",
        "()I"
    ) {
        visitModelGetter(args)
        visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/Object",
            "hashCode",
            "()I",
            false
        )
        visitInsn(Opcodes.IRETURN)
    }
}

private fun ClassVisitor.writeEquals(args: GeneratorArgs) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "equals",
        "(Ljava/lang/Object;)Z"
    ) {
        visitModelGetter(args)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            "java/lang/Object",
            "equals",
            "(Ljava/lang/Object;)Z",
            false
        )
        visitInsn(Opcodes.IRETURN)
    }
}

private fun ClassVisitor.writeSetThrowable(args: GeneratorArgs) {

}

private fun ClassVisitor.writeSetValue(args: GeneratorArgs) {

}

private fun MethodVisitor.visitGetter(prop: ImmutableProp, args: GeneratorArgs) {
    val getter = prop.kotlinProp.getter.javaMethod!!

    val loadValue: MethodVisitor.() -> Unit = {
        visitModelGetter(args)
        visitMethodInsn(
            Opcodes.INVOKEINTERFACE,
            args.modelInternalName,
            getter.name,
            Type.getMethodDescriptor(getter),
            true
        )
    }

    if (prop.targetType !== null) {
        visitToDraft(prop, args, loadValue)
    } else {
        loadValue()
    }
    visitReturn(prop.returnType.java)
}

private fun MethodVisitor.visitModelGetter(args: GeneratorArgs) {
    visitVarInsn(Opcodes.ALOAD, 0)
    visitFieldInsn(
        Opcodes.GETFIELD,
        args.draftImplInternalName,
        modifiedName(),
        args.modelImplDescriptor
    )
    visitCond(
        Opcodes.IFNULL,
        {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitFieldInsn(
                Opcodes.GETFIELD,
                args.draftImplInternalName,
                modifiedName(),
                args.modelImplDescriptor
            )
        },
        {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitFieldInsn(
                Opcodes.GETFIELD,
                args.draftImplInternalName,
                baseName(),
                args.modelDescriptor
            )
        }
    )
}

private fun MethodVisitor.visitSetter(prop: ImmutableProp, args: GeneratorArgs) {
    val local = when (prop.returnType.java) {
        Long::class.javaPrimitiveType -> 3
        Double::class.javaPrimitiveType -> 3
        else -> 2
    }
    visitMutableModelStorage(local, args)
    visitSetter(local, prop, args) {
        visitLoad(prop.returnType.java, 1)
    }
    visitInsn(Opcodes.RETURN)
}

private fun MethodVisitor.visitSetter(
    modifiedLocal: Int,
    prop: ImmutableProp,
    args: GeneratorArgs,
    valueBlocK: MethodVisitor.() -> Unit
) {
    visitVarInsn(Opcodes.ALOAD, modifiedLocal)
    visitInsn(Opcodes.ACONST_NULL)
    visitFieldInsn(
        Opcodes.PUTFIELD,
        args.modelImplInternalName,
        throwableName(prop),
        "Ljava/lang/Throwable;"
    )
    visitVarInsn(Opcodes.ALOAD, modifiedLocal)
    visitInsn(Opcodes.ICONST_1)
    visitFieldInsn(
        Opcodes.PUTFIELD,
        args.modelImplInternalName,
        loadedName(prop),
        "Z"
    )
    visitVarInsn(Opcodes.ALOAD, modifiedLocal)
    valueBlocK()
    visitFieldInsn(
        Opcodes.PUTFIELD,
        args.modelImplInternalName,
        prop.name,
        Type.getDescriptor(prop.returnType.java)
    )
}

private fun MethodVisitor.visitMutableModelStorage(local: Int, args: GeneratorArgs) {
    visitVarInsn(Opcodes.ALOAD, 0)
    visitFieldInsn(
        Opcodes.GETFIELD,
        args.draftImplInternalName,
        modifiedName(),
        args.modelImplDescriptor
    )
    visitVarInsn(Opcodes.ASTORE, local)
    visitVarInsn(Opcodes.ALOAD, local)
    visitCond(Opcodes.IFNONNULL) {
        visitTypeInsn(Opcodes.NEW, args.modelImplInternalName)
        visitInsn(Opcodes.DUP)
        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(
            Opcodes.GETFIELD,
            args.draftImplInternalName,
            baseName(),
            args.modelDescriptor
        )
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            args.modelImplInternalName,
            "<init>",
            "(${args.modelDescriptor})V",
            false
        )
        visitVarInsn(Opcodes.ASTORE, local)
        visitVarInsn(Opcodes.ALOAD, 0)
        visitVarInsn(Opcodes.ALOAD, local)
        visitFieldInsn(
            Opcodes.PUTFIELD,
            args.draftImplInternalName,
            modifiedName(),
            args.modelImplDescriptor
        )
    }
}

private fun MethodVisitor.visitToDraft(
    prop: ImmutableProp,
    args: GeneratorArgs,
    valueBlock: MethodVisitor.() -> Unit
) {
    visitVarInsn(Opcodes.ALOAD, 0)
    visitFieldInsn(
        Opcodes.GETFIELD,
        args.draftImplInternalName,
        draftContextName(),
        Type.getDescriptor(DraftContext::class.java)
    )
    valueBlock()
    visitMethodInsn(
        Opcodes.INVOKEINTERFACE,
        Type.getInternalName(DraftContext::class.java),
        "toDraft",
        if (prop.isList) {
            "(Ljava/util/List;)Ljava/util/List;"
        } else {
            "(${Type.getDescriptor(Immutable::class.java)})${Type.getDescriptor(Draft::class.java)}"
        },
        true
    )
    if (!prop.isList) {
        visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(prop.targetType!!.draftInfo.abstractType))
    }
}

private data class GeneratorArgs(
    val draftJavaType: Class<*>,
    val immutableType: ImmutableType
) {
    val draftImplInternalName = draftImplInternalName(draftJavaType)
    val draftInternalName = Type.getInternalName(draftJavaType)
    val draftDescriptor = Type.getDescriptor(draftJavaType)
    val draftContextInternalName = Type.getInternalName(DraftContext::class.java)
    val draftContextDescriptor = Type.getDescriptor(DraftContext::class.java)
    val modelInternalName = Type.getInternalName(immutableType.kotlinType.java)
    val modelDescriptor = Type.getDescriptor(immutableType.kotlinType.java)
    val modelImplInternalName = implInternalName(immutableType.kotlinType.java)
    val modelImplDescriptor = "L$modelImplInternalName;"
}
