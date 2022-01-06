package org.babyfish.graphql.provider.kimmer.runtime

import java.lang.IndexOutOfBoundsException

internal class ListDraft<E>(
    private val ctx: DraftContext,
    private val base: List<E>
): MutableList<E> {

    private var modified: MutableList<E>? = null

    private var modCount = 0

    override fun isEmpty(): Boolean =
        list.isEmpty()

    override val size: Int
        get() = list.size

    override fun contains(element: E): Boolean =
        list.contains(element)

    override fun containsAll(elements: Collection<E>): Boolean =
        list.containsAll(elements)

    override fun get(index: Int): E =
        list[index]

    override fun indexOf(element: E): Int =
        list.indexOf(element)

    override fun lastIndexOf(element: E): Int =
        list.lastIndexOf(element)

    override fun add(element: E): Boolean =
        mutableList.add(element).also {
            modCount++
        }

    override fun add(index: Int, element: E) {
        mutableList.add(index, element).also {
            modCount++
        }
    }

    override fun addAll(elements: Collection<E>): Boolean =
        mutableList.addAll(elements).also {
            modCount++
        }

    override fun addAll(index: Int, elements: Collection<E>): Boolean =
        mutableList.addAll(index, elements).also {
            modCount++
        }

    override fun clear() {
        mutableList.clear().also {
            modCount++
        }
    }

    override fun remove(element: E): Boolean =
        mutableList.remove(element).also {
            modCount++
        }

    override fun removeAt(index: Int): E =
        mutableList.removeAt(index).also {
            modCount++
        }

    override fun removeAll(elements: Collection<E>): Boolean =
        mutableList.removeAll(elements).also {
            modCount++
        }

    override fun retainAll(elements: Collection<E>): Boolean =
        mutableList.retainAll(elements).also {
            modCount++
        }

    override fun set(index: Int, element: E): E =
        mutableList.set(index, element).also {
            modCount++
        }

    private fun removeRange(headHide: Int, tailHide: Int) {
        mutableList.apply {
            subList(headHide, size - tailHide).clear()
        }
        modCount++
    }

    private fun removeRange(headHide: Int, tailHide: Int, block: (element: E) -> Boolean): Boolean {
        modCount++
        return mutableList.run {
            val itr = subList(headHide, size - tailHide).listIterator(size - headHide - tailHide)
            var changed = false
            while (itr.hasPrevious()) {
                val element = itr.previous()
                if (block(element)) {
                    itr.remove()
                    changed = true
                }
            }
            changed
        }
    }

    override fun iterator(): MutableIterator<E> =
        listIterator(0)

    override fun listIterator(): MutableListIterator<E> =
        listIterator(0)

    override fun listIterator(index: Int): MutableListIterator<E> {
        TODO("Not yet implemented")
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> {
        TODO("Not yet implemented")
    }

    override fun hashCode(): Int =
        list.hashCode()

    override fun equals(other: Any?): Boolean =
        list == other

    override fun toString(): String =
        list.toString()

    private inline val list: List<E>
        get() = modified ?: base

    private inline val mutableList: MutableList<E>
        get() = modified ?: base.toMutableList().also {
            modified = it
        }

    private class SubList<E>(
        private val draft: ListDraft<E>,
        private val headHide: Int,
        private val tailHide: Int
    ): MutableList<E> {

        private var modCount = draft.modCount

        override fun isEmpty(): Boolean = execute {
            draft.size <= headHide + tailHide
        }

        override val size: Int
            get() = execute {
                draft.size - headHide - tailHide
            }

        override fun contains(element: E): Boolean = execute {
            val absIndex = draft.indexOf(element)
            absIndex >= headHide && absIndex < draft.size - tailHide
        }

        override fun containsAll(elements: Collection<E>): Boolean = execute {
            for (element in elements) {
                val absIndex = draft.indexOf(element)
                if (absIndex < headHide || absIndex >= draft.size - tailHide) {
                    return@execute false
                }
            }
            true
        }

        override fun get(index: Int): E = execute {
            if (index < 0 || index >= draft.size - headHide - tailHide) {
                throw IndexOutOfBoundsException()
            }
            draft[headHide + index]
        }

        override fun indexOf(element: E): Int = execute {
            val absIndex = draft.indexOf(element)
            if (absIndex >= headHide && absIndex < draft.size - tailHide) {
                absIndex - headHide
            } else {
                -1
            }
        }

        override fun lastIndexOf(element: E): Int = execute {
            val absIndex = draft.lastIndexOf(element)
            if (absIndex >= headHide && absIndex < draft.size - tailHide) {
                absIndex - headHide
            } else {
                -1
            }
        }

        override fun add(element: E): Boolean = execute(true) {
            draft.add(draft.size - tailHide, element)
            true
        }

        override fun add(index: Int, element: E) = execute(true) {
            if (index < 0 || index >= draft.size - headHide - tailHide) {
                throw IndexOutOfBoundsException()
            }
            draft.add(index + headHide, element)
        }

        override fun addAll(elements: Collection<E>): Boolean = execute(true) {
            draft.addAll(draft.size - tailHide, elements)
        }

        override fun addAll(index: Int, elements: Collection<E>): Boolean = execute(true) {
            if (index < 0 || index >= draft.size - headHide - tailHide) {
                throw IndexOutOfBoundsException()
            }
            draft.addAll(index + headHide, elements)
            true
        }

        override fun clear() = execute(true) {
            draft.removeRange(headHide, tailHide)
        }

        override fun remove(element: E): Boolean = execute(true) {
            draft.removeRange(headHide, tailHide) {
                it == element
            }
        }

        override fun removeAt(index: Int): E = execute(true) {
            if (index < 0 || index >= draft.size - headHide - tailHide) {
                throw IndexOutOfBoundsException()
            }
            draft.removeAt(index + headHide)
        }

        override fun removeAll(elements: Collection<E>): Boolean = execute(true) {
            draft.removeRange(headHide, tailHide) {
                elements.contains(it)
            }
        }

        override fun retainAll(elements: Collection<E>): Boolean = execute(true) {
            draft.removeRange(headHide, tailHide) {
                !elements.contains(it)
            }
        }

        override fun set(index: Int, element: E): E = execute {
            if (index < 0 || index >= draft.size - headHide - tailHide) {
                throw IndexOutOfBoundsException()
            }
            draft.set(index + headHide, element)
        }

        override fun iterator(): MutableIterator<E> {
            TODO("Not yet implemented")
        }

        override fun listIterator(): MutableListIterator<E> {
            TODO("Not yet implemented")
        }

        override fun listIterator(index: Int): MutableListIterator<E> {
            TODO("Not yet implemented")
        }

        override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = execute {
            val size = draft.size - headHide - tailHide
            if (fromIndex > toIndex) {
                throw IllegalArgumentException()
            }
            if (fromIndex < 0 || toIndex > size) {
                throw IndexOutOfBoundsException()
            }
            SubList<E>(
                draft,
                headHide + fromIndex,
                tailHide + size - toIndex
            )
        }

        private fun <T> execute(syncModCount: Boolean = false, block: () -> T): T {
            if (modCount != draft.modCount) {
                throw ConcurrentModificationException()
            }
            return if (syncModCount) {
                val result = block()
                modCount = draft.modCount
                result
            } else {
                block()
            }
        }
    }
}