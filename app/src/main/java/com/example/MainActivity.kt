package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.screens.admin.AdminMainScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.member.MemberMainScreen
import com.example.ui.theme.GullakSocietyTheme
import com.example.ui.viewmodel.AuthState
import com.example.ui.viewmodel.GullakViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GullakSocietyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GullakAppRoot()
                }
            }
        }
    }
}

@Composable
fun GullakAppRoot(
    viewModel: GullakViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    when (val state = authState) {
        is AuthState.Unauthenticated -> {
            LoginScreen(viewModel = viewModel)
        }
        is AuthState.Authenticated -> {
            val user = state.user
            if (user.role == UserRole.ADMIN) {
                AdminMainScreen(adminUser = user, viewModel = viewModel)
            } else {
                MemberMainScreen(user = user, viewModel = viewModel)
            }
        }
    }
}
