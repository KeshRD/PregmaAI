package com.kairaxus.bundymom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kairaxus.bundymom.ui.theme.BundyMomTheme
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import org.tensorflow.lite.Interpreter

class AIDoctorActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
    private val CAMERA_PERMISSION_CODE = 100

    private val labels = arrayOf(
        "Actinic keratoses", "Basal cell carcinoma", "Benign keratosis",
        "Dermatofibroma", "Melanoma"
    )

    private var onImageCaptured: ((Bitmap) -> Unit)? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            onImageCaptured?.invoke(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        interpreter = Interpreter(loadModelFile("model.tflite"))

        setContent {
            BundyMomTheme {
                AIDoctorScreen(
                    onCameraRequest = { launchCamera() },
                    runModel = { bitmap -> runModel(bitmap) }
                )
            }
        }
    }

    private fun launchCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(null)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadModelFile(modelPath: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun runModel(bitmap: Bitmap): String {
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val input = convertBitmapToByteBuffer(resized)
        val output = Array(1) { FloatArray(labels.size) }

        interpreter.run(input, output)

        val maxIdx = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        val confidence = output[0][maxIdx] * 100
        return "${labels[maxIdx]} (${confidence.toInt()}%)"
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(224 * 224)
        bitmap.getPixels(pixels, 0, 224, 0, 0, 224, 224)

        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f

            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        return byteBuffer
    }

    @Composable
    private fun AIDoctorScreen(
        onCameraRequest: () -> Unit,
        runModel: (Bitmap) -> String
    ) {
        var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
        var diagnosis by remember { mutableStateOf("No scan yet") }

        val context = LocalContext.current
        onImageCaptured = {
            capturedImage = it
            diagnosis = runModel(it)
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = { BottomNavigationBar(currentRoute = "tools") }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // App Title
                Text(
                    text = "AI Scan",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Image(
                    painter = painterResource(id = R.drawable.scan), // replace with your image name
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )

                // Card for Diagnosis Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        capturedImage?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Scanned Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(MaterialTheme.shapes.large)
                            )
                        }

                        Text(
                            text = "Diagnosis",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = diagnosis,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Scan Button
                Button(
                    onClick = onCameraRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = ButtonDefaults.buttonElevation(8.dp)
                ) {
                    Text(
                        text = "Scan Skin",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

    }
}
