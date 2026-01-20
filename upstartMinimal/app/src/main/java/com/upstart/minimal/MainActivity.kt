package com.upstart.minimal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.upstart.minimal.ui.theme.UpstartMinimalTheme
import com.upstart.minimal.ui.form.LoanFormRoute
import com.upstart.minimal.ui.form.LoanFormViewModel
import com.upstart.minimal.ui.form.LoanFormViewModelFactory

class MainActivity : ComponentActivity() {

    private val loanFormViewModel: LoanFormViewModel by viewModels {
        LoanFormViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UpstartMinimalTheme {
                LoanFormRoute(viewModel = loanFormViewModel)
            }
        }
    }
}
