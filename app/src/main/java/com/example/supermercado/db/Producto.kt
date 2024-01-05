package com.example.supermercado.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Producto (
    @PrimaryKey(autoGenerate = true) val id:Int,
    var nombre:String,
    var comprado:Boolean
)