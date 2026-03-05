package com.example.moneymint.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Gasto::class, Categoria::class], version = 1, exportSchema = false)
abstract class MoneyDatabase : RoomDatabase() {

    abstract fun moneyDao(): MoneyDao

    companion object {
        @Volatile
        private var INSTANCE: MoneyDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): MoneyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoneyDatabase::class.java,
                    "money_mint_db"
                )
                    .addCallback(MoneyDatabaseCallback(scope)) // Aquí ocurre la magia del pre-poblado
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class MoneyDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.moneyDao()
                    // Insertamos categorías iniciales
                    dao.insertarCategoria(Categoria(nombre = "Comida", icono = "🍔"))
                    dao.insertarCategoria(Categoria(nombre = "Transporte", icono = "🚗"))
                    dao.insertarCategoria(Categoria(nombre = "Salud", icono = "💊"))
                    dao.insertarCategoria(Categoria(nombre = "Ocio", icono = "🎮"))
                }
            }
        }
    }
}