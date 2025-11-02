package com.dti.gymtimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return GymTimerViewModel(application) as T
            }
        }

        val viewModel = ViewModelProvider(this, viewModelFactory)[GymTimerViewModel::class.java]

        setContent {
            GymTimerApp(viewModel)
        }
    }
}
