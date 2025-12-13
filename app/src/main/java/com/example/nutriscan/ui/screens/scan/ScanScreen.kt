package com.example.nutriscan.ui.screens.scan

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun ScanScreen(
    navController: NavController,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.scanState.collectAsState()

    // --- Launcher untuk Galeri (Photo Picker) ---
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = uriToBitmap(context, uri)
            if (bitmap != null) {
                viewModel.analyzeImage(bitmap)
            } else {
                Toast.makeText(context, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Launcher untuk Izin Kamera ---
    var hasCameraPermission by remember { mutableStateOf(false) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )
    
    // --- Launcher untuk Izin Lokasi ---
    var hasLocationPermission by remember { mutableStateOf(false) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasLocationPermission = granted }
    )

    LaunchedEffect(Unit) {
        // Request camera permission
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        // Request location permission (untuk tracking lokasi saat scan)
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    if (hasCameraPermission) {
        // 1. TAMPILKAN KAMERA SELALU SEBAGAI BACKGROUND
        CameraView(
            context = context,
            lifecycleOwner = lifecycleOwner,
            isLoading = state is ScanState.Loading,
            onImageCaptured = { bitmap -> viewModel.analyzeImage(bitmap) },
            onGalleryClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        )

        // 2. OVERLAY HASIL JIKA SUKSES
        if (state is ScanState.Success) {
            // Ambil data dan gambar dari state
            val successState = state as ScanState.Success
            val foodData = successState.data
            val capturedImage = successState.image

            // Overlay Gelap Transparan (Scrim)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { viewModel.resetState() } // Klik luar untuk tutup
            )

            // Kartu Hasil (Bottom Sheet Style)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                FoodResultCard(
                    data = foodData,
                    imageBitmap = capturedImage,
                    locationAddress = (state as? ScanState.Success)?.locationAddress,
                    onScanAgain = { viewModel.resetState() }
                )
            }
        }

        // 3. JIKA ERROR, TAMPILKAN TOAST
        if (state is ScanState.Error) {
            val errorMsg = (state as ScanState.Error).message
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            viewModel.resetState() // Reset biar kamera aktif lagi
        }

    } else {
        // Tampilan jika izin ditolak
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Izin kamera diperlukan untuk fitur ini.")
        }
    }
}

