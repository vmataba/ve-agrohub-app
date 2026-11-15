package com.tabaapps.ve_agrohub_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tabaapps.ve_agrohub_app.ui.MainScreen
import com.tabaapps.ve_agrohub_app.ui.MainViewModel
import com.tabaapps.ve_agrohub_app.ui.theme.VeStockTheme

/**
 * Main Activity launching ve-stock Jetpack Compose UI.
 *
 * Java Educational Note:
 * `ComponentActivity` is the base class for Jetpack Compose apps in Android.
 * `enableEdgeToEdge()` configures transparent status and navigation bars for modern Material 3 design.
 * `ViewModelProvider.Factory` is the standard Android Architecture Components factory pattern in Java/Kotlin
 * to instantiate ViewModels requiring non-empty constructor arguments.
 */
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val app = application as QuestionAnswersApplication
                return MainViewModel(
                    authRepository = app.authRepository,
                    repository = app.repository,
                    syncEngine = app.syncEngine
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VeStockTheme {
                MainScreen(viewModel = mainViewModel)
            }
        }
    }
}
