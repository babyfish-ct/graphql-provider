package org.babyfish.graphql.provider.kimmer.runtime.asm.impl

import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.*
import org.babyfish.graphql.provider.kimmer.runtime.implInternalName
import org.babyfish.graphql.provider.kimmer.runtime.loadedName
import org.babyfish.graphql.provider.kimmer.runtime.throwableName
import org.babyfish.graphql.provider.kimmer.runtime.visitPropNameSwitch
import org.babyfish.graphql.provider.kimmer.runtime.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import kotlin.reflect.jvm.javaMethod

internal fun ClassVisitor.writeLoaded(type: ImmutableType) {
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

internal fun ClassVisitor.writeThrowable(type: ImmutableType) {
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

internal fun ClassVisitor.writeValue(type: ImmutableType) {
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