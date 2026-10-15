package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.auth.AuthManager
import com.example.data.AppRepository
import com.example.ui.BirlikteApp
import com.example.ui.screens.AuthScreen
import com.example.ui.theme.BirlikteTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current
            val repository = remember { AppRepository(context = context) }
            val isDark by repository.isDarkMode.collectAsState()

            BirlikteTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppContent(repository = repository)
                }
            }
        }
    }
}

@Composable
fun AppContent(repository: AppRepository) {
    var currentUser by remember { mutableStateOf<FirebaseUser?>(AuthManager.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        AuthManager.auth?.addAuthStateListener(listener)
        onDispose {
            AuthManager.auth?.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = { user ->
                currentUser = user
            }
        )
    } else {
        BirlikteApp(
            repository = repository,
            onSignOut = {
                currentUser = null
            }
        )
    }
}
