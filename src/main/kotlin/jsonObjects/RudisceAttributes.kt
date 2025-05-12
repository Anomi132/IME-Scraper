package jsonObjects

import kotlinx.serialization.Serializable

/*
    Hrani vse možne atribute, ki jih lahko vrne API
 */
@Serializable
data class RudisceAttributes(
    val id_naha: Int,
    val ime_nahajalisca: String,
    val Y: Double,
    val X: Double,
    val N: Double,
    val E: Double,
    val rudisce: Int,
    val premogovnik: Int
)

