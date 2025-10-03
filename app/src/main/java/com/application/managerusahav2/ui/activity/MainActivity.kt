package com.application.managerusahav2.ui.activity

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.application.managerusahav2.R
import androidx.navigation.ui.setupWithNavController
import com.application.managerusahav2.data.network.NetworkConfig

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_variant)
        NetworkConfig.init(this)

        setupNavigation()

    }

    private fun showBaseUrlDialog() {
        val editText = EditText(this).apply {
            hint = "Masukkan Base URL"
            val currentUrl = NetworkConfig.getCurrentBaseUrl()
            setText(currentUrl) // Default value
            setPadding(50, 30, 50, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("Konfigurasi Server")
            .setMessage("Masukkan URL server API:")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                val baseUrl = editText.text.toString().trim()
                if (baseUrl.isNotEmpty()) {
                    // Update base URL
                    NetworkConfig.updateBaseUrl(baseUrl)

                    // Setup navigation after URL is set
                    setupNavigation()
                    val currentUrl = NetworkConfig.getCurrentBaseUrl()
                    Toast.makeText(this, "Current URL: $currentUrl", Toast.LENGTH_LONG).show()
                } else {
                    setupNavigation()
                    val currentUrl = NetworkConfig.getCurrentBaseUrl()
                    Toast.makeText(this, "Current URL: $currentUrl", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                // Use default URL if cancelled
                setupNavigation()
                val currentUrl = NetworkConfig.getCurrentBaseUrl()
                Toast.makeText(this, "Current URL: $currentUrl", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setCancelable(false) // Prevent dismissing by touching outside
            .show()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
            R.id.bottomNavigationView
        )
        bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> bottomNav.menu.findItem(R.id.homeFragment).isChecked = true
                R.id.laporanFragment -> bottomNav.menu.findItem(R.id.laporanFragment).isChecked = true
                R.id.barangFragment -> bottomNav.menu.findItem(R.id.barangFragment).isChecked = true
                R.id.riwayatFragment -> bottomNav.menu.findItem(R.id.riwayatFragment).isChecked = true
            }
        }
    }
}