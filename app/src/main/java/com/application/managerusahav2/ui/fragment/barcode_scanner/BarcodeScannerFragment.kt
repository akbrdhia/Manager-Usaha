package com.application.managerusahav2.ui.fragment.barcode_scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.application.managerusahav2.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BarcodeScannerFragment : Fragment() {

    private lateinit var previewView: PreviewView
    private lateinit var btnBack: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvInstruction: TextView
    private lateinit var tvResult: TextView
    private lateinit var resultCard: MaterialCardView
    private lateinit var btnUseResult: MaterialButton
    private lateinit var btnScanAgain: MaterialButton
    private lateinit var btnFlash: MaterialButton

    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private var isFlashOn = false
    private var lastScannedValue = ""
    private var isResultShown = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Permission kamera diperlukan untuk memindai barcode", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_barcode_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun initViews(view: View) {
        previewView = view.findViewById(R.id.previewView)
        btnBack = view.findViewById(R.id.btn_back)
        tvTitle = view.findViewById(R.id.tv_title)
        tvInstruction = view.findViewById(R.id.tv_instruction)
        tvResult = view.findViewById(R.id.tv_result)
        resultCard = view.findViewById(R.id.result_card)
        btnUseResult = view.findViewById(R.id.btn_use_result)
        btnScanAgain = view.findViewById(R.id.btn_scan_again)
        btnFlash = view.findViewById(R.id.btn_flash)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        btnFlash.setOnClickListener {
            toggleFlash()
        }

        btnUseResult.setOnClickListener {
            // Send result back to previous fragment
            parentFragmentManager.setFragmentResult(
                "barcode_result",
                bundleOf("barcode_value" to lastScannedValue)
            )
            findNavController().popBackStack()
        }

        btnScanAgain.setOnClickListener {
            hideResult()
            startCamera() // Restart scanning
        }
    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcodes ->
                        processBarcodeResults(barcodes)
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )

            } catch (exc: Exception) {
                Toast.makeText(requireContext(), "Gagal memulai kamera: ${exc.message}", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun processBarcodeResults(barcodes: List<Barcode>) {
        if (isResultShown) return // Prevent multiple results

        for (barcode in barcodes) {
            val rawValue = barcode.rawValue
            if (rawValue != null && rawValue.isNotEmpty()) {
                lastScannedValue = rawValue
                requireActivity().runOnUiThread {
                    showResult(barcode)
                }
                break
            }
        }
    }

    private fun showResult(barcode: Barcode) {
        isResultShown = true

        // Stop camera
        try {
            ProcessCameraProvider.getInstance(requireContext()).get().unbindAll()
        } catch (e: Exception) {
            // Handle exception
        }

        // Show result UI
        tvInstruction.visibility = View.GONE
        resultCard.visibility = View.VISIBLE

        val result = StringBuilder()
        result.append("Tipe: ${getBarcodeFormat(barcode.format)}\n")
        result.append("Nilai: ${barcode.rawValue}")

        tvResult.text = result.toString()
    }

    private fun hideResult() {
        isResultShown = false
        tvInstruction.visibility = View.VISIBLE
        resultCard.visibility = View.GONE
        lastScannedValue = ""
    }

    private fun getBarcodeFormat(format: Int): String {
        return when (format) {
            Barcode.FORMAT_CODE_128 -> "CODE 128"
            Barcode.FORMAT_CODE_39 -> "CODE 39"
            Barcode.FORMAT_CODE_93 -> "CODE 93"
            Barcode.FORMAT_CODABAR -> "CODABAR"
            Barcode.FORMAT_DATA_MATRIX -> "DATA MATRIX"
            Barcode.FORMAT_EAN_13 -> "EAN 13"
            Barcode.FORMAT_EAN_8 -> "EAN 8"
            Barcode.FORMAT_ITF -> "ITF"
            Barcode.FORMAT_QR_CODE -> "QR CODE"
            Barcode.FORMAT_UPC_A -> "UPC A"
            Barcode.FORMAT_UPC_E -> "UPC E"
            Barcode.FORMAT_PDF417 -> "PDF 417"
            Barcode.FORMAT_AZTEC -> "AZTEC"
            else -> "UNKNOWN"
        }
    }

    private fun toggleFlash() {
        camera?.let { camera ->
            isFlashOn = !isFlashOn
            camera.cameraControl.enableTorch(isFlashOn)

            btnFlash.text = if (isFlashOn) "Flash ON" else "Flash OFF"
            btnFlash.setIconResource(
                if (isFlashOn) R.drawable.ic_flash_on else R.drawable.ic_flash_off
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}

// Barcode Analyzer Class
class BarcodeAnalyzer(
    private val onBarcodeDetected: (barcodes: List<Barcode>) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        onBarcodeDetected(barcodes)
                    }
                }
                .addOnFailureListener {
                    // Handle error
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}