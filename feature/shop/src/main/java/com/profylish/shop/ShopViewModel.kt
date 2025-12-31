package com.profylish.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profylish.domain.repository.UserDataRepository
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.UpdatedCustomerInfoListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShopUiState(
    val gems: Int = 0,
    val hearts: Int = 0,
    val hasStreakFreeze: Boolean = false,
    val isPremium: Boolean = false,
    val activeEntitlements: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface ShopEvent {
    data class ShowMessage(val message: String) : ShopEvent
}

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel(), UpdatedCustomerInfoListener {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    private val _shopEvent = Channel<ShopEvent>()
    val shopEvent = _shopEvent.receiveAsFlow()

    init {
        observeUserData()
        Purchases.sharedInstance.updatedCustomerInfoListener = this
        fetchCustomerInfo()
    }

    private fun observeUserData() {
        viewModelScope.launch {
            userDataRepository.userData.collectLatest { prefs ->
                _uiState.update {
                    it.copy(
                        gems = prefs.gems,
                        hearts = prefs.hearts,
                        hasStreakFreeze = prefs.hasStreakFreeze
                    )
                }
            }
        }
    }

    private fun fetchCustomerInfo() {
        // DÜZELTME: Callback arayüzünü açıkça uyguluyoruz
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onError(error: PurchasesError) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }

            override fun onReceived(customerInfo: CustomerInfo) {
                updateStateFromInfo(customerInfo)
            }
        })
    }

    override fun onReceived(customerInfo: CustomerInfo) {
        updateStateFromInfo(customerInfo)
    }

    private fun updateStateFromInfo(info: CustomerInfo) {
        val isPremium = info.entitlements["Profylish Premium"]?.isActive == true
        _uiState.update {
            it.copy(isPremium = isPremium, activeEntitlements = info.entitlements.active.keys)
        }
    }

    fun buyHeartRefill() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = userDataRepository.buyHeartRefill()
            result.onSuccess {
                _shopEvent.send(ShopEvent.ShowMessage("Hearts refilled! ❤️"))
            }.onFailure { e ->
                _shopEvent.send(ShopEvent.ShowMessage(e.message ?: "Purchase failed"))
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun buyStreakFreeze() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = userDataRepository.buyStreakFreeze()
            result.onSuccess {
                _shopEvent.send(ShopEvent.ShowMessage("Streak Freeze equipped! ❄️"))
            }.onFailure { e ->
                _shopEvent.send(ShopEvent.ShowMessage(e.message ?: "Purchase failed"))
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}