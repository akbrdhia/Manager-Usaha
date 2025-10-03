package com.application.managerusahav2.ui.fragment.home

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.application.managerusahav2.R
import com.application.managerusahav2.helper.StatusBarHelper
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.application.managerusahav2.data.network.NetworkConfig
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var tvHome: TextView
    private lateinit var btnLaporan: MaterialButton
    private lateinit var urlInputLayout: TextInputLayout
    private lateinit var urlEditText: TextInputEditText
    private lateinit var card1: MaterialCardView
    private lateinit var card2: MaterialCardView
    private lateinit var plate: MaterialCardView
    private lateinit var materialCardView2: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        StatusBarHelper.setStatusBar(requireActivity(), isLight = false)

        initViews(view)
        handleEvents()
    }

    private fun handleEvents() {
        card1.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_tentangFragment)
        }

        card2.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_panduanFragment)
        }
        urlInputLayout.setStartIconOnClickListener {
            val inputUrl = urlEditText.text.toString().trim()

            if (inputUrl.isNotEmpty()) {
                // Validasi minimal: harus diawali http:// atau https://
                if (!inputUrl.startsWith("http://") && !inputUrl.startsWith("https://")) {
                    Toast.makeText(requireContext(), "URL harus diawali http:// atau https://", Toast.LENGTH_SHORT).show()
                    return@setStartIconOnClickListener
                }

                // Update base URL
                NetworkConfig.updateBaseUrl(inputUrl)

                // Ambil current URL (pastikan konsisten)
                val currentUrl = NetworkConfig.getCurrentBaseUrl()
                urlEditText.setText(currentUrl)
                urlEditText.isEnabled = false
                urlEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey_hint))
                urlInputLayout.isEnabled = false

                Toast.makeText(requireContext(), "URL berhasil disimpan. (Restart App Jika Ingin Mengubah)" , Toast.LENGTH_LONG).show()
            } else {
                // Kalau kosong pakai default dari NetworkConfig
                val currentUrl = NetworkConfig.getCurrentBaseUrl()
                urlEditText.setText(currentUrl)
                Toast.makeText(requireContext(), "URL tidak boleh kosong. Pakai default: $currentUrl", Toast.LENGTH_SHORT).show()
            }
        }

        btnLaporan.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_laporanFragment)
        }

    }

    private fun initViews(view: View) {
        tvHome = view.findViewById(R.id.tv_home)
        btnLaporan = view.findViewById(R.id.btn_laporan_keuangan) // kalau lu kasih id button
        urlInputLayout = view.findViewById(R.id.url_input_layout)
        urlEditText = view.findViewById(R.id.url_edit_text)
        card1 = view.findViewById(R.id.card1)
        card2 = view.findViewById(R.id.card2)
        plate = view.findViewById(R.id.plate)
        materialCardView2 = view.findViewById(R.id.materialCardView2)

        urlEditText.setText(NetworkConfig.getCurrentBaseUrl())
    }
}