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

package com.skydoves.retrofit.adapters.serialize

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import retrofit2.HttpException

/**
 * Deserializes the Json string from error body of the [HttpException] to the [T] custom type.
 * It returns null if the error body is empty.
 */
public inline fun <reified T> HttpException.deserializeErrorBody(): T? {
  val errorBody = response()?.errorBody()?.toString() ?: return null
  return Json.decodeFromJsonElement(JsonPrimitive(errorBody))
}
