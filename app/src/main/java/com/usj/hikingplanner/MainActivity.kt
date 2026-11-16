package com.usj.hikingplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.usj.hikingplanner.ui.navigation.AppNavHost
import com.usj.hikingplanner.ui.theme.HikingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HikingTheme {
                AppNavHost()
            }
        }
    }
}
