package com.example.supermercado.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProductoDao {

    @Query("Select * from producto order by comprado")
    fun getAllProductos(): List<Producto>

    @Query("Select COUNT(*) from producto")
    fun contar(): Int

    @Query("select * from producto where id = :id")
    fun findById(id:Int) : Producto

    @Insert
    fun insertarProducto(producto: Producto): Long

    @Update
    fun actualizarProducto(producto: Producto)

    @Delete
    fun eliminarProducto(producto: Producto)
}