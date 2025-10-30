package com.example.uhfproject.model

data class PONBindVO(
    /**
     * 快递单号
     */
    val trackingId: String,

    /**
     * EPC
     */
    val epc: String,

    /**
     * 邮局id
     */
    val siteId: Int
)
