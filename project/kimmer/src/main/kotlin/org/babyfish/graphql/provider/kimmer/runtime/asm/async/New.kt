package org.babyfish.graphql.provider.kimmer.runtime.asm.async

import org.babyfish.graphql.provider.kimmer.runtime.asm.KCLASS_DESCRIPTOR
import org.babyfish.graphql.provider.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes

internal fun ClassVisitor.writeNew() {

    writeMethod(
        Opcodes.ACC_PUBLIC,
        "newAsync-9k1ZQyY",
        "($KCLASS_DESCRIPTOR)$KCLASS_DESCRIPTOR"
    ) {
        visitVarInsn(Opcodes.ALOAD, 1)
        visitInsn(Opcodes.ARETURN)
    }
}