package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storefront
import com.example.data.model.BranchProfile
import com.example.data.model.StaffRole
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.BuildConfig
import com.example.R
import com.example.data.model.capitalizeSentenceStart
import com.example.ui.components.WaffleoProductIllustration
import com.example.ui.components.WaffleoSingleSelectDropdown
import com.example.ui.components.WaffleoSoftTextField
import com.example.ui.theme.CaramelGold
import com.example.ui.theme.ChampagneGoldAccent
import com.example.ui.theme.DeepCaramelGold
import com.example.ui.theme.ElevatedIvoryCard
import com.example.ui.theme.HoneyCaramelAccent
import com.example.ui.theme.MatchaSageSuccess
import com.example.ui.theme.MutedCocoaText
import com.example.ui.theme.RichEspressoPrimary
import com.example.ui.theme.SidebarEspressoDark
import com.example.ui.theme.SidebarIconInactive
import com.example.ui.theme.SoftGoldHighlight
import com.example.ui.theme.SoftOatSurface
import com.example.ui.theme.SoftSageBg
import com.example.ui.theme.SoftTerracottaBg
import com.example.ui.theme.SubtleSandBorder
import com.example.ui.theme.TerracottaDanger
import com.example.ui.theme.WarmAlabasterBg
import com.example.ui.viewmodel.MainNavDestination
import com.example.ui.viewmodel.WaffleoViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun attemptAutoSignIn(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onUnauthenticated: () -> Unit,
    scope: CoroutineScope
) {
    if (Firebase.auth.currentUser != null) {
        onAuthSuccess()
        return
    }
    val clientId = try {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) context.getString(resId) else ""
    } catch (e: Exception) {
        ""
    }
    if (clientId.isBlank()) {
        onUnauthenticated()
        return
    }

    val authorizedOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(clientId)
        .setAutoSelectEnabled(true)
        .build()

    scope.launch {
        try {
            val result = credentialManager.getCredential(
                context,
                GetCredentialRequest.Builder().addCredentialOption(authorizedOption).build()
            )
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onUnauthenticated()
            }
        } catch (e: Exception) {
            onUnauthenticated()
        }
    }
}

fun onGoogleSignInClicked(
    context: Context,
    credentialManager: CredentialManager,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    scope: CoroutineScope,
    onAuthCancelled: () -> Unit = {}
) {
    val clientId = try {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) context.getString(resId) else ""
    } catch (e: Exception) {
        ""
    }
    if (clientId.isBlank()) {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
    }

    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    scope.launch {
        try {
            val activity = generateSequence(context) { (it as? android.content.ContextWrapper)?.baseContext }
                .filterIsInstance<Activity>()
                .firstOrNull()
            if (activity == null) {
                onAuthError("Activity context tidak tersedia untuk Google Sign-In.")
                return@launch
            }
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential
            if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                Firebase.auth.signInWithCredential(authCredential).await()
                onAuthSuccess()
            } else {
                onAuthError("Unexpected credential type")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
            onAuthCancelled()
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign-In failed", e)
            onAuthError(e.localizedMessage ?: "Sign in failed")
        }
    }
}

fun signOutCloud(
    credentialManager: CredentialManager,
    onSignOutComplete: () -> Unit,
    scope: CoroutineScope
) {
    Firebase.auth.signOut()
    scope.launch {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("Auth", "Failed to clear credential state", e)
        } finally {
            onSignOutComplete()
        }
    }
}

/**
 * Cloud Database Connection Gate (Google Sign-In via Credential Manager)
 */
@Composable
fun GoogleCloudAuthScreen(
    onAuthSuccess: () -> Unit,
    onContinueDirectAutoLogin: () -> Unit = onAuthSuccess
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = onAuthSuccess,
            onUnauthenticated = {},
            scope = scope
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmAlabasterBg),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .padding(12.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    WaffleoProductIllustration(
                        itemName = "Belgian Choco",
                        isWaffle = true,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Waffleo POS — Cloud Database Sync",
                            style = MaterialTheme.typography.headlineMedium,
                            color = RichEspressoPrimary
                        )
                        Text(
                            text = "Hubungkan akun Google bisnis Anda untuk mengaktifkan Bank Data Online & Riwayat Audit Log Otomatis.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedCocoaText
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftOatSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "• Sesi Kasir Default: Cabang 'Global' | Username 'owner' | Role 'Owner' (Auto-Login Aktif)",
                            style = MaterialTheme.typography.labelLarge,
                            color = RichEspressoPrimary
                        )
                        Text(
                            text = "• Seluruh perubahan pesanan dan harga menu tercatat pada Cloud Audit Log",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedCocoaText
                        )
                    }
                }

                if (errorMessage != null) {
                    Surface(
                        color = SoftTerracottaBg,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TerracottaDanger,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isLoading = false
                                    onAuthSuccess()
                                },
                                onAuthError = { msg ->
                                    isLoading = false
                                    errorMessage = msg
                                },
                                scope = scope,
                                onAuthCancelled = { isLoading = false }
                            )
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RichEspressoPrimary,
                            contentColor = ElevatedIvoryCard
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = SoftGoldHighlight,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Sign in with Google",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Halaman Login Cabang Waffleo dengan Sistem Verifikasi 1x Akun Google Cabang:
 * - Ketika suatu perangkat baru pertama kali ingin masuk ke suatu cabang, perangkat akan meminta
 *   verifikasi 1x Akun Google Cabang tersebut.
 * - Setelah cabang diotorisasi 1x pada perangkat ini, login berikutnya oleh user mana pun
 *   (@owner, @spv, @kasir) di cabang tersebut langsung masuk ke POS tanpa harus login Google lagi.
 */
