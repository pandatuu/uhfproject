package com.example.uhfproject.model

/**
 * 绑定和入库数量
 */
data class BoundAndInboundVO(

    /**
     * 绑定数量
     */
    val boundCount : Int,

    /**
     * 入库数量
     */
    val inboundCount : Int,

    /**
     * 邮局id
     */
    val siteId : Int

)
