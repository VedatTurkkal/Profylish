package com.profylish.home

// Gerekli Android Importları
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

// Gerekli Compose Animation Importları
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

// Gerekli Compose Foundation & UI Importları
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed // Bu import çok önemli
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

// Gerekli Material 3 Importları (Material 2 olmamalı)
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*

// Runtime Importları
import androidx.compose.runtime.*

// UI Helper Importları
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

// Hilt ve ViewModel Importları
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Proje İçi Importlar
import com.profylish.home.components.LessonSelectionBottomSheet
import com.profylish.model.roadmap.NodeStatus
import com.profylish.model.roadmap.RoadmapNode
import com.profylish.ui.components.GamifiedTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    // Navigasyon parametresi: levelId, profession, category (String)
    onLessonClick: (String, String, String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToChat: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Snackbar durumunu yönetmek için değişken (İsim çakışmasını önlemek için 'hostState' yaptık)
    val hostState = remember { SnackbarHostState() }

    // Kilitli mesajı animasyonu için state
    var showLockedMessage by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    var selectedLevelId by remember { mutableStateOf<String?>(null) }

    // Locked mesajı görünür olduğunda 2 saniye sonra otomatik gizle
    LaunchedEffect(showLockedMessage) {
        if (showLockedMessage) {
            delay(2000)
            showLockedMessage = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState) }, // hostState burada kullanılıyor
        topBar = {
            GamifiedTopBar(
                professionName = uiState.profession,
                gemCount = uiState.gems,
                streakCount = uiState.streak,
                heartCount = uiState.hearts,
                availableCourses = uiState.availableCourses,
                onSwitchCourse = { viewModel.switchCourse(it) },
                onAddCourse = onNavigateToSearch
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToChat,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    painter = painterResource(com.profylish.ui.R.drawable.robot),
                    contentDescription = "AI Interview"
                )
            }
        }
    ) { padding ->

        Box(modifier = Modifier.fillMaxSize()) {

            // Seviyeleri 5'erli gruplara bölüyoruz
            val units = remember(uiState.nodes) { uiState.nodes.chunked(5) }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                units.forEachIndexed { index, nodesInUnit ->
                    val unitNumber = index + 1

                    item {
                        UnitHeader(
                            unitNumber = unitNumber,
                            description = "Section $unitNumber",
                            color = getUnitColor(unitNumber)
                        )
                    }

                    // itemsIndexed kullanımı için import gereklidir (Yukarıda eklendi)
                    itemsIndexed(nodesInUnit) { nodeIndex, node ->
                        val offsetX = when (nodeIndex % 4) {
                            1 -> (-40).dp
                            3 -> 40.dp
                            else -> 0.dp
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LessonNodeItem(
                                node = node,
                                offsetX = offsetX,
                                unitColor = getUnitColor(unitNumber),
                                onClick = {
                                    if (node.status == NodeStatus.LOCKED) {
                                        if (uiState.isVibrationEnabled) {
                                            vibrateDevice(context)
                                        }
                                        // Custom mesajı göster
                                        showLockedMessage = true
                                    } else {
                                        selectedLevelId = node.id
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // --- CUSTOM LOCKED NOTIFICATION ---
            AnimatedVisibility(
                visible = showLockedMessage,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 500)
                ) + fadeIn(animationSpec = tween(durationMillis = 500)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(animationSpec = tween(durationMillis = 300)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .zIndex(2f)
            ) {
                Surface(
                    color = Color(0xFF333333),
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Locked! Complete previous levels first.",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // --- BOTTOM SHEET ---
    if (selectedLevelId != null) {
        val clickedLevelInt = selectedLevelId!!.toIntOrNull() ?: 1

        LessonSelectionBottomSheet(
            clickedLevelId = clickedLevelInt,
            userLevel = uiState.level,
            completedCategories = emptySet(), // Tamamlanan kategoriler ileride eklenebilir
            onDismiss = { selectedLevelId = null },
            onCategorySelected = { category ->
                if (uiState.hearts > 0) {
                    // Navigasyon tetikleniyor
                    onLessonClick(selectedLevelId!!, uiState.profession, category)
                    selectedLevelId = null
                } else {
                    scope.launch {
                        // hostState burada kullanılıyor, hata vermemesi lazım
                        hostState.showSnackbar("You have no hearts left! Practice to earn or wait.")
                    }
                }
            }
        )
    }
}

// --- YARDIMCI BİLEŞENLER ---

@Composable
fun UnitHeader(unitNumber: Int, description: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "UNIT $unitNumber",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
            color = color
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 2.dp,
            color = color.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun LessonNodeItem(
    node: RoadmapNode,
    offsetX: Dp,
    unitColor: Color,
    onClick: () -> Unit
) {
    val backgroundColor = when (node.status) {
        NodeStatus.LOCKED -> MaterialTheme.colorScheme.surfaceVariant
        NodeStatus.COMPLETED -> Color(0xFFFFC800)
        NodeStatus.ACTIVE -> unitColor
    }

    val borderColor = when(node.status) {
        NodeStatus.COMPLETED -> Color(0xFFE59400)
        NodeStatus.LOCKED -> MaterialTheme.colorScheme.background
        NodeStatus.ACTIVE -> MaterialTheme.colorScheme.background
    }

    val iconTint = if (node.status == NodeStatus.LOCKED) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .offset(x = offsetX, y = 4.dp)
            .size(70.dp)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.6f))
    )

    Box(
        modifier = Modifier
            .offset(x = offsetX, y = 0.dp)
            .size(70.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 4.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (node.status == NodeStatus.LOCKED) Icons.Default.Lock else Icons.Default.Star,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(32.dp)
        )
    }
}

fun getUnitColor(unitNumber: Int): Color {
    val colors = listOf(
        Color(0xFF58CC02),
        Color(0xFFCE82FF),
        Color(0xFF00CD9C),
        Color(0xFFFF4B4B),
        Color(0xFFFF9600),
        Color(0xFF1CB0F6)
    )
    return colors[(unitNumber - 1) % colors.size]
}

fun vibrateDevice(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
        } else {
            VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        vibrator.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(50)
    }
}