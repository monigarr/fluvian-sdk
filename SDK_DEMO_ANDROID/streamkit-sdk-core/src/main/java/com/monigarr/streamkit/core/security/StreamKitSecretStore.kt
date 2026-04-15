/**
 * File: StreamKitSecretStore.kt
 * Description: AES-backed [android.content.SharedPreferences] for integrator secrets (API keys, license tokens) using AndroidX Security Crypto.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-12
 * Version: 1.2.1
 *
 * Usage:
 *   Call [create] once per process; store non-PII credentials referenced by DRM or AI providers outside source control.
 *
 * Usage example:
 *   val store = StreamKitSecretStore.create(context)
 *   store.putSecret("openai_api_key", keyFromHostKeystore)
 *   val key = store.getSecret("openai_api_key")
 */
package com.monigarr.streamkit.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class StreamKitSecretStore private constructor(
    private val prefs: SharedPreferences,
) {

    fun putSecret(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getSecret(key: String): String? = prefs.getString(key, null)

    fun removeSecret(key: String) {
        prefs.edit().remove(key).apply()
    }

    companion object {
        private const val PREFS_NAME = "streamkit_sdk_secret_store"

        fun create(context: Context): StreamKitSecretStore {
            val appContext = context.applicationContext
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val prefs =
                EncryptedSharedPreferences.create(
                    PREFS_NAME,
                    masterKeyAlias,
                    appContext,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                )
            return StreamKitSecretStore(prefs)
        }
    }
}
