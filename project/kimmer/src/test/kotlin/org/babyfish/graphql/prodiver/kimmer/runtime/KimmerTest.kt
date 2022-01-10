package org.babyfish.graphql.prodiver.kimmer.runtime

import org.babyfish.graphql.prodiver.kimmer.meta.AuthorDraft
import org.babyfish.graphql.prodiver.kimmer.meta.Book
import org.babyfish.graphql.prodiver.kimmer.meta.BookDraft
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.jackson.immutableObjectMapper
import org.babyfish.graphql.provider.kimmer.new
import org.babyfish.graphql.provider.kimmer.runtime.UnloadedException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

class KimmerTest {

    @Test
    fun test() {
        val book = new(BookDraft.Sync::class) {
            name = "book"
            store().name = "store"
            authors() += new(AuthorDraft.Sync::class) {
                name = "Jim"
            }
            authors() += new(AuthorDraft.Sync::class) {
                name = "Kate"
            }
        }
        val book2 = new(BookDraft.Sync::class, book) {}
        val book3 = new(BookDraft.Sync::class, book) {
            name = "book!"
            name = "book"
            store().name = "store!"
            store().name = "store"
            authors[0].name = "Jim!"
            authors[0].name = "Jim"
            authors[1].name = "Kate!"
            authors[1].name = "Kate"
        }
        val book4 = new(BookDraft.Sync::class, book3) {
            name += "!"
            store().name += "!"
            for (author in authors) {
                author.name += "!"
            }
        }
        expect("book") {
            book.name
        }
        expect("store") {
            book.store?.name
        }
        expect(listOf("Jim", "Kate")) {
            book.authors.map { it.name }
        }
        expect(true) {
            book === book2
        }
        expect(true) {
            book2 === book3
        }
        expect("book!") {
            book4.name
        }
        expect("store!") {
            book4.store?.name
        }
        expect(listOf("Jim!", "Kate!")) {
            book4.authors.map { it.name }
        }

        assertFailsWith<UnloadedException> {
            book4.id
        }
        assertFailsWith<UnloadedException> {
            book4.store?.books
        }
        assertFailsWith<UnloadedException> {
            book4.authors[0].books
        }

        val json = """{"authors":[{"name":"Jim!"},{"name":"Kate!"}],"name":"book!","store":{"name":"store!"}}"""
        expect(json) {
            book4.toString()
        }
        expect(json) {
            immutableObjectMapper().writeValueAsString(book4)
        }
        expect(book4) {
            Immutable.fromString(json, Book::class)
        }
        expect(book4) {
            immutableObjectMapper().readValue(json, Book::class.java)
        }
    }
}
