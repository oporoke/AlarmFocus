package com.omondit.alarmfocus.presentation.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.omondit.alarmfocus.domain.model.Challenge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors

@Composable
fun PhotoCaptureScreen(
    challenge: Challenge,
    onPhotoTaken: (String) -> Unit, // Returns file path
    onTimeout: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var timeRemaining by remember { mutableStateOf(challenge.timeoutSeconds) }
    var capturedPhoto by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var showThumbnail by remember { mutableStateOf(false) }

    // Countdown timer
    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        } else {
            onTimeout()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Preview
        PhotoCameraPreview(
            onPhotoTaken = { bitmap, filePath ->
                capturedPhoto = bitmap
                isProcessing = true
                showThumbnail = true

                scope.launch {
                    delay(1500) // Show preview briefly
                    onPhotoTaken(filePath)
                }
            },
            onCameraReady = { cameraInstance ->
                camera = cameraInstance
            },
            modifier = Modifier.fillMaxSize()
        )

        // Photo overlay guide
        PhotoGuideOverlay(
            targetName = challenge.data["displayName"] as? String ?: "target location",
            modifier = Modifier.fillMaxSize()
        )

        // Top controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Cancel button
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.White
                )
            }

            // Timer
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.8f)
                )
            ) {
                Text(
                    text = "${timeRemaining}s",
                    color = if (timeRemaining <= 15) Color.Red else Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Reference photo thumbnail (if available)
        AnimatedVisibility(
            visible = showThumbnail && capturedPhoto != null,
            enter = slideInHorizontally() + fadeIn(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp, 80.dp, 16.dp, 16.dp)
        ) {
            capturedPhoto?.let { bitmap ->
                Card(
                    modifier = Modifier.size(100.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Green.copy(alpha = 0.9f)
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Captured photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(32.dp)
                        )
                    }
                }
            }
        }

        // Bottom instruction panel
        PhotoInstructionPanel(
            targetName = challenge.data["displayName"] as? String ?: "target location",
            location = challenge.data["location"] as? String ?: "",
            description = challenge.data["description"] as? String ?: "",
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Processing indicator
        AnimatedVisibility(
            visible = isProcessing,
            enter = fadeIn(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.8f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Processing photo...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoCameraPreview(
    onPhotoTaken: (Bitmap, String) -> Unit,
    onCameraReady: (Camera) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageCaptureBuilder = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)

                imageCapture = imageCaptureBuilder.build()

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                    onCameraReady(camera)
                } catch (exc: Exception) {
                    // Handle camera binding failure
                }
            }, executor)

            previewView
        },
        modifier = modifier
    )

    // Auto-capture after 3 seconds (or on tap - would need gesture detection)
    LaunchedEffect(imageCapture) {
        delay(3000)
        imageCapture?.let { capture ->
            val outputFile = File.createTempFile("photo_mission", ".jpg", context.cacheDir)
            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

            capture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val bitmap = BitmapFactory.decodeFile(outputFile.absolutePath)
                        bitmap?.let {
                            onPhotoTaken(it, outputFile.absolutePath)
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        // Handle error
                    }
                }
            )
        }
    }
}

@Composable
private fun PhotoGuideOverlay(
    targetName: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Semi-transparent overlay with cutout
        Canvas(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center)
        ) {
            val strokeWidth = 3.dp.toPx()
            val cornerLength = 30.dp.toPx()

            // Draw corner guides
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(0f, cornerLength),
                end = androidx.compose.ui.geometry.Offset(0f, 0f),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(cornerLength, 0f),
                strokeWidth = strokeWidth
            )

            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(size.width - cornerLength, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, cornerLength),
                strokeWidth = strokeWidth
            )

            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(size.width, size.height - cornerLength),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(size.width, size.height),
                end = androidx.compose.ui.geometry.Offset(size.width - cornerLength, size.height),
                strokeWidth = strokeWidth
            )

            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(cornerLength, size.height),
                end = androidx.compose.ui.geometry.Offset(0f, size.height),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(0f, size.height),
                end = androidx.compose.ui.geometry.Offset(0f, size.height - cornerLength),
                strokeWidth = strokeWidth
            )
        }

        // Center crosshair
        Icon(
            Icons.Default.CenterFocusStrong,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp)
        )
    }
}

@Composable
private fun PhotoInstructionPanel(
    targetName: String,
    location: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.PhotoCamera,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Match photo: $targetName",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (location.isNotBlank()) {
                Text(
                    text = "Location: $location",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            if (description.isNotBlank()) {
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Position yourself at the same location and angle as the reference photo",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}
