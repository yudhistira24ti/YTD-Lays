package com.example.pertemuan3.project

data class ProductModel(
    val name: String,
    val price: String,
    val stock: String,
    val imageRes: Int,
    val rating: String = "4.8"
)
