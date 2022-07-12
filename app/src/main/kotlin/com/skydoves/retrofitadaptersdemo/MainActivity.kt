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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.skydoves.retrofitadaptersdemo.adapters.MainPagingAdapter
import com.skydoves.retrofitadaptersdemo.databinding.ActivityMainBinding

public class MainActivity : AppCompatActivity() {

  private val viewModelFactory: MainViewModelFactory = MainViewModelFactory()
  private val viewModel: MainViewModel by lazy {
    viewModelFactory.create(MainViewModel::class.java)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

    viewModel.fetchPosters()
    viewModel.fetchPostersAsEither()

    val adapter = MainPagingAdapter()
    binding.recyclerView.adapter = adapter

    lifecycleScope.launchWhenStarted {
      viewModel.fetchPostersAsPagingSource().collect {
        adapter.submitData(it)
      }
    }
  }
}
