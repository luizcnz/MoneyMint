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

    @Insert
    suspend fun insertarGasto(gasto: Gasto)

    @Query("SELECT * FROM gastos ORDER BY fecha DESC")
    fun obtenerGastos(): Flow<List<Gasto>>
}