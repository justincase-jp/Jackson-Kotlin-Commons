package jp.justincase.jackson.kotlin.polymorphic

import com.fasterxml.jackson.databind.module.SimpleModule

class PolymorphicModule : SimpleModule() {
  override
  fun setupModule(context: SetupContext) {
    super.setupModule(context)

    context.addBeanSerializerModifier(PolymorphicSerializerModifier())
  }
}
