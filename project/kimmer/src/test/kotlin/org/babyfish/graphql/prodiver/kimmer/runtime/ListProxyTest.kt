package org.babyfish.graphql.prodiver.kimmer.runtime

import org.babyfish.graphql.provider.kimmer.runtime.ListProxy
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.expect

class ListProxyTest {

    @Test
    fun testReadList() {
        val base = listOf("a", "b", "c")
        testRead0(ListProxy(base, null))

        val base2 = listOf("-4", "-3", "-2", "-1", "a", "b", "c", "+1", "+2", "+3", "+4", "+5", "+6")
        val draft = ListProxy(base2, null)
            .let {
                it.subList(2, it.size - 3)
            }
            .let {
                it.subList(2, it.size - 3)
            }
        testRead0(draft)
    }

    @Test
    fun testWriteList() {
        val base = listOf("a", "b", "c")
        testWrite0(ListProxy(base, null))

        val base2 = listOf("-4", "-3", "-2", "-1", "a", "b", "c", "+1", "+2", "+3", "+4", "+5", "+6")
        val draft = ListProxy(base2, null)
            .let {
                it.subList(2, it.size - 3)
            }
            .let {
                it.subList(2, it.size - 3)
            }
        testWrite0(draft)

        expect(listOf("a", "b", "c")) {
            base
        }
    }

    private fun testRead0(draft: List<String>) {
        expect(false) {
            draft.isEmpty()
        }
        expect(3) {
            draft.size
        }
        expect(true) {
            draft.contains("a")
        }
        expect(false) {
            draft.contains("A")
        }
        expect(true) {
            draft.containsAll(listOf("a", "b"))
        }
        expect(false) {
            draft.containsAll(listOf("A", "B"))
        }
        draft.listIterator().let {
            expect(true) { it.hasNext() }
            expect("a") { it.next() }
            expect(1) { it.nextIndex() }
            expect(true) { it.hasNext()}
            expect("b") { it.next() }
            expect(2) { it.nextIndex() }
            expect(true) { it.hasNext()}
            expect("c") { it.next() }
            expect(3) { it.nextIndex() }
            expect(false) { it.hasNext() }
            assertFailsWith(NoSuchElementException::class) { it.next() }
            expect(3) { it.nextIndex() }
        }
        draft.listIterator(3).let {
            expect(true) { it.hasPrevious() }
            expect("c") { it.previous() }
            expect(1) { it.previousIndex() }
            expect(true) { it.hasPrevious()}
            expect("b") { it.previous() }
            expect(0) { it.previousIndex() }
            expect(true) { it.hasPrevious()}
            expect("a") { it.previous() }
            expect(-1) { it.previousIndex() }
            expect(false) { it.hasPrevious() }
            assertFailsWith(NoSuchElementException::class) { it.previous() }
            expect(-1) { it.previousIndex() }
        }
    }

    private fun testWrite0(draft: MutableList<String>) {

        draft.add("d")
        expect(listOf("a", "b", "c", "d")) {
            draft
        }

        draft.addAll(listOf("e", "f"))
        expect(listOf("a", "b", "c", "d", "e", "f")) {
            draft
        }

        draft.remove("d")
        expect(listOf("a", "b", "c", "e", "f")) {
            draft
        }

        draft.removeAll(listOf("d", "e", "f"))
        expect(listOf("a", "b", "c")) {
            draft
        }

        draft.retainAll(listOf("b", "c", "d"))
        expect(listOf("b", "c")) {
            draft
        }

        draft.add(0, "a")
        draft.addAll(listOf("d", "e", "f", "g"))
        expect(listOf("a", "b", "c", "d", "e", "f", "g")) {
            draft
        }

        draft.iterator().let {
            while (it.hasNext()) {
                val v = it.next()
                if ((v[0] - 'a') % 2 == 0) {
                    it.remove()
                }
            }
        }
        expect(listOf("b", "d", "f")) {
            draft
        }

        draft.add(0, "a")
        draft.add(2, "c")
        draft.add(4, "e")
        draft.add("g")
        expect(listOf("a", "b", "c", "d", "e", "f", "g")) {
            draft
        }

        draft.listIterator(draft.size).let {
            while (it.hasPrevious()) {
                val v = it.previous()
                if ((v[0] - 'a') % 2 == 0) {
                    it.remove()
                }
            }
        }
        expect(listOf("b", "d", "f")) {
            draft
        }

        draft.listIterator().let {
            while (it.hasNext()) {
                val new = (it.next()[0] + 1).toChar().toString()
                it.add(new)
            }
        }
        expect(listOf("b", "c", "d", "e", "f", "g")) {
            draft
        }

        draft.listIterator(draft.size).let {
            while (it.hasPrevious()) {
                val old = it.previous()
                val upper = old.uppercase()
                if (upper != old) {
                    it.add(upper)
                }
            }
        }
        expect(listOf("B", "b", "C", "c", "D", "d", "E", "e", "F", "f", "G", "g")) {
            draft
        }

        draft.listIterator().let {
            while (it.hasNext()) {
                it.set(it.next() + "*")
            }
        }
        expect(listOf("B*", "b*", "C*", "c*", "D*", "d*", "E*", "e*", "F*", "f*", "G*", "g*")) {
            draft
        }

        draft.listIterator(draft.size).let {
            while (it.hasPrevious()) {
                it.set("*" + it.previous())
            }
        }
        expect(listOf("*B*", "*b*", "*C*", "*c*", "*D*", "*d*", "*E*", "*e*", "*F*", "*f*", "*G*", "*g*")) {
            draft
        }
    }
}