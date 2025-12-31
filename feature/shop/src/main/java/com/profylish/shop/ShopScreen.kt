package com.profylish.shop

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
// Eğer kütüphane sürümü eskiyse bu import hata verir.
// Şimdilik CustomerCenter'ı kaldırıp basit bir dialog ile değiştiriyorum ki derlensin.
// Sürümü yükselttiğinde tekrar ekleyebilirsin.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: ShopViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showPaywall by remember { mutableStateOf(false) }
    var showCustomerCenter by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.shopEvent.collect { event ->
            when (event) {
                is ShopEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showPaywall) {
        Dialog(
            onDismissRequest = { showPaywall = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Paywall(
                    options = PaywallOptions.Builder(
                        dismissRequest = { showPaywall = false }
                    ).build()
                )
            }
        }
    }

    // Customer Center Dialog (Basitleştirilmiş)
    if (showCustomerCenter) {
        AlertDialog(
            onDismissRequest = { showCustomerCenter = false },
            confirmButton = {
                TextButton(onClick = { showCustomerCenter = false }) { Text("Close") }
            },
            title = { Text("Subscription Management") },
            text = { Text("You can manage your subscription via Google Play Store.") }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = "Shop",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color.Gray),
                modifier = Modifier.align(Alignment.Center)
            )

            Surface(
                color = Color(0xFFE5F6FD),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1CB0F6))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("💎", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = state.gems.toString(),
                        color = Color(0xFF1CB0F6),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFFE5E5E5), thickness = 2.dp)

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Subscription",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (state.isPremium) {
                item {
                    ShopItemCard(
                        title = "Profylish Premium Active",
                        description = "You have full access!",
                        icon = "✨",
                        price = 0,
                        isPurchased = false,
                        canAfford = true,
                        buttonText = "SETTINGS",
                        onBuy = { showCustomerCenter = true }
                    )
                }
            } else {
                item {
                    ShopItemCard(
                        title = "Get Profylish Premium",
                        description = "Unlimited hearts, no ads.",
                        icon = "🚀",
                        price = 0,
                        isPurchased = false,
                        canAfford = true,
                        buttonText = "UPGRADE",
                        onBuy = { showPaywall = true }
                    )
                }
            }

            item {
                Text(
                    text = "Power-ups",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
            }

            item {
                ShopItemCard(
                    title = "Refill Hearts",
                    description = "Get full hearts.",
                    icon = "❤️",
                    price = 50,
                    isPurchased = state.hearts >= 5 || state.isPremium,
                    canAfford = state.gems >= 50,
                    onBuy = { viewModel.buyHeartRefill() }
                )
            }

            item {
                ShopItemCard(
                    title = "Streak Freeze",
                    description = "Protect your streak.",
                    icon = "❄️",
                    price = 200,
                    isPurchased = state.hasStreakFreeze,
                    canAfford = state.gems >= 200,
                    buttonText = if (state.hasStreakFreeze) "ACTIVE" else null,
                    onBuy = { viewModel.buyStreakFreeze() }
                )
            }
        }
    }
}

@Composable
fun ShopItemCard(
    title: String,
    description: String,
    icon: String,
    price: Int,
    isPurchased: Boolean,
    canAfford: Boolean,
    buttonText: String? = null,
    onBuy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE5E5E5))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 40.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isPurchased && buttonText == null) {
                Text(
                    text = "FULL",
                    color = Color(0xFFE5E5E5),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Button(
                    onClick = onBuy,
                    enabled = (canAfford || buttonText != null) && !isPurchased && buttonText != "ACTIVE",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (buttonText != null && buttonText != "ACTIVE") Color(0xFF1CB0F6) else Color(0xFF58CC02),
                        disabledContainerColor = Color(0xFFE5E5E5)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 0.dp)
                ) {
                    if (buttonText != null) {
                        Text(text = buttonText, fontWeight = FontWeight.Bold, color = if(buttonText == "ACTIVE") Color.Gray else Color.White)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💎", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = price.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}