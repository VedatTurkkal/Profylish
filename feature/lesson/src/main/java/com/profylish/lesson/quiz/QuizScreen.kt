package com.profylish.lesson.quiz

import android.annotation.SuppressLint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profylish.lesson.model.QuizUiState
import com.profylish.model.lesson.QuestionType
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
@Composable
fun QuizScreen(
    onBackClick: () -> Unit,
    onNavigateToAuth: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showQuitDialog by remember { mutableStateOf(false) }
    var showSignUpDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    BackHandler { showQuitDialog = true }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is QuizNavigationEvent.NavigateToAuth -> showSignUpDialog = true
                is QuizNavigationEvent.NavigateHome -> onBackClick()
                is QuizNavigationEvent.VibrateSuccess -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(50)
                    }
                }
                is QuizNavigationEvent.VibrateError -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(300)
                    }
                }
            }
        }
    }

    if (showQuitDialog) {
        AlertDialog(
            onDismissRequest = { showQuitDialog = false },
            title = { Text("Quit this session?") },
            text = { Text("You will lose all progress in this lesson if you quit now.") },
            confirmButton = {
                TextButton(onClick = {
                    showQuitDialog = false
                    onBackClick()
                }) { Text("QUIT", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showQuitDialog = false }) { Text("CANCEL") }
            }
        )
    }

    if (showSignUpDialog) {
        AlertDialog(
            onDismissRequest = { showSignUpDialog = false; onBackClick() },
            title = { Text("Save Your Progress!") },
            text = { Text("Great job! Create a free profile now to save your XP, streak, and league ranking permanently.") },
            confirmButton = {
                Button(onClick = { showSignUpDialog = false; onNavigateToAuth() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1CB0F6))) {
                    Text("Create Profile", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignUpDialog = false; onBackClick() }) { Text("Maybe Later", color = Color.Gray) }
            }
        )
    }

    Scaffold(containerColor = Color.White) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is QuizUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is QuizUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚠️ ${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                        Button(onClick = onBackClick) { Text("Go Back") }
                    }
                }
                is QuizUiState.Success -> {
                    if (state.isLessonCompleted) {
                        // Matching soruları puanı şişirebilir, yüzde hesabı > 100 olabilir.
                        // Bu yüzden basit bir kontrol: ya yüzde > 70 ya da puan > 0
                        val totalPossible = state.totalQuestions * 10
                        val percentage = if (totalPossible > 0) (state.score.toFloat() / totalPossible) * 100 else 0f
                        val passed = percentage >= 70 || state.score > 0 // Matching varsa puan > 0 yeterli olabilir

                        LessonCompletedView(score = state.score, passed = passed, onContinueClick = { viewModel.onLessonFinished() })
                    } else {
                        QuizContent(
                            state = state,
                            onOptionSelected = viewModel::onOptionSelected,
                            onCheckAnswer = viewModel::onCheckAnswer,
                            onNextQuestion = viewModel::onNextQuestion,
                            onCloseClick = { showQuitDialog = true }
                        )

                        // COMBO ANIMASYONU
                        AnimatedVisibility(
                            visible = state.showComboAnim,
                            enter = scaleIn() + fadeIn(),
                            exit = scaleOut() + fadeOut(),
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC107)),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🔥", fontSize = 48.sp)
                                    Text(
                                        "COMBO x${state.comboStreak}!",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizContent(
    state: QuizUiState.Success,
    onOptionSelected: (Int) -> Unit,
    onCheckAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onCloseClick: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = (state.currentQuestionIndex + 1) / state.totalQuestions.toFloat(),
        label = "ProgressBar"
    )

    Column(modifier = Modifier.fillMaxSize()) {

        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCloseClick) { Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray) }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.weight(1f).height(12.dp).clip(RoundedCornerShape(8.dp)),
                color = Color(0xFF58CC02),
                trackColor = Color(0xFFE5E5E5)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Favorite, contentDescription = "Hearts", tint = Color(0xFFFF4B4B))
                Spacer(modifier = Modifier.width(4.dp))
                Text("${state.hearts}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color(0xFFFF4B4B))
            }
        }

        // SORU ALANI ve ALT BAR (Check Button)
        // Matching sorularında alt bar farklı davranabilir veya hiç olmayabilir.
        // Bu yüzden Column içinde weight vererek alanı bölüyoruz.

        Box(modifier = Modifier.weight(1f)) {

            // --- SORU TİPİNE GÖRE GÖSTERİM (Dinamik) ---
            when (state.currentQuestion.type) {

                // 1. MATCHING PAIRS (EŞLEŞTİRME)
                QuestionType.MATCHING_PAIRS -> {
                    // Matching ekranı tam ekran kaplar, check butonu içinde yönetilir
                    MatchingQuizContent(
                        pairs = state.currentQuestion.matchingPairs,
                        onAllMatched = {
                            // Hepsi eşleştiğinde otomatik kontrol et
                            onCheckAnswer()
                        }
                    )
                }

                // 2. DİĞER TİPLER (Scrollable Column İçinde)
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (state.currentQuestion.type) {
                            QuestionType.FILL_IN_THE_BLANK -> {
                                Text("Complete the phrase:", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                                Spacer(modifier = Modifier.height(16.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                                ) {
                                    Text(
                                        text = state.currentQuestion.questionText,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(24.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                state.currentQuestion.options.forEachIndexed { index, optionText ->
                                    QuizOptionCard(
                                        text = optionText,
                                        isSelected = state.selectedOptionIndex == index,
                                        isAnswerChecked = state.isAnswerChecked,
                                        isCorrectAnswer = index == state.currentQuestion.correctAnswerIndex,
                                        onClick = { onOptionSelected(index) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            QuestionType.TRUE_FALSE -> {
                                Text(
                                    text = state.currentQuestion.questionText,
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    state.currentQuestion.options.forEachIndexed { index, optionText ->
                                        val color = if(optionText == "True") Color(0xFF58CC02) else Color(0xFFEA2B2B)
                                        Box(modifier = Modifier.weight(1f)) {
                                            TrueFalseCard(
                                                text = optionText,
                                                baseColor = color,
                                                isSelected = state.selectedOptionIndex == index,
                                                isAnswerChecked = state.isAnswerChecked,
                                                onClick = { onOptionSelected(index) }
                                            )
                                        }
                                    }
                                }
                            }

                            else -> { // MULTIPLE_CHOICE
                                Text(
                                    text = state.currentQuestion.questionText,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF3C3C3C),
                                    modifier = Modifier.padding(bottom = 32.dp)
                                )
                                state.currentQuestion.options.forEachIndexed { index, optionText ->
                                    QuizOptionCard(
                                        text = optionText,
                                        isSelected = state.selectedOptionIndex == index,
                                        isAnswerChecked = state.isAnswerChecked,
                                        isCorrectAnswer = index == state.currentQuestion.correctAnswerIndex,
                                        onClick = { onOptionSelected(index) }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- ALT KONTROL BAR (Sadece Matching Dışındaki Sorular İçin) ---
        if (state.currentQuestion.type != QuestionType.MATCHING_PAIRS) {
            val footerColor = if (state.isAnswerChecked) {
                if (state.isAnswerCorrect) Color(0xFFD7FFB8) else Color(0xFFFFDFE0)
            } else {
                Color.White
            }

            Column(modifier = Modifier.fillMaxWidth().background(footerColor).padding(16.dp)) {
                if (state.isAnswerChecked) {
                    Row(modifier = Modifier.padding(bottom = 16.dp)) {
                        val icon = if (state.isAnswerCorrect) "🎉" else "❌"
                        val title = if (state.isAnswerCorrect) "Excellent!" else "Correct answer:"
                        Text(text = icon, style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = if (state.isAnswerCorrect) Color(0xFF58CC02) else Color(0xFFEA2B2B))
                            if (!state.isAnswerCorrect && state.currentQuestion.options.isNotEmpty()) {
                                Text(text = state.currentQuestion.options[state.currentQuestion.correctAnswerIndex], style = MaterialTheme.typography.bodyLarge, color = Color(0xFFEA2B2B))
                            }
                        }
                    }
                }

                Button(
                    onClick = { if (state.isAnswerChecked) onNextQuestion() else onCheckAnswer() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = state.selectedOptionIndex != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isAnswerChecked && !state.isAnswerCorrect) Color(0xFFEA2B2B) else Color(0xFF58CC02),
                        disabledContainerColor = Color(0xFFE5E5E5)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = if (state.isAnswerChecked) "CONTINUE" else "CHECK", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

// --- MATCHING QUIZ CONTENT ---
@Composable
fun MatchingQuizContent(
    pairs: List<Pair<String, String>>,
    onAllMatched: () -> Unit
) {
    val leftItems = remember(pairs) { pairs.map { it.first } }
    val rightItems = remember(pairs) { pairs.map { it.second }.shuffled() }

    var selectedLeft by remember { mutableStateOf<String?>(null) }
    var selectedRight by remember { mutableStateOf<String?>(null) }

    val matchedLeft = remember { mutableStateListOf<String>() }
    val matchedRight = remember { mutableStateListOf<String>() }

    LaunchedEffect(selectedLeft, selectedRight) {
        if (selectedLeft != null && selectedRight != null) {
            val isCorrect = pairs.any { it.first == selectedLeft && it.second == selectedRight }

            if (isCorrect) {
                matchedLeft.add(selectedLeft!!)
                matchedRight.add(selectedRight!!)
            }

            delay(300)
            selectedLeft = null
            selectedRight = null

            if (matchedLeft.size == pairs.size) {
                onAllMatched()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tap matching pairs", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                leftItems.forEach { item ->
                    val isMatched = matchedLeft.contains(item)
                    val isSelected = selectedLeft == item
                    MatchingCard(text = item, isSelected = isSelected, isMatched = isMatched, onClick = { if (!isMatched) selectedLeft = item })
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                rightItems.forEach { item ->
                    val isMatched = matchedRight.contains(item)
                    val isSelected = selectedRight == item
                    MatchingCard(text = item, isSelected = isSelected, isMatched = isMatched, isSmallText = true, onClick = { if (!isMatched) selectedRight = item })
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun MatchingCard(
    text: String,
    isSelected: Boolean,
    isMatched: Boolean,
    isSmallText: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = when {
        isMatched -> Color.Transparent
        isSelected -> Color(0xFFDDF4FF)
        else -> Color.White
    }

    val borderColor = if (isSelected) Color(0xFF1CB0F6) else Color(0xFFE5E5E5)
    val textColor = if (isMatched) Color.LightGray else Color(0xFF4B4B4B)
    val alpha = if (isMatched) 0.3f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isSmallText) 100.dp else 60.dp)
            .clickable(enabled = !isMatched && !isSelected) { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor.copy(alpha = alpha)),
        border = if (!isMatched) BorderStroke(2.dp, borderColor) else null
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = if (isSmallText) MaterialTheme.typography.bodySmall else MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = if (isSmallText) 4 else 2
            )
        }
    }
}

// --- YARDIMCI BİLEŞENLER ---

@Composable
fun QuizOptionCard(
    text: String,
    isSelected: Boolean,
    isAnswerChecked: Boolean,
    isCorrectAnswer: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isAnswerChecked && isCorrectAnswer -> Color(0xFF58CC02)
        isAnswerChecked && isSelected -> Color(0xFFEA2B2B)
        isSelected -> Color(0xFF1CB0F6)
        else -> Color(0xFFE5E5E5)
    }
    val backgroundColor = when {
        isAnswerChecked && isCorrectAnswer -> Color(0xFFD7FFB8)
        isAnswerChecked && isSelected -> Color(0xFFFFDFE0)
        isSelected -> Color(0xFFDDF4FF)
        else -> Color.White
    }
    val textColor = if (!isAnswerChecked && !isSelected) Color(0xFF4B4B4B) else borderColor

    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(enabled = !isAnswerChecked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        color = backgroundColor
    ) {
        Text(text, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = textColor)
    }
}

@Composable
fun TrueFalseCard(
    text: String,
    baseColor: Color,
    isSelected: Boolean,
    isAnswerChecked: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) baseColor else Color(0xFFE5E5E5)
    val bgColor = if (isSelected) baseColor.copy(alpha = 0.1f) else Color.White
    val textColor = if (isSelected) baseColor else Color.Black

    Surface(
        modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(16.dp)).clickable(enabled = !isAnswerChecked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, borderColor),
        color = bgColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = textColor)
        }
    }
}

@Composable
fun LessonCompletedView(score: Int, passed: Boolean, onContinueClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val icon = if (passed) "🎉" else "💪"
        val title = if (passed) "Lesson Complete!" else "Don't give up!"
        val subtitle = if (passed) "You earned $score XP" else "You need 70% to pass. Try again!"
        val btnColor = if (passed) Color(0xFF58CC02) else Color.Gray

        Text(icon, style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = Color(0xFF58CC02))
        Spacer(modifier = Modifier.height(8.dp))
        Text(subtitle, style = MaterialTheme.typography.titleLarge, color = Color(0xFFFFC800))
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onContinueClick, modifier = Modifier.fillMaxWidth(0.8f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = btnColor)) {
            Text("CONTINUE")
        }
    }
}