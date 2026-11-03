package com.karen.citasdeportivas.model

data class Professional(
    val id: Long? = null,
    val name: String,
    val specialty: String,
    val attentionStart: String,
    val attentionEnd: String
)