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
package com.skydoves.retrofit.adapters.result

import com.skydoves.retrofit.adapters.core.NullBodyException
import com.skydoves.retrofit.adapters.test.ApiMockServiceTest
import com.skydoves.retrofit.adapters.test.MainCoroutinesRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class NullBodyTest : ApiMockServiceTest<PokemonService>() {

  @get:Rule
  val coroutinesRule = MainCoroutinesRule()

  private fun service(nullBodyAsFailure: Boolean): PokemonService = createService(
    PokemonService::class.java,
    ResultCallAdapterFactory.create(
      coroutineScope = TestScope(coroutinesRule.testDispatcher),
      nullBodyAsFailure = nullBodyAsFailure,
    ),
  )

  @Test
  fun `a null body stays a successful null by default`() = runTest {
    enqueueEmptyBody()

    val result = service(nullBodyAsFailure = false).fetchPokemonList()

    assertThat(result.isSuccess, `is`(true))
    assertThat(result.getOrNull(), `is`(nullValue()))
  }

  @Test
  fun `a null body becomes a failure when nullBodyAsFailure is on`() = runTest {
    enqueueEmptyBody()

    val result = service(nullBodyAsFailure = true).fetchPokemonList()

    assertThat(result.isFailure, `is`(true))
    assertThat(result.exceptionOrNull() is NullBodyException, `is`(true))
  }
}
