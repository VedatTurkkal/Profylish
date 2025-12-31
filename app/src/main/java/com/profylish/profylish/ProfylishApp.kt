package com.profylish.profylish

import android.app.Application
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ProfylishApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Purchases.logLevel = LogLevel.DEBUG

        Purchases.configure(
            PurchasesConfiguration.Builder(this, "test_NNrWhOLLSbxcymEakSmFkbKboAn")
                .build()
        )
    }
}