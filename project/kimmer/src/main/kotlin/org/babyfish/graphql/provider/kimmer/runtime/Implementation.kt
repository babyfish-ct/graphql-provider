package org.babyfish.graphql.provider.kimmer.runtime

import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableProp
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.springframework.asm.*
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.reflect.jvm.javaMethod

internal fun implementationOf(type: Class<out Immutable>): Class<out Immutable> =
    cacheLock.read {
        cacheMap[type]
    } ?: cacheLock.write {
        cacheMap[type]
            ?: (createImplementation(ImmutableType.of(type)) as Class<out Immutable>).also {
                cacheMap[type] = it
            }
    }

private val cacheMap = WeakHashMap<Class<out Immutable>, Class<out Immutable>>()

private val cacheLock = ReentrantReadWriteLock()

private fun createImplementation(type: ImmutableType): Class<*> =
    ClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES)
        .apply {
            writeType(type)
        }
        .toByteArray()
        .let {
            type.kotlinType.java.classLoader.defineClass(it)
        } as Class<out Immutable>

private fun ClassVisitor.writeType(type: ImmutableType) {

    val implInternalName = implInternalName(type.kotlinType.java)

    visit(
        BYTECODE_VERSION,
        Opcodes.ACC_PUBLIC,
        implInternalName(type.kotlinType.java),
        null,
        OBJECT_INTERNAL_NAME,
        arrayOf(Type.getInternalName(type.kotlinType.java), Type.getInternalName(ImmutableSpi::class.java))
    )

    writeConstructor()
    writeCopyConstructor(type)

    for (prop in type.props.values) {
        writeProp(prop, implInternalName)
    }

    writeRuntimeType(type)
    writeLoaded(type)
    writeThrowable(type)
    writeValue(type)

    visitEnd()
}

private fun ClassVisitor.writeConstructor() {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "<init>",
        "()V",
    ) {
        visitVarInsn(Opcodes.ALOAD, 0)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            OBJECT_INTERNAL_NAME,
            "<init>",
            "()V",
            false
        )
        visitInsn(Opcodes.RETURN)
    }
}

private fun ClassVisitor.writeCopyConstructor(type: ImmutableType) {

    val implInternalName = implInternalName(type.kotlinType.java)

    val itfInternalName = Type.getInternalName(type.kotlinType.java)

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "<init>",
        "(L$itfInternalName;)V"
    ) {

        val throwableDesc = Type.getDescriptor(Throwable::class.java)
        val implItfInternalName = Type.getInternalName(ImmutableSpi::class.java)

        visitVarInsn(Opcodes.ALOAD, 0)
        visitMethodInsn(
            Opcodes.INVOKESPECIAL,
            OBJECT_INTERNAL_NAME,
            "<init>",
            "()V",
            false
        )

        val throwableSlot = 2
        val loadedSlot = 3
        for (prop in type.props.values) {

            var getter = prop.kotlinProp.getter.javaMethod!!

            visitVarInsn(Opcodes.ALOAD, 1)
            visitTypeInsn(Opcodes.CHECKCAST, implItfInternalName)
            visitLdcInsn(prop.name)
            visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                implItfInternalName,
                "{throwable}",
                "(Ljava/lang/String;)$throwableDesc",
                true
            )
            visitVarInsn(Opcodes.ASTORE, throwableSlot)
            visitVarInsn(Opcodes.ALOAD, 0)
            visitVarInsn(Opcodes.ALOAD, throwableSlot)
            visitFieldInsn(
                Opcodes.PUTFIELD,
                implInternalName,
                throwableName(prop),
                throwableDesc
            )

            visitVarInsn(Opcodes.ALOAD, 1)
            visitTypeInsn(Opcodes.CHECKCAST, implItfInternalName)
            visitLdcInsn(prop.name)
            visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                implItfInternalName,
                "{loaded}",
                "(Ljava/lang/String;)Z",
                true
            )
            visitVarInsn(Opcodes.ISTORE, loadedSlot)
            visitVarInsn(Opcodes.ALOAD, 0)
            visitVarInsn(Opcodes.ILOAD, loadedSlot)
            visitFieldInsn(
                Opcodes.PUTFIELD,
                implInternalName,
                loadedName(prop),
                "Z"
            )

            val endPropLabel = Label()
            visitVarInsn(Opcodes.ALOAD, throwableSlot)
            visitJumpInsn(Opcodes.IFNONNULL, endPropLabel)
            visitVarInsn(Opcodes.ILOAD, loadedSlot)
            visitJumpInsn(Opcodes.IFEQ, endPropLabel)

            visitVarInsn(Opcodes.ALOAD, 0)
            visitVarInsn(Opcodes.ALOAD, 1)
            visitMethodInsn(
                Opcodes.INVOKEINTERFACE,
                itfInternalName,
                getter.name,
                Type.getMethodDescriptor(getter),
                true
            )
            visitFieldInsn(
                Opcodes.PUTFIELD,
                implInternalName,
                prop.name,
                Type.getDescriptor(getter.returnType)
            )

            visitLabel(endPropLabel)
        }
        visitInsn(Opcodes.RETURN)
    }
}