@Composable
fun CameraView(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    isLoading: Boolean,
    onImageCaptured: (Bitmap) -> Unit,
    onGalleryClick: () -> Unit
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val executor = ContextCompat.getMainExecutor(context)

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. PREVIEW KAMERA
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val preview = Preview.Builder().build()
                val selector = CameraSelector.DEFAULT_BACK_CAMERA

                imageCapture = ImageCapture.Builder().build()

                preview.setSurfaceProvider(previewView.surfaceProvider)

                val cameraProvider = cameraProviderFuture.get()
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageCapture
                    )
                } catch (e: Exception) {
                    Log.e("CameraX", "Binding failed", e)
                }
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. VIEWFINDER OVERLAY (Kotak Putih di Tengah)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 100.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(280.dp)) {
                val strokeWidth = 4.dp.toPx()
                val cornerSize = 40.dp.toPx()
                val color = Color.White

                // Gambar sudut-sudut kotak
                // Kiri Atas
                drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(cornerSize, 0f), strokeWidth = strokeWidth)
                drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(0f, cornerSize), strokeWidth = strokeWidth)
                // Kanan Atas
                drawLine(color, start = androidx.compose.ui.geometry.Offset(size.width, 0f), end = androidx.compose.ui.geometry.Offset(size.width - cornerSize, 0f), strokeWidth = strokeWidth)
                drawLine(color, start = androidx.compose.ui.geometry.Offset(size.width, 0f), end = androidx.compose.ui.geometry.Offset(size.width, cornerSize), strokeWidth = strokeWidth)
                // Kiri Bawah
                drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(cornerSize, size.height), strokeWidth = strokeWidth)
                drawLine(color, start = androidx.compose.ui.geometry.Offset(0f, size.height), end = androidx.compose.ui.geometry.Offset(0f, size.height - cornerSize), strokeWidth = strokeWidth)
                // Kanan Bawah
                drawLine(color, start = androidx.compose.ui.geometry.Offset(size.width, size.height), end = androidx.compose.ui.geometry.Offset(size.width - cornerSize, size.height), strokeWidth = strokeWidth)
                drawLine(color, start = androidx.compose.ui.geometry.Offset(size.width, size.height), end = androidx.compose.ui.geometry.Offset(size.width, size.height - cornerSize), strokeWidth = strokeWidth)
            }
        }

        // 3. AREA KONTROL (Bawah)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 50.dp, start = 32.dp, end = 32.dp)
        ) {
            // A. Tombol JEPRET (Tengah)
            FloatingActionButton(
                onClick = {
                    val imgCap = imageCapture ?: return@FloatingActionButton
                    imgCap.takePicture(
                        executor,
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val bitmap = image.toBitmap()
                                val rotatedBitmap = rotateBitmap(bitmap, image.imageInfo.rotationDegrees)
                                onImageCaptured(rotatedBitmap)
                                image.close()
                            }
                            override fun onError(exception: ImageCaptureException) {
                                Toast.makeText(context, "Gagal ambil gambar", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                },
                containerColor = Color.White,
                contentColor = Color.Black,
                modifier = Modifier.size(80.dp).align(Alignment.Center),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Camera, contentDescription = "Capture", modifier = Modifier.size(36.dp))
            }

            // B. Tombol GALERI (Kanan)
            Surface(
                onClick = onGalleryClick,
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier.size(50.dp).align(Alignment.CenterEnd)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = "Gallery", tint = Color.White)
                }
            }
        }

        // 4. OVERLAY LOADING
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Sedang menganalisis...", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FoodResultCard(
    data: com.example.nutriscan.ui.screens.scan.FoodResult,
    imageBitmap: Bitmap,
    locationAddress: String? = null,
    onScanAgain: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .clickable(enabled = false) {}
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Handle Bar
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.LightGray)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 1. PREVIEW GAMBAR
            Image(
                bitmap = imageBitmap.asImageBitmap(),
                contentDescription = "Hasil Foto",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. HEADER (Nama & Status)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Nama Makanan
                Text(
                    text = data.nama,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                // Badge Status
                val statusColor = when {
                    data.status.contains("Sehat", ignoreCase = true) && !data.status.contains("Tidak", ignoreCase = true) -> Color(0xFF4CAF50)
                    data.status.contains("Kurang", ignoreCase = true) -> Color(0xFFFF9800)
                    else -> Color(0xFFE91E63)
                }
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = data.status,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. PENJELASAN
            Text("Tentang Makanan Ini", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = data.penjelasan, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)

            Spacer(modifier = Modifier.height(24.dp))

            // 4. KANDUNGAN GIZI (STYLING BARU)
            Text("Informasi Nilai Gizi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)), // Abu sangat muda
                border = BorderStroke(1.dp, Color(0xFFEEEEEE)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Logika Parsing: Pisahkan string berdasarkan koma ","
                    val nutrients = data.gizi.split(",").map { it.trim() }

                    if (nutrients.isNotEmpty()) {
                        // Bagi menjadi baris-baris (2 item per baris)
                        nutrients.chunked(2).forEachIndexed { index, rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                rowItems.forEach { item ->
                                    // Format: "Protein: 25g" -> Label: "Protein", Value: "25g"
                                    val parts = item.split(":")
                                    if (parts.size >= 2) {
                                        NutritionItem(
                                            label = parts[0].trim(),
                                            value = parts[1].trim(),
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        Text(item, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    }
                                }
                                // Jika ganjil, isi kekosongan agar rapi
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            // Garis pemisah antar baris (kecuali baris terakhir)
                            if (index < nutrients.chunked(2).size - 1) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(thickness = 1.dp, color = Color(0xFFEEEEEE))
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    } else {
                        // Fallback jika format beda
                        Text(text = data.gizi, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. LOKASI SCAN (jika tersedia)
            if (!locationAddress.isNullOrEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Lokasi",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Lokasi scan:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                text = locationAddress,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 6. TOMBOL AKSI
            Button(
                onClick = onScanAgain,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Scan Makanan Lain", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

// Komponen Kecil untuk Item Gizi
@Composable
fun NutritionItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32) // Hijau Tua
        )
    }
}

// --- HELPER FUNCTIONS ---

fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(degrees.toFloat())
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val contentResolver = context.contentResolver
        if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}