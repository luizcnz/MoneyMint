package com.example.moneymint.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

import com.example.moneymint.data.Categoria
import com.example.moneymint.data.Gasto

@Dao
interface MoneyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCategoria(categoria: Categoria)

    @Query("SELECT * FROM categorias")
    fun obtenerCategorias(): Flow<List<Categoria>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Cambiado para evitar crashes por duplicados
    suspend fun insertarGasto(gasto: Gasto)

    @Update
    suspend fun actualizarGasto(gasto: Gasto)

    @Delete
    suspend fun eliminarGasto(gasto: Gasto)

    @Query("SELECT * FROM transacciones ORDER BY fecha DESC")
    fun obtenerGastos(): Flow<List<Gasto>>
}
