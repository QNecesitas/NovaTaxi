package com.qnecesitas.novataxiapp.model

data class User(
    val email: String,
    val phone: String,
    val name: String,
    val state: String,
    val password: String
)