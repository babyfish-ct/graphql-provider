package org.babyfish.graphql.provider.kimmer.runtime.asm

import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.SyncDraft
import org.babyfish.graphql.provider.kimmer.runtime.AsyncDraftContext
import org.babyfish.graphql.provider.kimmer.runtime.DraftContext
import org.babyfish.graphql.provider.kimmer.runtime.Factory
import org.babyfish.graphql.provider.kimmer.runtime.SyncDraftContext
import org.objectweb.asm.Type
import kotlin.reflect.KClass

internal val KCLASS_DESCRIPTOR = Type.getDescriptor(KClass::class.java)

internal val IMMUTABLE_DESCRIPTOR = Type.getDescriptor(Immutable::class.java)

internal val DRAFT_DESCRIPTOR = Type.getDescriptor(Draft::class.java)

internal val SYNC_DRAFT_DESCRIPTOR = Type.getDescriptor(SyncDraft::class.java)

internal val DRAFT_CONTEXT_INTERNAL_NAME = Type.getInternalName(DraftContext::class.java)

internal val SYNC_DRAFT_CONTEXT_INTERNAL_NAME = Type.getInternalName(SyncDraftContext::class.java)

internal val ASYNC_DRAFT_CONTEXT_INTERNAL_NAME = Type.getInternalName(AsyncDraftContext::class.java)

internal val DRAFT_CONTEXT_DESCRIPTOR = Type.getDescriptor(DraftContext::class.java)

internal val SYNC_DRAFT_CONTEXT_DESCRIPTOR = Type.getDescriptor(SyncDraftContext::class.java)

internal val ASYNC_DRAFT_CONTEXT_DESCRIPTOR = Type.getDescriptor(AsyncDraftContext::class.java)

internal val FACTORY_DESCRIPTOR = Type.getDescriptor(Factory::class.java)

internal val FACTORY_INTERNAL_NAME = Type.getInternalName(Factory::class.java)

internal val KFUNCTION1_INTERNAL_NAME = "kotlin/jvm/functions/Function1"

internal val KFUNCTION1_DESCRITPOR = "L${KFUNCTION1_INTERNAL_NAME};"

