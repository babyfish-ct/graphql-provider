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
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaMethod

internal fun draftImplementationOf(draftType: Class<out Draft<*>>): Class<out Draft<*>> =
    cacheLock.read {
        cacheMap[draftType]
    } ?: cacheLock.write {
        cacheMap[draftType]
            ?: createDraftImplementation(draftType).also {
                cacheMap[draftType] = it
            }
    }

private val cacheMap = WeakHashMap<Class<out Draft<*>>, Class<out Draft<*>>>()

private val cacheLock = ReentrantReadWriteLock()

private fun createDraftImplementation(draftType: Class<out Draft<*>>): Class<out Draft<*>> {
    if (draftType.typeParameters.isEmpty()) {
        throw IllegalArgumentException("draftArgument must have type parameters")
    }
    val draftTypeInfo = DraftTypeInfo.of(draftType.kotlin)
    if (draftTypeInfo.draftType.java !== draftType) {
        throw IllegalArgumentException("Illegal draftType: '${draftType.name}'")
    }
    val immutableType = ImmutableType.of(draftTypeInfo.immutableType as KClass<out Immutable>)
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
    val typeDesc = Type.getDescriptor(getter.returnType)
    writeMethod(
        Opcodes.ACC_PUBLIC,
        getter.name,
        "()$typeDesc"
    ) {
        visitGetter(prop, args)
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
    visitModelGetter(args)
    val getter = prop.kotlinProp.getter.javaMethod!!
    visitMethodInsn(
        Opcodes.INVOKEINTERFACE,
        args.modelInternalName,
        getter.name,
        Type.getMethodDescriptor(getter),
        true
    )
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
    visitVarInsn(Opcodes.ALOAD, 0)
    visitFieldInsn(
        Opcodes.GETFIELD,
        args.draftImplInternalName,
        modifiedName(),
        args.modelImplDescriptor
    )
    val local = when (prop.returnType.java) {
        Long::class.javaPrimitiveType -> 3
        Double::class.javaPrimitiveType -> 3
        else -> 2
    }
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
    visitVarInsn(Opcodes.ALOAD, local)
    visitInsn(Opcodes.ACONST_NULL)
    visitFieldInsn(
        Opcodes.PUTFIELD,
        args.modelImplInternalName,
        throwableName(prop),
        "Ljava/lang/Throwable;"
    )
    visitVarInsn(Opcodes.ALOAD, local)
    visitInsn(Opcodes.ICONST_1)
    visitFieldInsn(
        Opcodes.PUTFIELD,
        args.modelImplInternalName,
        loadedName(prop),
        "Z"
    )
    visitVarInsn(Opcodes.ALOAD, local)
    visitLoad(prop.returnType.java, 1)
    visitFieldInsn(
        Opcodes.PUTFIELD,
        args.modelImplInternalName,
        prop.name,
        Type.getDescriptor(prop.returnType.java)
    )
    visitInsn(Opcodes.RETURN)
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
