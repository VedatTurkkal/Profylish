package com.profylish.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profylish.home.components.LessonSelectionBottomSheet
import com.profylish.model.roadmap.NodeStatus
import com.profylish.model.roadmap.RoadmapNode
import com.profylish.ui.components.GamifiedTopBar
import com.profylish.ui.components.RavenMascot
import com.profylish.ui.components.RavenState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onLessonClick: (String, String, String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToChat: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val hostState = remember { SnackbarHostState() }
    var showLockedMessage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    var selectedLevelId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(showLockedMessage) {
        if (showLockedMessage) {
            delay(2000)
            showLockedMessage = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState) },
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

            if (uiState.isLoading) {
                // Loading sırasında düşünen Raven
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RavenMascot(state = RavenState.THINKING, modifier = Modifier.size(100.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }
            } else {
                val units = remember(uiState.nodes) { uiState.nodes.chunked(5) }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {

                    // LİSTENİN BAŞINA KARŞILAMA RAVEN'I EKLİYORUZ
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            RavenMascot(
                                state = RavenState.ROADMAP, // veya WELCOME
                                modifier = Modifier.size(120.dp)
                            )
                            Text(
                                text = "Let's learn something new!",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    units.forEachIndexed { index, nodesInUnit ->
                        val unitNumber = index + 1

                        item {
                            UnitHeader(
                                unitNumber = unitNumber,
                                description = "Section $unitNumber",
                                color = getUnitColor(unitNumber)
                            )
                        }

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
            }

            // --- RAVEN İLE SÜSLENMİŞ KİLİTLİ MESAJI ---
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
                        // Kilit ikonu yerine uyaran/öğreten Raven (veya kilit ikonu kalsın ama Raven yanında olsun)
                        // Yer darlığından dolayı burada ikon daha iyi olabilir, ama Raven'ı denemek isterseniz:
                        /*
                        RavenMascot(state = RavenState.CONFUSED, modifier = Modifier.size(40.dp))
                        */
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Complete previous levels first!",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }

    if (selectedLevelId != null) {
        val clickedLevelInt = selectedLevelId!!.toIntOrNull() ?: 1

        LessonSelectionBottomSheet(
            clickedLevelId = clickedLevelInt,
            userLevel = uiState.level,
            completedCategories = uiState.completedCategoriesByLevel[clickedLevelInt] ?: emptySet(),
            onDismiss = { selectedLevelId = null },
            onCategorySelected = { category ->
                if (uiState.hearts > 0) {
                    onLessonClick(selectedLevelId!!, uiState.profession, category)
                    selectedLevelId = null
                } else {
                    scope.launch {
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
                color = MaterialTheme.colorScheme.background,
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

@SuppressLint("MissingPermission")
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