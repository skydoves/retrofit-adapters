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

import com.skydoves.retrofit.adapters.test.ApiMockServiceTest
import com.skydoves.retrofit.adapters.test.MainCoroutinesRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.moshi.MoshiConverterFactory

@RunWith(JUnit4::class)
internal class ResultCallTest : ApiMockServiceTest<PokemonService>() {

  @get:Rule
  val coroutinesRule = MainCoroutinesRule()

  private val testScope = TestScope(coroutinesRule.testDispatcher)

  private lateinit var service: PokemonService

  @Before
  fun initService() {
    service = createService(PokemonService::class.java, ResultCallAdapterFactory.create(testScope))
  }

  @Test
  fun `fetch items as Result type with suspend function successfully`() = runTest {
    enqueueResponse("/PokemonResponse.json")

    val result = service.fetchPokemonList()
    assertThat(result.isSuccess, `is`(true))

    val data = result.getOrNull()!!
    assertThat(data.count, `is`(964))
    assertThat(data.results[0].name, `is`("bulbasaur"))
    assertThat(data.results[0].url, `is`("https://pokeapi.co/api/v2/pokemon/1/"))
  }

  @Test
  fun `fetch items as Result type suspend function with error`() = runTest {
    val retrofit: Retrofit = Retrofit.Builder()
      .baseUrl(mockWebServer.url("/"))
      .addConverterFactory(MoshiConverterFactory.create())
      .addCallAdapterFactory(ResultCallAdapterFactory.create(testScope))
      .build()

    val service = retrofit.create(PokemonService::class.java)
    mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("foo"))

    val result = service.fetchPokemonList()
    assertThat(result.isSuccess, `is`(false))
  }

  @Test
  fun `fetch items as Result type suspend function with empty body`() = runTest {
    val retrofit: Retrofit = Retrofit.Builder()
      .baseUrl(mockWebServer.url("/"))
      .addConverterFactory(MoshiConverterFactory.create())
      .addCallAdapterFactory(ResultCallAdapterFactory.create(testScope))
      .build()

    val service = retrofit.create(PokemonService::class.java)
    mockWebServer.enqueue(MockResponse().setResponseCode(204).setBody(""))

    val result = service.fetchPokemonListEmptyBody()
    assertThat(result.isSuccess, `is`(true))
    assertThat(result.getOrNull(), `is`(Unit))
  }

  @Test
  fun `fetch items as Call type of Result successfully`() = runTest {
    enqueueResponse("/PokemonResponse.json")

    val result = service.fetchPokemonListAsCall().awaitResponse().body()!!
    assertThat(result.isSuccess, `is`(true))

    val data = result.getOrNull()!!
    assertThat(data.count, `is`(964))
    assertThat(data.results[0].name, `is`("bulbasaur"))
    assertThat(data.results[0].url, `is`("https://pokeapi.co/api/v2/pokemon/1/"))
  }

  @Test
  fun `fetch items as Result type suspend function with optional body`() = runTest {
    enqueueResponse("/PokemonResponse.json")

    val result = service.fetchPokemonListOptional()
    assertThat(result.isSuccess, `is`(true))

    val data = result.getOrNull()!!
    assertThat(data.count, `is`(964))
    assertThat(data.results[0].name, `is`("bulbasaur"))
    assertThat(data.results[0].url, `is`("https://pokeapi.co/api/v2/pokemon/1/"))

    mockWebServer.enqueue(MockResponse().setResponseCode(204))

    val resultNull = service.fetchPokemonListOptional()
    assertThat(resultNull.isSuccess, `is`(true))
    Assert.assertNull(resultNull.getOrNull())
  }
}
