package com.dzakdzaks.ocr.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.dzakdzaks.ocr.core.util.OCRResult
import com.dzakdzaks.ocr.core.util.collectLatestLifecycleFlow
import com.dzakdzaks.ocr.core.util.goToApplicationSettings
import com.dzakdzaks.ocr.core.util.outputImageDirectory
import com.dzakdzaks.ocr.core.util.rotateImageCorrectly
import com.dzakdzaks.ocr.core.util.showSnackBar
import com.dzakdzaks.ocr.core.util.showSnackBarIndefinite
import com.dzakdzaks.ocr.core.util.visibleGone
import com.dzakdzaks.ocr.data.maps.api.model.Route
import com.dzakdzaks.ocr.databinding.ActivityMainBinding
import com.dzakdzaks.ocr.ui.second.SecondActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.AndroidEntryPoint
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@ExperimentalGetImage
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<MainViewModel>()

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val granted = it.value
                val permission = it.key
                if (!granted) {
                    val neverAskAgain = !ActivityCompat.shouldShowRequestPermissionRationale(
                        this, permission
                    )
                    if (neverAskAgain) {
                        binding.frameButton.showSnackBarIndefinite("Need approve permission on settings") { goToApplicationSettings() }
                    } else {
                        binding.frameButton.showSnackBarIndefinite("Need approve permission") { checkPermission() }
                    }
                    return@registerForActivityResult
                }
            }
            startCamera()
        }

    private var imageCapture: ImageCapture? = null
    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            this
        )
    }
    private val cancellationToken: CancellationToken = object : CancellationToken() {
        override fun onCanceledRequested(p0: OnTokenCanceledListener) =
            CancellationTokenSource().token

        override fun isCancellationRequested() = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        collectData()
        checkPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun initView() {
        supportActionBar?.hide()
        with(binding) {
            onBackPressedDispatcher.addCallback(this@MainActivity,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (constraintPreview.isVisible) removePreview()
                        else finish()
                    }
                })

            imageTakePicture.setOnClickListener { takeSnapshot(binding.previewView.bitmap) }
            imageButtonClose.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun collectData() {
        with(viewModel) {
            this@MainActivity.collectLatestLifecycleFlow(path) {
                when (it) {
                    OCRResult.Empty -> {}
                    is OCRResult.Error -> it.let(::handleError)
                    OCRResult.Loading -> {}
                    is OCRResult.Success -> it.data.route.let(::getPathHandleSuccess)
                }
            }
            this@MainActivity.collectLatestLifecycleFlow(uploadPath) {
                when (it) {
                    OCRResult.Empty -> {}
                    is OCRResult.Error -> it.let(::handleError)
                    OCRResult.Loading -> {}
                    is OCRResult.Success -> it.data.let(::uploadPathHandleSuccess)
                }
            }
            this@MainActivity.collectLatestLifecycleFlow(isLoading) {
                binding.frameLoading.visibleGone(it)
            }
        }
    }

    private fun handleError(error: OCRResult.Error) {
        viewModel.setIsLoading(false)
        binding.root.showSnackBar(error.message)
    }

    private fun getPathHandleSuccess(route: Route) {
        with(viewModel) {
            duration = route.duration
            distance = route.distance
            uploadDrivingPath()
        }
    }

    private fun uploadPathHandleSuccess(string: String) {
        viewModel.setIsLoading(false)
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
        SecondActivity.start(
            context = this@MainActivity,
            resultText = viewModel.resultText,
            duration = viewModel.duration,
            distance = viewModel.distance,
        )
        removePreview()
    }

    private fun checkPermission() {
        PERMISSIONS.forEach { permission ->
            if (ContextCompat.checkSelfPermission(
                    this, permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                requestMultiplePermissions.launch(PERMISSIONS)
                return
            }
        }
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            imageCapture =
                ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture,
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takeSnapshot(bitmap: Bitmap?) {
        viewModel.setIsLoading(true)

        viewModel.photoFile = outputImageDirectory()

        viewModel.photoFile?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                val stream: OutputStream = FileOutputStream(it)
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                stream.flush()
                stream.close()
                rotateImageCorrectly(it)

                withContext(Dispatchers.Main) {
                    val savedUri = it.toUri()
                    with(binding) {
                        constraintPreview.visibleGone()
                        imagePreview.setImageURI(savedUri)
                    }
                    recognizeText(savedUri)
                }
            }
        }
    }

    private fun recognizeText(uri: Uri) {
        try {
            val image: InputImage = InputImage.fromFilePath(this, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image).addOnSuccessListener { resultText ->
                viewModel.resultText = resultText.text
                getLocationCurrent()
            }.addOnFailureListener {
                it.printStackTrace()
                viewModel.setIsLoading(false)
            }.addOnCompleteListener {
                recognizer.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getLocationCurrent() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            checkPermission()
            return
        }
        fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationToken)
            .addOnSuccessListener {
                viewModel.currentLocation = it
                viewModel.loadFindDrivingPath()
            }.addOnFailureListener {
                viewModel.setIsLoading(false)
            }
    }

    private fun removePreview() {
        with(binding) {
            val result = viewModel.deletePhotoFile()
            if (result) {
                constraintPreview.visibleGone()
            }
        }
    }

    companion object {
        private val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }

}
