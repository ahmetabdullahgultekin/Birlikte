package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.AppRepository
import com.example.ui.BirlikteApp
import com.example.ui.theme.BirlikteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val repository = remember { AppRepository() }
            val isDark by repository.isDarkMode.collectAsState()

            BirlikteTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BirlikteApp(repository = repository)
                }
            }
        }
    }
}
