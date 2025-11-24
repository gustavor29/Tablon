package com.tab.tablon.data.repository

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp
import com.tab.tablon.data.model.Household
import com.tab.tablon.data.model.Product
import com.tab.tablon.data.model.User
import com.tab.tablon.data.model.ArchivedList
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

class ShoppingListRepository {

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val householdsCollection = db.collection("households")
    private val usersCollection = db.collection("users")

    // --- FUNCIÓN RELLENADA ---
    fun getActiveShoppingList(householdId: String): Flow<List<Product>> = callbackFlow {
        val docRef = householdsCollection.document(householdId)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("ShopRepo", "Listen failed.", error)
                close(error)
                return@addSnapshotListener
            }

            val productList = if (snapshot != null && snapshot.exists()) {
                snapshot.toObject<Household>()?.activeList ?: emptyList()
            } else {
                emptyList()
            }
            Log.d("ShopRepo", "Nueva lista recibida con ${productList.size} productos.")
            trySend(productList)
        }

        awaitClose {
            Log.d("ShopRepo", "Listener de la lista removido.")
            listener.remove()
        }
    }

    // --- FUNCIÓN RELLENADA CON LOGS ---
    suspend fun addProduct(householdId: String, product: Product) {
        try {
            Log.d("ShopRepo", "Intentando añadir producto: $product a hogar: $householdId")
            val docRef = householdsCollection.document(householdId)
            docRef.update("activeList", FieldValue.arrayUnion(product)).await()
            Log.i("ShopRepo", "¡ÉXITO al añadir producto a Firestore!")
        } catch (e: Exception) {
            Log.e("ShopRepo", "ERROR al añadir producto a Firestore. Causa: ${e.javaClass.simpleName}", e)
        }
    }

    // --- FUNCIÓN RELLENADA ---
    suspend fun updateProduct(householdId: String, updatedProduct: Product) {
        try {
            val docRef = householdsCollection.document(householdId)
            val household = docRef.get().await().toObject<Household>() ?: return

            val newList = household.activeList.map {
                if (it.id == updatedProduct.id) updatedProduct else it
            }
            docRef.update("activeList", newList).await()
            Log.i("ShopRepo", "Producto actualizado con éxito: ${updatedProduct.id}")
        } catch (e: Exception) {
            Log.e("ShopRepo", "Error al actualizar producto", e)
        }
    }

    // --- FUNCIÓN RELLENADA ---
    suspend fun removeProduct(householdId: String, productToRemove: Product) {
        try {
            val docRef = householdsCollection.document(householdId)
            docRef.update("activeList", FieldValue.arrayRemove(productToRemove)).await()
            Log.i("ShopRepo", "Producto eliminado con éxito: ${productToRemove.id}")
        } catch (e: Exception) {
            Log.e("ShopRepo", "Error al eliminar producto", e)
        }
    }

    // --- NUEVA FUNCIÓN: Crear un Hogar ---
    /**
     * Crea un nuevo hogar en Firestore, genera un código de invitación
     * y vincula al usuario actual como el primer miembro.
     * @return El ID del nuevo hogar creado.
     */
    suspend fun createHousehold(): String {
        val currentUser = auth.currentUser ?: throw IllegalStateException("Usuario no logueado")
        val invitationCode = generateInvitationCode()

        val newHousehold = Household(
            invitationCode = invitationCode,
            members = listOf(currentUser.uid)
        )

        // Creamos el documento del hogar en Firestore
        val householdRef = householdsCollection.add(newHousehold).await()

        /// --- CAMBIO CLAVE ---
        // Usamos .set() con merge=true. Esto creará el campo householdId si no existe,
        // o lo actualizará si ya existe, sin borrar otros campos como el email.
        val userData = mapOf("householdId" to householdRef.id)
        //usersCollection.document(currentUser.uid).update("householdId", householdRef.id).await()
        usersCollection.document(currentUser.uid).set(userData, SetOptions.merge()).await()

        return householdRef.id
    }

    // --- NUEVA FUNCIÓN: Unirse a un Hogar ---
    /**
     * Busca un hogar por su código de invitación y, si lo encuentra,
     * añade al usuario actual a la lista de miembros.
     * @param code El código de invitación.
     * @return El ID del hogar al que se unió, o null si el código es inválido.
     */
    suspend fun joinHousehold(code: String): String? {
        val currentUser = auth.currentUser ?: throw IllegalStateException("Usuario no logue-ado")

        // Buscamos en Firestore un hogar que tenga ese código
        val query = householdsCollection.whereEqualTo("invitationCode", code).limit(1).get().await()

        if (query.isEmpty) {
            return null // No se encontró ningún hogar con ese código
        }

        val householdDoc = query.documents.first()
        val householdId = householdDoc.id

        // Añadimos el UID del usuario al array de miembros del hogar
        householdsCollection.document(householdId).update("members", FieldValue.arrayUnion(currentUser.uid)).await()
        val userData = mapOf("householdId" to householdId)
        // Actualizamos el documento del usuario para añadirle el ID del hogar
        //usersCollection.document(currentUser.uid).update("householdId", householdId).await()
        usersCollection.document(currentUser.uid).set(userData, SetOptions.merge()).await()

        return householdId
    }

    // Función de ayuda para generar un código simple
    private fun generateInvitationCode(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
    // --- NUEVA FUNCIÓN ---
    /**
     * Archiva la lista de compras activa actual y luego la vacía.
     * @param householdId El ID del hogar.
     * @param currentList La lista actual de productos a archivar.
     */
    suspend fun archiveCurrentList(householdId: String, currentList: List<Product>) {
        if (currentList.isEmpty()) {
            Log.d("ShopRepo", "Intento de archivar una lista vacía. No se hace nada.")
            return // No archivamos listas vacías
        }

        try {
            // Creamos la referencia a la nueva colección de listas archivadas
            val archivedCollection = db.collection("archived_lists")

            // Creamos el objeto que vamos a guardar
            val listToArchive = ArchivedList(
                archivedDate = Timestamp.now(), // La fecha y hora actual
                products = currentList,
                householdId = householdId
            )

            // Usamos un 'write batch' para asegurar que ambas operaciones se completen o ninguna lo haga
            db.runBatch { batch ->
                // Operación 1: Añadir la nueva lista al archivo
                val newArchivedDoc = archivedCollection.document() // Firestore genera un ID
                batch.set(newArchivedDoc, listToArchive)

                // Operación 2: Vaciar la lista activa en el documento del hogar
                val householdDoc = householdsCollection.document(householdId)
                batch.update(householdDoc, "activeList", emptyList<Product>())

            }.await()
            Log.i("ShopRepo", "¡Lista archivada con éxito!")

        } catch (e: Exception) {
            Log.e("ShopRepo", "Error al archivar la lista", e)
        }
    }
}
