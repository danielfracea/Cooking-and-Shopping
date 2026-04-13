package com.cooking.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cooking.app.ui.navigation.AppNavigation
import com.cooking.app.ui.theme.CookAndShopTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as CookingApp).repository
        setContent {
            CookAndShopTheme {
                AppNavigation(repository = repository, activity = this)
            }
        }
    }
}
