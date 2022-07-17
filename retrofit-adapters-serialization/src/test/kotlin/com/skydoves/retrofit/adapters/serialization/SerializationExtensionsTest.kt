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

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.HttpException
import retrofit2.Response

@RunWith(JUnit4::class)
internal class SerializationExtensionsTest {

  @Test
  fun `deserializeErrorBody Test`() {
    val response = Response.error<String>(
      403,
      ("""{"code":10001, "message":"This is a custom error message"}""".trimIndent()).toResponseBody(
        contentType = "text/plain".toMediaType()
      )
    )
    val httpException = HttpException(response)
    val errorMessage = httpException.deserializeErrorBody<ErrorMessage>()
    assertThat(errorMessage?.code, `is`(10001))
    assertThat(errorMessage?.message, `is`("This is a custom error message"))
  }
}
