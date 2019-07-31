package jp.justincase.jackson.kotlin.numerical.codec

import com.fasterxml.jackson.databind.module.SimpleModule

class NumericalModule : SimpleModule() {
  override
  fun setupModule(context: SetupContext) {
    super.setupModule(context)

    context.addBeanSerializerModifier(NumericalSerializerModifier)
    context.addBeanDeserializerModifier(NumericalDeserializerModifier)
  }
}
