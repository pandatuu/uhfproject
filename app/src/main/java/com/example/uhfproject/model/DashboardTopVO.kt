package com.example.uhfproject.model

/**
 * @Author LiShuai
 * @Date 2025-09-02 22:50
 * @Description 看板排行榜实体类s
 * @Version 1.0
 */
data class DashboardTopVO (
    /**
     * 序号
     */
    val sn: String? = null,

    /**
     * db
     */
    val db: String? = null,

    /**
     * 总数
     */
    val total: Int? = null,

    /**
     * 入库完成数量
     */
    val inboundComplete: Int? = null,

    /**
     * 出库完成数量
     */
    val outboundComplete: Int? = null
)