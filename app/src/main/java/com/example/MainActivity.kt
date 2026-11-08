package com.example

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.repository.WaffleoRepository
import com.example.ui.screens.GoogleCloudAuthScreen
import com.example.ui.screens.WaffleoLandscapeMainShell
import com.example.ui.screens.attemptAutoSignIn
import com.example.ui.screens.signOutCloud
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WaffleoViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure auto-landscape mode across all tablets
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        enableEdgeToEdge()
        hideSystemBars()
        setContent {
            MyApplicationTheme {
                AppNavigation()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    // 1-Server Cloud Architecture: Directly enter Waffleo POS Login Cabang without Google Account prompt.
    var showCloudAuthScreen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (auth.currentUser == null) {
            try {
                auth.signInAnonymously()
            } catch (_: Exception) {
                // Fallback to direct 1-server shared workspace if Anonymous Auth is disabled in Firebase Console
            }
        }
    }

    val user = currentUser
    if (showCloudAuthScreen && user == null) {
        GoogleCloudAuthScreen(
            onAuthSuccess = { showCloudAuthScreen = false },
            onContinueDirectAutoLogin = { showCloudAuthScreen = false }
        )
    } else {
        val activeUserId = WaffleoRepository.SHARED_WORKSPACE_ID
        val posViewModel: WaffleoViewModel = viewModel(
            key = activeUserId,
            factory = viewModelFactory {
                initializer {
                    val app = checkNotNull(this[APPLICATION_KEY]) {
                        "APPLICATION_KEY missing from CreationExtras"
                    }
                    val databaseId = app.getString(R.string.firestore_database_id)
                    val db = FirebaseFirestore.getInstance(databaseId)
                    val localStore = com.example.data.local.WaffleoLocalStore(app)
                    WaffleoViewModel(WaffleoRepository(db), activeUserId, localStore)
                }
            }
        )

        WaffleoLandscapeMainShell(
            viewModel = posViewModel,
            isCloudSignedIn = true,
            onRequestCloudSignInScreen = { posViewModel.flushPendingSyncToCloud() },
            onSignOutGoogleCloud = {
                signOutCloud(
                    credentialManager = credentialManager,
                    onSignOutComplete = { showCloudAuthScreen = false },
                    scope = scope
                )
            }
        )
    }
}
