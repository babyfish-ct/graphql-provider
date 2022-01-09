package org.babyfish.graphql.provider.kimmer.runtime

import org.babyfish.graphql.provider.kimmer.Immutable
import kotlin.reflect.full.isSubclassOf

internal class ListDraft<E: Immutable>(
    val draftContext: DraftContext, base: List<E>
): ListProxy<E?>(
    base,
    object: ListElementHandler<E?> {

        override fun input(element: E?) {
            if (element !== null && !element::class.isSubclassOf(Immutable::class)) {
                throw IllegalArgumentException("List element must be instance of '${Immutable::class.qualifiedName}'")
            }
        }

        override fun output(element: E?): E? {
            return draftContext.toDraft(element) as E?
        }

        override fun resolve(element: E?): E {
            return draftContext.resolve(element)!!
        }

        override fun changed(a: E?, b: E?): Boolean {
            return a !== b
        }
    }
)