package org.babyfish.graphql.provider.kimmer.runtime.asm.impl

import org.babyfish.graphql.provider.kimmer.meta.ImmutableProp
import org.babyfish.graphql.provider.kimmer.runtime.*
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import kotlin.reflect.jvm.javaMethod

internal fun ClassVisitor.writeProp(prop: ImmutableProp, ownerInternalName: String) {

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
        visitCond(
            Opcodes.IFNULL
        ) {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitFieldInsn(Opcodes.GETFIELD, ownerInternalName, throwableName, throwableDesc)
            visitInsn(Opcodes.ATHROW)
        }

        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(Opcodes.GETFIELD, ownerInternalName, loadedName, "Z")
        visitCond(
            Opcodes.IFNE
        ) {
            visitThrow(UnloadedException::class, "The field '${prop.kotlinProp}' is unloaded")
        }

        visitVarInsn(Opcodes.ALOAD, 0)
        visitFieldInsn(Opcodes.GETFIELD, ownerInternalName, prop.name, desc)
        visitReturn(prop.returnType.java)
    }
}