@Composable
fun BranchPosLoginScreen(
    viewModel: WaffleoViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val branches by viewModel.branches.collectAsState()
    val initialBranch = remember(branches) {
        viewModel.getLastLoginBranch()?.takeIf { it in branches } ?: (branches.firstOrNull() ?: "Global")
    }
    var selectedBranch by remember { mutableStateOf(initialBranch) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var loginError by remember { mutableStateOf<String?>(null) }

    val isNetworkOnline by viewModel.isNetworkOnline.collectAsState()
    val isCloudConnected = isNetworkOnline

    // Ensure selectedBranch stays valid if branches update from Cloud
    LaunchedEffect(branches) {
        if (branches.isNotEmpty() && branches.none { it.equals(selectedBranch, ignoreCase = true) }) {
            selectedBranch = viewModel.getLastLoginBranch()?.takeIf { it in branches } ?: branches.first()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmAlabasterBg),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.74f)
                .padding(12.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        WaffleoProductIllustration(
                            itemName = "Belgian Choco",
                            isWaffle = true,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Column {
                            Text(
                                text = "Waffleo POS — Login Cabang",
                                style = MaterialTheme.typography.headlineMedium,
                                color = RichEspressoPrimary
                            )
                            Text(
                                text = "Pilih cabang dan masukkan Username & Password akun cabang Anda",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedCocoaText
                            )
                        }
                    }

                    // Indikator Status Koneksi Shared Cloud Server pada Perangkat Ini
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isCloudConnected) SoftSageBg else SoftTerracottaBg,
                        border = BorderStroke(
                            1.dp,
                            if (isCloudConnected) MatchaSageSuccess.copy(alpha = 0.4f)
                            else TerracottaDanger.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                viewModel.forceFullCloudRefreshAndSync()
                            }
                            .testTag("login_cloud_status_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isCloudConnected) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = if (isCloudConnected) MatchaSageSuccess else TerracottaDanger,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (isCloudConnected) "☁ Terhubung" else "☁ Terputus",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isCloudConnected) MatchaSageSuccess else TerracottaDanger
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    WaffleoSingleSelectDropdown(
                        label = "Pilih Cabang",
                        selectedText = "Cabang ${viewModel.getBranchDisplayName(selectedBranch)}",
                        options = branches,
                        optionLabel = { "Cabang ${viewModel.getBranchDisplayName(it)}" },
                        onOptionSelected = {
                            selectedBranch = it
                            loginError = null
                        },
                        modifier = Modifier.weight(1f),
                        testTagPrefix = "login_branch"
                    )

                    WaffleoSoftTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            loginError = null
                        },
                        label = "Username",
                        placeholder = "Username",
                        modifier = Modifier.weight(1f),
                        testTag = "login_username_input"
                    )

                    WaffleoSoftTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            loginError = null
                        },
                        label = "Password",
                        placeholder = "Password",
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.weight(1f),
                        testTag = "login_password_input"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftOatSurface,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { rememberMe = !rememberMe }
                            .testTag("login_remember_me_checkbox")
                    ) {
                        Row(
                            modifier = Modifier.padding(end = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = RichEspressoPrimary)
                            )
                            Text(
                                text = "Ingatkan Saya (Auto-Login)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RichEspressoPrimary
                            )
                        }
                    }

                    Button(
                        onClick = {
                            // 1. Jika Developer Master, langsung masuk ke Portal Developer
                            if (viewModel.isDeveloperCredentials(username, password)) {
                                viewModel.loginStaff(selectedBranch, username, password, rememberMe)
                                return@Button
                            }
                            // 2. Validasi Username & Password untuk Cabang terpilih
                            val validAccount = viewModel.validateStaffCredentials(selectedBranch, username, password)
                            if (validAccount == null) {
                                loginError = "Username / Password salah"
                                return@Button
                            }
                            // 3. 1-Server Access: Langsung masuk ke sistem POS cabang tanpa perlu otentikasi Google
                            viewModel.loginStaff(selectedBranch, username, password, rememberMe)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RichEspressoPrimary,
                            contentColor = ElevatedIvoryCard
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("login_submit_button")
                    ) {
                        Text(
                            text = "Masuk ke Waffleo POS",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                if (loginError != null) {
                    Surface(
                        color = SoftTerracottaBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = loginError.orEmpty(),
                            style = MaterialTheme.typography.labelLarge,
                            color = TerracottaDanger,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Main Landscape Tablet Shell:
 * - Left Column: Collapsed Sidebar Kiri (full vertical height, icons only, never cut by Top Bar)
 * - Right Column:
 *   - Top Bar (Sidebar Atas) above the page content with Store Buka/Tutup pill & Profile pill (login time + Log Out)
 *   - Active Page Workspace (Menus, Orders, Order History, Dashboard, Settings)
 */
@Composable
fun WaffleoLandscapeMainShell(
    viewModel: WaffleoViewModel,
    isCloudSignedIn: Boolean = false,
    onRequestCloudSignInScreen: () -> Unit = {},
    onSignOutGoogleCloud: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val session by viewModel.session.collectAsState()
    val isStoreOpen by viewModel.isStoreOpen.collectAsState()
    val currentDest by viewModel.currentDestination.collectAsState()
    val allOrders by viewModel.orders.collectAsState()
    val recappedOrderIds by viewModel.recappedOrderIds.collectAsState()
    val catalogRecords by viewModel.catalogRecords.collectAsState()
    val bannerMessage by viewModel.statusBannerMessage.collectAsState()
    val isNetworkOnline by viewModel.isNetworkOnline.collectAsState()

    val activeOrderCount = remember(allOrders) { allOrders.count { it.status == "ACTIVE" } }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showStoreConfirmDialog by remember { mutableStateOf(false) }
    var isTopHeaderExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(bannerMessage) {
        if (bannerMessage != null) {
            delay(2000L)
            viewModel.dismissBanner()
        }
    }

    if (!session.isLoggedIn) {
        BranchPosLoginScreen(viewModel = viewModel)
        return
    }

    if (session.role == StaffRole.DEVELOPER && session.isInDeveloperPortal) {
        DeveloperBranchPortalScreen(viewModel = viewModel)
        return
    }

    if (currentDest != MainNavDestination.MENUS) {
        BackHandler {
            viewModel.navigateTo(MainNavDestination.MENUS)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmAlabasterBg)
        ) {
        // 1. SIDEBAR KIRI: Full Vertical Dock (Icons only, full vertical height, flush straight edges)
        Surface(
            modifier = Modifier
                .width(70.dp)
                .fillMaxHeight(),
            color = SidebarEspressoDark,
            shape = RectangleShape,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Emblem Logo Pill at top of Left Sidebar
                Surface(
                    shape = CircleShape,
                    color = ChampagneGoldAccent,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.BakeryDining,
                            contentDescription = "Waffleo Logo",
                            tint = SidebarEspressoDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                val visibleDestinations = remember(session.role) {
                    MainNavDestination.entries.filter { dest ->
                        dest != MainNavDestination.MONEY_MANAGER || session.role.canAccessMoneyManager
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
                ) {
                    visibleDestinations.forEach { dest ->
                        val isSelected = currentDest == dest
                        val iconVec = when (dest) {
                            MainNavDestination.MENUS -> Icons.Default.RestaurantMenu
                            MainNavDestination.ORDERS -> Icons.Default.ReceiptLong
                            MainNavDestination.ORDER_HISTORY -> Icons.Default.History
                            MainNavDestination.DASHBOARD -> Icons.Default.Analytics
                            MainNavDestination.SETTINGS -> Icons.Default.Settings
                            MainNavDestination.MONEY_MANAGER -> Icons.Default.AccountBalanceWallet
                        }

                        Surface(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.navigateTo(dest) }
                                .testTag("sidebar_nav_${dest.name.lowercase()}"),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) ChampagneGoldAccent else Color.Transparent
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (dest == MainNavDestination.ORDERS && activeOrderCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = if (isSelected) SidebarEspressoDark else ChampagneGoldAccent,
                                                contentColor = if (isSelected) SoftGoldHighlight else SidebarEspressoDark
                                            ) {
                                                Text(activeOrderCount.toString(), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = iconVec,
                                            contentDescription = dest.title,
                                            tint = if (isSelected) SidebarEspressoDark else SidebarIconInactive,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = iconVec,
                                        contentDescription = dest.title,
                                        tint = if (isSelected) SidebarEspressoDark else SidebarIconInactive,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Cloud Sync Active Indicator Pill & App Version Badge at bottom of Left Sidebar
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { showProfileDialog = true }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isNetworkOnline) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                contentDescription = "Cloud Sync Status",
                                tint = if (isNetworkOnline) MatchaSageSuccess else ChampagneGoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { showProfileDialog = true }
                            .testTag("sidebar_app_version_badge")
                    ) {
                        Text(
                            text = "v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ChampagneGoldAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // 2. RIGHT MAIN AREA: TOP BAR (Sidebar Atas) + ACTIVE PAGE CONTENT
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            // SIDEBAR ATAS: Integrated Luxury Café POS Header (Dapat disembunyikan / Toggle Collapse)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(
                        visible = isTopHeaderExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            color = ElevatedIvoryCard,
                            border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.85f)),
                            shadowElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left of Top Bar: Brand Emblem & Logo + Breadcrumbs (Layar, Cabang, Role)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Brand Emblem & Logo
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = SoftGoldHighlight,
                                            border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.45f)),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.BakeryDining,
                                                    contentDescription = "Waffleo Logo",
                                                    tint = DeepCaramelGold,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.Bottom) {
                                            Text(
                                                text = "Waffleo",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = RichEspressoPrimary,
                                                letterSpacing = (-0.5).sp
                                            )
                                            Text(
                                                text = ".",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = HoneyCaramelAccent
                                            )
                                        }
                                    }

                                    // Subtle Vertical Divider
                                    Box(
                                        modifier = Modifier
                                            .height(20.dp)
                                            .width(1.dp)
                                            .background(SubtleSandBorder)
                                    )

                                    // Breadcrumbs: Screen Title + Branch Badge + Role Badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = currentDest.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = RichEspressoPrimary
                                        )

                                        // Branch Pill Badge
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = SoftOatSurface,
                                            border = BorderStroke(1.dp, SubtleSandBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = "Cabang",
                                                    tint = CaramelGold,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Text(
                                                    text = "Cabang ${viewModel.getBranchDisplayName(session.branchId)}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = RichEspressoPrimary
                                                )
                                            }
                                        }

                                        // Role Pill Badge
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = SoftGoldHighlight,
                                            border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.35f))
                                        ) {
                                            Text(
                                                text = session.role.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = DeepCaramelGold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // Right of Top Bar: [ Portal Cabang (Dev) ] + [ Buka / Tutup Toko (Live Dot) ] & [ Profile Dropdown ]
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (session.role == StaffRole.DEVELOPER) {
                                        Surface(
                                            modifier = Modifier
                                                .height(38.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { viewModel.openDeveloperBranchPortal() }
                                                .testTag("topbar_dev_portal_btn"),
                                            shape = RoundedCornerShape(10.dp),
                                            color = RichEspressoPrimary
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AdminPanelSettings,
                                                    contentDescription = "Developer Portal",
                                                    tint = SoftGoldHighlight,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Portal Cabang",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SoftGoldHighlight
                                                )
                                            }
                                        }
                                    }

                                    // Live Store Status Button [ Toko Buka / Toko Tutup ] with glowing pulse dot
                                    val storeStatusColor = if (isStoreOpen) MatchaSageSuccess else TerracottaDanger
                                    val storeStatusBg = if (isStoreOpen) SoftSageBg else SoftTerracottaBg
                                    Surface(
                                        modifier = Modifier
                                            .height(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { showStoreConfirmDialog = true }
                                            .testTag("topbar_store_open_close_btn"),
                                        shape = RoundedCornerShape(12.dp),
                                        color = storeStatusBg,
                                        border = BorderStroke(1.dp, storeStatusColor.copy(alpha = 0.4f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                                        ) {
                                            // Live Status Indicator Dot
                                            Box(contentAlignment = Alignment.Center) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = storeStatusColor.copy(alpha = 0.25f),
                                                    modifier = Modifier.size(12.dp)
                                                ) {}
                                                Surface(
                                                    shape = CircleShape,
                                                    color = storeStatusColor,
                                                    modifier = Modifier.size(7.dp)
                                                ) {}
                                            }
                                            Icon(
                                                imageVector = Icons.Default.Storefront,
                                                contentDescription = "Status Toko",
                                                tint = storeStatusColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = if (isStoreOpen) "Toko Buka" else "Toko Tutup",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = storeStatusColor
                                            )
                                        }
                                    }

                                    // Profile & Account Button with subtle dropdown indicator
                                    Surface(
                                        modifier = Modifier
                                            .height(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { showProfileDialog = true }
                                            .testTag("topbar_profile_btn"),
                                        shape = RoundedCornerShape(12.dp),
                                        color = SoftOatSurface,
                                        border = BorderStroke(1.dp, SubtleSandBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(start = 6.dp, end = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = RichEspressoPrimary,
                                                border = BorderStroke(1.dp, HoneyCaramelAccent.copy(alpha = 0.6f)),
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = "Profile",
                                                        tint = SoftGoldHighlight,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = session.username,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = RichEspressoPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Dropdown",
                                                tint = MutedCocoaText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Floating Toggle Button in Center Bottom of Header
                Surface(
                    shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
                    color = ElevatedIvoryCard,
                    border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.85f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .offset(y = 12.dp)
                        .clip(RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp))
                        .clickable { isTopHeaderExpanded = !isTopHeaderExpanded }
                        .testTag("topbar_toggle_collapse_btn")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTopHeaderExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isTopHeaderExpanded) "Sembunyikan Header" else "Tampilkan Header",
                            tint = DeepCaramelGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ACTIVE PAGE CONTENT + FLOATING OVERLAY POP-UP BANNER (Nimpa Layer-to-Layer tanpa mendorong halaman)
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentDest) {
                    MainNavDestination.MENUS -> MenusAndCartScreen(viewModel = viewModel)
                    MainNavDestination.ORDERS -> OrdersQueueScreen(viewModel = viewModel)
                    MainNavDestination.ORDER_HISTORY -> OrderHistoryScreen(viewModel = viewModel)
                    MainNavDestination.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                    MainNavDestination.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    MainNavDestination.MONEY_MANAGER -> MoneyManagerScreen(viewModel = viewModel)
                }

                // True Floating Pop-up Layer right below Top Bar (does NOT shift layout)
                androidx.compose.animation.AnimatedVisibility(
                    visible = bannerMessage != null,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(20f)
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Surface(
                        color = RichEspressoPrimary,
                        shape = RoundedCornerShape(50),
                        shadowElevation = 10.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50))
                            .clickable { viewModel.dismissBanner() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bannerMessage.orEmpty(),
                                style = MaterialTheme.typography.labelLarge,
                                color = SoftGoldHighlight
                            )
                            Text(
                                text = "Tutup",
                                style = MaterialTheme.typography.labelMedium,
                                color = ChampagneGoldAccent
                            )
                        }
                    }
                }
            }
        }
    }

        // Floating Maintenance Mode Toggle Button (Developer Only)
        if (session.role == StaffRole.DEVELOPER) {
            val isMaintOn by viewModel.isMaintenanceModeOn.collectAsState()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 82.dp, bottom = 16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isMaintOn) TerracottaDanger else MatchaSageSuccess,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { viewModel.toggleMaintenanceMode() }
                        .testTag("developer_maintenance_floater")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isMaintOn) Icons.Default.AdminPanelSettings else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Maintenance Mode : ${if (isMaintOn) "ON" else "OFF"}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

    // Store Open / Close Confirmation Modal ("Tutup Toko? Ya/Tidak" atau "Buka Toko? Ya/Tidak")
    if (showStoreConfirmDialog) {
        val todayOrders = remember(allOrders) {
            val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val todayKey = dateFmt.format(Date())
            allOrders.filter { ord ->
                ord.isPaid && ord.status != "CANCELLED" &&
                    !viewModel.isBackdatedOrder(ord) &&
                    dateFmt.format(WaffleoViewModel.resolveOrderTimestampDate(ord)) == todayKey
            }
        }
        val shiftOrders = remember(todayOrders, recappedOrderIds) {
            todayOrders.filter { it.id !in recappedOrderIds }
        }
        val dailyRecapPreview = remember(shiftOrders, catalogRecords, isStoreOpen) {
            if (isStoreOpen) viewModel.computeStoreCloseDailyRecap(shiftOrders, catalogRecords, onlyToday = true, ignoreAlreadyRecapped = false) else emptyList()
        }
        val shiftTotalItemsSummary = remember(shiftOrders) {
            viewModel.computeOrdersTotalItemSummary(shiftOrders)
        }
        val totalNetRecap = remember(dailyRecapPreview) {
            dailyRecapPreview.sumOf { it.netAmount }
        }

        Dialog(
            onDismissRequest = { showStoreConfirmDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.7f)),
                modifier = Modifier
                    .width(if (isStoreOpen) 540.dp else 380.dp)
                    .wrapContentHeight()
                    .heightIn(max = if (isStoreOpen) 640.dp else 420.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(horizontal = 24.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Scrollable Content Area (Header + Rekapitulasi Income + Data Lainnya)
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isStoreOpen) SoftTerracottaBg else SoftSageBg,
                            border = BorderStroke(1.dp, (if (isStoreOpen) TerracottaDanger else MatchaSageSuccess).copy(alpha = 0.25f)),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = if (isStoreOpen) TerracottaDanger else MatchaSageSuccess,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isStoreOpen) "Tutup Toko & Rekap Keuangan?" else "Buka Toko?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isStoreOpen) {
                                "Menutup toko Cabang ${viewModel.getBranchDisplayName(session.branchId)} akan otomatis merekap seluruh pendapatan shift ini ke dalam catatan Income Money Manager:"
                            } else {
                                "Ubah status operasional Cabang ${viewModel.getBranchDisplayName(session.branchId)} menjadi BUKA?"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedCocoaText,
                            textAlign = TextAlign.Center
                        )

                        if (isStoreOpen) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = SoftOatSurface,
                                border = BorderStroke(1.dp, SubtleSandBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "💰 Rekap Income Shift Ini → Money Manager",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = RichEspressoPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = SoftSageBg,
                                            border = BorderStroke(1.dp, MatchaSageSuccess.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = "Auto-Sync Saat Tutup",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MatchaSageSuccess,
                                                maxLines = 1,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = SubtleSandBorder)

                                    if (dailyRecapPreview.isEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = ElevatedIvoryCard.copy(alpha = 0.7f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Belum ada transaksi penjualan lunas pada shift ini yang belum direkap.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MutedCocoaText,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp)
                                            )
                                        }
                                    } else {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            dailyRecapPreview.forEach { item ->
                                                val channelLabel = when {
                                                    item.subCategory.equals("Cash", ignoreCase = true) -> "Cash (Tunai)"
                                                    item.subCategory.equals("Transfer Tunai", ignoreCase = true) -> "Transfer Tunai"
                                                    item.subCategory.equals("QRIS", ignoreCase = true) -> "QRIS • ${item.accountName}"
                                                    else -> "${item.subCategory} • ${item.accountName}"
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = ElevatedIvoryCard,
                                                    border = BorderStroke(1.dp, SubtleSandBorder.copy(alpha = 0.6f)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = channelLabel,
                                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = RichEspressoPrimary,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            Surface(
                                                                shape = RoundedCornerShape(50),
                                                                color = SoftOatSurface
                                                            ) {
                                                                Text(
                                                                    text = "${item.orderCount} Pesanan",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.SemiBold,
                                                                    color = MutedCocoaText,
                                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                                )
                                                            }
                                                            Text(
                                                                text = WaffleoViewModel.formatIdr(item.netAmount),
                                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                                color = MatchaSageSuccess
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = SubtleSandBorder)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Total Item Terjual Shift Ini",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = MutedCocoaText
                                            )
                                            Text(
                                                text = shiftTotalItemsSummary,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = RichEspressoPrimary
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = SoftSageBg.copy(alpha = 0.65f),
                                            border = BorderStroke(1.dp, MatchaSageSuccess.copy(alpha = 0.35f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Total Net Income Shift Ini",
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = RichEspressoPrimary
                                                )
                                                Text(
                                                    text = WaffleoViewModel.formatIdr(totalNetRecap),
                                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = MatchaSageSuccess
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Always-Visible Pinned Bottom Action Row ("Tidak" / "Ya, Tutup & Sync")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftOatSurface,
                            border = BorderStroke(1.dp, SubtleSandBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { showStoreConfirmDialog = false }
                                .testTag("store_confirm_no_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Tidak",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary,
                                    maxLines = 1
                                )
                            }
                        }
                        Button(
                            onClick = {
                                viewModel.toggleStoreOpenStatus()
                                showStoreConfirmDialog = false
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isStoreOpen) TerracottaDanger else MatchaSageSuccess,
                                contentColor = ElevatedIvoryCard
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("store_confirm_yes_btn")
                        ) {
                            Text(
                                text = if (isStoreOpen) "Ya, Tutup & Sync" else "Ya, Buka Toko",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    // Profile Modal showing Login Time, Cloud Sync connection, and Log Out Access
    if (showProfileDialog) {
        Dialog(onDismissRequest = { showProfileDialog = false }) {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(22.dp)
                ) {
                    val cloudConnected = isNetworkOnline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = SoftGoldHighlight,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = RichEspressoPrimary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = session.displayName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = RichEspressoPrimary
                                )
                                Text(
                                    text = "Cabang ${viewModel.getBranchDisplayName(session.branchId)} • Role: ${session.role.label}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DeepCaramelGold
                                )
                            }
                        }

                        // Pojok Kanan Atas: Single Badge [ ☁ Terhubung / Terputus ]
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (cloudConnected) SoftSageBg else SoftTerracottaBg,
                            border = BorderStroke(
                                1.dp,
                                if (cloudConnected) MatchaSageSuccess.copy(alpha = 0.5f) else TerracottaDanger.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    viewModel.forceFullCloudRefreshAndSync()
                                    showProfileDialog = false
                                }
                                .testTag("profile_cloud_status_corner_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (cloudConnected) "☁ Terhubung" else "☁ Terputus",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cloudConnected) MatchaSageSuccess else TerracottaDanger
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SoftOatSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Waktu Login (Login Time):",
                                style = MaterialTheme.typography.labelLarge,
                                color = MutedCocoaText
                            )
                            Text(
                                text = session.loginTimeFormatted,
                                style = MaterialTheme.typography.titleLarge,
                                color = RichEspressoPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SoftOatSurface,
                        border = BorderStroke(1.dp, SubtleSandBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Versi Aplikasi",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MutedCocoaText
                                )
                                Text(
                                    text = "WAFFLEO POS v${BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SoftGoldHighlight,
                                border = BorderStroke(1.dp, ChampagneGoldAccent.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME} • Build ${BuildConfig.VERSION_CODE}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepCaramelGold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (session.role == StaffRole.DEVELOPER) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SoftGoldHighlight,
                                    modifier = Modifier
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            showProfileDialog = false
                                            viewModel.openDeveloperBranchPortal()
                                        }
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            "Pilih / Tambah Cabang",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = RichEspressoPrimary
                                        )
                                    }
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = SoftOatSurface,
                                modifier = Modifier
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable { showProfileDialog = false }
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                                    Text("Tutup", style = MaterialTheme.typography.labelLarge, color = RichEspressoPrimary)
                                }
                            }
                            Button(
                                onClick = {
                                    showProfileDialog = false
                                    viewModel.logoutStaff()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TerracottaDanger,
                                    contentColor = ElevatedIvoryCard
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .height(40.dp)
                                    .testTag("profile_logout_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Log Out Cabang", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
}

/**
 * Halaman Khusus Developer (Mutlak & Kasta Tertinggi):
 * - Memilih cabang mana yang ingin dimasuki (Full Access)
 * - Menambahkan Cabang Baru beserta data Owner & opsi Salin Sistem/Data atau Kosong (Manual)
 */
@Composable
fun DeveloperBranchPortalScreen(
    viewModel: WaffleoViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val branchProfiles by viewModel.branchProfiles.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val allCatalog by viewModel.allCatalogRecords.collectAsState()
    val staffAccounts by viewModel.staffAccounts.collectAsState()
    val bannerMessage by viewModel.statusBannerMessage.collectAsState()
    val isNetworkOnline by viewModel.isNetworkOnline.collectAsState()
    val isCloudConnected = isNetworkOnline

    var showCreateBranchDialog by remember { mutableStateOf(false) }
    var selectedBranchToDelete by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmAlabasterBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Top Developer Command Header
            Surface(
                shape = RoundedCornerShape(50),
                color = ElevatedIvoryCard,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = RichEspressoPrimary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = SoftGoldHighlight,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Waffleo Developer Portal — Master Multi-Cabang",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = RichEspressoPrimary
                            )
                            Text(
                                text = "Akses Mutlak Developer • Pilih cabang untuk dikelola atau tambahkan cabang baru",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedCocoaText
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isCloudConnected) SoftSageBg else SoftTerracottaBg,
                            border = BorderStroke(
                                1.dp,
                                if (isCloudConnected) MatchaSageSuccess.copy(alpha = 0.4f)
                                else TerracottaDanger.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .height(42.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    viewModel.forceFullCloudRefreshAndSync()
                                }
                                .testTag("dev_portal_cloud_sync_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCloudConnected) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = if (isCloudConnected) MatchaSageSuccess else TerracottaDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isCloudConnected) "Cloud Synced" else "Offline",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCloudConnected) MatchaSageSuccess else TerracottaDanger
                                )
                            }
                        }

                        Button(
                            onClick = { showCreateBranchDialog = true },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RichEspressoPrimary,
                                contentColor = SoftGoldHighlight
                            ),
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("dev_add_branch_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tambah Cabang Baru",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SoftTerracottaBg,
                            modifier = Modifier
                                .height(42.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { viewModel.logoutStaff() }
                                .testTag("dev_logout_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Log Out",
                                    tint = TerracottaDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Log Out",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaDanger
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid of Registered Branches
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(branchProfiles, key = { it.branchId }) { profile ->
                    val branchCatalogCount = remember(allCatalog, profile.branchId) {
                        allCatalog.count {
                            it.branchId.equals(profile.branchId, ignoreCase = true) &&
                                it.recordType != "BRANCH_CONFIG"
                        }
                    }
                    val branchStaffCount = remember(staffAccounts, profile.branchId) {
                        staffAccounts.count {
                            it.branchId.equals(profile.branchId, ignoreCase = true) &&
                                it.role != StaffRole.DEVELOPER
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = SoftGoldHighlight,
                                        modifier = Modifier.size(44.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Storefront,
                                                contentDescription = null,
                                                tint = RichEspressoPrimary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = "Cabang ${profile.displayName}",
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = RichEspressoPrimary
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = DeepCaramelGold,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = profile.location,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MutedCocoaText
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = SoftSageBg
                                ) {
                                    Text(
                                        text = com.example.data.model.sanitizeBranchIdsInText(profile.initializedFrom),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MatchaSageSuccess,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = SoftOatSurface.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Pemilik: ${profile.ownerName} (@${profile.ownerUsername})",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = RichEspressoPrimary
                                        )
                                        Text(
                                            text = profile.ownerPhone.ifBlank { "-" },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DeepCaramelGold
                                        )
                                    }
                                    Text(
                                        text = "Email: ${profile.ownerEmail.ifBlank { "-" }}  •  Data Item: $branchCatalogCount item  •  Akun: $branchStaffCount staff",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedCocoaText
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!profile.branchId.equals("Global", ignoreCase = true)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = SoftTerracottaBg,
                                        border = BorderStroke(1.dp, TerracottaDanger.copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .clickable { selectedBranchToDelete = profile.branchId }
                                            .testTag("dev_delete_branch_${profile.branchId}")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus Cabang ${profile.displayName}",
                                                tint = TerracottaDanger,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = { viewModel.enterBranchAsDeveloper(profile.branchId) },
                                    shape = RoundedCornerShape(50),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = RichEspressoPrimary,
                                        contentColor = ElevatedIvoryCard
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .testTag("dev_enter_branch_${profile.branchId}")
                                ) {
                                    Text(
                                        text = "Masuk ke Cabang ${profile.displayName} (Full Access)",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedBranchToDelete?.let { branchToDelete ->
            DeleteBranchConfirmationDialog(
                branchId = branchToDelete,
                onDismiss = { selectedBranchToDelete = null },
                onConfirmDelete = { devUser, devPass ->
                    val res = viewModel.deleteBranchPermanently(branchToDelete, devUser, devPass)
                    if (res.isSuccess) {
                        selectedBranchToDelete = null
                    }
                    res
                }
            )
        }

        // Floating Banner on Developer Portal
        AnimatedVisibility(
            visible = bannerMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(20f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 72.dp)
        ) {
            Surface(
                color = RichEspressoPrimary,
                shape = RoundedCornerShape(50),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .clickable { viewModel.dismissBanner() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bannerMessage.orEmpty(),
                        style = MaterialTheme.typography.labelLarge,
                        color = SoftGoldHighlight
                    )
                    Text(
                        text = "Tutup",
                        style = MaterialTheme.typography.labelMedium,
                        color = ChampagneGoldAccent
                    )
                }
            }
        }
    }

    if (showCreateBranchDialog) {
        CreateNewBranchDialog(
            existingBranches = branches,
            onDismiss = { showCreateBranchDialog = false },
            onCreateBranch = { name: String, loc: String, ownerName: String, email: String, phone: String, ownerUser: String, ownerPass: String, copySource: String? ->
                viewModel.createNewBranchByDeveloper(
                    branchName = name,
                    location = loc,
                    ownerName = ownerName,
                    ownerEmail = email,
                    ownerPhone = phone,
                    ownerUsername = ownerUser,
                    ownerPassword = ownerPass,
                    copyFromBranch = copySource
                )
            }
        )
    }
}

@Composable
fun CreateNewBranchDialog(
    existingBranches: List<String>,
    onDismiss: () -> Unit,
    onCreateBranch: (
        branchName: String,
        location: String,
        ownerName: String,
        ownerEmail: String,
        ownerPhone: String,
        ownerUsername: String,
        ownerPassword: String,
        copyFromBranch: String?
    ) -> Result<String>
) {
    var branchName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var ownerEmail by remember { mutableStateOf("") }
    var ownerPhone by remember { mutableStateOf("") }
    var ownerUsername by remember { mutableStateOf("") }
    var ownerPassword by remember { mutableStateOf("") }

    // true = Copy/Paste system & data from existing branch; false = Empty (manual input by branch owner)
    var isCopyMode by remember { mutableStateOf(true) }
    var sourceBranch by remember(existingBranches) {
        mutableStateOf(existingBranches.firstOrNull() ?: "Global")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = ElevatedIvoryCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SoftGoldHighlight,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = RichEspressoPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Tambah Cabang Baru",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                        Text(
                            text = "Lengkapi data cabang, identitas pemilik (Owner), kredensial login, dan mode inisialisasi data",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedCocoaText
                        )
                    }
                }

                // Row 1: Nama Cabang & Lokasi Cabang
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WaffleoSoftTextField(
                        value = branchName,
                        onValueChange = {
                            branchName = capitalizeSentenceStart(it)
                            errorMessage = null
                        },
                        label = "Nama Cabang",
                        placeholder = "Contoh: Kemang / Tebet",
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.weight(1f),
                        testTag = "new_branch_name_input"
                    )
                    WaffleoSoftTextField(
                        value = location,
                        onValueChange = { location = capitalizeSentenceStart(it) },
                        label = "Lokasi / Alamat Cabang",
                        placeholder = "Jl. Kemang Raya No. 12...",
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.weight(1f),
                        testTag = "new_branch_location_input"
                    )
                }

                // Row 2: Nama Pemilik, Email, Nomor HP
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WaffleoSoftTextField(
                        value = ownerName,
                        onValueChange = { ownerName = capitalizeSentenceStart(it) },
                        label = "Nama Pemilik (Owner)",
                        placeholder = "Nama lengkap pemilik...",
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.weight(1f),
                        testTag = "new_branch_owner_name_input"
                    )
                    WaffleoSoftTextField(
                        value = ownerEmail,
                        onValueChange = { ownerEmail = it },
                        label = "Email Pemilik",
                        placeholder = "owner@email.com",
                        modifier = Modifier.weight(1f),
                        testTag = "new_branch_owner_email_input"
                    )
                    WaffleoSoftTextField(
                        value = ownerPhone,
                        onValueChange = { ownerPhone = it },
                        label = "Nomor HP",
                        placeholder = "0812...",
                        modifier = Modifier.weight(1f),
                        testTag = "new_branch_owner_phone_input"
                    )
                }

                // Row 3: Username Owner & Password Owner
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    WaffleoSoftTextField(
                        value = ownerUsername,
                        onValueChange = {
                            ownerUsername = it
                            errorMessage = null
                        },
                        label = "Username Owner Cabang",
                        placeholder = "Contoh: owner_kemang",
                        modifier = Modifier.weight(1f),
                        testTag = "new_branch_owner_username_input"
                    )
                    WaffleoSoftTextField(
                        value = ownerPassword,
                        onValueChange = {
                            ownerPassword = it
                            errorMessage = null
                        },
                        label = "Password Owner Cabang",
                        placeholder = "Password login owner...",
                        modifier = Modifier.weight(1f),
                        testTag = "new_branch_owner_password_input"
                    )
                }

                // Row 4: Opsi Inisialisasi Sistem & Data Cabang (Copy-Paste vs Kosong/Manual)
                Text(
                    text = "Pengaturan Awal Sistem & Data Katalog Cabang",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = RichEspressoPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isCopyMode) SoftGoldHighlight else SoftOatSurface,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { isCopyMode = true }
                            .testTag("new_branch_mode_copy")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = RichEspressoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Copy-Paste Sistem & Data",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )
                                Text(
                                    text = "Salin Menu, Modifier, Stok & Promo dari cabang yang ada",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedCocoaText
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (!isCopyMode) SoftGoldHighlight else SoftOatSurface,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { isCopyMode = false }
                            .testTag("new_branch_mode_empty")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = RichEspressoPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Kosong (Input Manual Owner)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = RichEspressoPrimary
                                )
                                Text(
                                    text = "Data item kosong, ditambahkan manual oleh Owner cabang",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedCocoaText
                                )
                            }
                        }
                    }
                }

                if (isCopyMode) {
                    WaffleoSingleSelectDropdown(
                        label = "Pilih Sumber Cabang untuk Copy-Paste Data",
                        selectedText = "Cabang ${com.example.data.model.formatCleanBranchName(sourceBranch, sourceBranch)}",
                        options = existingBranches.ifEmpty { listOf("Global") },
                        optionLabel = { "Cabang ${com.example.data.model.formatCleanBranchName(it, it)}" },
                        onOptionSelected = { sourceBranch = it },
                        modifier = Modifier.fillMaxWidth(),
                        testTagPrefix = "new_branch_source"
                    )
                }

                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftTerracottaBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = TerracottaDanger,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftOatSurface,
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable(onClick = onDismiss)
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 18.dp), contentAlignment = Alignment.Center) {
                            Text("Batal", style = MaterialTheme.typography.labelLarge, color = RichEspressoPrimary)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val result = onCreateBranch(
                                branchName,
                                location,
                                ownerName,
                                ownerEmail,
                                ownerPhone,
                                ownerUsername,
                                ownerPassword,
                                if (isCopyMode) sourceBranch else null
                            )
                            result.onSuccess {
                                onDismiss()
                            }.onFailure { err ->
                                errorMessage = err.message ?: "Gagal membuat cabang baru."
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RichEspressoPrimary,
                            contentColor = SoftGoldHighlight
                        ),
                        modifier = Modifier
                            .height(42.dp)
                            .testTag("new_branch_submit_btn")
                    ) {
                        Text(
                            text = "Simpan & Buat Cabang",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteBranchConfirmationDialog(
    branchId: String,
    onDismiss: () -> Unit,
    onConfirmDelete: (username: String, password: String) -> Result<Unit>
) {
    var devUsername by remember { mutableStateOf("") }
    var devPassword by remember { mutableStateOf("") }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = ElevatedIvoryCard,
            border = BorderStroke(1.5.dp, TerracottaDanger.copy(alpha = 0.5f)),
            shadowElevation = 16.dp,
            modifier = Modifier
                .width(460.dp)
                .padding(16.dp)
                .testTag("delete_branch_confirm_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SoftTerracottaBg,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = null,
                                    tint = TerracottaDanger,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "Konfirmasi Hapus Cabang",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = RichEspressoPrimary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MutedCocoaText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "Tindakan ini akan menghapus 'Cabang ${com.example.data.model.formatCleanBranchName(branchId, branchId)}' beserta seluruh data katalog, akun staff, dan transaksi secara permanen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedCocoaText
                )

                // Layout specified by user:
                // [ username ] [ password ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WaffleoSoftTextField(
                        value = devUsername,
                        onValueChange = {
                            devUsername = it
                            errorMessage = null
                        },
                        label = "Username Developer",
                        placeholder = "username",
                        modifier = Modifier.weight(1f)
                    )

                    WaffleoSoftTextField(
                        value = devPassword,
                        onValueChange = {
                            devPassword = it
                            errorMessage = null
                        },
                        label = "Password Developer",
                        placeholder = "password",
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SoftTerracottaBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage.orEmpty(),
                            style = MaterialTheme.typography.labelMedium,
                            color = TerracottaDanger,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // >>> Hapus Cabang >>> (Interactive Swipe Slider)
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(50))
                        .background(SoftTerracottaBg)
                        .border(1.dp, TerracottaDanger.copy(alpha = 0.4f), RoundedCornerShape(50))
                ) {
                    val density = LocalDensity.current
                    val maxOffsetPx = with(density) { (maxWidth - 54.dp).toPx() }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ">>> Hapus Cabang >>>",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaDanger
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = TerracottaDanger,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .offset { IntOffset(dragOffsetPx.roundToInt(), 0) }
                            .size(54.dp)
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (dragOffsetPx >= maxOffsetPx * 0.7f) {
                                            val result = onConfirmDelete(devUsername, devPassword)
                                            result.onFailure { err ->
                                                errorMessage = err.message ?: "Username / Password salah"
                                            }
                                        }
                                        dragOffsetPx = 0f
                                    },
                                    onDragCancel = {
                                        dragOffsetPx = 0f
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        val next = dragOffsetPx + dragAmount
                                        dragOffsetPx = next.coerceIn(0f, maxOffsetPx)
                                    }
                                )
                            }
                            .testTag("dev_delete_branch_slider_handle")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Geser Hapus",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
