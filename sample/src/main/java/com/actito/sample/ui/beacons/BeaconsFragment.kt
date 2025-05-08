package com.actito.sample.ui.beacons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.actito.Actito
import com.actito.geo.ActitoGeo
import com.actito.geo.ktx.geo
import com.actito.geo.models.ActitoBeacon
import com.actito.geo.models.ActitoLocation
import com.actito.geo.models.ActitoRegion
import com.actito.sample.databinding.FragmentBeaconsBinding

class BeaconsFragment : Fragment(), ActitoGeo.Listener {
    private lateinit var binding: FragmentBeaconsBinding
    private val adapter = BeaconsListAdapter()
    private val viewModel: BeaconsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBeaconsBinding.inflate(inflater, container, false)

        binding.beaconsList.layoutManager = LinearLayoutManager(requireContext())
        binding.beaconsList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.beaconsList.adapter = adapter

        setupObservers()

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        Actito.geo().addListener(this)
    }

    override fun onStop() {
        super.onStop()

        Actito.geo().removeListener(this)
    }

    override fun onLocationUpdated(location: ActitoLocation) {
        Snackbar.make(requireView(), "location = (${location.latitude}, ${location.longitude})", Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun onBeaconsRanged(region: ActitoRegion, beacons: List<ActitoBeacon>) {
        val beaconsRanged = BeaconsViewModel.BeaconsData(region, beacons)

        binding.noBeaconsLabel.isVisible = beacons.isEmpty()
        viewModel.beaconsData.postValue(beaconsRanged)
    }

    private fun setupObservers() {
        viewModel.beaconsData.observe(viewLifecycleOwner) { beaconsData ->
            adapter.submitList(beaconsData.beacons)
        }
    }
}
