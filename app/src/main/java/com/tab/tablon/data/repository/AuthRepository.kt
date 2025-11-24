package com.tab.tablon.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.tab.tablon.data.model.User
import kotlinx.coroutines.tasks.await
import android.util.Log

class AuthRepository {

    private val auth: FirebaseAuth = Firebase.auth
    // --- NUEVO: Referencia a Firestore ---
    private val db = Firebase.firestore
    private val usersCollection = db.collection("users")

    fun getCurrentUser() = auth.currentUser

    suspend fun registerWithEmail(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            // --- NUEVO: Después del registro, creamos el documento del usuario ---
            result.user?.let { firebaseUser ->
                val newUser = User(uid = firebaseUser.uid, email = firebaseUser.email ?: "")
                usersCollection.document(firebaseUser.uid).set(newUser).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    // --- NUEVA FUNCIÓN ---
    /**
     * Obtiene el documento de usuario de Firestore para el UID actual.
     * @return El objeto User si existe, o null.
     */
    suspend fun getUserData(): User? {
        val uid = auth.currentUser?.uid ?: run {
            Log.e("AuthRepo", "getUserData: currentUser es null")
            return null
        }
        Log.d("AuthRepo", "Buscando datos para UID: $uid") // LOG #1
        return try {
            val document = usersCollection.document(uid).get().await()
            val user = document.toObject<User>()
            Log.d("AuthRepo", "Usuario encontrado: $user") // LOG #2
            user
        } catch (e: Exception) {
            Log.e("AuthRepo", "Error obteniendo datos del usuario", e) // LOG #3
            null
        }
    }
}