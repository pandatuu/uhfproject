package com.example.uhfproject.model

data class PONInboundDTO(

    val epc: List<String>,
    /**
     * 邮局id
     */
    val siteId: Int
)
