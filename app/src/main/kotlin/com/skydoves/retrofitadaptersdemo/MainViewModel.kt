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

package com.skydoves.retrofitadaptersdemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.skydoves.retrofit.adapters.arrow.onLeftSuspend
import com.skydoves.retrofit.adapters.arrow.onRightSuspend
import com.skydoves.retrofit.adapters.result.onFailureSuspend
import com.skydoves.retrofit.adapters.result.onSuccessSuspend
import com.skydoves.retrofit.adapters.serialization.deserializeHttpError
import com.skydoves.retrofitadaptersdemo.network.ErrorMessage
import com.skydoves.retrofitadaptersdemo.network.Pokemon
import com.skydoves.retrofitadaptersdemo.network.PokemonService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber

public class MainViewModel constructor(
  private val pokemonService: PokemonService
) : ViewModel() {

  public fun fetchPosters() {
    viewModelScope.launch {
      val result = pokemonService.fetchPokemonList()
      result.onSuccessSuspend {
        Timber.d("fetched as Result: $it")
      }.onFailureSuspend { throwable ->
        val errorBody = throwable.deserializeHttpError<ErrorMessage>()
        Timber.e("errorBody: $errorBody")
      }
    }
  }

  public fun fetchPostersAsEither() {
    viewModelScope.launch {
      val either = pokemonService.fetchPokemonListAsEither()
      either.onRightSuspend {
        Timber.d("fetched as Either: $it")
      }.onLeftSuspend {
        val errorBody = it.deserializeHttpError<ErrorMessage>()
        Timber.e("errorBody: $errorBody")
      }
    }
  }

  public fun fetchPostersAsPagingSource(): Flow<PagingData<Pokemon>> = flow {
    val pagingSource = pokemonService.fetchPokemonListAsPagingSource()
    val pagerFlow = Pager(PagingConfig(pageSize = 20)) { pagingSource }.flow
    emitAll(pagerFlow)
  }
}
