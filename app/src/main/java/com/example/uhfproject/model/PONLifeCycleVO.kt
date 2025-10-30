package com.example.uhfproject.model

/**
 * PON生命周期实体类
 */
data class PONLifeCycleVO(
    /**
     * 快递单号
     */
    val trackingNumber: String,

    /**
     * epc
     */
    val epc: String,

    /**
     * 状态
     */
    val status: Int,

    /**
     * 绑定时间
     */
    val uploadTime: String,

    /**
     * 绑定人姓名
     */
    val uploadBy: String,

    /**
     * 入库时间
     */
    val inboundDate: String,

    /**
     * 入库人姓名
     */
    val inboundBy: String,

    /**
     * 邮局名称
     */
    val siteName: String
)
