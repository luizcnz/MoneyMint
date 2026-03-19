package com.example.moneymint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.moneymint.data.AuthManager
import com.example.moneymint.data.FirestoreManager
import com.example.moneymint.data.MoneyDatabase
import com.example.moneymint.ui.MoneyScreen
import com.example.moneymint.ui.MoneyViewModel
import com.example.moneymint.ui.MoneyViewModelFactory
import com.example.moneymint.ui.theme.MoneyMintTheme

class MainActivity : ComponentActivity() {

    private val authManager by lazy { AuthManager() }
    private val firestoreManager by lazy { FirestoreManager() }
    
    private val database by lazy { 
        MoneyDatabase.getDatabase(this, lifecycleScope) 
    }
    
    private val dao by lazy { database.moneyDao() }

    private val moneyViewModel: MoneyViewModel by viewModels {
        MoneyViewModelFactory(dao, firestoreManager, authManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MoneyMintTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // SALTADO: Entramos directo a la pantalla principal
                    MoneyScreen(viewModel = moneyViewModel)
                }
            }
        }
    }
}
