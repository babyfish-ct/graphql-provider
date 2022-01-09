package org.babyfish.graphql.provider.kimmer.jackson

import com.fasterxml.jackson.databind.module.SimpleModule

class ImmutableModule: SimpleModule() {

    override fun setupModule(ctx: SetupContext) {

        super.setupModule(ctx)

        ctx.addSerializers(ImmutableSerializers())
    }
}