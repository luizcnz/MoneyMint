package com.example.moneymint.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transacciones",
    foreignKeys = [
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Gasto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val monto: Double = 0.0,
    val descripcion: String = "",
    val fecha: Long = 0L,
    val categoriaId: Int = 0,
    val esIngreso: Boolean = false,
    val userId: String = "" // ID del usuario de Firebase
)
