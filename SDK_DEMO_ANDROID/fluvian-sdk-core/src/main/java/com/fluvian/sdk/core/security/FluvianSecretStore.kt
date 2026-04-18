/**
 * File: FluvianSecretStore.kt
 * Description: AES-backed [android.content.SharedPreferences] for integrator-held material (short-lived tokens,
 * integration keys your policy allows on device) using AndroidX Security Crypto.
 * Author: monigarr@monigarr.com
 * Date: 2026-04-15
 * Version: 1.3.6
 *
 * **Security and IP posture (read before use):**
 * - This class is a **reference pattern** only. The open-core repository ships **no** production credentials, license
 *   servers, or decryptable customer secrets.
 * - Provision values at runtime from your secure backend, enterprise vault, or OEM keystore. **Never** commit real
 *   API keys, Widevine tokens, or license JWTs to git, CI logs, crash reports, or public forks.
 * - Encrypted preferences protect data at rest on the device; they do **not** substitute for contract-grade key
 *   management or compliance review. Do not store highly sensitive PII here without legal and security sign-off.
 * - Treat any sample key *names* in documentation as illustrative; rotate and namespace keys per app build and tenant.
 *
 * Usage:
 *   Call [create] once per process; persist only material your counsel approves for device-bound storage.
 *
 * Usage example (key names illustrative; values must never appear in source):
 *   val store = FluvianSecretStore.create(context)
 *   store.putSecret("integrator_cloud_inference_key", keyFromHostKeystoreOrBackend)
 *   val key = store.getSecret("integrator_cloud_inference_key")
 */
package com.fluvian.sdk.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class FluvianSecretStore private constructor(
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
        private const val PREFS_NAME = "fluvian_sdk_secret_store"

        fun create(context: Context): FluvianSecretStore {
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
            return FluvianSecretStore(prefs)
        }
    }
}
