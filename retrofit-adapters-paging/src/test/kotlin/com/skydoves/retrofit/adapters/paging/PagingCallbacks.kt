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
package com.skydoves.retrofit.adapters.paging

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.skydoves.retrofit.adapters.test.Pokemon

internal class PokemonListCallback : ListUpdateCallback {
  override fun onInserted(position: Int, count: Int) = Unit
  override fun onRemoved(position: Int, count: Int) = Unit
  override fun onMoved(fromPosition: Int, toPosition: Int) = Unit
  override fun onChanged(position: Int, count: Int, payload: Any?) = Unit
}

internal class PokemonDiffCallback : DiffUtil.ItemCallback<Pokemon>() {
  override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon) = oldItem.name == newItem.name
  override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon) = oldItem == newItem
}
