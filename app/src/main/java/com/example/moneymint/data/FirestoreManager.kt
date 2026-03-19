package com.example.moneymint.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Gestiona la sincronización con Cloud Firestore.
 */
class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    private val collectionName = "transacciones"

    /**
     * Sube un gasto a Firestore.
     * Usamos el ID de Room como identificador en Firebase para que no se dupliquen.
     */
    suspend fun subirTransaccion(gasto: Gasto) {
        if (gasto.userId.isBlank()) return
        
        try {
            db.collection(collectionName)
                .document(gasto.id.toString()) // Usamos el mismo ID de Room
                .set(gasto)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Elimina un gasto de Firestore.
     */
    suspend fun eliminarTransaccion(gastoId: Int) {
        try {
            db.collection(collectionName)
                .document(gastoId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Descarga todas las transacciones de un usuario específico.
     */
    suspend fun descargarTransacciones(userId: String): List<Gasto> {
        return try {
            val snapshot = db.collection(collectionName)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            snapshot.toObjects(Gasto::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
