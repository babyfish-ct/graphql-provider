package org.babyfish.graphql.provider.kimmer.runtime

import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

open class LockedList<E>(
    private val target: MutableList<E>,
    private val rwl: ReentrantReadWriteLock
): MutableList<E> {

    override fun isEmpty(): Boolean = read {
        target.isEmpty()
    }

    override val size: Int
        get() = read {
            target.size
        }

    override fun contains(element: E): Boolean = read {
        target.contains(element)
    }

    override fun containsAll(elements: Collection<E>): Boolean = read {
        target.containsAll(elements)
    }

    override fun indexOf(element: E): Int = read {
        target.indexOf(element)
    }

    override fun lastIndexOf(element: E): Int = read {
        target.lastIndexOf(element)
    }

    override fun get(index: Int): E = read {
        target[index]
    }

    override fun add(element: E): Boolean = write {
        target.add(element)
    }

    override fun add(index: Int, element: E) = write {
        target.add(index, element)
    }

    override fun addAll(elements: Collection<E>): Boolean = write {
        target.addAll(elements)
    }

    override fun addAll(index: Int, elements: Collection<E>): Boolean = write {
        target.addAll(index, elements)
    }

    override fun clear() = write {
        target.clear()
    }

    override fun remove(element: E): Boolean = write {
        target.remove(element)
    }

    override fun removeAt(index: Int): E = write {
        target.removeAt(index)
    }

    override fun removeAll(elements: Collection<E>): Boolean = write {
        target.removeAll(elements)
    }

    override fun retainAll(elements: Collection<E>): Boolean = write {
        target.retainAll(elements)
    }

    override fun set(index: Int, element: E): E = write {
        target.set(index, element)
    }

    override fun iterator(): MutableIterator<E> = read {
        Itr(target.iterator(), rwl)
    }

    override fun listIterator(): MutableListIterator<E> = read {
        ListItr(target.listIterator(), rwl)
    }

    override fun listIterator(index: Int): MutableListIterator<E> = read {
        ListItr(target.listIterator(index), rwl)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<E> = read {
        SubList(target.subList(fromIndex, toIndex), rwl)
    }

    override fun hashCode(): Int = read {
        target.hashCode()
    }

    override fun equals(other: Any?): Boolean = read {
        target == other
    }

    override fun toString(): String = read {
        target.toString()
    }

    private inline fun <T> read(block: () -> T): T =
        rwl.read(block)

    private inline fun <T> write(block: () -> T): T =
        rwl.write(block)

    private abstract class AbstractItr<E>(
        private val rwl: ReentrantReadWriteLock
    ): MutableIterator<E> {

        override fun hasNext(): Boolean = rwl.read {
            target.hasNext()
        }

        override fun next(): E = rwl.write {
            target.next()
        }

        override fun remove() {
            rwl.write {
                target.remove()
            }
        }

        protected abstract val target: MutableIterator<E>
    }

    private class Itr<E>(
        override val target: MutableIterator<E>,
        private val rwl: ReentrantReadWriteLock
    ): AbstractItr<E>(rwl)

    private class ListItr<E>(
        override val target: MutableListIterator<E>,
        private val rwl: ReentrantReadWriteLock
    ): AbstractItr<E>(rwl), MutableListIterator<E> {

        override fun nextIndex(): Int = rwl.read {
            target.nextIndex()
        }

        override fun hasPrevious(): Boolean = rwl.read {
            target.hasPrevious()
        }

        override fun previous(): E = rwl.write {
            target.previous()
        }

        override fun previousIndex(): Int = rwl.write {
            target.previousIndex()
        }

        override fun add(element: E) = rwl.write {
            target.add(element)
        }

        override fun set(element: E) = rwl.write {
            target.set(element)
        }
    }

    private class SubList<E>(
        target: MutableList<E>,
        rwl: ReentrantReadWriteLock
    ): LockedList<E>(target, rwl)
}