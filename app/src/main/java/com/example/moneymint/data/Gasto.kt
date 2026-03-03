package com.example.moneymint.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "gastos",
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
    val monto: Double,
    val descripcion: String,
    val fecha: Long,
    val categoriaId: Int
)