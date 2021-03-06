package jp.justincase.jackson.kotlin.polymorphic.codec

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.google.common.reflect.TypeToken
import jp.justincase.jackson.kotlin.internal.reportInputMismatch
import jp.justincase.jackson.kotlin.internal.throwIfFatal
import jp.justincase.jackson.kotlin.internal.toJavaType
import jp.justincase.jackson.kotlin.internal.toTypeToken
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass

internal
class PolymorphicDeserializer<T : Any>(
    private
    val typeTable: Map<String, Map<String, Pair<KClass<out T>, String?>>>,
    private
    val rootClass: KClass<T>,
    private
    val rootDelegate: (JsonParser, DeserializationContext) -> T
) : ContextualDeserializer, JsonDeserializer<T>() {
  override
  fun createContextual(ctxt: DeserializationContext, property: BeanProperty?): JsonDeserializer<*> =
    when (val t = ctxt.contextualType) {
      null -> this
      else -> object : JsonDeserializer<T>() {
        override
        fun deserialize(p: JsonParser, ctxt: DeserializationContext): T =
            typedDeserialize(p, ctxt, t)
      }
    }


  override
  fun deserialize(p: JsonParser, ctxt: DeserializationContext): T =
      rootDelegate(p, ctxt)

  private
  fun typedDeserialize(p: JsonParser, ctxt: DeserializationContext, contextualType: JavaType): T {
    val node = p.readValueAsTree<TreeNode>().let {
      it as? ObjectNode ?: throw reportInputMismatch(ctxt, "${it::class} is not a JSON object representation")
    }

    val matches = iterator {
      for ((k, m) in typeTable) {
        (node[k] as? TextNode)?.asText()?.let(m::get)?.let {
          yield(k to it)
        }
      }
    }

    return if (!matches.hasNext()) {
      rootDelegate(node.traverse(p.codec).also { it.nextToken() }, ctxt)
    } else {
      val (key, factory) = matches.next()
      val (type, valueKey) = factory

      if (matches.hasNext()) {
        val message = matches
            .asSequence()
            .joinToString(", ", "Ambiguous type definitions ", "and {$key: $type}") { (k, f) ->
              "{$k: ${f.first}}"
            }

        throw reportInputMismatch(ctxt, message)
      } else {
        if (type == rootClass) {
          val traversal = if (valueKey != null) {
            node.get(valueKey).traverse(p.codec)
          } else {
            node.remove(key)

            node.traverse(p.codec)
          }
          traversal.nextToken()

          rootDelegate(traversal, ctxt)
        } else {
          val out = contextualType
              .toTypeToken()
              .let {
                try {
                  it.getSubtype(type.java)
                } catch (e: Throwable) {
                  e.throwIfFatal()
                  try {
                    // Work around '%s does not appear to be a subtype of %s' during TypeToken#getSubtype
                    @Suppress("UnstableApiUsage")
                    TypeToken.of(it.resolveTypeArgsForSubclass(type.java))
                  } catch (e1: Throwable) {
                    e1.throwIfFatal()
                    throw reportInputMismatch(e, ctxt, "$type is not a subtype of $it: $e")
                  }
                }
              }
              .toJavaType(ctxt.typeFactory)
              .let { t ->
                t.rawClass.kotlin.objectInstance ?: ctxt
                    .findNonContextualValueDeserializer(t)
                    .deserialize(node.traverse(p.codec).also { it.nextToken() }, ctxt)
              }

          // Deserialization as a subtype of the contextual type
          @Suppress("UNCHECKED_CAST")
          out as T
        }
      }
    }
  }
}
