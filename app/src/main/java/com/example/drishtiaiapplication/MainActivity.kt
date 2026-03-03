package com.example.drishtiaiapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.drishtiaiapplication.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private val cameraPermissionCode = 100
    private var isScanning = false
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var binding: ActivityMainBinding
    private var lastSpokenText = ""
    private val recognizer = TextRecognition.getClient(
        TextRecognizerOptions.DEFAULT_OPTIONS
    )

    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        previewView = findViewById(R.id.previewView)
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                cameraPermissionCode
            )
        } else {
            binding.btnStart.setOnClickListener {

                if (!isScanning) {
                    startCamera()
                    binding.btnStart.text = "Stop"
                } else {
                    stopCamera()
                    binding.btnStart.text = "Scan"
                }
            }
        }

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
                textToSpeech.setSpeechRate(0.9f)  // slower for accessibility
            }
        }

        binding.btnSettings.setOnClickListener {

            startActivity(Intent(this, SettingsActivity::class.java))
            overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.slide_out_right
            )
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun startCamera() {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            // Store globally
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(this)
            ) { imageProxy ->

                val mediaImage = imageProxy.image
                if (mediaImage != null) {

                    val image = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->

                            val detectedText = visionText.text

                            binding.tvDetectedContent.text = detectedText

                            if (detectedText.isNotEmpty() && detectedText != lastSpokenText) {
                                lastSpokenText = detectedText
                                processSignboard(detectedText)
                            }

                        }
                        .addOnFailureListener {
                            it.printStackTrace()
                        }
                        .addOnCompleteListener {
                            imageProxy.close()   // VERY IMPORTANT
                        }

                } else {
                    imageProxy.close()
                }
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider?.unbindAll()

            cameraProvider?.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalysis
            )

            // Show preview when started
            binding.previewView.visibility = View.VISIBLE
            isScanning = true

        }, ContextCompat.getMainExecutor(this))
    }


    private fun stopCamera() {
        cameraProvider?.unbindAll()
        binding.previewView.visibility = View.GONE
        isScanning = false
    }

    private fun speakText(text: String) {
        textToSpeech.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            null
        )
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == cameraPermissionCode) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun processSignboard(text: String) {
        val upperText = text.uppercase()

        when {
            upperText.contains("STOP") -> speakText("Stop sign ahead")
            upperText.contains("NO ENTRY") -> speakText("No entry")
            upperText.contains("SPEED") -> speakText("Speed limit sign")
            upperText.contains("LEFT") -> speakText("Turn left")
            upperText.contains("RIGHT") -> speakText("Turn right")
            upperText.contains("HOSPITAL") -> speakText("Hospital nearby")
            upperText.contains("SCHOOL") -> speakText("School zone")
            else -> speakText(text)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }
}