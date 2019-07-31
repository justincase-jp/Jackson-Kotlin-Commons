package jp.justincase.jackson.kotlin.numerical.codec

import com.fasterxml.jackson.databind.module.SimpleModule

class NumericModule : SimpleModule() {
  override
  fun setupModule(context: SetupContext) {
    super.setupModule(context)

    context.addBeanSerializerModifier(NumericSerializerModifier)
    context.addBeanDeserializerModifier(NumericDeserializerModifier)
  }
}