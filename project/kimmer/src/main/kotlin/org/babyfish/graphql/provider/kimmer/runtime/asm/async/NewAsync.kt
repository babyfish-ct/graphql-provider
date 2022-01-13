package org.babyfish.graphql.provider.kimmer.runtime.asm.async

import org.babyfish.graphql.provider.kimmer.AsyncDraft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.runtime.DraftSpi
import org.babyfish.graphql.provider.kimmer.runtime.asm.*
import org.babyfish.graphql.provider.kimmer.runtime.asm.IMMUTABLE_DESCRIPTOR
import org.babyfish.graphql.provider.kimmer.runtime.asm.KCLASS_DESCRIPTOR
import org.babyfish.graphql.provider.kimmer.runtime.asm.KFUNCTION2_DESCRIPTOR
import org.babyfish.graphql.provider.kimmer.runtime.asm.writeMethod
import org.springframework.asm.ClassVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type
import kotlin.reflect.KClass
import kotlin.reflect.jvm.javaMethod

internal fun ClassVisitor.writeNewAsync(args: GeneratorArgs) {
    writeMethod(
        Opcodes.ACC_PUBLIC,
        "newAsync",
        newAsyncDescriptor
    ) {
        visitGetRawDraft(args)
        visitTypeInsn(Opcodes.CHECKCAST, DRAFT_SPI_INTERNAL_NAME)
        visitVarInsn(Opcodes.ALOAD, 1)
        visitVarInsn(Opcodes.ALOAD, 2)
        visitVarInsn(Opcodes.ALOAD, 3)
        visitVarInsn(Opcodes.ALOAD, 4)
        visitMethodInsn(
            Opcodes.INVOKESTATIC,
            newAsyncHelperMethodInternalName,
            "newAsyncHelper",
            newAsyncHelperDescriptor,
            false
        )
        visitInsn(Opcodes.ARETURN)
    }
}

internal suspend fun newAsyncHelper(
    draftSpi: DraftSpi,
    draftType: KClass<out AsyncDraft<*>>,
    base: Immutable?,
    block: suspend AsyncDraft<*>.() -> Unit
): Immutable {
    val ctx = draftSpi.`{draftContext}`()
    val draft = ctx.createDraft(draftType, base) as AsyncDraft<*>
    draft.block()
    return (draft as DraftSpi).`{resolve}`()
}

val newAsyncDescriptor = "($KCLASS_DESCRIPTOR$IMMUTABLE_DESCRIPTOR$KFUNCTION2_DESCRIPTOR$CONTINUATION_DESCRIPTOR)Ljava/lang/Object;"

val newAsyncHelperMethod = ::newAsyncHelper.javaMethod!!
val newAsyncHelperMethodInternalName: String = Type.getInternalName(newAsyncHelperMethod.declaringClass)
val newAsyncHelperDescriptor: String = Type.getMethodDescriptor(newAsyncHelperMethod)