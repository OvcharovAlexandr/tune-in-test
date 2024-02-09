/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.tune_in_test.overview

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.android.tune_in_test.R
import com.example.android.tune_in_test.databinding.FragmentOverviewBinding
import com.example.android.tune_in_test.network.TuneInProperty

class OverviewFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        val application = requireNotNull(activity).application
        val binding = FragmentOverviewBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.lifecycleOwner = viewLifecycleOwner

        var tuneInProperty: TuneInProperty = try {
            OverviewFragmentArgs.fromBundle(requireArguments()).selectedProperty
        } catch (e: Exception) {
            TuneInProperty("", "", "", listOf(), "")
        }

        val viewModelFactory = OverviewViewModelFactory(tuneInProperty, application)

        val viewModel: OverviewViewModel = ViewModelProvider(
            this, viewModelFactory
        )[OverviewViewModel::class.java]

        binding.viewModel = viewModel

        binding.itemList.adapter = ItemListAdapter(ItemListAdapter.OnClickListener {
            viewModel.displayPropertyDetails(it)
        })

        viewModel.navigateToSelectedProperty.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                if (it.type != "audio") {
                    this.findNavController()
                        .navigate(OverviewFragmentDirections.actionShowProperties(it))
                } else {
                    this.findNavController()
                        .navigate(OverviewFragmentDirections.actionShowProperty(it))
                }
                viewModel.displayPropertyDetailsComplete()
            }
        })

        return binding.root
    }

}
