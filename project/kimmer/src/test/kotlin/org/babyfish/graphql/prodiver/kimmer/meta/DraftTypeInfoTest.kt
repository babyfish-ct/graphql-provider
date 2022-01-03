package org.babyfish.graphql.prodiver.kimmer.meta

import org.babyfish.graphql.provider.kimmer.runtime.DraftTypeInfo
import kotlin.test.Test
import kotlin.test.expect

class DraftTypeInfoTest {

    @Test
    fun testNodeDraft() {
        val draftTypeInfo = DraftTypeInfo.of(NodeDraft::class)
        expect(draftTypeInfo) {
            DraftTypeInfo.of(NodeDraft.Sync::class)
        }
        expect(draftTypeInfo) {
            DraftTypeInfo.of(NodeDraft.Async::class)
        }
        expect(Node::class) {
            draftTypeInfo.immutableType
        }
        expect(NodeDraft::class) {
            draftTypeInfo.draftType
        }
        expect(emptyList()) {
            draftTypeInfo.superDraftTypes
        }
    }

    @Test
    fun testBookStoreDraft() {
        val draftTypeInfo = DraftTypeInfo.of(BookStoreDraft::class)
        expect(draftTypeInfo) {
            DraftTypeInfo.of(BookStoreDraft.Sync::class)
        }
        expect(draftTypeInfo) {
            DraftTypeInfo.of(BookStoreDraft.Async::class)
        }
        expect(BookStore::class) {
            draftTypeInfo.immutableType
        }
        expect(BookStoreDraft::class) {
            draftTypeInfo.draftType
        }
        expect(listOf(NodeDraft::class)) {
            draftTypeInfo.superDraftTypes
        }
    }

    @Test
    fun testBookDraft() {
        val draftTypeInfo = DraftTypeInfo.of(BookDraft::class)
        expect(draftTypeInfo) {
            DraftTypeInfo.of(BookDraft.Sync::class)
        }
        expect(draftTypeInfo) {
            DraftTypeInfo.of(BookDraft.Async::class)
        }
        expect(Book::class) {
            draftTypeInfo.immutableType
        }
        expect(BookDraft::class) {
            draftTypeInfo.draftType
        }
        expect(listOf(NodeDraft::class)) {
            draftTypeInfo.superDraftTypes
        }
    }

    @Test
    fun testAuthorDraft() {
        val draftTypeInfo = DraftTypeInfo.of(AuthorDraft::class)
        expect(draftTypeInfo) {
            DraftTypeInfo.of(AuthorDraft.Sync::class)
        }
        expect(draftTypeInfo) {
            DraftTypeInfo.of(AuthorDraft.Async::class)
        }
        expect(Author::class) {
            draftTypeInfo.immutableType
        }
        expect(AuthorDraft::class) {
            draftTypeInfo.draftType
        }
        expect(listOf(NodeDraft::class)) {
            draftTypeInfo.superDraftTypes
        }
    }
}