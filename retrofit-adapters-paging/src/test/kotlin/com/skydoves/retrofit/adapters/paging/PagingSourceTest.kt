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
@file:OptIn(ExperimentalTime::class)

package com.skydoves.retrofit.adapters.paging

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import com.skydoves.retrofit.adapters.test.ApiMockServiceTest
import com.skydoves.retrofit.adapters.test.MainCoroutinesRule
import com.skydoves.retrofit.adapters.test.Pokemon
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.time.ExperimentalTime

@RunWith(JUnit4::class)
internal class PagingSourceTest : ApiMockServiceTest<PokemonService>() {

  @get:Rule
  val coroutinesRule = MainCoroutinesRule()
  private val testScope = TestScope(coroutinesRule.testDispatcher)

  private lateinit var service: PokemonService

  @Before
  fun initService() {
    service = createService(PokemonService::class.java, PagingCallAdapterFactory.create())
  }

  @Test
  fun `fetches items as NetworkPagingSource type with suspend function successfully`() = runTest {
    enqueueResponse("/PokemonResponse.json")

    val pagingSource = service.fetchPokemonList()
    val loadResult = pagingSource.load(
      PagingSource.LoadParams.Refresh(
        key = 0,
        loadSize = 20,
        placeholdersEnabled = false,
      ),
    )

    assertThat(loadResult, instanceOf(PagingSource.LoadResult.Page::class.java))

    val page = loadResult as PagingSource.LoadResult.Page
    val data = page.data[0]
    assertThat(data.name, `is`("bulbasaur"))
    assertThat(data.url, `is`("https://pokeapi.co/api/v2/pokemon/1/"))
  }

  @Test
  fun `differ transforms loaded paging data`() = testScope.runTest {
    val differ = AsyncPagingDataDiffer(
      diffCallback = PokemonDiffCallback(),
      updateCallback = PokemonListCallback(),
      workerDispatcher = coroutinesRule.testDispatcher,
    )
    val pokemon = Pokemon(
      name = "bulbasaur",
      url = "https://pokeapi.co/api/v2/pokemon/1/",
    )
    val pagingData = PagingData.from(
      listOf(pokemon, pokemon, pokemon),
    )
    differ.submitData(pagingData)

    // Wait for transforms and the differ to process all updates.
    advanceUntilIdle()

    assertThat(differ.snapshot().items.size, `is`(3))
    assertThat(differ.snapshot().items[0], `is`(pokemon))
  }
}
