package org.babyfish.graphql.provider.kimmer.runtime

internal class ListDraft<E>(
    draftContext: DraftContext, base: List<E>
): ListProxy<E>(
    base,
    object: ElementHandler<E> {

        override fun input(element: E) {

        }

        override fun output(element: E): E {
            return element
        }
    }
)