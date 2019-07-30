package jp.justincase.jackson.kotlin.textual

import com.fasterxml.jackson.databind.module.SimpleModule

class TextualModule : SimpleModule() {
  override
  fun setupModule(context: SetupContext) {
    super.setupModule(context)

    context.addBeanSerializerModifier(TextualSerializerModifier)
    context.addBeanDeserializerModifier(TextualDeserializerModifier)
  }
}
