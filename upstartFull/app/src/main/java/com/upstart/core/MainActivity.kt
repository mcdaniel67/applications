package com.upstart.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upstart.core.theme.UpstartTheme
import com.upstart.features.form.FormCoordinator
import com.upstart.features.form.FormScreen
import com.upstart.features.loanDetails.LoanDetailsViewModel
import com.upstart.features.personalInfo.PersonalInfoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UpstartTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Create feature ViewModels - these survive configuration changes
                    val loanDetailsViewModel: LoanDetailsViewModel = viewModel()
                    val personalInfoViewModel: PersonalInfoViewModel = viewModel()

                    // Create coordinator with factory to survive configuration changes
                    val coordinator: FormCoordinator = viewModel(
                        factory = FormCoordinator.factory(
                            loanDetailsViewModel = loanDetailsViewModel,
                            personalInfoViewModel = personalInfoViewModel
                        )
                    )

                    // Single screen - form handles all steps including result
                    FormScreen(coordinator = coordinator)
                }
            }
        }
    }
}