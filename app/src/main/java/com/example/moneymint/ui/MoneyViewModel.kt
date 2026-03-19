package com.example.moneymint.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneymint.data.AuthManager
import com.example.moneymint.data.Categoria
import com.example.moneymint.data.FirestoreManager
import com.example.moneymint.data.Gasto
import com.example.moneymint.data.MoneyDao
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

enum class FiltroGastos {
    HOY, SEMANA, MES, ULTIMOS_100, POR_CATEGORIA
}

/**
 * MoneyViewModel actualizado para manejar la sincronización con Firebase.
 */
class MoneyViewModel(
    private val dao: MoneyDao,
    private val firestoreManager: FirestoreManager,
    private val authManager: AuthManager
) : ViewModel() {

    val todasLasCategorias: Flow<List<Categoria>> = dao.obtenerCategorias()
    val todasLasTransacciones: Flow<List<Gasto>> = dao.obtenerGastos()

    private val _filtroActual = MutableStateFlow(FiltroGastos.MES)
    val filtroActual: StateFlow<FiltroGastos> = _filtroActual.asStateFlow()

    private val _categoriaFiltroId = MutableStateFlow<Int?>(null)
    val categoriaFiltroId: StateFlow<Int?> = _categoriaFiltroId.asStateFlow()

    val gastosFiltrados: Flow<List<Gasto>> = combine(
        todasLasTransacciones,
        _filtroActual,
        _categoriaFiltroId
    ) { lista, filtro, catId ->
        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        when (filtro) {
            FiltroGastos.HOY -> lista.filter { it.fecha >= hoy }
            FiltroGastos.SEMANA -> {
                val inicioSemana = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                }.timeInMillis
                lista.filter { it.fecha >= inicioSemana }
            }
            FiltroGastos.MES -> {
                val inicioMes = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                }.timeInMillis
                lista.filter { it.fecha >= inicioMes }
            }
            FiltroGastos.ULTIMOS_100 -> lista.take(100)
            FiltroGastos.POR_CATEGORIA -> {
                if (catId != null) lista.filter { it.categoriaId == catId } else lista
            }
        }
    }

    init {
        // Al iniciar, si hay un usuario, intentamos descargar sus datos de la nube
        descargarDatosDeNube()
    }

    private fun descargarDatosDeNube() {
        val userId = authManager.getUserId() ?: return
        viewModelScope.launch {
            val transaccionesNube = firestoreManager.descargarTransacciones(userId)
            transaccionesNube.forEach { gasto ->
                // Guardamos en Room lo que viene de la nube
                dao.insertarGasto(gasto)
            }
        }
    }

    fun cambiarFiltro(nuevoFiltro: FiltroGastos, categoriaId: Int? = null) {
        _filtroActual.value = nuevoFiltro
        _categoriaFiltroId.value = categoriaId
    }

    fun agregarGasto(monto: Double, descripcion: String, categoriaId: Int, fecha: Long, esIngreso: Boolean) {
        val userId = authManager.getUserId() ?: ""
        viewModelScope.launch {
            // 1. Guardar en Room (Local) para que la UI se actualice rápido
            val nuevoGasto = Gasto(
                monto = monto,
                descripcion = descripcion,
                fecha = fecha,
                categoriaId = categoriaId,
                esIngreso = esIngreso,
                userId = userId
            )
            // Necesitamos el ID generado por Room para sincronizarlo bien
            // Nota: En una app pro, usaríamos IDs únicos (UUID) en lugar de Int auto-incrementales
            dao.insertarGasto(nuevoGasto)
            
            // 2. Sincronizar con Firestore (Nube)
            // Para simplificar esta prueba, lo enviamos después de insertar
            // (En Room el ID se genera al insertar)
            todasLasTransacciones.first().firstOrNull()?.let { ultimo ->
                firestoreManager.subirTransaccion(ultimo)
            }
        }
    }

    fun actualizarGasto(gasto: Gasto) {
        viewModelScope.launch {
            dao.actualizarGasto(gasto)
            firestoreManager.subirTransaccion(gasto)
        }
    }

    fun eliminarGasto(gasto: Gasto) {
        viewModelScope.launch {
            dao.eliminarGasto(gasto)
            firestoreManager.eliminarTransaccion(gasto.id)
        }
    }
}

class MoneyViewModelFactory(
    private val dao: MoneyDao,
    private val firestoreManager: FirestoreManager,
    private val authManager: AuthManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoneyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoneyViewModel(dao, firestoreManager, authManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
