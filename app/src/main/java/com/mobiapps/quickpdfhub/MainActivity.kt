package com.mobiapps.quickpdfhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.mobiapps.quickpdfhub.data.ThemeManager
import com.mobiapps.quickpdfhub.navigation.AppNavGraph
import com.mobiapps.quickpdfhub.ui.components.BannerAd
import com.mobiapps.quickpdfhub.ui.theme.QuickPDFHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        MobileAds.initialize(this)
        enableEdgeToEdge()
        setContent {
            QuickPDFHubTheme {
                val navController = rememberNavController()
                Column(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(
                        navController = navController,
                        modifier = Modifier.weight(1f),
                    )
                    BannerAd(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                    )
                }
            }
        }
    }
}
