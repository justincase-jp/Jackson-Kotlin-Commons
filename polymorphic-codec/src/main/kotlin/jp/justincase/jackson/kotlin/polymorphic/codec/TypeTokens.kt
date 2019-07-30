/*
 * Copyright (C) 2019 Jackson Kotlin Commons Authors
 * Copyright (C) 2006 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
@file:Suppress("UnstableApiUsage")

package jp.justincase.jackson.kotlin.polymorphic.codec

import com.google.common.reflect.TypeResolver
import com.google.common.reflect.TypeToken
import java.lang.reflect.Modifier
import java.lang.reflect.Type

/**
 * Copied from Guava to work around '%s does not appear to be a subtype of %s' during TypeToken#getSubtype
 */
internal
fun <T> TypeToken<T>.resolveTypeArgsForSubclass(subclass: Class<*>): Type {
  // If both runtimeType and subclass are not parameterized, return subclass
  // If runtimeType is not parameterized but subclass is, process subclass as a parameterized type
  // If runtimeType is a raw type (i.e. is a parameterized type specified as a Class<?>), we
  // return subclass as a raw type
  if (type is Class<*> && (subclass.typeParameters.isEmpty() || rawType.typeParameters.isNotEmpty())) {
    // no resolution needed
    return subclass
  }
  // class Base<A, B> {}
  // class Sub<X, Y> extends Base<X, Y> {}
  // Base<String, Integer>.subtype(Sub.class):

  // Sub<X, Y>.getSupertype(Base.class) => Base<X, Y>
  // => X=String, Y=Integer
  // => Sub<X, Y>=Sub<String, Integer>
  val genericSubtype = toGenericType(subclass)

  @Suppress("UNCHECKED_CAST")
  // Subclass isn't <? extends T>
  val supertypeWithArgsFromSubtype = genericSubtype.getSupertype(rawType as Class<in Any>).type

  return TypeResolver()
      .where(supertypeWithArgsFromSubtype, type)
      .resolveType(genericSubtype.type)
}

private
fun <T> toGenericType(cls: Class<T>): TypeToken<out T> {
  if (cls.isArray) {
    throw IllegalArgumentException(cls.toString()) // Unsupported here to reduce copy of code
  }
  val typeParams = cls.typeParameters
  val ownerType = if (cls.isMemberClass && !Modifier.isStatic(cls.modifiers)) {
    toGenericType(cls.enclosingClass).type
  } else {
    null
  }

  return if (typeParams.isNotEmpty() || ownerType != null && ownerType !== cls.enclosingClass) {
    throw IllegalArgumentException(cls.toString()) // Unsupported here to reduce copy of code
  } else {
    TypeToken.of<T>(cls)
  }
}
