package com.example.base

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
abstract class FirestoreEmulatorTestBase {

    protected lateinit var firestore: FirebaseFirestore
    protected lateinit var auth: FirebaseAuth
    protected lateinit var databaseId: String

    @Before
    open fun setUpFirebase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val resId = context.resources.getIdentifier("firestore_database_id", "string", context.packageName)
        databaseId = if (resId != 0) context.getString(resId) else "ai-studio-android-waffleop-09a8b0fe-6e50-4d32-bf9c-bf8e8f86fe76"

        uploadLatestRulesToEmulator(databaseId)

        val app = if (FirebaseApp.getApps(context).isEmpty()) {
            val newApp = FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("com.aistudio.waffleopos.wfpos")
                    .setProjectId(PROJECT_ID)
                    .setApiKey("fake-api-key-for-emulator")
                    .build()
            )
            val newFirestore = FirebaseFirestore.getInstance(newApp, databaseId)
            newFirestore.useEmulator(EMULATOR_HOST, FIRESTORE_PORT)
            newFirestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build()

            val newAuth = FirebaseAuth.getInstance(newApp)
            newAuth.useEmulator(EMULATOR_HOST, AUTH_PORT)
            newApp
        } else {
            FirebaseApp.getInstance()
        }

        firestore = FirebaseFirestore.getInstance(app, databaseId)
        auth = FirebaseAuth.getInstance(app)
    }

    private fun uploadLatestRulesToEmulator(targetDbId: String) {
        try {
            android.os.StrictMode.setThreadPolicy(android.os.StrictMode.ThreadPolicy.Builder().permitAll().build())
            val rulesFile = listOf(
                java.io.File("firestore.rules"),
                java.io.File("../firestore.rules"),
                java.io.File("/app/applet/firestore.rules")
            ).firstOrNull { it.exists() } ?: return
            val rulesContent = rulesFile.readText()
            val payload = org.json.JSONObject().apply {
                put("release_name", "projects/$PROJECT_ID/releases/cloud.firestore/$targetDbId")
                put(
                    "rules",
                    org.json.JSONObject().apply {
                        put(
                            "files",
                            org.json.JSONArray().put(
                                org.json.JSONObject().apply {
                                    put("name", "security.rules")
                                    put("content", rulesContent)
                                }
                            )
                        )
                    }
                )
            }.toString()
            val url = java.net.URL("http://$EMULATOR_HOST:$FIRESTORE_PORT/emulator/v1/projects/$PROJECT_ID/ruleSets")
            val conn = (url.openConnection() as java.net.HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 2000
                readTimeout = 2000
            }
            conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            conn.responseCode
            conn.disconnect()
        } catch (_: Exception) {
        }
    }

    @After
    open fun tearDownFirebase() {
        auth.signOut()
    }

    protected suspend fun signOutAndIdle() {
        auth.signOut()
        kotlinx.coroutines.delay(150)
    }

    protected suspend fun signInTestUser(email: String): String = withContext(Dispatchers.IO) {
        withTimeout(AUTH_TIMEOUT_MS) {
            val result = try {
                auth.signInWithEmailAndPassword(email, DEFAULT_PASSWORD).await()
            } catch (unused: Exception) {
                try {
                    auth.createUserWithEmailAndPassword(email, DEFAULT_PASSWORD).await()
                } catch (_: Exception) {
                    auth.signInWithEmailAndPassword(email, DEFAULT_PASSWORD).await()
                }
            }
            val user = checkNotNull(result.user) { "User auth failed" }
            user.getIdToken(true).await()
            kotlinx.coroutines.delay(100)
            user.uid
        }
    }

    protected fun Throwable.unwrapFirestoreException(): FirebaseFirestoreException? {
        var current: Throwable? = this
        while (current != null) {
            if (current is FirebaseFirestoreException) return current
            current = current.cause
        }
        return null
    }

    private companion object {
        const val EMULATOR_HOST = "127.0.0.1"
        const val FIRESTORE_PORT = 8085
        const val AUTH_PORT = 9099
        const val PROJECT_ID = "demo-project"
        const val DEFAULT_PASSWORD = "password123"
        const val AUTH_TIMEOUT_MS = 5000L
    }
}
