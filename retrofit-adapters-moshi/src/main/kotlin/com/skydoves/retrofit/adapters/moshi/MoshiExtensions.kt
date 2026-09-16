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
package com.skydoves.retrofit.adapters.moshi

import com.skydoves.retrofit.adapters.core.httpErrorBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import retrofit2.HttpException
import java.io.IOException

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.2.0
 *
 * The [Moshi] instance used by [deserializeHttpError] unless you supply your own.
 *
 * It resolves generated adapters, so annotate your error model with
 * `@JsonClass(generateAdapter = true)` and apply `moshi-kotlin-codegen`. Pass your own [Moshi] with
 * `KotlinJsonAdapterFactory` registered if you would rather reflect over the model instead.
 */
public val DefaultErrorMoshi: Moshi = Moshi.Builder().build()

/**
 * @author skydoves (Jaewoong Eum)
 * @since 1.2.0
 *
 * Deserializes the json string from the error body of the [HttpException] to the [T] custom type.
 * The [Moshi] instance could be configured as needed.
 *
 * It returns `null` if the exception is not [HttpException], the error body is empty, or the body
 * does not deserialize into [T]. Reading the error body does not consume it, so it can be
 * deserialized more than once.
 */
@OptIn(ExperimentalStdlibApi::class)
public inline fun <reified T> Throwable.deserializeHttpError(moshi: Moshi = DefaultErrorMoshi): T? {
  val errorBody = httpErrorBody() ?: return null
  return try {
    moshi.adapter<T>().fromJson(errorBody)
  } catch (e: IOException) {
    null
  } catch (e: com.squareup.moshi.JsonDataException) {
    null
  }
}
