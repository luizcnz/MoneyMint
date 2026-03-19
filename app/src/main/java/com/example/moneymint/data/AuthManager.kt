package com.example.moneymint.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Gestiona la autenticación con Firebase.
 * Actualizado para permitir modo de prueba sin login real.
 */
class AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _usuario = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val usuario: StateFlow<FirebaseUser?> = _usuario

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _usuario.value = firebaseAuth.currentUser
        }
    }

    fun iniciarSesion(usuarioNombre: String, contrasena: String, onResult: (Boolean, String?) -> Unit) {
        val email = if (usuarioNombre.contains("@")) usuarioNombre else "$usuarioNombre@moneymint.com"
        
        auth.signInWithEmailAndPassword(email, contrasena)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    registrarUsuario(email, contrasena, onResult)
                }
            }
    }

    private fun registrarUsuario(email: String, contrasena: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, contrasena)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    /**
     * Si no hay usuario real (porque saltamos el login), 
     * devolvemos un ID fijo para que la app no falle al sincronizar.
     */
    fun getUserId(): String {
        return auth.currentUser?.uid ?: "usuario_de_prueba_luiz"
    }
}
