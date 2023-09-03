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
package com.skydoves.retrofit.adapters.arrow

import com.skydoves.retrofit.adapters.test.ApiMockServiceTest
import com.skydoves.retrofit.adapters.test.MainCoroutinesRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
internal class EitherDeferredCallTest : ApiMockServiceTest<PokemonDeferredService>() {

  @get:Rule
  val coroutinesRule = MainCoroutinesRule()

  private lateinit var service: PokemonDeferredService

  @Before
  fun initService() {
    val testScope = TestScope(coroutinesRule.testDispatcher)
    service =
      createService(PokemonDeferredService::class.java, EitherCallAdapterFactory.create(testScope))
  }

  @Test
  fun `fetch items as Deferred type of Result successfully`() = runTest {
    enqueueResponse("/PokemonResponse.json")

    val response = service.fetchPokemonListAsync().await()
    assertThat(response.isRight(), `is`(true))

    val data = response.orNull()!!
    assertThat(data.count, `is`(964))
    assertThat(data.results[0].name, `is`("bulbasaur"))
    assertThat(data.results[0].url, `is`("https://pokeapi.co/api/v2/pokemon/1/"))
  }
}
