package com.example.uhfproject.model

data class BindBody(
    var trackingId: String,
    var epc: String,
    var isRePrint: Boolean? = false
)