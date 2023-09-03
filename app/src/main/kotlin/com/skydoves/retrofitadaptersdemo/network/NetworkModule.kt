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
package com.skydoves.retrofitadaptersdemo.network

import com.skydoves.retrofit.adapters.arrow.EitherCallAdapterFactory
import com.skydoves.retrofit.adapters.paging.PagingCallAdapterFactory
import com.skydoves.retrofit.adapters.result.ResultCallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

public object NetworkModule {

  private val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl("https://pokeapi.co/api/v2/")
    .addConverterFactory(MoshiConverterFactory.create())
    .addCallAdapterFactory(ResultCallAdapterFactory.create())
    .addCallAdapterFactory(EitherCallAdapterFactory.create())
    .addCallAdapterFactory(PagingCallAdapterFactory.create())
    .build()

  public val disneyService: PokemonService = retrofit.create()
}
