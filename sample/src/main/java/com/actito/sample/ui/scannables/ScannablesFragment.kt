package com.actito.sample.ui.scannables

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.actito.Actito
import com.actito.push.ui.ktx.pushUI
import com.actito.sample.R
import com.actito.sample.databinding.FragmentScannablesBinding
import com.actito.scannables.ActitoScannables
import com.actito.scannables.ActitoUserCancelledScannableSessionException
import com.actito.scannables.ktx.scannables
import com.actito.scannables.models.ActitoScannable
import timber.log.Timber

class ScannablesFragment : Fragment(), ActitoScannables.ScannableSessionListener {
    private lateinit var binding: FragmentScannablesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Actito.scannables().addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        Actito.scannables().removeListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentScannablesBinding.inflate(inflater, container, false)

        checkNfcStatus()
        setupListeners()

        return binding.root
    }

    override fun onScannableDetected(scannable: ActitoScannable) {
        val notification = scannable.notification ?: run {
            Timber.i("Scannable without notification detected.")
            Snackbar.make(requireView(), "Scannable without notification detected.", Snackbar.LENGTH_SHORT)
                .show()

            return
        }

        Actito.pushUI().presentNotification(requireActivity(), notification)
    }

    override fun onScannableSessionError(error: Exception) {
        if (error is ActitoUserCancelledScannableSessionException) {
            return
        }

        Timber.e(error, "Scannable session error.")
        Snackbar.make(requireView(), "Scannable session error: ${error.message}", Snackbar.LENGTH_SHORT)
            .show()
    }

    private fun checkNfcStatus() {
        val nfcStatus = binding.nfcAvailableStatus

        if (Actito.scannables().canStartNfcScannableSession) {
            nfcStatus.text = getString(R.string.scannables_nfc_and_qr_code_available)

            return
        }

        nfcStatus.text = getString(R.string.scannables_only_qr_code_available)
        binding.scannablesNfcButton.isEnabled = false
    }

    private fun setupListeners() {
        binding.scannablesNfcButton.setOnClickListener {
            Actito.scannables().startNfcScannableSession(requireActivity())
        }

        binding.scannablesQrCodeButton.setOnClickListener {
            Actito.scannables().startQrCodeScannableSession(requireActivity())
        }
    }
}
