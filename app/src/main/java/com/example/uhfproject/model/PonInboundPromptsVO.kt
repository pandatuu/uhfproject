package com.example.uhfproject.model

/**
 * PON入库时的信息提示
 */
data class PonInboundPromptsVO(
    /**
     * 提示消息
     */
    val msg: String,

    /**
     * 本次入库失败的数量
     */
    val failNumval : Int,

    /**
     * 无记录的数据集合
     */
    val noRecordList: List<String>,

    /**
     * 无记录的数据集合
     */
    val otherPostOfficeList: List<String>
)