private fun ClassVisitor.writeProp(prop: ImmutableProp, ownerInternalName: String) {

    val desc = Type.getDescriptor(prop.returnType.java)
    val loadedName = loadedName(prop)
    val throwableName = throwableName(prop)
    val throwableDesc = Type.getDescriptor(Throwable::class.java)

    writeField(
        Opcodes.ACC_PROTECTED,
        prop.name,
        desc
    )

    writeField(
        Opcodes.ACC_PROTECTED,
        loadedName,
        "Z"
    )

    writeField(
        Opcodes.ACC_PROTECTED,
        throwableName,
        throwableDesc
    )

    val javaGetter = prop.kotlinProp.getter.javaMethod!!
    writeMethod(
        Opcodes.ACC_PUBLIC,
        javaGetter.name,
        Type.getMethodDescriptor(javaGetter)
    ) {

        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(Opcodes.GETFIELD, ownerInternalName, throwableName, throwableDesc)
        visitIf(
            Opcodes.IFNULL
        ) {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitFieldInsn(Opcodes.GETFIELD, ownerInternalName, throwableName, throwableDesc)
            visitInsn(Opcodes.ATHROW)
        }

        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(Opcodes.GETFIELD, ownerInternalName, loadedName, "Z")
        visitIf(
            Opcodes.IFNE
        ) {
            visitThrow(UnloadedException::class, "The field '${prop.kotlinProp}' is unloaded")
        }

        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(Opcodes.GETFIELD, ownerInternalName, prop.name, desc)
        visitReturn(prop.returnType.java)
    }
}

private fun ClassVisitor.writeRuntimeType(type: ImmutableType) {

    val typeInternalName = Type.getInternalName(ImmutableType::class.java)
    val typeDesc = Type.getDescriptor(ImmutableType::class.java)

    writeField(
        Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC or Opcodes.ACC_FINAL,
        "{immutableType}",
        typeDesc
    )

    writeMethod(
        Opcodes.ACC_STATIC,
        "<clinit>",
        "()V"
    ) {
        visitLdcInsn(Type.getType(type.kotlinType.java))
        visitMethodInsn(
            Opcodes.INVOKESTATIC,
            typeInternalName,
            "of",
            "(Ljava/lang/Class;)$typeDesc",
            true
        )
        visitFieldInsn(
            Opcodes.PUTSTATIC,
            implInternalName(type.kotlinType.java),
            "{immutableType}",
            typeDesc
        )
        visitInsn(Opcodes.RETURN)
    }

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{type}",
        "()" + typeDesc
    ) {
        visitFieldInsn(
            Opcodes.GETSTATIC,
            implInternalName(type.kotlinType.java),
            "{immutableType}",
            typeDesc
        )
        visitInsn(Opcodes.ARETURN)
    }
}

private fun ClassVisitor.writeLoaded(type: ImmutableType) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{loaded}",
    "(Ljava/lang/String;)Z"
    ) {
        visitPropNameSwitch(type, { visitVarInsn(Opcodes.ALOAD, 1)}) { prop, _ ->
            visitVarInsn(Opcodes.ALOAD, 0)
            visitFieldInsn(
                Opcodes.GETFIELD,
                implInternalName(type.kotlinType.java),
                loadedName(prop),
                "Z"
            )
            visitInsn(Opcodes.IRETURN)
        }
    }
}

private fun ClassVisitor.writeThrowable(type: ImmutableType) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{throwable}",
        "(Ljava/lang/String;)Ljava/lang/Throwable;"
    ) {
        visitPropNameSwitch(type, { visitVarInsn(Opcodes.ALOAD, 1)}) { prop, _ ->
            visitVarInsn(Opcodes.ALOAD, 0)
            visitFieldInsn(
                Opcodes.GETFIELD,
                implInternalName(type.kotlinType.java),
                throwableName(prop),
                "Ljava/lang/Throwable;"
            )
            visitInsn(Opcodes.ARETURN)
        }
    }
}

private fun ClassVisitor.writeValue(type: ImmutableType) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "{value}",
        "(Ljava/lang/String;)Ljava/lang/Object;"
    ) {
        visitPropNameSwitch(type, { visitVarInsn(Opcodes.ALOAD, 1)}) { prop, _ ->
            val getter = prop.kotlinProp.getter.javaMethod!!
            visitVarInsn(Opcodes.ALOAD, 0)
            visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                implInternalName(type.kotlinType.java),
                getter.name,
                Type.getMethodDescriptor(getter),
                false
            )
            visitBox(getter.returnType)
            visitReturn(getter.returnType)
        }
    }
}
