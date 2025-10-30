package com.example.uhfproject.model

/**
 * 快递单号信息
 */
data class TrackingVO(
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
    val status: String,

    /**
     * 上传时间
     */
    val uploadTime: String,

    /**
     * 上传用户
     */
    val uploadUser: String,

    /**
     * 邮局编号
     */
    val siteId: Int? = null,

    /**
     * 邮局名称
     */
    val siteName: String? = null
)
