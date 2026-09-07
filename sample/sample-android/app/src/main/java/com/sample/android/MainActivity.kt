package com.sample.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.github.core.GithubCoreSdk
import com.github.core.domain.usecase.SearchRepositoriesUseCase
import com.sample.android.search.RepoSearchScreen
import com.sample.android.ui.theme.Sample_androidTheme

class MainActivity : ComponentActivity() {
    // Initialize the headless GitHub Core KMP SDK and the search use case
    private val sdk by lazy { GithubCoreSdk.create() }
    private val searchUseCase by lazy { SearchRepositoriesUseCase(sdk.networkClient.apiService) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Sample_androidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RepoSearchScreen(
                        searchUseCase = searchUseCase,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}