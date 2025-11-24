package com.tab.tablon.data.secure

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class CredentialsManager(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secret_shared_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(email: String, password: String) {
        with(sharedPreferences.edit()) {
            putString("email", email)
            putString("password", password)
            apply()
        }
    }

    fun getEmail(): String? = sharedPreferences.getString("email", null)

    fun getPassword(): String? = sharedPreferences.getString("password", null)

    fun hasCredentials(): Boolean = getEmail() != null && getPassword() != null

    fun clearCredentials() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }
}