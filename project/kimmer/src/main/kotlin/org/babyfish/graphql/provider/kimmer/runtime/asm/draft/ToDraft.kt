package org.babyfish.graphql.provider.kimmer.runtime.asm.draft

import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.meta.ImmutableProp
import org.babyfish.graphql.provider.kimmer.runtime.DraftContext
import org.babyfish.graphql.provider.kimmer.runtime.draftContextName
import org.springframework.asm.MethodVisitor
import org.springframework.asm.Opcodes
import org.springframework.asm.Type

internal fun MethodVisitor.visitToDraft(
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