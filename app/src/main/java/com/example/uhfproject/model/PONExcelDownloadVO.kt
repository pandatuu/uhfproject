package com.example.uhfproject.model

data class PONExcelDownloadVO(

    /**
     * 序号
     */
    val sn: Long,

    /**
     * 快递单号
     */
    val trackingNumber: String? = null,

    /**
     * epc
     */
    val epc: String? = null,

    /**
     * 邮局名称
     */
    val siteName: String? = null,

    /**
     * 状态
     */
    val status: Int? = null,

    /**
     * 绑定操作人员
     */
    val uploadBy: String? = null,

    /**
     * 绑定时间
     */
    val uploadTime: String? = null,

    /**
     * 入库操作人员
     */
    val inboundBy: String? = null,

    /**
     * 入库时间
     */
    val inboundDate: String? = null,
)