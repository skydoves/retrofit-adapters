/*
 * Copyright (C) 2022 skydoves (Jaewoong Eum)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.skydoves.retrofit.adapters.serialization

import com.skydoves.retrofit.adapters.core.httpErrorBody
import kotlinx.serialization.json.Json
import retrofit2.HttpException

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.2.0
 *
 * The [Json] instance used by [deserializeHttpError] unless you supply your own.
 *
 * Error payloads routinely carry fields that your error model does not declare, so unknown keys are
 * ignored rather than treated as a parse failure.
 */
public val DefaultErrorJson: Json = Json { ignoreUnknownKeys = true }

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.0.1
 *
 * Deserializes the Json string from error body of the [HttpException] to the [T] custom type. The
 * [Json] instance could be configured as needed.
 *
 * It returns `null` if the exception is not [HttpException], the error body is empty, or the body
 * does not deserialize into [T]. Reading the error body does not consume it, so it can be
 * deserialized more than once.
 */
public inline fun <reified T> Throwable.deserializeHttpError(json: Json = DefaultErrorJson): T? {
  val errorBody = httpErrorBody() ?: return null
  return try {
    json.decodeFromString<T>(errorBody)
  } catch (e: IllegalArgumentException) {
    // SerializationException extends IllegalArgumentException.
    null
  }
}
