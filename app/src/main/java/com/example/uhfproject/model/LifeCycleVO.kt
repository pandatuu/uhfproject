package com.example.uhfproject.model

data class LifeCycleVO(
    /**
     * 快递单号
     */
    val trackingNumber: String,


    /**
     * 邮编
     */
    val postCode: String,


    /**
     * 区号
     */
    val bitCode: String,


    /**
     * LM
     */
    val db: String,


    /**
     * epc
     */
    val epc: String,


    /**
     * 状态
     */
    val status: Int,


    /**
     * 上传时间
     */
    val uploadTime: String? = "",


    /**
     * 上传人姓名
     */
    val uploadBy: String? = "",


    /**
     * 打印时间
     */
    val rfidPrintTime: String? = "",


    /**
     * rfid打印人姓名
     */
    val printBy: String? = "",


    /**
     * 入库时间
     */
    val inboundDate: String? = "",


    /**
     * 入库人姓名
     */
    val inboundBy: String? = "",


    /**
     * 出库时间
     */
    val outboundDate: String? = "",


    /**
     * 出库人姓名
     */
    val outboundBy: String? = "",
)
