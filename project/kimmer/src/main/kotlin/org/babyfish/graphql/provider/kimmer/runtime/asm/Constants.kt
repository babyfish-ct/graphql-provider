package org.babyfish.graphql.provider.kimmer.runtime.asm

import com.fasterxml.jackson.databind.ObjectMapper
import org.babyfish.graphql.provider.kimmer.Draft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.SyncDraft
import org.babyfish.graphql.provider.kimmer.meta.ImmutableType
import org.babyfish.graphql.provider.kimmer.runtime.*
import org.babyfish.graphql.provider.kimmer.runtime.AsyncDraftContext
import org.babyfish.graphql.provider.kimmer.runtime.DraftContext
import org.babyfish.graphql.provider.kimmer.runtime.DraftSpi
import org.babyfish.graphql.provider.kimmer.runtime.Factory
import org.babyfish.graphql.provider.kimmer.runtime.ImmutableSpi
import org.babyfish.graphql.provider.kimmer.runtime.SyncDraftContext
import org.springframework.asm.Type
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.coroutines.Continuation
import kotlin.reflect.KClass

internal val KCLASS_DESCRIPTOR = Type.getDescriptor(KClass::class.java)

internal val IMMUTABLE_DESCRIPTOR = Type.getDescriptor(Immutable::class.java)

internal val IMMUTABLE_SPI_INTERNAL_NAME = Type.getInternalName(ImmutableSpi::class.java)

internal val IMMUTABLE_TYPE_DESCRIPTOR = Type.getDescriptor(ImmutableType::class.java)

internal val DRAFT_DESCRIPTOR = Type.getDescriptor(Draft::class.java)

internal val SYNC_DRAFT_DESCRIPTOR = Type.getDescriptor(SyncDraft::class.java)

internal val DRAFT_SPI_INTERNAL_NAME = Type.getInternalName(DraftSpi::class.java)

internal val DRAFT_CONTEXT_INTERNAL_NAME = Type.getInternalName(DraftContext::class.java)

internal val SYNC_DRAFT_CONTEXT_INTERNAL_NAME = Type.getInternalName(SyncDraftContext::class.java)

internal val ASYNC_DRAFT_CONTEXT_INTERNAL_NAME = Type.getInternalName(AsyncDraftContext::class.java)

internal val DRAFT_CONTEXT_DESCRIPTOR = Type.getDescriptor(DraftContext::class.java)

internal val SYNC_DRAFT_CONTEXT_DESCRIPTOR = Type.getDescriptor(SyncDraftContext::class.java)

internal val ASYNC_DRAFT_CONTEXT_DESCRIPTOR = Type.getDescriptor(AsyncDraftContext::class.java)

internal val FACTORY_DESCRIPTOR = Type.getDescriptor(Factory::class.java)

internal val FACTORY_INTERNAL_NAME = Type.getInternalName(Factory::class.java)

internal val KFUNCTION1_INTERNAL_NAME = "kotlin/jvm/functions/Function1"

internal val KFUNCTION1_DESCRIPTOR = "L${KFUNCTION1_INTERNAL_NAME};"

internal val KFUNCTION2_DESCRIPTOR = "Lkotlin/jvm/functions/Function2;"

internal val CONTINUATION_DESCRIPTOR = Type.getDescriptor(Continuation::class.java)

internal val OBJECT_MAPPER_INTERNAL_NAME = Type.getInternalName(ObjectMapper::class.java)

internal val OBJECT_MAPPER_DESCRIPTOR = Type.getDescriptor(ObjectMapper::class.java)

internal val RWL_INTERNAL_NAME = Type.getInternalName(ReentrantReadWriteLock::class.java)

internal val RWL_DESCRIPTOR = Type.getDescriptor(ReentrantReadWriteLock::class.java)


