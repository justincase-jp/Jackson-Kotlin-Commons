package jp.justincase.jackson.kotlin.polymorphic.codec

import com.fasterxml.jackson.databind.module.SimpleModule

class PolymorphicModule : SimpleModule() {
  override
  fun setupModule(context: SetupContext) {
    super.setupModule(context)

    context.addBeanSerializerModifier(PolymorphicSerializerModifier)
    context.addBeanDeserializerModifier(PolymorphicDeserializerModifier)
  }
}
