package org.babyfish.graphql.prodiver.kimmer.runtime

import org.babyfish.graphql.prodiver.kimmer.meta.AuthorDraft
import org.babyfish.graphql.prodiver.kimmer.meta.BookDraft
import org.babyfish.graphql.provider.kimmer.new
import kotlin.test.Test
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
        val book2 = new(BookDraft.Sync::class, book) {
            name = "book!"
            name = "book"
            store().name = "store!"
            store().name = "store"
            authors[0].name = "Jim!"
            authors[0].name = "Jim"
            authors[1].name = "Kate!"
            authors[1].name = "Kate"
        }
        val book3 = new(BookDraft.Sync::class, book) {
            name += "!"
            store().name += "!"
            for (author in authors) {
                author.name += "!"
            }
            println(this)
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
        expect("book!") {
            book3.name
        }
        expect("store!") {
            book3.store?.name
        }
        expect(listOf("Jim!", "Kate!")) {
            book3.authors.map { it.name }
        }
    }
}