package com.example.uhfproject.model

/**
 * 看板数量统计实体类
 * @Author LiShuai
 * @Date 2025-09-02 21:56
 * @Description 看板数量统计实体类
 * @Version 1.0
 */
data class DashboardNumberVO (

    /**
     * （所选日期（默认当日））上传总数
     */
    val total: Int? = null,

    /**
     * （所选日期（默认当日））入库完成数量
     */
    val inboundNumber: Int? = null,

    /**
     * （所选日期（默认当日））待入库数量
     */
    val pendingInboundNumber: Int? = null,

    /**
     * （所选日期（默认当日））出库完成数量
     */
    val outboundNumber: Int? = null,

    /**
     * （所选日期（默认当日））剩余出库数量
     */
    val outboundRemainingNumber: Int? = null
)