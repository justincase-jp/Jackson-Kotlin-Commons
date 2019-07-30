package jp.justincase.jackson.kotlin.polymorphic.codec

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import jp.justincase.jackson.kotlin.internal.reportInputMismatch

internal
class PolymorphicDirectDeserializer<T : Any>(
    private val typeKey: String,
    private val typeName: String,
    private val valueKey: String?,
    private val delegate: (JsonParser, DeserializationContext) -> T,
    private val delegateResolution: (DeserializationContext) -> Unit
) : ResolvableDeserializer, JsonDeserializer<T>() {
  override
  fun resolve(ctxt: DeserializationContext) = delegateResolution(ctxt)

  override
  fun deserialize(p: JsonParser, ctxt: DeserializationContext): T {
    val node = p.readValueAsTree<TreeNode>().let {
      it as? ObjectNode ?: throw reportInputMismatch(ctxt, "${it::class} is not a JSON object representation")
    }

    (node.remove(typeKey) as? TextNode).let {
      when (val text = it?.textValue()) {
        typeName -> Unit
        else -> throw reportInputMismatch(ctxt, "Type name $text found instead of $typeName")
      }
    }
    val traversal = if (valueKey == null) {
      node.traverse(p.codec)
    } else {
      node[valueKey].traverse(p.codec)
    }
    traversal.nextToken()

    return delegate(traversal, ctxt)
  }
}
