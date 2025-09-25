package com.example.uhfproject.model

data class OutboundVerifyVO(
    /**
     * 积压数量
     */
    val backlog: Int,
    /**
     * 今日上传数量
     */
    val total: Int,
    /**
     * 已完成出库数量
     */
    val completed: Int,
    /**
     * 异常数量(手持端统计，返回0)
     */
    val exceptions: Int
)
