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
package com.skydoves.retrofitadaptersdemo.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.skydoves.retrofitadaptersdemo.databinding.ItemPokemonBinding
import com.skydoves.retrofitadaptersdemo.network.Pokemon

public class MainPagingAdapter :
  PagingDataAdapter<Pokemon, MainPagingAdapter.ViewHolder>(DataDifferntiator) {

  public class ViewHolder(
    public val binding: ItemPokemonBinding,
  ) : RecyclerView.ViewHolder(binding.root)

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val pokemon = getItem(position) ?: return
    holder.binding.name.text = pokemon.name
    Glide.with(holder.binding.image.context)
      .load(pokemon.getImageUrl())
      .into(holder.binding.image)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val binding = ItemPokemonBinding.inflate(
      LayoutInflater.from(parent.context),
      parent,
      false,
    )
    return ViewHolder(binding)
  }

  internal object DataDifferntiator : DiffUtil.ItemCallback<Pokemon>() {

    override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
      return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
      return oldItem == newItem
    }
  }
}
