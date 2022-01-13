package org.babyfish.graphql.prodiver.kimmer.runtime

import com.fasterxml.jackson.core.type.TypeReference
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.babyfish.graphql.prodiver.kimmer.*
import org.babyfish.graphql.provider.kimmer.Immutable
import org.babyfish.graphql.provider.kimmer.jackson.immutableObjectMapper
import org.babyfish.graphql.provider.kimmer.new
import org.babyfish.graphql.provider.kimmer.newAsync
import org.babyfish.graphql.provider.kimmer.runtime.UnloadedException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

class KimmerTest {

    @Test
    fun testSimple() {
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
        val book3 = new(BookDraft.Sync::class, book2) {
            name = "book!"
            name = "book"
            store().name = "store!"
            store().name = "store"
            authors()[0].name = "Jim!"
            authors()[0].name = "Jim"
            authors()[1].name = "Kate!"
            authors()[1].name = "Kate"
        }
        val book4 = new(BookDraft.Sync::class, book3) {
            name += "!"
            store().name += "!"
            for (author in authors()) {
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

    @Test
    fun testOneToManyPolymorphic() {
        val zoo = new(SealedZooDraft.Sync::class) {
            location = "city center"
            animals() += new(TigerDraft.Sync::class) {
                weight = 600
            }
            animals() += new(OtterDraft.Sync::class) {
                length = 50
            }
        }
        val json = """{"__typename":"SealedZoo","location":"city center","animals":[{"__typename":"Tiger","weight":600},{"__typename":"Otter","length":50}]}"""
        expect(json) {
            zoo.toString()
        }
        val deserializedZoo = Immutable.fromString(json, SealedZoo::class)
        expect(false) {
            zoo === deserializedZoo
        }
        expect(zoo) {
            deserializedZoo
        }
        expect(true) {
            deserializedZoo.animals[0] is Tiger
        }
        expect(600) {
            (deserializedZoo.animals[0] as Tiger).weight
        }
        expect(true) {
            deserializedZoo.animals[1] is Otter
        }
        expect(50) {
            (deserializedZoo.animals[1] as Otter).length
        }
    }

    @Test
    fun testManyToOnePolymorphic() {
        val typeReference = object: TypeReference<List<Animal>>() {}
        val animals = listOf(
            new(TigerDraft.Sync::class) {
                weight = 600
                zoo = new(SealedZooDraft.Sync::class) {
                    location = "city center"
                }
            },
            new(OtterDraft.Sync::class) {
                length = 50
                zoo = new(WildZooDraft.Sync::class) {
                    area = 3000
                }
            }
        )
        val json = """[{"__typename":"Tiger","weight":600,"zoo":{"__typename":"SealedZoo","location":"city center"}},{"__typename":"Otter","length":50,"zoo":{"__typename":"WildZoo","area":3000}}]"""
        expect(json) {
            immutableObjectMapper().writerFor(typeReference).writeValueAsString(animals)
        }
        val deserializedAnimals = immutableObjectMapper().readValue(json, typeReference)
        expect(false) {
            animals === deserializedAnimals
        }
        expect(animals) {
            deserializedAnimals
        }
        expect(true) {
            deserializedAnimals[0].zoo is SealedZoo
        }
        expect("city center") {
            (deserializedAnimals[0].zoo as SealedZoo).location
        }
        expect(true) {
            deserializedAnimals[1].zoo is WildZoo
        }
        expect(3000) {
            (deserializedAnimals[1].zoo as WildZoo).area
        }
    }

    @Test
    fun testPrimitive() {
        val primitiveInfo = new(PrimitiveInfoDraft.Sync::class) {
            boolean = true
            char = 'X'
            byte = 23
            short = 234
            int = 2345
            long = 23456
            float = 23456.7F
            double = 23456.78
        }
        val json = """{"boolean":true,"byte":23,"char":"X","double":23456.78,"float":23456.7,"int":2345,"long":23456,"short":234}"""
        expect(json) {
            primitiveInfo.toString()
        }
        val deserializedPrimitiveInfo = Immutable.fromString(json, PrimitiveInfo::class)
        expect(false) {
            primitiveInfo === deserializedPrimitiveInfo
        }
        expect(primitiveInfo) {
            deserializedPrimitiveInfo
        }
    }

    @Test
    fun testAsync() {
        val (book, time) = executeAndCollectTime {
            newAsync(BookDraft.Async::class) {
                delay(500)
                name = "The book"
                store().name = "The store"
                authors() += newAsync(AuthorDraft.Async::class) {
                    delay(500)
                    name = "Jim"
                }
                authors() += newAsync(AuthorDraft.Async::class) {
                    delay(500)
                    name = "Kate"
                }
            }
        }
        expect(true) {
            time >= 1500
        }
        expect("""{"authors":[{"name":"Jim"},{"name":"Kate"}],"name":"The book","store":{"name":"The store"}}""") {
            book.toString()
        }
        val (book2, time2) = executeAndCollectTime {
            newAsync(BookDraft.Async::class, book) {
                name += "!"
                store().name += "!"
                delay(500)
                for (author in authors()) {
                    delay(500)
                    author.name += "!"
                }
            }
        }
        expect(true) {
            time2 >= 1500
        }
        expect("""{"authors":[{"name":"Jim!"},{"name":"Kate!"}],"name":"The book!","store":{"name":"The store!"}}""") {
            book2.toString()
        }
    }

    private fun <T> executeAndCollectTime(block: suspend () -> T): Pair<T, Long> {
        val start = System.currentTimeMillis()
        return runBlocking {
            block()
        } to System.currentTimeMillis() - start
    }
}
